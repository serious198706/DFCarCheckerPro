package com.df.kia.carCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.df.kia.R;
import com.df.library.entries.Action;
import com.df.library.entries.PhotoEntity;
import com.df.library.entries.PosEntity;
import com.df.kia.paintView.InteriorPaintPreviewView;
import com.df.library.asyncTask.DownloadImageTask;
import com.df.library.entries.UserInfo;
import com.df.library.util.Common;
import com.df.library.util.Helper;
import com.df.library.util.MyScrollView;
import com.df.kia.service.util.AppCommon;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.df.library.util.Helper.getEditViewText;
import static com.df.library.util.Helper.getSpinnerSelectedText;
import static com.df.library.util.Helper.setEditViewText;
import static com.df.library.util.Helper.setSpinnerSelectionWithString;
import static com.df.library.util.Helper.showView;

/**
 * Created by 岩 on 13-12-20.
 *
 * 内饰检查，包括内饰缺陷图的绘制，内饰标准照及其他选项
 */
public class InteriorLayout extends LinearLayout {
    private static View rootView;
    private static Context context;

    public static List<PosEntity> posEntities;
    public static List<PhotoEntity> photoEntities;

    // 内饰缺陷图预览
    public static InteriorPaintPreviewView interiorPaintPreviewView;

    // 承载checkbox
    private TableLayout root;

    // 记录脏污部位
    private String dirtyResult = "";

    // 记录破损部位
    private String brokenResult = "";

    public static int sketchIndex;
    private int figure;
    private DownloadImageTask downloadImageTask;

    public InteriorLayout(Context context) {
        super(context);
        init(context);
    }

    public InteriorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public InteriorLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        InteriorLayout.context = context;
        rootView = LayoutInflater.from(context).inflate(R.layout.interior_layout, this);

        posEntities = new ArrayList<PosEntity>();
        photoEntities = new ArrayList<PhotoEntity>();
        interiorPaintPreviewView = (InteriorPaintPreviewView) findViewById(R.id.interior_image);

