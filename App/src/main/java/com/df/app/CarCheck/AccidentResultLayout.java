package com.df.app.CarCheck;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.paintview.FramePaintPreviewView;
import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-20.
 */
public class AccidentResultLayout extends LinearLayout {
    private View rootView;

    public static FramePaintPreviewView framePaintPreviewViewFront;
    public static FramePaintPreviewView framePaintPreviewViewRear;

    public static Bitmap previewBitmapFront;
    public static Bitmap previewBitmapRear;

    public static List<PosEntity> posEntitiesFront;
    public static List<PosEntity> posEntitiesRear;

    public AccidentResultLayout(Context context) {
        super(context);
        init(context);
    }

    public AccidentResultLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AccidentResultLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.accident_result_layout, this);

        posEntitiesFront = new ArrayList<PosEntity>();
        posEntitiesRear = new ArrayList<PosEntity>();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        previewBitmapFront = BitmapFactory.decodeFile(Common.utilDirectory + "d4_f", options);

        LayoutParams layoutParams = new LayoutParams(previewBitmapFront.getWidth(), previewBitmapFront.getHeight());
        layoutParams.gravity = Gravity.CENTER;

        framePaintPreviewViewFront = (FramePaintPreviewView)rootView.findViewById(R.id.front_preview);
        framePaintPreviewViewFront.init(previewBitmapFront, posEntitiesFront);
        framePaintPreviewViewFront.setLayoutParams(layoutParams);

        previewBitmapRear = BitmapFactory.decodeFile(Common.utilDirectory + "d4_r", options);
        framePaintPreviewViewRear = (FramePaintPreviewView)rootView.findViewById(R.id.rear_preview);
        framePaintPreviewViewRear.init(previewBitmapRear, posEntitiesRear);
        framePaintPreviewViewRear.setLayoutParams(layoutParams);
    }

    public void updateUi() {
        setFigureImage(Integer.parseInt(BasicInfoLayout.mCarSettings.getFigure()));
    }

    public PosEntity getPosEntity(int flag) {
        return (flag == Common.ADD_COMMENT_FOR_ACCIDENT_FRONT_PHOTO) ?
                posEntitiesFront.get(posEntitiesFront.size() - 1) : posEntitiesRear.get(posEntitiesRear.size() - 1);
    }

    private String getPart(int flag) {
        return (flag == Common.ADD_COMMENT_FOR_ACCIDENT_FRONT_PHOTO) ? "front" : "rear";
    }

    public void saveAccidentPhoto(int flag) {
        PhotoEntity photoEntity = generatePhotoEntity(flag);

        PhotoFaultLayout.photoListAdapter.addItem(photoEntity);
        PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();
    }

    private PhotoEntity generatePhotoEntity(int flag) {
        PhotoEntity photoEntity = new PhotoEntity();

        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject photoJsonObject = new JSONObject();

            jsonObject.put("Group", "frame");
            jsonObject.put("Part", getPart(flag));

            PosEntity posEntity = getPosEntity(flag);

            photoJsonObject.put("x", posEntity.getStartX());
            photoJsonObject.put("y", posEntity.getStartY());
            photoJsonObject.put("issueId", posEntity.getIssueId());
            photoJsonObject.put("comment", posEntity.getComment());

            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("CarId", BasicInfoLayout.carId);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());

            photoEntity.setName("结构缺陷");
            photoEntity.setFileName(posEntity.getImageFileName());
            photoEntity.setComment(posEntity.getComment());
            photoEntity.setJsonString(jsonObject.toString());
        } catch (JSONException e) {
            Log.d(Common.TAG, e.getMessage());
        }

        return photoEntity;
    }

    private void setFigureImage(int figure) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        String path = Common.utilDirectory;

        // 默认为4门图
        String front = "d4_f";
        String rear = "d4_r";

        // 只有当figure为2、3时才是2门图
        switch (figure) {
            case 2:
            case 3:
                front = "d2_f";
                rear = "d2_r";
                break;
        }

        previewBitmapFront = BitmapFactory.decodeFile(path + front, options);

        LayoutParams layoutParams = new LayoutParams(previewBitmapFront.getWidth(), previewBitmapFront.getHeight());
        layoutParams.gravity = Gravity.CENTER;

        framePaintPreviewViewFront.init(previewBitmapFront, posEntitiesFront);
        framePaintPreviewViewFront.setLayoutParams(layoutParams);

        previewBitmapRear = BitmapFactory.decodeFile(path + rear, options);
        framePaintPreviewViewRear.init(previewBitmapRear, posEntitiesRear);
        framePaintPreviewViewRear.setLayoutParams(layoutParams);
    }

    public List<PhotoEntity> generateSketches() {
        List<PhotoEntity> temp = new ArrayList<PhotoEntity>();

        temp.add(generateSketch(framePaintPreviewViewFront, "fSketch"));
        temp.add(generateSketch(framePaintPreviewViewRear, "rSketch"));

        return temp;
    }

    private PhotoEntity generateSketch(View view, String sketchName) {
        Bitmap bitmap = null;
        Canvas c;

        try {
            bitmap = Bitmap.createBitmap(view.getWidth(),view.getHeight(),
                    Bitmap.Config.ARGB_8888);
            c = new Canvas(bitmap);
            view.draw(c);

            FileOutputStream out = new FileOutputStream(Common.photoDirectory + sketchName);
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();

            photoJsonObject.put("height", bitmap.getHeight());
            photoJsonObject.put("width", bitmap.getWidth());

            jsonObject.put("Group", "frame");
            jsonObject.put("Part", sketchName);
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("CarId", BasicInfoLayout.carId);
        } catch (JSONException e) {

        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName(sketchName);
        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }
}
