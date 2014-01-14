package com.df.app.CarCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
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

/**
 * Created by 岩 on 13-12-20.
 */
public class ExteriorLayout extends LinearLayout {
    private Context context;
    private View rootView;

    public static List<PosEntity> posEntities;
    public static List<PhotoEntity> photoEntities;
    public static List<PhotoEntity> standardPhotoEntities;

    private ExteriorPaintPreviewView exteriorPaintPreviewView;

    private Bitmap previewViewBitmap;

    private int figure;

    // 记录已经拍摄的照片数
    private int[] photoShotCount = {0, 0, 0, 0, 0, 0, 0};

    // 记录当前拍摄的文件名
    private long currentTimeMillis;

    // 记录当前正在拍摄的部位
    private int currentShotPart;

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
        this.context = context;

        rootView = LayoutInflater.from(context).inflate(R.layout.exterior_layout, this);

        posEntities = new ArrayList<PosEntity>();
        photoEntities = new ArrayList<PhotoEntity>();
        standardPhotoEntities = new ArrayList<PhotoEntity>();

        Button startCameraButton = (Button)findViewById(R.id.exterior_camera_button);
        startCameraButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                starCamera();
            }
        });

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

        MyScrollView scrollView = (MyScrollView)findViewById(R.id.root);
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
    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void updateUi() {
        // 点击图片进入绘制界面
        figure = Integer.parseInt(BasicInfoLayout.mCarSettings.getFigure());
        previewViewBitmap = getBitmapFromFigure(figure);

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

    public void updateExteriorPreview() {
        if(!posEntities.isEmpty()) {
            exteriorPaintPreviewView.setAlpha(1f);
            exteriorPaintPreviewView.invalidate();
            findViewById(R.id.tipOnPreview).setVisibility(View.GONE);
        }
        // 如果没点，则将图片设为半透明，添加提示文字
        else {
            exteriorPaintPreviewView.setAlpha(0.3f);
            exteriorPaintPreviewView.invalidate();
            findViewById(R.id.tipOnPreview).setVisibility(View.VISIBLE);
        }
    }

    private void starCamera() {
        String[] itemArray = getResources().getStringArray(R.array.exterior_camera_item);

        for(int i = 0; i < itemArray.length; i++) {
            itemArray[i] += " (";
            itemArray[i] += Integer.toString(photoShotCount[i]);
            itemArray[i] += ") ";
        }

        AlertDialog dialog = new AlertDialog.Builder(context).setTitle(R.string.exterior_camera)
        .setItems(itemArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentShotPart = i;

                String group = getResources().getStringArray(R.array.exterior_camera_item)[currentShotPart];

                Toast.makeText(context, "正在拍摄" + group + "组", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                currentTimeMillis = System.currentTimeMillis();
                Uri fileUri = Helper.getOutputMediaFileUri(Long.toString(currentTimeMillis) + ".jpg"); //
                // create a
                // file to save the image
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                ((Activity)getContext()).startActivityForResult(intent, Common.PHOTO_FOR_EXTERIOR_STANDARD);
            }
        })
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        })
        .create();

        dialog.show();
    }

    private void chooseGlass() {
        showPopupWindow("glass", getResources().getString(R.string.glass),
                getResources().getStringArray(R.array.glass_item));
    }

    private void chooseScrew() {
        showPopupWindow("screw", getResources().getString(R.string.screw),
                getResources().getStringArray(R.array.screw_item));
    }

    private void chooseBroken() {
        showPopupWindow("broken", getResources().getString(R.string.ex_broken),
                getResources().getStringArray(R.array.ex_broken_item));
    }

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

    public void saveExteriorStandardPhoto() {
        Helper.setPhotoSize(Long.toString(currentTimeMillis) + ".jpg", 800);

        PhotoEntity photoEntity = generatePhotoEntity();

        PhotoExteriorLayout.photoListAdapter.addItem(photoEntity);
        PhotoExteriorLayout.photoListAdapter.notifyDataSetChanged();

        photoShotCount[currentShotPart]++;

        starCamera();
    }

    private PhotoEntity generatePhotoEntity() {
        // 组织JsonString
        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();
            String currentPart = "";

            switch (currentShotPart) {
                case 0:
                    currentPart = "leftFront45";
                    break;
                case 1:
                    currentPart = "rightFront45";
                    break;
                case 2:
                    currentPart = "left";
                    break;
                case 3:
                    currentPart = "right";
                    break;
                case 4:
                    currentPart = "leftRear45";
                    break;
                case 5:
                    currentPart = "rightRear45";
                    break;
                case 6:
                    currentPart = "other";
                    break;
            }

            photoJsonObject.put("part", currentPart);

            jsonObject.put("Group", "exterior");
            jsonObject.put("Part", "standard");
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("CarId", BasicInfoLayout.carId);
        } catch (JSONException e) {

        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName(Long.toString(currentTimeMillis) + ".jpg");
        photoEntity.setJsonString(jsonObject.toString());
        String group = getResources().getStringArray(R.array.exterior_camera_item)[currentShotPart];
        photoEntity.setName(group);

        return photoEntity;
    }

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

    public void fillInData(JSONObject exterior) {

    }
}
