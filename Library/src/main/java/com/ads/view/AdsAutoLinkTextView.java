package com.ads.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class AdsAutoLinkTextView extends TextView {

    private static final String tag = AdsAutoLinkTextView.class.getSimpleName();
    
    public AdsAutoLinkTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AdsAutoLinkTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public AdsAutoLinkTextView(Context context) {
        super(context);
    }
    
    @Override
    public boolean onTouchEvent (MotionEvent event) {
        boolean ret = true;
        //We do this to avoid crash problem when the auto link intent is not handled.
        try {
            ret = super.onTouchEvent(event);
        } catch (Exception e) {
            Log.w(tag, "Error when handle touch event:"+e.getMessage());
        }
        return ret;
    }
}
