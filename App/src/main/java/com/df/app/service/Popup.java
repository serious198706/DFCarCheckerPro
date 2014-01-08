package com.df.app.service;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.df.app.R;

/**
 * Created by 岩 on 14-1-2.
 */
public class Popup extends LinearLayout {
    private View rootView;

    public Popup(Context context) {
        super(context);
        init(context);
    }

    public Popup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Popup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.popup_layout, this);
    }
}
