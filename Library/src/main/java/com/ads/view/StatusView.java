package com.ads.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.df.library.R;

public class StatusView extends FrameLayout {
    private TextView mTitle;
    private TextView mContent;
    private ProgressBar mProgress;
    
    public StatusView(Context context) {
        super(context);
    }

    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        inflater.inflate(R.layout.status_view, this); 
        
        mTitle = (TextView) findViewById(R.id.ads_status_title);
        mContent = (TextView) findViewById(R.id.ads_status_content);
        mProgress = (ProgressBar) findViewById(R.id.ads_status_progress);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //Consume the touch event here!
        return true;
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }
    
    public void setContentText(String text) {
        mContent.setText(text);
    }
    
    public void hideProgress() {
        mProgress.setVisibility(View.GONE);
    }
    
    public void setProgress(int progress) {
        mProgress.setVisibility(View.VISIBLE);
        mProgress.setProgress(progress);
        
    }
}
