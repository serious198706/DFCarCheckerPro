package com.ads.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.RadioButton;

import com.df.library.R;


public class RadioTextView extends FrameLayout implements Checkable{

    private RadioButton mRadioButton;
    private boolean mChecked = false;
    
    public RadioTextView(Context context, AttributeSet attrs) {
        super(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        inflater.inflate(R.layout.radio_text, this); 
        mRadioButton = (RadioButton) findViewById(R.id.radioBtn);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        if(mChecked != checked) {
            mChecked = checked;
            mRadioButton.setChecked(checked);
        } 
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

}
