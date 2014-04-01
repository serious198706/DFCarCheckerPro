package com.df.app.paintView;

/**
 * Created by 岩 on 13-9-26.
 *
 * 内饰缺陷绘制
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.df.app.carCheck.BasicInfoLayout;
import com.df.app.carCheck.ExteriorLayout;
import com.df.app.carCheck.InteriorLayout;
import com.df.app.MainActivity;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.util.Common;
import com.df.app.util.Helper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.df.app.util.Helper.getBitmapHeight;
import static com.df.app.util.Helper.getBitmapWidth;

public class InteriorPaintView extends PaintView {
    public interface OnAddEmptyPhoto {
        public void onAddEmptyPhoto(PosEntity posEntity);
    }

    private int currentType = Common.DIRTY;
    private boolean move;
    // 本次更新的坐标点，如果用户点击取消，则不将thisTimeNewData中的坐标加入到data中
    private List<PosEntity> thisTimeNewData;
    private List<PosEntity> undoData;
    private List<PosEntity> data = ExteriorLayout.posEntities;

    // 本次更新的照片，如果用户点击取消，则不将thisTimeNewPhoto加入照片列表中
    private List<PhotoEntity> thisTimeNewPhoto;
    private List<PhotoEntity> photo = InteriorLayout.photoEntities;
    private List<PhotoEntity> undoPhoto;

    private Bitmap bitmap;

    private int max_x, max_y;

    private long currentTimeMillis;

    private SparseArray<String> typeNameMap;

    private OnAddEmptyPhoto mCallback;

    public InteriorPaintView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init();
    }

    public InteriorPaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init();
    }

    public InteriorPaintView(Context context) {
        super(context);
        //init();
    }

    public void init(Bitmap bitmap, List<PosEntity> entities, OnAddEmptyPhoto listener) {
        typeNameMap = new SparseArray<String>();
        typeNameMap.put(Common.DIRTY, "脏污");
        typeNameMap.put(Common.BROKEN, "破损");

        this.bitmap = bitmap;
        data = entities;

        this.mCallback = listener;

        max_x = bitmap.getWidth();
        max_y = bitmap.getHeight();

        undoData = new ArrayList<PosEntity>();
        thisTimeNewData = new ArrayList<PosEntity>();

        thisTimeNewPhoto = new ArrayList<PhotoEntity>();
        undoPhoto = new ArrayList<PhotoEntity>();

        this.setOnTouchListener(onTouchListener);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, null);
        paint(canvas);
    }

    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (currentType >= Common.DIRTY && currentType <= Common.BROKEN) {

                int x = (int) event.getX();
                int y = (int) event.getY();
                PosEntity entity = null;

                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    entity = new PosEntity(currentType);
                    entity.setMaxX(max_x);
                    entity.setMaxY(max_y);
                    entity.setStart(x, y);
                    entity.setEnd(x, y);

                    // 按下时就设置此PosEntity的fileName
                    currentTimeMillis = System.currentTimeMillis();
                    entity.setImageFileName(Long.toString(currentTimeMillis) + ".jpg");

                    data.add(entity);
                    thisTimeNewData.add(entity);
                } else if(event.getAction() == MotionEvent.ACTION_MOVE){
                    entity = data.get(data.size() - 1);
                    entity.setEnd(x, y);
                    move = true;
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    if(move){
                        entity = data.get(data.size()-1);
                        entity.setEnd(x, y);
                        move = false;
                    }

                    //showCamera();

                    // 如果手指在屏幕上移动范围非常小
                    if(entity == null) {
                        entity = data.get(data.size() - 1);
                    }

                    if(entity != null) {
                        if(Math.abs(entity.getEndX() - entity.getStartX()) < 10 &&
                                Math.abs(entity.getEndY() - entity.getStartY()) < 10) {
                            data.remove(entity);
                        } else {
                            showCamera();
                        }
                    }
                }
                invalidate();
            }
            return true;
        }
    };

    private Paint getPaint(int type) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 根据当前类型决定笔触的颜色
        paint.setColor(Common.PAINTCOLOR);
        paint.setAlpha(0x80);   //80%透明
        paint.setStyle(Paint.Style.STROKE); // 线类型填充
        paint.setStrokeWidth(4);  // 笔触粗细

        return paint;
    }

    private void paint(Canvas canvas) {
        for (PosEntity entity : data) {
            paint(entity, canvas);
        }
    }

    private void paint(PosEntity entity, Canvas canvas) {
        if(entity.isDelete()) {
            return;
        }

        int type = entity.getType();

        switch (type) {
            case Common.BROKEN:
                // 计算半径
                int dx = Math.abs(entity.getEndX() - entity.getStartX());
                int dy = Math.abs(entity.getEndY() - entity.getStartY());
                int dr = (int)Math.sqrt(dx * dx + dy * dy);

                // 计算圆心
                int x0 = (entity.getStartX() + entity.getEndX()) / 2;
                int y0 = (entity.getStartY() + entity.getEndY()) / 2;

                canvas.drawCircle(x0, y0, dr / 2, getPaint(type));
                return;
            case Common.DIRTY:
                RectF rectF = null;

                // Android:4.0+ 如果Rect的right < left，或者bottom < top，则会画不出矩形
                // 为了修正这个，需要做点处理

                // 右下
                if(entity.getStartX() < entity.getEndX() &&
                        entity.getStartY() < entity.getEndY()) {
                    rectF = new RectF(entity.getStartX(), entity.getStartY(), entity.getEndX(), entity.getEndY());
                }
                // 右上
                else if(entity.getStartX() < entity.getEndX() &&
                        entity.getStartY() > entity.getEndY()) {
                    rectF = new RectF(entity.getStartX(), entity.getEndY(), entity.getEndX(), entity.getStartY());
                }
                // 左下
                else if(entity.getStartX() > entity.getEndX() &&
                        entity.getStartY() < entity.getEndY()) {
                    rectF = new RectF(entity.getEndX(), entity.getStartY(), entity.getStartX(), entity.getEndY());
                }
                // 左上
                else if(entity.getStartX() > entity.getEndX() &&
                        entity.getStartY() > entity.getEndY()) {
                    rectF = new RectF(entity.getEndX(), entity.getEndY(), entity.getStartX(), entity.getStartY());
                }
                // 重合或者默认
                else {
                    rectF = new RectF(entity.getStartX(), entity.getStartY(), entity.getEndX(), entity.getEndY());
                }

                canvas.drawRect(rectF, getPaint(entity.getType()));
        }
    }

    private void showCamera(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("拍照");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Uri fileUri = Helper.getOutputMediaFileUri(data.get(data.size() - 1).getImageFileName());
                // create a file to save the image
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                ((Activity)getContext()).startActivityForResult(intent, Common.PHOTO_FOR_INTERIOR_FAULT);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getPosEntity().setImageFileName("");
                getPosEntity().setComment("");

                mCallback.onAddEmptyPhoto(getPosEntity());
                thisTimeNewPhoto.add(getPhotoEntities().get(getPhotoEntities().size() - 1));
            }
        }).setCancelable(false);

        builder.show();
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
    public List<PhotoEntity> getPhotoEntities() { return photo; }
    public List<PhotoEntity> getPhotoEntities(String sight) { return null; }
    public List<PhotoEntity> getNewPhotoEntities() {return thisTimeNewPhoto;}
    public List<PosEntity> getNewPosEntities() {return thisTimeNewData;}
    public Bitmap getSketchBitmap() {
        return this.bitmap;
    }
    public String getGroup() {
        return "interior";
    }
    public void setType(int type) {
        this.currentType = type;
    }
    public int getType() {return this.currentType;}
    public String getTypeName() {
        return typeNameMap.get(getType());
    }


    public void clear() {
        if(!data.isEmpty()) {
            data.clear();
            undoData.clear();
            invalidate();
        }
        if(!photo.isEmpty()) {
            photo.clear();
            undoPhoto.clear();
        }
    }

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

    public void cancel() {
        if(!thisTimeNewData.isEmpty()) {
            for (PosEntity aThisTimeNewData : thisTimeNewData) {
                data.remove(aThisTimeNewData);
            }
        }
        if(!thisTimeNewPhoto.isEmpty()) {
            for (PhotoEntity aThisTimeNewPhoto : thisTimeNewPhoto) {
                photo.remove(aThisTimeNewPhoto);
            }
        }
    }
}

