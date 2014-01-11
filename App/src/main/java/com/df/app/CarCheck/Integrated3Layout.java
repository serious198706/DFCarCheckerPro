package com.df.app.CarCheck;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.df.app.R;

import static com.df.app.util.Helper.getEditViewText;

/**
 * Created by 岩 on 13-12-25.
 */
public class Integrated3Layout extends LinearLayout {
    private View rootView;

    public Integrated3Layout(Context context) {
        super(context);
        init(context);
    }

    public Integrated3Layout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Integrated3Layout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.integrated3_layout, this);
    }

    public String generateCommentString() {
        return "";
    }
}
