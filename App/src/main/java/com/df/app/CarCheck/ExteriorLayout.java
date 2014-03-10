package com.df.app.carCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
import android.widget.Toast;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.paintview.ExteriorPaintPreviewView;
import com.df.app.service.MyScrollView;
import com.df.app.util.Common;

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
import static com.df.app.util.Helper.setSpinnerSelectionWithIndex;
import static com.df.app.util.Helper.setSpinnerSelectionWithString;

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

    // 承载所有的checkbox
    private TableLayout root;

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

    private void init(Context context) {
        ExteriorLayout.context = context;

        rootView = LayoutInflater.from(context).inflate(R.layout.exterior_layout, this);

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

        setSpinnerSelectionWithIndex(rootView, R.id.smooth_spinner, 1);

        MyScrollView scrollView = (MyScrollView) findViewById(R.id.root);
        scrollView.setListener(new MyScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy) {
                if (scrollView.getScrollY() > 5) {
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

    public void updateUi() {
        // 点击图片进入绘制界面
        int figure = Integer.parseInt(BasicInfoLayout.mCarSettings.getFigure());
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

        for(int i = 0; i < count / 2; i++) {
            TableRow row = new TableRow(view.getContext());

            for(int j = 0; j < 2; j++) {
                CheckBox checkBox = new CheckBox(view.getContext());
                checkBox.setText(array[i * 2 + j]);
                checkBox.setTextSize(22f);
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

    /**
     * 拷贝草图
     */
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

    /**
     * 根据车型信息调用不同的预览图
     * @param figure 车辆类型代码
     * @return 图片
     */
    private Bitmap getBitmapFromFigure(int figure) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeFile(getBitmapNameFromFigure(figure), options);
        } catch (OutOfMemoryError e) {
            Toast.makeText(rootView.getContext(), "内存不足，请稍候重试！", Toast.LENGTH_SHORT).show();
            ((Activity)rootView.getContext()).finish();
        }

        return bitmap;
    }

    private String getBitmapNameFromFigure(int figure) {
        return Common.utilDirectory + getNameFromFigure(figure);
    }

    private static String getNameFromFigure(int figure) {
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

    /**
     * 生成草图
     */
    public PhotoEntity generateSketch() {
        Bitmap bitmap = null;
        Canvas c;

        try {
            bitmap = Bitmap.createBitmap(exteriorPaintPreviewView.getMaxWidth(),exteriorPaintPreviewView.getMaxHeight(),
                    Bitmap.Config.ARGB_8888);
            c = new Canvas(bitmap);
            exteriorPaintPreviewView.draw(c);

            FileOutputStream out = new FileOutputStream(Common.photoDirectory + "exterior");
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
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
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName("exterior");
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
        //exterior.put("broken", getEditViewText(rootView, R.id.broken_edit));

        CheckBox checkBox = (CheckBox)findViewById(R.id.needRepair);
        exterior.put("needRepair", checkBox.isChecked() ? "是" : "否");

        return exterior;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     */
    public void fillInData(JSONObject exterior) throws JSONException{
        setSpinnerSelectionWithString(rootView, R.id.smooth_spinner, exterior.getString("smooth"));
        setEditViewText(rootView, R.id.exterior_comment_edit, exterior.getString("comment"));
        setEditViewText(rootView, R.id.glass_edit, exterior.getString("glass"));
        glassResult = exterior.getString("glass");
        setEditViewText(rootView, R.id.screw_edit, exterior.getString("screw"));
        screwResult = exterior.getString("screw");

        CheckBox checkBox = (CheckBox)findViewById(R.id.needRepair);
        checkBox.setChecked(exterior.getString("needRepair").equals("是"));
    }

    public void clearCache() {
        posEntities = null;
        photoEntities = null;
        exteriorPaintPreviewView = null;
    }
}
