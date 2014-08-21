package com.ads.view;

import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustView extends FrameLayout {

    public static final int CUST_TEXT_DEFAULT_SIZE = 20;
    private HashMap<Integer, TextView> mTextViews = new HashMap<Integer, TextView>();

    public CustView(Context context) {
        super(context);
    }
    
    public CustView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void clearViews() {
        removeAllViews();
        mTextViews.clear();
    }
    
    public void showText(String txt, int pos, int color) {
        TextView text = mTextViews.get(pos);
        if(text == null) {
            int x = pos >> 16;
            int y = (int) ((pos & 0x00FF) * 1.5);
            text = new TextView(getContext());
            text.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            text.setTextSize(CUST_TEXT_DEFAULT_SIZE);
            text.setPadding(x, y, 0, 0);
            this.addView(text);
            mTextViews.put(pos, text);
        }
        text.setText(txt);
        text.setTextColor(color|0xFF000000);
    }
    
}
