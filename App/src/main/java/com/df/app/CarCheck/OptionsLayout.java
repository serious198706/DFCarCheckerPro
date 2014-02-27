package com.df.app.carCheck;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.df.app.R;
import com.df.app.entries.CarSettings;
import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

import static com.df.app.util.Helper.getEditViewText;
import static com.df.app.util.Helper.getSpinnerSelectedText;
import static com.df.app.util.Helper.setEditViewText;
import static com.df.app.util.Helper.setSpinnerSelectionWithString;
import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 13-12-20.
 *
 * 配置信息
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
        final String configArray[] = carConfigs.split(",");

        for(int i = 0; i < configArray.length; i++) {
            int selection = Integer.parseInt(configArray[i]);
            setSpinnerSelection(Common.carSettingsSpinnerMap[i][0], selection);
        }

        setTextView(rootView, R.id.brandText, "车辆型号：" +
            BasicInfoLayout.mCarSettings.getBrandString());

        // 改动“综合检查”里的档位类型选项
        Integrated1Layout.setGearType(mCarSettings.getTransmissionText());
    }

    /**
     * 设置配置信息中的Spinner，并与综合检查中的Spinner产生联动
     * @param spinnerId
     * @param selection
     */
    private void setSpinnerSelection(final int spinnerId, int selection) {
        final Spinner spinner = (Spinner) rootView.findViewById(spinnerId);
        spinner.setSelection(selection);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Integrated1Layout.updateAssociatedSpinners(spinnerId, adapterView.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    /**
     * 生成配置信息的JSONObject
     * @return
     */
    public JSONObject generateJSONObject() {
        JSONObject options = new JSONObject();

        try {
            options.put("vin", VehicleInfoLayout.getVin());
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

            // 有则直接放入，无则放入null
            if(!getSpinnerSelectedText(rootView, R.id.abs_spinner).equals("无"))
                options.put("abs", getSpinnerSelectedText(rootView, R.id.abs_spinner));
            else
                options.put("abs", JSONObject.NULL);
            if(!getSpinnerSelectedText(rootView, R.id.powerSteering_spinner).equals("无"))
                options.put("powerSteering", getSpinnerSelectedText(rootView, R.id.powerSteering_spinner));
            else
                options.put("powerSteering", JSONObject.NULL);
            if(!getSpinnerSelectedText(rootView, R.id.powerWindows_spinner).equals("无"))
                options.put("powerWindows", getSpinnerSelectedText(rootView, R.id.powerWindows_spinner));
            else
                options.put("powerWindows", JSONObject.NULL);
            if(!getSpinnerSelectedText(rootView, R.id.sunroof_spinner).equals("无"))
                options.put("sunroof", getSpinnerSelectedText(rootView, R.id.sunroof_spinner));
            else
                options.put("sunroof", JSONObject.NULL);
            if(!getSpinnerSelectedText(rootView, R.id.airConditioning_spinner).equals("无"))
                options.put("airConditioning", getSpinnerSelectedText(rootView, R.id.airConditioning_spinner));
            else
                options.put("airConditioning", JSONObject.NULL);
            if(!getSpinnerSelectedText(rootView, R.id.leatherSeats_spinner).equals("无"))
                options.put("leatherSeats", getSpinnerSelectedText(rootView, R.id.leatherSeats_spinner));
            else
                options.put("leatherSeats", JSONObject.NULL);
            if(!getSpinnerSelectedText(rootView, R.id.powerSeats_spinner).equals("无"))
                options.put("powerSeats", getSpinnerSelectedText(rootView, R.id.powerSeats_spinner));
            else
                options.put("powerSeats", JSONObject.NULL);
            if(!getSpinnerSelectedText(rootView, R.id.powerMirror_spinner).equals("无"))
                options.put("powerMirror", getSpinnerSelectedText(rootView, R.id.powerMirror_spinner));
            else
                options.put("powerMirror", JSONObject.NULL);
            if(!getSpinnerSelectedText(rootView, R.id.reversingRadar_spinner).equals("无"))
                options.put("reversingRadar", getSpinnerSelectedText(rootView, R.id.reversingRadar_spinner));
            else
                options.put("reversingRadar", JSONObject.NULL);
            if(!getSpinnerSelectedText(rootView, R.id.reversingCamera_spinner).equals("无"))
                options.put("reversingCamera", getSpinnerSelectedText(rootView, R.id.reversingCamera_spinner));
            else
                options.put("reversingCamera", JSONObject.NULL);
            if(!getSpinnerSelectedText(rootView, R.id.ccs_spinner).equals("无"))
                options.put("ccs", getSpinnerSelectedText(rootView, R.id.ccs_spinner));
            else
                options.put("ccs", JSONObject.NULL);
            if(!getSpinnerSelectedText(rootView, R.id.softCloseDoors_spinner).equals("无"))
                options.put("softCloseDoors", getSpinnerSelectedText(rootView, R.id.softCloseDoors_spinner));
            else
                options.put("softCloseDoors", JSONObject.NULL);
            if(!getSpinnerSelectedText(rootView, R.id.rearPowerSeats_spinner).equals("无"))
                options.put("rearPowerSeats", getSpinnerSelectedText(rootView, R.id.rearPowerSeats_spinner));
            else
                options.put("rearPowerSeats", JSONObject.NULL);
            if(!getSpinnerSelectedText(rootView, R.id.ahc_spinner).equals("无"))
                options.put("ahc", getSpinnerSelectedText(rootView, R.id.ahc_spinner));
            else
                options.put("ahc", JSONObject.NULL);
            if(!getSpinnerSelectedText(rootView, R.id.parkAssist_spinner).equals("无"))
                options.put("parkAssist", getSpinnerSelectedText(rootView, R.id.parkAssist_spinner));
            else
                options.put("parkAssist", JSONObject.NULL);
            if(!getSpinnerSelectedText(rootView, R.id.clapboard_spinner).equals("无"))
                options.put("clapboard", getSpinnerSelectedText(rootView, R.id.clapboard_spinner));
            else
                options.put("clapboard", JSONObject.NULL);

            options.put("spareTire", Integrated2Layout.getSpareTireSelection());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return options;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * @param options
     */
    public void fillInData(JSONObject options) {
        try {
            setEditViewText(rootView, R.id.displacement_edit, options.getString("displacement"));
            setEditViewText(rootView, R.id.transmission_edit, options.getString("transmission"));

            setSpinnerSelectionWithString(rootView, R.id.driveType_spinner, options.getString("driveType"));
            setSpinnerSelectionWithString(rootView, R.id.airBag_spinner, options.getString("airBags"));

            // 有则放入无则放无
            if(options.has("abs"))
                setSpinnerSelectionWithString(rootView, R.id.abs_spinner, options.isNull("abs") ? "无" : options.getString("abs"));
            if(options.has("powerSteering"))
                setSpinnerSelectionWithString(rootView, R.id.powerSteering_spinner, options.isNull("powerSteering") ? "无" : options.getString("powerSteering"));
            if(options.has("powerWindows"))
                setSpinnerSelectionWithString(rootView, R.id.powerWindows_spinner, options.isNull("powerWindows") ? "无" : options.getString("powerWindows"));
            if(options.has("sunroof"))
                setSpinnerSelectionWithString(rootView, R.id.sunroof_spinner, options.isNull("sunroof") ? "无" : options.getString("sunroof"));
            if(options.has("airConditioning"))
                setSpinnerSelectionWithString(rootView, R.id.airConditioning_spinner, options.isNull("airConditioning") ? "无" : options.getString("airConditioning"));
            if(options.has("leatherSeats"))
                setSpinnerSelectionWithString(rootView, R.id.leatherSeats_spinner, options.isNull("leatherSeats") ? "无" : options.getString("leatherSeats"));
            if(options.has("powerSeats"))
                setSpinnerSelectionWithString(rootView, R.id.powerSeats_spinner, options.isNull("powerSeats") ? "无" : options.getString("powerSeats"));
            if(options.has("powerMirror"))
                setSpinnerSelectionWithString(rootView, R.id.powerMirror_spinner, options.isNull("powerMirror") ? "无" : options.getString("powerMirror"));
            if(options.has("reversingRadar"))
                setSpinnerSelectionWithString(rootView, R.id.reversingRadar_spinner, options.isNull("reversingRadar") ? "无" : options.getString("reversingRadar"));
            if(options.has("reversingCamera"))
                setSpinnerSelectionWithString(rootView, R.id.reversingCamera_spinner, options.isNull("reversingCamera") ? "无" : options.getString("reversingCamera"));
            if(options.has("ccs"))
                setSpinnerSelectionWithString(rootView, R.id.ccs_spinner, options.isNull("ccs") ? "无" : options.getString("ccs"));
            if(options.has("softCloseDoors"))
                setSpinnerSelectionWithString(rootView, R.id.softCloseDoors_spinner, options.isNull("softCloseDoors") ? "无" : options.getString("softCloseDoors"));
            if(options.has("rearPowerSeats"))
                setSpinnerSelectionWithString(rootView, R.id.rearPowerSeats_spinner, options.isNull("rearPowerSeats") ? "无" : options.getString("rearPowerSeats"));
            if(options.has("ahc"))
                setSpinnerSelectionWithString(rootView, R.id.ahc_spinner, options.isNull("ahc") ? "无" : options.getString("ahc"));
            if(options.has("parkAssist"))
                setSpinnerSelectionWithString(rootView, R.id.parkAssist_spinner, options.isNull("parkAssist") ? "无" : options.getString("parkAssist"));
            if(options.has("clapboard"))
                setSpinnerSelectionWithString(rootView, R.id.clapboard_spinner, options.isNull("clapboard") ? "无" : options.getString("clapboard"));

            // 备胎
            if(options.has("spareTire"))
                Integrated2Layout.setSpareTireSelection(options.getString("spareTire"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
