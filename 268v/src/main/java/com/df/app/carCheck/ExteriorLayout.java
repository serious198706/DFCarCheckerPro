package com.df.app.carCheck;

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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.R;
import com.df.app.service.util.AppCommon;
import com.df.library.entries.Action;
import com.df.library.entries.CarSettings;
import com.df.library.entries.PhotoEntity;
import com.df.library.entries.PosEntity;
import com.df.app.paintView.ExteriorPaintPreviewView;
import com.df.library.asyncTask.DownloadImageTask;
import com.df.library.service.SpeechRecognize.SpeechDialog;
import com.df.library.util.Common;
import com.df.library.entries.UserInfo;
import com.df.library.util.Helper;
import com.df.library.util.MyScrollView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.df.library.util.Helper.getEditViewText;
import static com.df.library.util.Helper.getSpinnerSelectedText;
import static com.df.library.util.Helper.setEditViewText;
import static com.df.library.util.Helper.setSpinnerSelectionWithIndex;
import static com.df.library.util.Helper.setSpinnerSelectionWithString;
import static com.df.library.util.Helper.showView;

/**
 * Created by 岩 on 13-12-20.
 *
 * 外观检查模块，包括外观缺陷的绘制、标准照以及其他选项
 */
public class ExteriorLayout extends LinearLayout {
    private static Context context;
    private static View rootView;

    public static List<PosEntity> posEntities;
    public static List<PhotoEntity> photoEntities;

    public static ExteriorPaintPreviewView exteriorPaintPreviewView;

    // 记录玻璃
    private String glassResult = "";

    // 记录螺丝
    private String screwResult = "";

    // 记录破损
    private String brokenResult = "";

    // 记录胶体检查
    private String colloidalResult = "";

    // 承载所有的checkbox
    private TableLayout root;
    private int sketchIndex;
    private int figure;

    private EditText commentEdit;
    private MyScrollView scrollView;

    public ExteriorLayout(Context context) {
        super(context);
        init(context);
    }

