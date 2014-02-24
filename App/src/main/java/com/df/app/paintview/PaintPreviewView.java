package com.df.app.paintview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by 岩 on 14-1-24.
 *
 * 预览基类
 */
public abstract class PaintPreviewView extends ImageView {
    public PaintPreviewView(Context context) {
        super(context);
    }

    public PaintPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PaintPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public abstract int getMaxWidth();
    public abstract int getMaxHeight();
}
