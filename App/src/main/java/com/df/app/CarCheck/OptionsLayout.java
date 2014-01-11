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
import com.df.app.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import static com.df.app.util.Helper.getEditViewText;
import static com.df.app.util.Helper.getSpinnerSelectedText;
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

    public JSONObject generateJSONObject() {
        JSONObject options = new JSONObject();

        try {
            //options.put("vin", mC.getText().toString());
            options.put("country", mCarSettings.getCountry().name);
            options.put("countryId", Integer.parseInt(mCarSettings.getCountry().id));
            options.put("brand", mCarSettings.getBrand().name);
            options.put("brandId", Integer.parseInt(mCarSettings.getBrand().id));
            options.put("manufacturer", mCarSettings.getManufacturer().name);
            options.put("manufacturerId", Integer.parseInt(mCarSettings.getManufacturer().id));
            options.put("series", mCarSettings.getSeries().name);
            options.put("seriesId", Integer.parseInt(mCarSettings.getSeries().id));
            options.put("model", mCarSettings.getModel().name);
            options.put("modelId", Integer.parseInt(mCarSettings.getModel().id));
            options.put("displacement", getEditViewText(rootView, R.id.displacement_edit));
            options.put("category", mCarSettings.getCategory());
            options.put("driveType", getSpinnerSelectedText(rootView, R.id.driveType_spinner));
            options.put("transmission", getEditViewText(rootView, R.id.transmission_edit));
            options.put("airBags", getSpinnerSelectedText(rootView, R.id.airBag_spinner));

            // 有则放入无则无视
            if(!getSpinnerSelectedText(rootView, R.id.abs_spinner).equals("无"))
                options.put("abs", getSpinnerSelectedText(rootView, R.id.abs_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.powerSteering_spinner).equals("无"))
                options.put("powerSteering", getSpinnerSelectedText(rootView, R.id.powerSteering_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.powerWindows_spinner).equals("无"))
                options.put("powerWindows", getSpinnerSelectedText(rootView, R.id.powerWindows_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.sunroof_spinner).equals("无"))
                options.put("sunroof", getSpinnerSelectedText(rootView, R.id.sunroof_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.airConditioning_spinner).equals("无"))
                options.put("airConditioning", getSpinnerSelectedText(rootView, R.id.airConditioning_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.leatherSeats_spinner).equals("无"))
                options.put("leatherSeats", getSpinnerSelectedText(rootView, R.id.leatherSeats_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.powerSeats_spinner).equals("无"))
                options.put("powerSeats", getSpinnerSelectedText(rootView, R.id.powerSeats_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.powerMirror_spinner).equals("无"))
                options.put("powerMirror", getSpinnerSelectedText(rootView, R.id.powerMirror_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.reversingRadar_spinner).equals("无"))
                options.put("reversingRadar", getSpinnerSelectedText(rootView, R.id.reversingRadar_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.reversingCamera_spinner).equals("无"))
                options.put("reversingCamera", getSpinnerSelectedText(rootView, R.id.reversingCamera_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.ccs_spinner).equals("无"))
                options.put("ccs", getSpinnerSelectedText(rootView, R.id.ccs_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.softCloseDoors_spinner).equals("无"))
                options.put("softCloseDoors", getSpinnerSelectedText(rootView, R.id.softCloseDoors_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.rearPowerSeats_spinner).equals("无"))
                options.put("rearPowerSeats", getSpinnerSelectedText(rootView, R.id.rearPowerSeats_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.ahc_spinner).equals("无"))
                options.put("ahc", getSpinnerSelectedText(rootView, R.id.ahc_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.parkAssist_spinner).equals("无"))
                options.put("parkAssist", getSpinnerSelectedText(rootView, R.id.parkAssist_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.clapboard_spinner).equals("无"))
                options.put("clapboard", getSpinnerSelectedText(rootView, R.id.clapboard_spinner));

            options.put("spareTire", Integrated2Layout.getSpareTireSelction());
        } catch (JSONException e) {

        }

        return options;
    }
}
