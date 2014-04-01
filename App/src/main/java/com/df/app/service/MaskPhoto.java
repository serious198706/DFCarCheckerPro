package com.df.app.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by �� on 13-11-29.
 */
@SuppressLint("DrawAllocation")
class MaskPhoto extends View {
    Bitmap bgr;
    Bitmap overlayDefault;
    Bitmap overlay;
    Paint pTouch;

    Context context;

    ArrayList<Point> points = new ArrayList<Point>();

    Path drawPath = new Path();

    int X = -100;
    int Y = -100;
    Canvas c2;

    public MaskPhoto(Context context, String fileName) {
        super(context);

        this.context = context;

        bgr = BitmapFactory.decodeFile(fileName);
        bgr = fastblur(bgr, 60, 0, 0, 0, 0);

        overlayDefault = BitmapFactory.decodeFile(fileName);
        overlay = overlayDefault.copy(Bitmap.Config.ARGB_8888, true);

        init();
    }

    public MaskPhoto(Context context, Bitmap bitmap) {
        super(context);

        this.context = context;

        bgr = bitmap;
        bgr = fastblur(bgr, 60, 0, 0, 0, 0);

        overlayDefault = bitmap;
        overlay = overlayDefault.copy(Bitmap.Config.ARGB_8888, true);

        init();
    }

    public void init() {
        c2 = new Canvas(overlay);

        pTouch = new Paint(Paint.ANTI_ALIAS_FLAG);
        pTouch.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        pTouch.setColor(Color.TRANSPARENT);
        pTouch.setMaskFilter(new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL));
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                X = (int) ev.getX();
                Y = (int) ev.getY();

                drawPath.moveTo(ev.getX(), ev.getY());

                points.add(new Point(X, Y));
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                X = (int) ev.getX();
                Y = (int) ev.getY();

                drawPath.lineTo(ev.getX(), ev.getY());

                points.add(new Point(X, Y));
                break;

            }

            case MotionEvent.ACTION_UP:

                //drawPath.lineTo(ev.getX(), ev.getY());
                //c2.drawPath(drawPath, pTouch);
                //drawPath.reset();
                break;

        }

        invalidate();
        return true;
    }


    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        canvas.drawBitmap(bgr, 0, 0, null);

        //copy the default overlay into temporary overlay and punch a hole in it
        c2.drawBitmap(overlayDefault, 0, 0, null);

        for(int i = 0; i < points.size(); i++)
        {
            c2.drawCircle(points.get(i).x, points.get(i).y, 30, pTouch);
        }

        Paint new_paint = new Paint();
        new_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(overlay, 0, 0, new_paint);
    }

    private Bitmap fastblur(Bitmap sentBitmap, int radius, int fromX, int fromY, int width, int height) {
        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w;
        int h;

        if((width == 0) || (height == 0))
        {
            w = sentBitmap.getWidth();
            h = sentBitmap.getHeight();
        }
        else
        {
            w = width;
            h = height;
        }

        int[] pix = new int[w * h];

        bitmap.getPixels(pix, 0, w, fromX, fromY, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        int originRadius = radius;
        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }

        radius = originRadius;

        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = 0xff000000 | (dv[rsum] << 16) | (dv[gsum] << 8)
                        | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, fromX, fromY, w, h);

        return (bitmap);
    }
}
