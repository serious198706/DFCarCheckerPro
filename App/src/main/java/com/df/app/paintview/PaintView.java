package com.df.app.paintview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;

import java.util.List;

/**
 * Created by 岩 on 13-11-4.
 *
 * 绘制基类
 */
public abstract class PaintView extends ImageView {

    public PaintView(Context context) {
        super(context);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PaintView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public abstract void onDraw(Canvas canvas);

    public abstract void cancel();
    public abstract void undo();
    public abstract void redo();
    public abstract void clear();
    public abstract PosEntity getPosEntity();
    public abstract List<PhotoEntity> getPhotoEntities();
    public abstract List<PhotoEntity> getPhotoEntities(String sight);
    public abstract List<PhotoEntity> getNewPhotoEntities();
    public abstract long getCurrentTimeMillis();
    public abstract String getGroup();
    public abstract int getType();
    public abstract List<PosEntity> getPosEntities();
    public abstract Bitmap getSketchBitmap();
    public abstract List<PosEntity> getNewPosEntities();
    public abstract String getTypeName();
}