        Button dirtyButton = (Button)findViewById(R.id.dirty_button);
        dirtyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseDirty();
            }
        });

        Button brokenButton = (Button)findViewById(R.id.broken_button);
        brokenButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseBroken();
            }
        });

        MyScrollView scrollView = (MyScrollView) findViewById(R.id.root);
        scrollView.setListener(new MyScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy) {
                showShadow(scrollView.getScrollY() > 5);
            }
        });
        // 移除输入框的焦点，避免每次输入完成后界面滚动
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        scrollView.setFocusable(true);
        scrollView.setFocusableInTouchMode(true);
        scrollView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
            }
        });
    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    /**
     * 更新界面
     */
    public void updateUi() {
        figure = Integer.parseInt(BasicInfoLayout.mCarSettings.getFigure());
        Bitmap previewViewBitmap = getBitmapFromFigure(figure);

        interiorPaintPreviewView = (InteriorPaintPreviewView) findViewById(R.id.interior_image);
        interiorPaintPreviewView.init(previewViewBitmap, posEntities);
        interiorPaintPreviewView.setMinimumHeight(previewViewBitmap.getHeight());

        // 点击预览图进入绘制界面
        interiorPaintPreviewView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PaintActivity.class);
                intent.putExtra("PAINT_TYPE", "IN_PAINT");
                ((Activity) getContext()).startActivityForResult(intent, Common.ENTER_INTERIOR_PAINT);
            }
        });
    }

    /**
     * 绘制完成后，更新预览图
     */
    public static void updateInteriorPreview() {
        if(!posEntities.isEmpty()) {
            interiorPaintPreviewView.setAlpha(1f);
            interiorPaintPreviewView.invalidate();
            rootView.findViewById(R.id.tipOnPreview).setVisibility(View.GONE);
        }
        // 如果没点，则将图片设为半透明，添加提示文字
        else {
            interiorPaintPreviewView.setAlpha(0.3f);
            interiorPaintPreviewView.invalidate();
            rootView.findViewById(R.id.tipOnPreview).setVisibility(View.VISIBLE);
        }
    }

    /**
     * 选择脏污部位（去掉）
     */
    private void chooseDirty() {
        showPopupWindow("dirty", getResources().getString(R.string.dirty),
                getResources().getStringArray(R.array.dirty_item));
    }

    /**
     * 选择破损部位（去掉）
     */
    private void chooseBroken() {
        showPopupWindow("broken", getResources().getString(R.string.in_broken),
                getResources().getStringArray(R.array.in_broken_item));
    }

    /**
     * 弹出选择窗口
     * @param type dirty/broken
     * @param title 标题
     * @param array 内容
     */
    private void showPopupWindow(final String type, String title, String array[]) {
        View view = getPopupView(type, title, array);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (type.equals("dirty")) {
                            dirtyResult = getPopupResult();
                            setEditViewText(rootView, R.id.dirty_edit, dirtyResult);
                        } else {
                            brokenResult = getPopupResult();
                            setEditViewText(rootView, R.id.broken_edit, brokenResult);
                        }
                    }
                })
                .create();

        dialog.show();
    }

    /**
     * 填充弹出窗口的内容
     * @param type dirty/broken
     * @param title 标题
     * @param array 内容
     * @return view
     */
    private View getPopupView(String type, String title, String array[]) {
        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_layout, null);

        TextView titleText = (TextView)view.findViewById(R.id.title);
        titleText.setText(title);

        root = (TableLayout)view.findViewById(R.id.contentArea);

        int count = array.length;

        for(int i = 0; i < count / 2; i++) {
            TableRow row = new TableRow(view.getContext());

            for(int j = 0; j < 2; j++) {
                CheckBox checkBox = new CheckBox(view.getContext());
                checkBox.setText(array[i * 2 + j]);
                checkBox.setTextSize(20f);
                checkBox.setButtonDrawable(R.drawable.checkbox_button);
                checkBox.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT, 1f));

                if(type.equals("dirty")) {
                    if(brokenResult.contains(array[i * 2 + j])) {
                        checkBox.setEnabled(false);
                    }

                    if(dirtyResult.contains(array[i * 2 + j])) {
                        checkBox.setChecked(true);
                    }
                } else {
                    if(dirtyResult.contains(array[i * 2 + j])) {
                        checkBox.setEnabled(false);
                    }

                    if(brokenResult.contains(array[i * 2 + j])) {
                        checkBox.setChecked(true);
                    }
                }

                row.addView(checkBox);
            }

            root.addView(row);
        }

        return view;
    }

    /**
     * 获取弹出窗口的选择结果
     * @return
     */
    private String getPopupResult() {
        String result = "";

        for (int i = 0; i < root.getChildCount(); i++) {
            TableRow row = (TableRow) root.getChildAt(i);

            for (int j = 0; j < row.getChildCount(); j++) {
                CheckBox checkBox = (CheckBox) row.getChildAt(j);

                if (checkBox.isChecked()) {
                    result += checkBox.getText();
                    result += ",";
                }
            }
        }

        if (result.length() > 1) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    /**
     * 根据车辆类型编码，确定预览图
     * @param figure
     * @return
     */
    private Bitmap getBitmapFromFigure(int figure) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return BitmapFactory.decodeFile(AppCommon.utilDirectory + getBitmapNameFromFigure(figure), options);
    }

    private String getBitmapNameFromFigure(int figure) {
        return getNameFromFigure(figure);
    }

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private static String getNameFromFigure(int figure) {
        // 默认为三厢四门图
        String name = "d4s4";

        switch (figure) {
            case 2:
                name = "d2s4";
                break;
            case 3:
                name = "d2s4";
                break;
            case 4:
                name = "d4s4";
                break;
            case 5:
                name = "van_i";
                break;
        }

        return name;
    }

    /**
     * 生成内饰的JSONObject
     * @return
     * @throws org.json.JSONException
     */
    public JSONObject generateJSONObject() throws JSONException{
        JSONObject interior = new JSONObject();

        interior.put("sealingStrip", getSpinnerSelectedText(rootView, R.id.sealingStrip_spinner));
        interior.put("comment", getEditViewText(rootView, R.id.interior_comment_edit));
        //interior.put("dirty", getEditViewText(rootView, R.id.dirty_edit));
        //interior.put("broken", getEditViewText(rootView, R.id.broken_edit));

        return interior;
    }

