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

import static com.df.app.util.Helper.setEditViewText;

/**
 * Created by 岩 on 13-12-20.
 */
public class InteriorLayout extends LinearLayout {
    private View rootView;
    private Context context;

    public static List<PosEntity> posEntities;
    public static List<PhotoEntity> photoEntities;
    public static List<PhotoEntity> standardPhotoEntities;

    private Bitmap previewViewBitmap;

    InteriorPaintPreviewView interiorPaintPreviewView;

    // 记录已经拍摄的照片数
    private int[] photoShotCount = {0, 0, 0, 0, 0, 0, 0};

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

    public void updateUi() {

        // 点击图片进入绘制界面
        figure = Integer.parseInt(BasicInfoLayout.mCarSettings.getFigure());
        previewViewBitmap = getBitmapFromFigure(figure);

        interiorPaintPreviewView = (InteriorPaintPreviewView) findViewById(R.id.interior_image);
        interiorPaintPreviewView.init(previewViewBitmap, posEntities);
        interiorPaintPreviewView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PaintActivity.class);
                intent.putExtra("PAINT_TYPE", "IN_PAINT");
                ((Activity)getContext()).startActivityForResult(intent, Common.INTERIOR);
            }
        });
    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void updateInteriorPreview() {
        if(!posEntities.isEmpty()) {
            interiorPaintPreviewView.setAlpha(1f);
            interiorPaintPreviewView.invalidate();
            findViewById(R.id.tipOnPreview).setVisibility(View.GONE);
        }
        // 如果没点，则将图片设为半透明，添加提示文字
        else {
            interiorPaintPreviewView.setAlpha(0.3f);
            interiorPaintPreviewView.invalidate();
            findViewById(R.id.tipOnPreview).setVisibility(View.VISIBLE);
        }
    }

    public void startCamera() {
        String[] itemArray = getResources().getStringArray(R.array
                .interior_camera_item);

        for(int i = 0; i < itemArray.length; i++) {
            itemArray[i] += " (";
            itemArray[i] += Integer.toString(photoShotCount[i]);
            itemArray[i] += ") ";
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
        .setTitle(R.string.interior_camera)
        .setItems(itemArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentShotPart = i;
                String group = getResources().getStringArray(R.array.interior_camera_item)[currentShotPart];

                Toast.makeText(context, "正在拍摄" + group + "组", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                currentTimeMillis = System.currentTimeMillis();
                Uri fileUri = Helper.getOutputMediaFileUri(Long.toString(currentTimeMillis));
                // create a file to save the image
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                ((Activity)getContext()).startActivityForResult(intent, Common.PHOTO_FOR_INTERIOR_STANDARD);
            }
        })
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        //builder.setView(inflater.inflate(R.layout.bi_camera_cato_dialog, null));

        //builder.setMessage(R.string.ci_attention_content).setTitle(R.string.ci_attention);
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).create();

        dialog.show();
    }

    private void chooseDirty() {
        showPopupWindow("dirty", getResources().getString(R.string.dirty),
                getResources().getStringArray(R.array.dirty_item));
    }

    private void chooseBroken() {
        showPopupWindow("broken", getResources().getString(R.string.broken),
                getResources().getStringArray(R.array.broken_item));
    }

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

    public List<PhotoEntity> generatePhotoEntities() {
        photoEntities.addAll(standardPhotoEntities);
        return photoEntities;
    }

    public void saveInteriorStandardPhoto() {
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
            jsonObject.put("UniqueId", BasicInfoLayout.carId);
        } catch (JSONException e) {

        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName(Long.toString(currentTimeMillis) + ".jpg");
        photoEntity.setJsonString(jsonObject.toString());
        String group = getResources().getStringArray(R.array.interior_camera_item)[currentShotPart];
        photoEntity.setName(group);

        Helper.setPhotoSize(Long.toString(currentTimeMillis), 800);

        standardPhotoEntities.add(photoEntity);

        PhotoInteriorLayout.photoListAdapter.setItems(
                (ArrayList<PhotoEntity>)standardPhotoEntities);
        PhotoInteriorLayout.photoListAdapter.notifyDataSetChanged();

        // 立刻上传
        //imageUploadQueue.addImage(photoEntity);

        photoShotCount[currentShotPart]++;

        startCamera();
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
}
