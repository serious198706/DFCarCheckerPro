package com.ads.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.df.library.R;

public class DtcItemView extends FrameLayout {

    private TextView mTxtCode;
    private TextView mTxtDesc;
    private TextView mTxtState;
    
    public DtcItemView(Context context) {
        super(context);
        initLayout(context);
    }

    private void initLayout(Context context) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        inflater.inflate(R.layout.dtc_item_view, this);

        mTxtCode = (TextView) findViewById(R.id.ads_dtc_item_code);
        mTxtDesc = (TextView) findViewById(R.id.ads_dtc_item_desc);
        mTxtState = (TextView) findViewById(R.id.ads_dtc_item_state);
    }
    
    public void setContent(String code, String desc, String state) {
        mTxtCode.setText(code);
        mTxtDesc.setText(desc);
        mTxtState.setText(state);
    }

}