    public ExteriorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ExteriorLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(final Context context) {
        ExteriorLayout.context = context;

        rootView = LayoutInflater.from(context).inflate(R.layout.exterior_layout, this);

        commentEdit = (EditText)findViewById(R.id.exterior_comment_edit);

        posEntities = new ArrayList<PosEntity>();
        photoEntities = new ArrayList<PhotoEntity>();
        exteriorPaintPreviewView = (ExteriorPaintPreviewView) findViewById(R.id.exterior_image);

        Button glassButton = (Button)findViewById(R.id.glass_button);
        glassButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseGlass();
            }
        });

        Button screwButton = (Button)findViewById(R.id.screw_button);
        screwButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseScrew();
            }
        });

        Button brokenButton = (Button)findViewById(R.id.broken_button);
        brokenButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseBroken();
            }
        });

        Button colloidalButton = (Button)findViewById(R.id.colloidal_button);
        colloidalButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseColloidal();
            }
        });

        setSpinnerSelectionWithIndex(rootView, R.id.smooth_spinner, 1);

        scrollView = (MyScrollView) findViewById(R.id.root);
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
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
            }
        });

        findViewById(R.id.speech_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(commentEdit.getWindowToken(), 0);

                SpeechDialog speechDialog = new SpeechDialog(context, new SpeechDialog.OnResult() {
                    @Override
                    public void onResult(String result) {
                        int start = commentEdit.getSelectionStart();

                        String originText = commentEdit.getText().toString();

                        commentEdit.setText(originText.substring(0, start) + result + originText.substring(start, originText.length()));

                        commentEdit.setSelection(start + result.length());
                    }
                });

                speechDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        findViewById(R.id.placeHolder).setVisibility(View.GONE);
                    }
                });

                speechDialog.show();

                findViewById(R.id.placeHolder).setVisibility(View.VISIBLE);

                scrollView.post(new Runnable() {
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void updateUi() {
        // 点击图片进入绘制界面
        if(CarSettings.getInstance().getFigure().equals("")) {
            figure = 0;
        } else {
            figure = Integer.parseInt(CarSettings.getInstance().getFigure());
        }

        Bitmap previewViewBitmap = getBitmapFromFigure(figure);

        exteriorPaintPreviewView = (ExteriorPaintPreviewView) findViewById(R.id.exterior_image);
        exteriorPaintPreviewView.init(previewViewBitmap, posEntities);
        exteriorPaintPreviewView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PaintActivity.class);
                intent.putExtra("PAINT_TYPE", "EX_PAINT");
                ((Activity) rootView.getContext()).startActivityForResult(intent, Common.ENTER_EXTERIOR_PAINT);
            }
        });
    }

    /**
     * 获取车辆配置信息后，更新此页面
     */
    public static void updateExteriorPreview() {
        if(!posEntities.isEmpty()) {
            exteriorPaintPreviewView.setAlpha(1f);
            exteriorPaintPreviewView.invalidate();
            rootView.findViewById(R.id.tipOnPreview).setVisibility(View.GONE);
        }
        // 如果没点，则将图片设为半透明，添加提示文字
        else {
            exteriorPaintPreviewView.setAlpha(0.3f);
            exteriorPaintPreviewView.invalidate();
            rootView.findViewById(R.id.tipOnPreview).setVisibility(View.VISIBLE);
        }
    }

    /**
     * 选择玻璃生产年份不符的
     */
    private void chooseGlass() {
        showPopupWindow("glass", getResources().getString(R.string.glass),
                getResources().getStringArray(R.array.glass_item));
    }

    /**
     * 选择螺丝有松动的
     */
    private void chooseScrew() {
        showPopupWindow("screw", getResources().getString(R.string.screw),
                getResources().getStringArray(R.array.screw_item));
    }

    /**
     * 选择外观破损部位（暂扔）
     */
    private void chooseBroken() {
        showPopupWindow("broken", getResources().getString(R.string.ex_broken),
                getResources().getStringArray(R.array.ex_broken_item));
    }

    /**
     * 选择胶体检查
     */
    private void chooseColloidal() {
        showPopupWindow("colloidal", getResources().getString(R.string.colloidal),
                getResources().getStringArray(R.array.colloidal_item));
    }

    /**
     * 弹出窗口供用户选择
     */
    private void showPopupWindow(final String type, String title, String array[]) {
        View view = getPopupView(type, title, array);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (type.equals("glass")) {
                            glassResult = getPopupResult();
                            setEditViewText(rootView, R.id.glass_edit, glassResult);
                        } else if (type.equals("screw")) {
                            screwResult = getPopupResult();
                            setEditViewText(rootView, R.id.screw_edit, screwResult);
                        } else if(type.equals("colloidal")) {
                            colloidalResult = getPopupResult();
                            setEditViewText(rootView, R.id.colloidal_edit, colloidalResult);
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
     * 根据内容生成弹出窗口的内容
     */
    private View getPopupView(String type, String title, String array[]) {
        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        View view = inflater.inflate(R.layout.popup_layout, null);

        TextView titleText = (TextView)view.findViewById(R.id.title);
        titleText.setText(title);

        root = (TableLayout)view.findViewById(R.id.contentArea);

        int count = array.length;

        if(type.equals("colloidal")) {
            for(int i = 0; i < count; i++) {
                TableRow row = new TableRow(view.getContext());

                CheckBox checkBox = new CheckBox(view.getContext());
                checkBox.setText(array[i]);
                checkBox.setTextSize(20f);
                checkBox.setButtonDrawable(R.drawable.checkbox_button);
                checkBox.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT, 1f));

                if(colloidalResult.contains(array[i])) {
                    checkBox.setChecked(true);
                }

                row.addView(checkBox);

                root.addView(row);
            }
        } else {
            for(int i = 0; i < (count % 2 == 0 ? count / 2 : count / 2 + 1); i++) {
                TableRow row = new TableRow(view.getContext());

                for(int j = 0; j < 2; j++) {
                    if(i * 2 + j >= count) {
                        break;
                    }

                    CheckBox checkBox = new CheckBox(view.getContext());
                    checkBox.setText(array[i * 2 + j]);
                    checkBox.setTextSize(20f);
                    checkBox.setButtonDrawable(R.drawable.checkbox_button);
                    checkBox.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT, 1f));

                    if(type.equals("glass")) {
                        if(glassResult.contains(array[i * 2 + j])) {
                            checkBox.setChecked(true);
                        }
                    } else if(type.equals("screw")) {
                        if(screwResult.contains(array[i * 2 + j])) {
                            checkBox.setChecked(true);
                        }
                    } else {
                        if(brokenResult.contains(array[i * 2 + j])) {
                            checkBox.setChecked(true);
                        }
                    }

                    row.addView(checkBox);
                }

                root.addView(row);
            }
        }

        return view;
    }

    /**
     * 从弹出窗口中获取用户的选择结果
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

//    /**
//     * 生成草图
//     */
//    public PhotoEntity generateSketch() {
//        Bitmap bitmap = null;
//        Canvas c;
//
//        try {
//            bitmap = Bitmap.createBitmap(exteriorPaintPreviewView.getMaxWidth(),exteriorPaintPreviewView.getMaxHeight(),
//                    Bitmap.Config.ARGB_8888);
//            c = new Canvas(bitmap);
//            exteriorPaintPreviewView.draw(c);
//
//            FileOutputStream out = new FileOutputStream(AppCommon.photoDirectory + "exterior");
//            bitmap.compress(Bitmap.CompressFormat.PNG, 70, out);
//            out.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        PhotoEntity photoEntity = new PhotoEntity();
//        photoEntity.setFileName("exterior");
//
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
//            jsonObject.put("Group", "exterior");
//            jsonObject.put("Part", "sketch");
//
//            JSONObject photoData = new JSONObject();
//
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
                    new File(AppCommon.photoDirectory + "exterior"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName("exterior");

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
            jsonObject.put("Group", "exterior");
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
     * 生成外观检查JSON串
     */
    public JSONObject generateJSONObject() throws JSONException{
        JSONObject exterior = new JSONObject();

        exterior.put("smooth", getSpinnerSelectedText(rootView, R.id.smooth_spinner));
        exterior.put("comment", getEditViewText(rootView, R.id.exterior_comment_edit));
        exterior.put("glass", getEditViewText(rootView, R.id.glass_edit));
        exterior.put("screw", getEditViewText(rootView, R.id.screw_edit));
        exterior.put("colloidal", getEditViewText(rootView, R.id.colloidal_edit));
        exterior.put("needRepair", ((CheckBox)findViewById(R.id.needRepair)).isChecked() ? "是" : "否");

        return exterior;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     */
    public void fillInData(JSONObject exterior) throws JSONException{
        setSpinnerSelectionWithString(rootView, R.id.smooth_spinner, exterior.getString("smooth"));

        String comment = exterior.get("comment") == JSONObject.NULL ? "" : exterior.getString("comment");
        setEditViewText(rootView, R.id.exterior_comment_edit, comment);

        glassResult = exterior.get("glass") == JSONObject.NULL ? "" : exterior.getString("glass");
        setEditViewText(rootView, R.id.glass_edit, glassResult);

        screwResult = exterior.get("screw") == JSONObject.NULL ? "" : exterior.getString("screw");
        setEditViewText(rootView, R.id.screw_edit, screwResult);

        colloidalResult = exterior.get("colloidal") == JSONObject.NULL ? "" : exterior.getString("colloidal");
        setEditViewText(rootView, R.id.colloidal_edit, colloidalResult);

        ((CheckBox)findViewById(R.id.needRepair)).setChecked(exterior.getString("needRepair").equals("是"));
    }

    public void fillInData(JSONObject exterior, JSONObject photo) throws JSONException {
        fillInData(exterior);
        updateImage(photo);
    }

    private void updateImage(JSONObject photo) throws JSONException{
        showView(rootView, R.id.exProgressBar, false);
        rootView.findViewById(R.id.tipOnPreview).setVisibility(View.GONE);

        JSONObject exterior = photo.getJSONObject("exterior");

        // 结构草图 - 前视角
        JSONObject exSketch = exterior.getJSONObject("sketch");

        if(exSketch != JSONObject.NULL) {
            sketchIndex = exSketch.getInt("index");

            if(sketchIndex >= PhotoLayout.photoIndex) {
                PhotoLayout.photoIndex = sketchIndex + 1;
            }
        }
    }

    /**
     * 根据车型数据调用不同的预览图
     * @param figure 车辆类型代码
     * @return 图片
     */
    private Bitmap getBitmapFromFigure(int figure) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeFile(AppCommon.utilDirectory + getBitmapNameFromFigure(figure), options);
        } catch (OutOfMemoryError e) {
            Toast.makeText(rootView.getContext(), "内存不足，请稍候重试！", Toast.LENGTH_SHORT).show();
        }

        return bitmap;
    }

    private String getBitmapNameFromFigure(int figure) {
        return getNameFromFigure(figure);
    }

    private String getNameFromFigure(int figure) {
        // 默认为三厢四门图
        String name = "r3d4";

        switch (figure) {
            case 2:
                name = "r3d2";
                break;
            case 3:
                name = "r2d2";
                break;
            case 4:
                name = "r2d4";
                break;
            case 5:
                name = "van_o";
                break;
        }

        return name;
    }

    public void clearCache() {
        posEntities = null;
        photoEntities = null;
        exteriorPaintPreviewView = null;
    }


}
