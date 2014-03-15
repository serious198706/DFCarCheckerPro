package com.df.app.carsChecked;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.df.app.R;

import org.json.JSONObject;

/**
 * Created by 岩 on 14-3-14.
 */
public class OtherLayout extends LinearLayout {
    public OtherLayout(Context context, JSONObject jsonObject) {
        super(context);
        init(context, jsonObject);
    }

    public OtherLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OtherLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    private void init(Context context, JSONObject jsonObject) {
        LayoutInflater.from(context).inflate(R.layout.car_report_other_layout, this);
    }
}
