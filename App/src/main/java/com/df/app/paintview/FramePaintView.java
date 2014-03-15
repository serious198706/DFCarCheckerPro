package com.df.app.paintView;

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
import com.df.app.entries.Issue;
import com.df.app.entries.ListedPhoto;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.service.Adapter.IssuePhotoListAdapter;
import com.df.app.util.Common;
import com.df.app.util.Helper;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.df.app.util.Helper.drawTextToBitmap;
import static com.df.app.util.Helper.getBitmapHeight;
import static com.df.app.util.Helper.getBitmapWidth;

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

    private long currentTimeMillis;

    private Issue issue;
    private IssuePhotoListAdapter adapter;
    private Context context;

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
        if(isInEditMode()) {
        }
        //init();
    }

    public void init(Context context, Bitmap bitmap, Issue issue, IssuePhotoListAdapter adapter, List<PosEntity> entities, String sight) {
        this.context = context;
        this.bitmap = bitmap;
        this.issue = issue;
        this.data = entities;
        this.sight = sight;
        this.adapter = adapter;

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
                    entity.setIssueId(issue.getId());

                    // 按下时就设置comment(由Adapter传来)
                    entity.setComment(issue.getDesc());

                    data.add(entity);
                    thisTimeNewData.add(entity);
                    issue.addPos(entity);
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
        for(int i = 0; i < issue.getPosEntities().size(); i++) {
            PosEntity entity = issue.getPosEntities().get(i);
            paint(entity, i, canvas);
        }
    }

    private void paint(PosEntity entity, int index, Canvas canvas) {
        damageBitmap = drawTextToBitmap(context, R.drawable.damage, index + 1);
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

                // 拍照的时候，记录当前的issue & adapter & thisTimeNewPhoto
                AccidentResultLayout.issue = issue;
                AccidentResultLayout.adapter = adapter;
                AccidentResultLayout.thisTimeNewPhoto = thisTimeNewPhoto;
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
                thisTimeNewPhoto.add(photoEntity);
                issue.addPhoto(photoEntity);

                int index = adapter.getCount();
                ListedPhoto listedPhoto = new ListedPhoto(index, photoEntity.getThumbFileName(),
                        photoEntity.getComment());
                adapter.addItem(listedPhoto);
                adapter.notifyDataSetChanged();
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
            photoJsonObject.put("width", getBitmapWidth(posEntity.getImageFileName()));
            photoJsonObject.put("height", getBitmapHeight(posEntity.getImageFileName()));
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

    // 清除
    @Override
    public void clear() {
        // 如果此issue有数据
        if(!issue.getPosEntities().isEmpty()) {
            // 从所有点的集合（前方视角或后方视角）中删掉此issue的所有点
            data.removeAll(issue.getPosEntities());

            // 回撤的点
            undoData.clear();

            // 此次新添加的点
            thisTimeNewData.clear();

            // 此issue的点
            issue.clearPos();

            // 刷新视图
            invalidate();
        }

        // 如果此issue有数据
        if(!issue.getPhotoEntities().isEmpty()) {
            // 从所有照片的集合（前方视角或后方视角）中删掉此issue的所有照片
            data.removeAll(issue.getPhotoEntities());

            // 回撤的照片
            undoPhoto.clear();

            // 此次新添加的照片
            thisTimeNewPhoto.clear();

            // 此issue的照片
            issue.clearPhoto();
        }

    }

    @Override
    public void undo() {
        if(!issue.getPosEntities().isEmpty()) {
            undoData.add(data.get(data.size() - 1));
            data.remove(data.size() - 1);
            issue.removeLastPos();
            invalidate();
        }
        if(!photo.isEmpty()) {
            undoPhoto.add(photo.get(photo.size() - 1));
            photo.remove(photo.size() - 1);
            issue.removeLastPhoto();
        }
    }

    @Override
    public void redo() {
        if(!undoData.isEmpty()) {
            data.add(undoData.get(undoData.size() - 1));
            undoData.remove(undoData.size() - 1);
            issue.addPos(undoData.get(undoData.size() - 1));
            invalidate();
        }
        if(!undoPhoto.isEmpty()) {
            photo.add(undoPhoto.get(undoPhoto.size() - 1));
            undoPhoto.remove(undoPhoto.size() - 1);
            issue.addPhoto(undoPhoto.get(undoPhoto.size() - 1));
        }
    }

    @Override
    public void cancel() {
        if(!thisTimeNewData.isEmpty()) {
            for(int i = 0; i < thisTimeNewData.size(); i++) {
                data.remove(thisTimeNewData.get(i));
                issue.remove(thisTimeNewData.get(i));
                adapter.remove(adapter.getCount() - 1);
            }
        }
        if(!thisTimeNewPhoto.isEmpty()) {
            for(int i = 0; i < thisTimeNewPhoto.size(); i++) {
                photo.remove(thisTimeNewPhoto.get(i));
                issue.remove(thisTimeNewPhoto.get(i));
            }
        }
    }
}

