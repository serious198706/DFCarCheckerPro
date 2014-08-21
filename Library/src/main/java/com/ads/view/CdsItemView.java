package com.ads.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CdsItemView extends FrameLayout implements Checkable{

    private CheckBox mCheckBox;
    private TextView mTxtName;
    private TextView mTxtValue;
    private boolean mChecked;
    
    public CdsItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
//        inflater.inflate(R.layout.cds_item_view, this);
//        mCheckBox = (CheckBox) findViewById(R.id.ads_cds_item_checkbox);
//        mTxtName = (TextView) findViewById(R.id.ads_cds_item_name);
//        mTxtValue = (TextView) findViewById(R.id.ads_cds_item_value);
    }

    public void setName(String name) {
        mTxtName.setText(name);
    }
    
    public void showValue(String value) {
        mCheckBox.setVisibility(View.GONE);
        mTxtValue.setVisibility(View.VISIBLE);
        mTxtValue.setText(value);
    }
    
    public void showCheckBox() {
        mCheckBox.setVisibility(View.VISIBLE);
        mTxtValue.setVisibility(View.GONE);
    }
    
    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        if(mChecked != checked) {
            mChecked = checked;
            mCheckBox.setChecked(checked);
        } 
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

}
