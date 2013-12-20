package com.df.app.CarCheck;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.df.app.R;

/**
 * Created by 岩 on 13-12-20.
 */
public class AccidentResultLayout extends LinearLayout {
    public AccidentResultLayout(Context context) {
        super(context);
        init(context);
    }

    public AccidentResultLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AccidentResultLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.accident_result_layout, this);
    }
}
