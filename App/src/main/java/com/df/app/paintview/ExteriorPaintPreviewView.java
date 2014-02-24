package com.df.app.paintview;

/**
 * Created by 岩 on 13-9-26.
 *
 * 外观缺陷预览视图
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.df.app.R;
import com.df.app.entries.PosEntity;
import com.df.app.util.Common;

import java.util.List;

public class ExteriorPaintPreviewView extends PaintPreviewView {
    private List<PosEntity> data;
    private Bitmap bitmap;
    private Bitmap colorDiffBitmap;
    private Bitmap otherBitmap;

    private int max_x, max_y;

    public ExteriorPaintPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init();
    }

    public ExteriorPaintPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init();
    }

    public ExteriorPaintPreviewView(Context context) {
        super(context);
        //init();
    }

    @Override
    public int getMaxWidth() {
        return max_x;
    }

    @Override
    public int getMaxHeight() {
        return max_y;
    }

    public void init(Bitmap bitmap, List<PosEntity> entities) {
        this.bitmap = bitmap;
        data = entities;

        max_x = bitmap.getWidth();
        max_y = bitmap.getHeight();

        colorDiffBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.out_color_diff);
        otherBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.out_other);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, null);
        paint(canvas);
    }

    /**
     * 获取绘制笔触
     * @return
     */
    private Paint getPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
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

    /**
     * 将所有点绘制到背景上
     * @param entity
     * @param canvas
     */
    private void paint(PosEntity entity, Canvas canvas) {
        int type = entity.getType();

        switch (type) {
            case Common.COLOR_DIFF:
                canvas.drawBitmap(colorDiffBitmap, entity.getStartX(), entity.getStartY(), null);
                return;
            case Common.SCRATCH:
                canvas.drawLine(entity.getStartX(), entity.getStartY(), entity.getEndX(), entity.getEndY(), getPaint());
                return ;
            case Common.TRANS:
                int dx = Math.abs(entity.getEndX() - entity.getStartX());
                int dy = Math.abs(entity.getEndY() - entity.getStartY());
                int dr = (int)Math.sqrt(dx * dx + dy * dy);
                int x0 = (entity.getStartX() + entity.getEndX()) / 2;
                int y0 = (entity.getStartY() + entity.getEndY()) / 2;
                canvas.drawCircle(x0, y0, dr / 2, getPaint());
                return;
            case Common.SCRAPE:
                RectF rectF = null;

                // 如果Rect的right < left，或者bottom < top，则会画不出矩形
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

                canvas.drawRect(rectF, getPaint());

                return;
            case Common.OTHER:
                canvas.drawBitmap(otherBitmap, entity.getStartX(), entity.getStartY(), null);
                return;
        }
    }
}

