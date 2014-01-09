package com.df.app.CarCheck;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.paintview.FramePaintPreviewView;
import com.df.app.service.Command;
import com.df.app.service.UploadPictureTask;
import com.df.app.util.Common;
import com.df.app.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-20.
 */
public class AccidentResultLayout extends LinearLayout {
    private View rootView;

    private static FramePaintPreviewView framePaintPreviewViewFront;
    private static FramePaintPreviewView framePaintPreviewViewRear;

    public static Bitmap previewBitmapFront;
    public static Bitmap previewBitmapRear;

    public static List<PosEntity> posEntitiesFront;
    public static List<PosEntity> posEntitiesRear;
    public static List<PhotoEntity> photoEntitiesFront;
    public static List<PhotoEntity> photoEntitiesRear;

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

        photoEntitiesFront = new ArrayList<PhotoEntity>();
        photoEntitiesRear = new ArrayList<PhotoEntity>();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        previewBitmapFront = BitmapFactory.decodeFile(Common.utilDirectory + "d4_f", options);
        framePaintPreviewViewFront = (FramePaintPreviewView)rootView.findViewById(R.id.front_preview);
        framePaintPreviewViewFront.init(previewBitmapFront, posEntitiesFront);

        previewBitmapRear = BitmapFactory.decodeFile(Common.utilDirectory + "d4_r", options);
        framePaintPreviewViewRear = (FramePaintPreviewView)rootView.findViewById(R.id.rear_preview);
        framePaintPreviewViewRear.init(previewBitmapRear, posEntitiesRear);
    }

    public void updateUi() {

    }

    public PosEntity getPosEntity(int flag) {
        return (flag == Common.PHOTO_FOR_ACCIDENT_FRONT) ?
                posEntitiesFront.get(posEntitiesFront.size() - 1) : posEntitiesRear.get(posEntitiesRear.size() - 1);
    }

    public List<PhotoEntity> generatePhotoEntities() {
        for(PosEntity posEntity : posEntitiesFront) {
            PhotoEntity photoEntity = generatePhotoEntity(posEntity, "front");
            photoEntitiesFront.add(photoEntity);
        }

        for(PosEntity posEntity : posEntitiesRear) {
            PhotoEntity photoEntity = generatePhotoEntity(posEntity, "rear");
            photoEntitiesRear.add(photoEntity);
        }

        photoEntitiesFront.addAll(photoEntitiesRear);

        return photoEntitiesFront;
    }

    public void uploadPictures() {

    }

    private PhotoEntity generatePhotoEntity(PosEntity posEntity, String part) {
        PhotoEntity photoEntity = new PhotoEntity();

        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject photoJsonObject = new JSONObject();

            jsonObject.put("Group", "frame");
            jsonObject.put("Part", part);

            photoJsonObject.put("x", posEntity.getStartX());
            photoJsonObject.put("y", posEntity.getStartY());
            photoJsonObject.put("issueId", posEntity.getIssueId());

            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("CarId", BasicInfoLayout.carId);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());

            photoEntity.setName("缺陷");
            photoEntity.setFileName(posEntity.getImageFileName());
            photoEntity.setJsonString(jsonObject.toString());
        } catch (JSONException e) {
            Log.d(Common.TAG, e.getMessage());
        }

        return photoEntity;
    }
}
