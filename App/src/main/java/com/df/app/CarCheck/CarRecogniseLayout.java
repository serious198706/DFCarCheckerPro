package com.df.app.CarCheck;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import com.df.app.R;

import static com.df.app.util.Common.getEditViewText;
import static com.df.app.util.Common.setEditViewText;
import static com.df.app.util.Common.setTextViewText;
import static com.df.app.util.Common.showView;

/**
 * Created by 岩 on 13-12-20.
 */
public class CarRecogniseLayout extends LinearLayout {
    OnShowContentListener mCallback;
    View rootView;

    LinearLayout brandLayout;

    public CarRecogniseLayout(Context context, OnShowContentListener listener) {
        super(context);
        mCallback = listener;
        init(context);
    }

    public CarRecogniseLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CarRecogniseLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.car_recognise_layout, this);

        Button vinConfirmButton = (Button) rootView.findViewById(R.id.vinConfirm_button);
        vinConfirmButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getEditViewText(rootView, R.id.vin_edit).equals("")) {
                    
                }

                mCallback.showContent();
                showView(rootView, R.id.brand_input, true);
                setEditViewText(rootView, R.id.bi_brand_edit, "奥迪 100");
            }
        });

        Button recogniseButton = (Button) rootView.findViewById(R.id.recognise_button);
        recogniseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                fillInDummyData();
            }
        });
    }

    // BasicInfoLayout必须实现此方法
    public interface OnShowContentListener {
        public void showContent();
    }

    private void fillInDummyData() {
        setEditViewText(rootView, R.id.plateNumber_edit, "粤M26332");
        setEditViewText(rootView, R.id.licenseModel_edit, "奇瑞SQR7160");
        setEditViewText(rootView, R.id.vehicleType_edit, "小型轿车");
        setEditViewText(rootView, R.id.useCharacter_edit, "非营运");
        setEditViewText(rootView, R.id.engineSerial_edit, "K1H00875");
        setEditViewText(rootView, R.id.vin_edit, "LSJDA11A21D012476");
    }
}
