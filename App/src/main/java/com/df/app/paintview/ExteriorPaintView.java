package com.df.app.paintView;

/**
 * Created by 岩 on 13-9-26.
 *
 * 外观缺陷绘制
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.df.app.carCheck.ExteriorLayout;
import com.df.app.R;
import com.df.app.entries.Action;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.service.AddPhotoCommentActivity;
import com.df.app.service.customCamera.IPhotoProcessListener;
import com.df.app.service.customCamera.PhotoProcessManager;
import com.df.app.service.customCamera.PhotoTask;
import com.df.app.util.Common;
import com.df.app.util.Helper;
import com.df.app.util.PhotoUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ExteriorPaintView extends PaintView implements IPhotoProcessListener {
    public interface OnAddEmptyPhoto {
        public void onAddEmptyPhoto(PosEntity posEntity);
    }

    private int currentType = Common.COLOR_DIFF;
    private boolean move;

    // 本次更新的坐标点，如果用户点击取消，则不将thisTimeNewData中的坐标加入到data中
    private List<PosEntity> thisTimeNewData;
    private List<PosEntity> undoData;
    private List<PosEntity> data = ExteriorLayout.posEntities;

    // 本次更新的照片，如果用户点击取消，则不将thisTimeNewPhoto加入照片列表中
    private List<PhotoEntity> thisTimeNewPhoto;
    private List<PhotoEntity> photo = ExteriorLayout.photoEntities;
    private List<PhotoEntity> undoPhoto;

    private Bitmap bitmap;
    private Bitmap colorDiffBitmap;
    private Bitmap otherBitmap;

    private int max_x, max_y;

    private long currentTimeMillis;

    private SparseArray<String> typeNameMap;

    private OnAddEmptyPhoto mCallback;

    public long getCurrentTimeMillis() {return currentTimeMillis;}

    public ExteriorPaintView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init();
    }

    public ExteriorPaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init();
    }

    public ExteriorPaintView(Context context) {
        super(context);
        //init();
    }

    public void init(Bitmap bitmap, List<PosEntity> entities, OnAddEmptyPhoto listener) {
        typeNameMap = new SparseArray<String>();
        typeNameMap.put(Common.COLOR_DIFF, "色差");
        typeNameMap.put(Common.SCRATCH, "划痕");
        typeNameMap.put(Common.TRANS, "变形");
        typeNameMap.put(Common.SCRAPE, "刮蹭");
        typeNameMap.put(Common.OTHER, "其他");

        this.mCallback = listener;

        this.bitmap = bitmap;
        data = entities;

        max_x = bitmap.getWidth();
        max_y = bitmap.getHeight();

        undoData = new ArrayList<PosEntity>();
        thisTimeNewData = new ArrayList<PosEntity>();

        thisTimeNewPhoto = new ArrayList<PhotoEntity>();
        undoPhoto = new ArrayList<PhotoEntity>();

        colorDiffBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.out_color_diff);
        otherBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.out_other);
        this.setOnTouchListener(onTouchListener);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, null);
        paint(canvas);
    }

    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (currentType > 0 && currentType <= 5) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                PosEntity entity = null;

                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    entity = new PosEntity(currentType);
                    entity.setMaxX(max_x);
                    entity.setMaxY(max_y);
                    entity.setStart(x, y);

                    // 当前绘图类型不为色差和其它时，设置终点
                    if((currentType != Common.COLOR_DIFF) && (currentType != Common.OTHER)){
                        entity.setEnd(x, y);
                    }

                    // 按下时就设置此PosEntity的fileName
                    currentTimeMillis = System.currentTimeMillis();
                    entity.setImageFileName(Long.toString(currentTimeMillis) + ".jpg");

                    data.add(entity);
                    thisTimeNewData.add(entity);
                } else if(event.getAction() == MotionEvent.ACTION_MOVE){
                    if((currentType != Common.COLOR_DIFF) && (currentType != Common.OTHER)){
                        entity = data.get(data.size() - 1);
                        entity.setEnd(x, y);
                        move = true;
                    } else {
                        entity = data.get(data.size() - 1);
                        entity.setStart(x, y);
                        move = true;
                        invalidate();
                    }
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    if(currentType == Common.SCRATCH && move){
                        entity = data.get(data.size() - 1);
                        entity.setEnd(x, y);

                        move = false;
                    }

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
        paint.setColor(Common.PAINTCOLOR);
        paint.setAlpha(0x80);//半透明
        paint.setStyle(Paint.Style.STROKE); //加粗
        paint.setStrokeWidth(4); //宽度

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
            case Common.COLOR_DIFF:
                canvas.drawBitmap(colorDiffBitmap, entity.getStartX(), entity.getStartY(), null);
                return;
            case Common.SCRATCH:
                canvas.drawLine(entity.getStartX(), entity.getStartY(), entity.getEndX(), entity.getEndY(), getPaint(type));
                return ;
            case Common.TRANS:
                // 计算半径
                int dx = Math.abs(entity.getEndX() - entity.getStartX());
                int dy = Math.abs(entity.getEndY() - entity.getStartY());
                int dr = (int)Math.sqrt(dx * dx + dy * dy);

                // 计算圆心
                int x0 = (entity.getStartX() + entity.getEndX()) / 2;
                int y0 = (entity.getStartY() + entity.getEndY()) / 2;

                canvas.drawCircle(x0, y0, dr / 2, getPaint(type));
                return;
            case Common.SCRAPE:
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

                canvas.drawRect(rectF, getPaint(type));
                return;
            case Common.OTHER:
                canvas.drawBitmap(otherBitmap, entity.getStartX(), entity.getStartY(), null);
        }
    }

    /**
     * 拍照
     */
    private void showCamera(){
        PhotoProcessManager.getInstance().registPhotoProcessListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("拍照");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String temp =  getPosEntity().getImageFileName();

                String name;
                int type = getPosEntity().getType();

                switch (type) {
                    case Common.COLOR_DIFF:
                        name = "色差";
                        break;
                    case Common.SCRATCH:
                        name = "划痕";
                        break;
                    case Common.TRANS:
                        name = "变形";
                        break;
                    case Common.SCRAPE:
                        name = "刮蹭";
                        break;
                    case Common.OTHER:
                        name = "其他";
                        break;
                    default:
                        name = "";
                        break;
                }

                long fileName = Long.parseLong(temp.substring(0, temp.length() - 4));

                Helper.startCamera(getContext(), name, fileName);

//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//                Uri fileUri = Helper.getOutputMediaFileUri(data.get(data.size() - 1).getImageFileName());
//                // create a file to save the image
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
//
//                ((Activity) getContext()).startActivityForResult(intent, Common.PHOTO_FOR_EXTERIOR_FAULT);
            }
        })
        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
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

    @Override
    public void onPhotoProcessFinish(List<PhotoTask> list) {
        if(list == null) {
            return;
        }

        for(PhotoTask photoTask : list) {
            // 如果为完成状态
            if(photoTask.getState() == PhotoTask.STATE_COMPLETE) {
                String fileName = data.get(data.size() - 1).getImageFileName();

                // 如果确定拍摄了照片，则缩小照片尺寸
                Helper.handlePhoto(fileName);

                // 进入备注界面
                Intent intent = new Intent(getContext(), AddPhotoCommentActivity.class);
                intent.putExtra("fileName", fileName);
                ((Activity)getContext()).startActivityForResult(intent, Common.ADD_COMMENT_FOR_EXTERIOR_AND_INTERIOR_PHOTO);
            } else {
                // 如果取消了拍摄，将照片名称置空
                getPosEntity().setImageFileName("");
                getPosEntity().setComment("");

                mCallback.onAddEmptyPhoto(getPosEntity());
                thisTimeNewPhoto.add(getPhotoEntities().get(getPhotoEntities().size() - 1));
            }
        }
    }

    /**
     * 获取该paintView的一些内容
     * @return
     */
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
    public Bitmap getSketchBitmap() {
        return this.bitmap;
    }
    public List<PosEntity> getNewPosEntities() {return thisTimeNewData;}
    public String getGroup() {
        return "exterior";
    }
    public void setType(int type) {
        this.currentType = type;
    }
    public int getType() {return this.currentType;}
    public String getTypeName() {
        return typeNameMap.get(getType());
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