//    /**
//     * 生成内饰草图
//     * @return
//     */
//    public PhotoEntity generateSketch() {
//        Bitmap bitmap = null;
//        Canvas c;
//
//        try {
//            bitmap = Bitmap.createBitmap(interiorPaintPreviewView.getMaxWidth(),interiorPaintPreviewView.getMaxHeight(),
//                    Bitmap.Config.ARGB_8888);
//            c = new Canvas(bitmap);
//            interiorPaintPreviewView.draw(c);
//
//            FileOutputStream out = new FileOutputStream(AppCommon.photoDirectory + "interior");
//            bitmap.compress(Bitmap.CompressFormat.PNG, 70, out);
//            out.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // 如果是修改模式，则Action就是add
//        PhotoEntity photoEntity = new PhotoEntity();
//        photoEntity.setFileName("interior");
//
//        // 如果是走了这段代码，则一定是添加照片
//        // 如果是修改模式，则Action就是modify
//        if(CarCheckActivity.isModify()) {
//            photoEntity.setIndex(sketchIndex);
//            photoEntity.setModifyAction(Action.MODIFY);
//        } else {
//            photoEntity.setIndex(PhotoLayout.photoIndex++);
//            photoEntity.setModifyAction(Action.NORMAL);
//        }
//
//        // 组织jsonString
//        JSONObject jsonObject = new JSONObject();
//
//        try {
//            jsonObject.put("Group", "interior");
//            jsonObject.put("Part", "sketch");
//
//            JSONObject photoData = new JSONObject();
//            photoData.put("height", bitmap.getHeight());
//            photoData.put("width", bitmap.getWidth());
//
//            jsonObject.put("PhotoData", photoData);
//            jsonObject.put("CarId", BasicInfoLayout.carId);
//            jsonObject.put("UserId", UserInfo.getInstance().getId());
//            jsonObject.put("Key", UserInfo.getInstance().getKey());
//            jsonObject.put("Action", photoEntity.getModifyAction());
//            jsonObject.put("Index", photoEntity.getIndex());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        photoEntity.setJsonString(jsonObject.toString());
//
//        return photoEntity;
//    }

    /**
     * 获取草图Bitmap
     * @return
     */
    public Bitmap getSketch() { return getBitmapFromFigure(figure); }

    /**
     * 生成草图(干净的)
     */
    public PhotoEntity generateSketch() {
        Bitmap bitmap = getBitmapFromFigure(figure);

        try {
            Helper.copy(new File(AppCommon.utilDirectory + getBitmapNameFromFigure(figure)),
                    new File(AppCommon.photoDirectory + "interior"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName("interior");

        // 如果是修改模式，则Action就是modify
        if(CarCheckActivity.isModify()) {
            photoEntity.setIndex(sketchIndex);
            photoEntity.setModifyAction(Action.MODIFY);
        } else {
            photoEntity.setIndex(PhotoLayout.photoIndex++);
            photoEntity.setModifyAction(Action.NORMAL);
        }

        // 组织jsonString
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("Group", "interior");
            jsonObject.put("Part", "sketch");

            JSONObject photoData = new JSONObject();

            photoData.put("height", bitmap.getHeight());
            photoData.put("width", bitmap.getWidth());

            jsonObject.put("PhotoData", photoData);
            jsonObject.put("CarId", BasicInfoLayout.carId);
            jsonObject.put("UserId", UserInfo.getInstance().getId());
            jsonObject.put("Key", UserInfo.getInstance().getKey());
            jsonObject.put("Action", photoEntity.getModifyAction());
            jsonObject.put("Index", photoEntity.getIndex());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     *
     * @param interior
     * @throws org.json.JSONException
     */
    public void fillInData(JSONObject interior) throws JSONException{
        setSpinnerSelectionWithString(rootView, R.id.sealingStrip_spinner, interior.getString("sealingStrip"));
        setEditViewText(rootView, R.id.interior_comment_edit, interior.get("comment") == JSONObject.NULL ? "" : interior.getString("comment"));
    }

    public void fillInData(JSONObject interior, JSONObject photo) throws JSONException {
        fillInData(interior);
        updateImage(photo);
    }

    private void updateImage(JSONObject photo) throws JSONException{
        showView(rootView, R.id.inProgressBar, false);
        rootView.findViewById(R.id.tipOnPreview).setVisibility(View.GONE);

        JSONObject interior = photo.getJSONObject("interior");

        // 结构草图 - 后视角
        JSONObject inSketch = interior.getJSONObject("sketch");

        if(inSketch != JSONObject.NULL) {
            sketchIndex = inSketch.getInt("index");

            if(sketchIndex >= PhotoLayout.photoIndex) {
                PhotoLayout.photoIndex = sketchIndex + 1;
            }

//            String sketchUrl = inSketch.getString("photo");
//            downloadImageTask = new DownloadImageTask(Common.getPICTURE_ADDRESS() + sketchUrl, new DownloadImageTask.OnDownloadFinished() {
//                @Override
//                public void onFinish(Bitmap bitmap) {
//                    showView(rootView, R.id.inProgressBar, false);
//                    interiorPaintPreviewView.init(bitmap, posEntities);
//                    interiorPaintPreviewView.setAlpha(1.0f);
//                    interiorPaintPreviewView.invalidate();
//                }
//
//                @Override
//                public void onFailed() {
//                    Log.d(AppCommon.TAG, "下载后视角草图失败！");
//                }
//            });
//            downloadImageTask.execute();
        }
    }

    public void clearCache() {
        posEntities = null;
        photoEntities = null;
        interiorPaintPreviewView = null;

        if(downloadImageTask != null) {
            downloadImageTask.cancel(true);
        }
    }
}
