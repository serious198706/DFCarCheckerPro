package com.df.app.CarCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.paintview.InteriorPaintPreviewView;
import com.df.app.service.MyScrollView;
import com.df.app.util.Common;
import com.df.app.util.Helper;

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

import static com.df.app.util.Helper.getEditViewText;
import static com.df.app.util.Helper.getSpinnerSelectedText;
import static com.df.app.util.Helper.setEditViewText;
import static com.df.app.util.Helper.setSpinnerSelectionWithString;
import static com.df.app.util.Helper.setTextView;

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
    public static List<PhotoEntity> standardPhotoEntities;

    // 内饰缺陷图
    private Bitmap previewViewBitmap;

    // 内饰缺陷图预览
    public static InteriorPaintPreviewView interiorPaintPreviewView;

    // 记录已经拍摄的照片数
    public static int[] photoShotCount = {0, 0, 0, 0, 0, 0, 0};

    // 记录当前拍摄的文件名
    private long currentTimeMillis;

    // 记录当前正在拍摄的部位
    private int currentShotPart;

    // 车辆型号标识
    private int figure;

    // 承载checkbox
    private TableLayout root;

    // 记录脏污部位
    private String dirtyResult = "";

    // 记录破损部位
    private String brokenResult = "";

    // 自定义scrollView
    private MyScrollView scrollView;

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
        this.context = context;
        rootView = LayoutInflater.from(context).inflate(R.layout.interior_layout, this);

        posEntities = new ArrayList<PosEntity>();
        photoEntities = new ArrayList<PhotoEntity>();
        standardPhotoEntities = new ArrayList<PhotoEntity>();
        interiorPaintPreviewView = (InteriorPaintPreviewView) findViewById(R.id.interior_image);

        Button startCameraButton = (Button)findViewById(R.id.interior_camera_button);
        startCameraButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startCamera();
            }
        });

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

        scrollView = (MyScrollView)findViewById(R.id.root);
        scrollView.setListener(new MyScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy) {
                if(scrollView.getScrollY() > 5) {
                    showShadow(true);
                } else {
                    showShadow(false);
                }
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
    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    /**
     * 更新界面
     */
    public void updateUi() {
        figure = Integer.parseInt(BasicInfoLayout.mCarSettings.getFigure());
        previewViewBitmap = getBitmapFromFigure(figure);

        interiorPaintPreviewView = (InteriorPaintPreviewView) findViewById(R.id.interior_image);
        interiorPaintPreviewView.init(previewViewBitmap, posEntities);

        // 点击预览图进入绘制界面
        interiorPaintPreviewView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PaintActivity.class);
                intent.putExtra("PAINT_TYPE", "IN_PAINT");
                ((Activity)getContext()).startActivityForResult(intent, Common.ENTER_INTERIOR_PAINT);
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
     * 拍摄内饰标准照
     */
    private void startCamera() {
        String[] itemArray = getResources().getStringArray(R.array.interior_camera_item);

        for(int i = 0; i < itemArray.length; i++) {
            itemArray[i] += " (";
            itemArray[i] += Integer.toString(photoShotCount[i]);
            itemArray[i] += ") ";
        }

        View view1 = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.popup_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view1)
                .create();

        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        final ListView listView = new ListView(view1.getContext());
        listView.setAdapter(new ArrayAdapter<String>(view1.getContext(), android.R.layout.simple_list_item_1, itemArray));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialog.dismiss();
                currentShotPart = i;
                String group = getResources().getStringArray(R.array.interior_camera_item)[currentShotPart];
                Toast.makeText(context, "正在拍摄" + group + "组", Toast.LENGTH_LONG).show();

                // 使用当前毫秒数当作照片名
                currentTimeMillis = System.currentTimeMillis();
                Uri fileUri = Helper.getOutputMediaFileUri(Long.toString(currentTimeMillis) + ".jpg");

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // 设置拍摄的文件名
                ((Activity)getContext()).startActivityForResult(intent, Common.PHOTO_FOR_INTERIOR_STANDARD);
            }
        });
        contentArea.addView(listView);

        setTextView(view1, R.id.title, getResources().getString(R.string.interior_camera));

        dialog.show();
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
                checkBox.setTextSize(22f);
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
     * 保存内饰标准照
     */
    public void saveInteriorStandardPhoto() {
        Helper.setPhotoSize(Long.toString(currentTimeMillis) + ".jpg", 800);
        Helper.generatePhotoThumbnail(Long.toString(currentTimeMillis) + ".jpg", 400);

        PhotoEntity photoEntity = generatePhotoEntity();

        PhotoInteriorLayout.photoListAdapter.addItem(photoEntity);
        PhotoInteriorLayout.photoListAdapter.notifyDataSetChanged();

        photoShotCount[currentShotPart]++;

        startCamera();
    }

    /**
     * 生成photoEntity
     * @return
     */
    private PhotoEntity generatePhotoEntity() {
        // 组织JsonString
        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();
            String currentPart = "";

            switch (currentShotPart) {
                case 0:
                    currentPart = "workbench";
                    break;
                case 1:
                    currentPart = "steeringWheel";
                    break;
                case 2:
                    currentPart = "dashboard";
                    break;
                case 3:
                    currentPart = "leftDoor+steeringWheel";
                    break;
                case 4:
                    currentPart = "rearSeats";
                    break;
                case 5:
                    currentPart = "coDriverSeat";
                    break;
                case 6:
                    currentPart = "other";
                    break;
            }

            photoJsonObject.put("part", currentPart);

            jsonObject.put("Group", "interior");
            jsonObject.put("Part", "standard");
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("CarId", BasicInfoLayout.carId);
        } catch (JSONException e) {

        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName(Long.toString(currentTimeMillis) + ".jpg");
        if(!photoEntity.getFileName().equals(""))
            photoEntity.setThumbFileName(Long.toString(currentTimeMillis) + "_t.jpg");
        else
            photoEntity.setThumbFileName("");
        photoEntity.setJsonString(jsonObject.toString());
        String group = getResources().getStringArray(R.array.interior_camera_item)[currentShotPart];
        photoEntity.setName(group);

        return photoEntity;
    }

    /**
     * 根据车辆类型编码，确定预览图
     * @param figure
     * @return
     */
    private Bitmap getBitmapFromFigure(int figure) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return BitmapFactory.decodeFile(getBitmapNameFromFigure(figure), options);
    }

    private String getBitmapNameFromFigure(int figure) {

        return Common.utilDirectory + getNameFromFigure(figure);
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
     * @throws JSONException
     */
    public JSONObject generateJSONObject() throws JSONException{
        JSONObject interior = new JSONObject();

        interior.put("sealingStrip", getSpinnerSelectedText(rootView, R.id.sealingStrip_spinner));
        interior.put("comment", getEditViewText(rootView, R.id.interior_comment_edit));
        //interior.put("dirty", getEditViewText(rootView, R.id.dirty_edit));
        //interior.put("broken", getEditViewText(rootView, R.id.broken_edit));

        return interior;
    }

    /**
     * 生成内饰草图
     * @return
     */
    public PhotoEntity generateSketch() {
        Bitmap bitmap = null;
        Canvas c;

        try {
            bitmap = Bitmap.createBitmap(interiorPaintPreviewView.getMaxWidth(),interiorPaintPreviewView.getMaxHeight(),
                    Bitmap.Config.ARGB_8888);
            c = new Canvas(bitmap);
            interiorPaintPreviewView.draw(c);

            FileOutputStream out = new FileOutputStream(Common.photoDirectory + "interior");
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
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
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
        } catch (JSONException e) {

        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName("interior");
        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * @param interior
     * @throws JSONException
     */
    public void fillInData(JSONObject interior) throws JSONException{
        setSpinnerSelectionWithString(rootView, R.id.sealingStrip_spinner, interior.getString("sealingStrip"));
        setEditViewText(rootView, R.id.interior_comment_edit, interior.getString("comment"));
    }

    /**
     * 提交前的检查
     * @return
     */
    public String checkAllFields() {
        int sum = 0;

        for(int i = 0; i < photoShotCount.length; i++) {
            sum += photoShotCount[i];
        }

        if(sum < 1) {
            return "interior";
        } else {
            return "";
        }
    }

    /**
     * 定位到拍摄按钮
     */
    public void locateCameraButton() {
        final Button button = (Button)findViewById(R.id.interior_camera_button);
        button.setFocusable(true);
        button.requestFocus();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, button.getBottom());
            }
        });
    }
}
