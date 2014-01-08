package com.df.app.CarCheck;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.df.app.Procedures.CarRecogniseLayout;
import com.df.app.Procedures.InputProceduresLayout;
import com.df.app.R;
import com.df.app.entries.CarSettings;
import com.df.app.util.Common;

import static com.df.app.util.Helper.setEditViewText;
import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 13-12-20.
 */
public class OptionsLayout extends LinearLayout {
    private View rootView;

    private CarSettings mCarSettings;

    public OptionsLayout(Context context) {
        super(context);
        init(context);
    }

    public OptionsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OptionsLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.options_layout, this);

        mCarSettings = BasicInfoLayout.mCarSettings;
    }

    public void updateUi() {
        // 设置排量EditText
        setEditViewText(rootView, R.id.displacement_edit, mCarSettings.getDisplacement());

        // 设置驱动方式EditText
        setEditViewText(rootView, R.id.transmission_edit, mCarSettings.getTransmissionText());

        // 改动配置信息中的Spinner
        String carConfigs = mCarSettings.getCarConfigs();
        String configArray[] = carConfigs.split(",");

        for(int i = 0; i < configArray.length; i++) {
            int selection = Integer.parseInt(configArray[i]);
            setSpinnerSelection(Common.carSettingsSpinnerMap[i][0], selection);
        }

        setTextView(rootView, R.id.brandText, "车辆型号：" +
            VehicleInfoLayout.brandEdit.getText().toString());

//        // 改动“车体结构检查”里显示的图片
//        if(!mCarSettings.getFigure().equals(""))
//            CarCheckFrameFragment.setFigureImage(Integer.parseInt(mCarSettings.getFigure()));

        // 改动“综合检查”里的档位类型选项
        Integrated1Layout.setGearType(mCarSettings.getTransmissionText());

//        // 修改模式时，要手动填写牌照号码
//        if(!jsonData.equals("")) {
//            mShowContentCallback.onUpdateIntegratedUi();
//        }
    }

    // 设置配置信息中的Spinner，并与综合检查中的Spinner产生联动
    private void setSpinnerSelection(final int spinnerId, int selection) {
        final Spinner spinner = (Spinner) rootView.findViewById(spinnerId);
        spinner.setSelection(selection);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Integrated1Layout.updateAssociatedSpinners(spinnerId,
                        adapterView.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }
}
