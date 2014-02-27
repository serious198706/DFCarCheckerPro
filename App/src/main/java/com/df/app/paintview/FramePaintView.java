package com.df.app.paintview;

/**
 * Created by 岩 on 13-9-26.
 *
 * 事故查勘绘制
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.df.app.carCheck.AccidentResultLayout;
import com.df.app.carCheck.BasicInfoLayout;
import com.df.app.carCheck.PhotoFaultLayout;
import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.util.Common;
import com.df.app.util.Helper;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FramePaintView extends PaintView {

    private int currentType = Common.COLOR_DIFF;

    // 本次更新的坐标点，如果用户点击取消，则不将thisTimeNewData中的坐标加入到data中
    private List<PosEntity> thisTimeNewData;
    private List<PosEntity> undoData;
    private List<PosEntity> data;

    // 本次更新的照片，如果用户点击取消，则不将thisTimeNewPhoto加入照片列表中
    private List<PhotoEntity> thisTimeNewPhoto;
    private List<PhotoEntity> photo;
    private List<PhotoEntity> undoPhoto;

    private Bitmap bitmap;
    private Bitmap damageBitmap;

    private int max_x, max_y;

    private String sight;
    private int issueId;
    private String comment;

    private long currentTimeMillis;

    public long getCurrentTimeMillis() {return currentTimeMillis;}

    public FramePaintView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init();
    }

    public FramePaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init();
    }

    public FramePaintView(Context context) {
        super(context);
        //init();
    }

    public void init(Bitmap bitmap, List<PosEntity> entities, String sight, int issueId, String comment) {
        this.bitmap = bitmap;
        this.data = entities;
        this.sight = sight;
        this.issueId = issueId;
        this.comment = comment;

        max_x = bitmap.getWidth();
        max_y = bitmap.getHeight();

        undoData = new ArrayList<PosEntity>();
        thisTimeNewData = new ArrayList<PosEntity>();

        photo = sight.equals("F") ? AccidentResultLayout.photoEntitiesFront : AccidentResultLayout.photoEntitiesRear;
        thisTimeNewPhoto = new ArrayList<PhotoEntity>();
        undoPhoto = new ArrayList<PhotoEntity>();

        damageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.damage);
        this.setOnTouchListener(onTouchListener);
    }

    public void setBitmap(Bitmap bitmap) {
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, null);
        paint(canvas);
    }

    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (currentType > 0 && currentType <= 4) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                PosEntity entity;

                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    entity = new PosEntity(currentType);
                    entity.setMaxX(max_x);
                    entity.setMaxY(max_y);
                    entity.setStart(x, y);

                    // 按下时就设置此PosEntity的fileName
                    currentTimeMillis = System.currentTimeMillis();
                    entity.setImageFileName(Long.toString(currentTimeMillis) + ".jpg");

                    // 按下时就设置issueId(由Adapter传来)
                    entity.setIssueId(issueId);

                    // 按下时就设置comment(由Adapter传来)
                    entity.setComment(comment);

                    data.add(entity);
                    thisTimeNewData.add(entity);
                } else if(event.getAction() == MotionEvent.ACTION_MOVE){
                        entity = data.get(data.size() - 1);
                        entity.setStart(x, y);
                        invalidate();
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    showCamera();
                }

                invalidate();
            }

            return true;
        }
    };

    private void paint(Canvas canvas) {
        for (PosEntity entity : data) {
            paint(entity, canvas);
        }
    }

    private void paint(PosEntity entity, Canvas canvas) {
        canvas.drawBitmap(damageBitmap, entity.getStartX(), entity.getStartY(), null);
    }

    private void showCamera(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("拍照");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // 根据此点文件名进行拍照
                Uri fileUri = Helper.getOutputMediaFileUri(data.get(data.size() - 1).getImageFileName());

                // create a file to save the image
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                ((Activity)getContext()).startActivityForResult(intent,
                        sight.equals("F") ? Common.PHOTO_FOR_ACCIDENT_FRONT : Common.PHOTO_FOR_ACCIDENT_REAR);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 如果点击了取消，则直接将此点对应的文件名设置为""，并且生成PhotoEntity
                getPosEntity().setImageFileName("");
                getPosEntity().setComment("");

                PhotoEntity photoEntity = generatePhotoEntity();
                PhotoFaultLayout.photoListAdapter.addItem(photoEntity);
                PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();

                photo.add(photoEntity);
            }
        }).setCancelable(false);

        builder.show();
    }

    private PhotoEntity generatePhotoEntity() {
        PhotoEntity photoEntity = new PhotoEntity();

        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject photoJsonObject = new JSONObject();

            jsonObject.put("Group", "frame");
            jsonObject.put("Part", sight.equals("F") ? "front" : "rear");

            PosEntity posEntity = getPosEntity();

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
            if(photoEntity.getFileName().equals("")) {
                photoEntity.setThumbFileName("");
            } else {
                photoEntity.setThumbFileName(posEntity.getImageFileName().substring(0, posEntity.getImageFileName().length() - 4) + "_t.jpg");
            }
            photoEntity.setJsonString(jsonObject.toString());
        } catch (JSONException e) {
            Log.d(Common.TAG, e.getMessage());
        }

        return photoEntity;
    }

    // 获取该paintView的一些内容
    public PosEntity getPosEntity(){
        if(data.isEmpty()){
            return null;
        }
        return data.get(data.size()-1);
    }
    public List<PosEntity> getPosEntities() {
        return data;
    }
    public List<PhotoEntity> getPhotoEntities() {
        return null;
    }
    public List<PhotoEntity> getPhotoEntities(String sight) {
        return null;
    }
    public List<PhotoEntity> getNewPhotoEntities() {return null;}
    public List<PosEntity> getNewPosEntities() {return thisTimeNewData;}
    public Bitmap getSketchBitmap() {
        return this.bitmap;
    }
    public String getGroup() {
        return "frame";
    }
    public void setType(int type) {
        this.currentType = type;
    }
    public int getType() {return this.currentType;}
    public String getTypeName() {
        return "结构缺陷";
    }


    @Override
    public void clear() {
        if(!data.isEmpty()) {
            data.clear();
            undoData.clear();
            thisTimeNewData.clear();
            invalidate();
        }
        if(!photo.isEmpty()) {
            photo.clear();
            undoPhoto.clear();
            thisTimeNewPhoto.clear();
        }
    }

    @Override
    public void undo() {
        if(!data.isEmpty()) {
            undoData.add(data.get(data.size() - 1));
            data.remove(data.size() - 1);
            invalidate();
        }
        if(!photo.isEmpty()) {
            undoPhoto.add(photo.get(photo.size() - 1));
            photo.remove(photo.size() - 1);
        }
    }

    @Override
    public void redo() {
        if(!undoData.isEmpty()) {
            data.add(undoData.get(undoData.size() - 1));
            undoData.remove(undoData.size() - 1);
            invalidate();
        }
        if(!undoPhoto.isEmpty()) {
            photo.add(undoPhoto.get(undoPhoto.size() - 1));
            undoPhoto.remove(undoPhoto.size() - 1);
        }
    }

    @Override
    public void cancel() {
        if(!thisTimeNewData.isEmpty()) {
            for(int i = 0; i < thisTimeNewData.size(); i++) {
                data.remove(thisTimeNewData.get(i));
            }
        }
        if(!thisTimeNewPhoto.isEmpty()) {
            for(int i = 0; i < thisTimeNewPhoto.size(); i++) {
                photo.remove(thisTimeNewPhoto.get(i));
            }
        }
    }
}

