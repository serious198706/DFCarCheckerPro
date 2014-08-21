package com.df.app.carsChecked;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.df.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import static com.df.library.util.Helper.setTextView;

/**
 * Created by 岩 on 14-3-14.
 */
public class OptionsLayout extends LinearLayout {
    View rootView;

    public OptionsLayout(Context context, JSONObject jsonObject) {
        super(context);
        init(context, jsonObject);
    }

    public OptionsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OptionsLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init(Context context, JSONObject jsonObject) {
        rootView = LayoutInflater.from(context).inflate(R.layout.car_report_options_layout, this);

        try {
            fillInData(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fillInData(JSONObject options) throws JSONException{
        setTextView(rootView, R.id.displacement_text, options.getString("displacement"));
        setTextView(rootView, R.id.transmission_text, options.getString("transmission"));
        setTextView(rootView, R.id.driveType_text, options.getString("driveType"));
        setTextView(rootView, R.id.airBag_text, options.isNull("airBags") ? "无" : options.getString("airBags"));
        setTextView(rootView, R.id.abs_text, options.isNull("abs") ? "无" : options.getString("abs"));
        setTextView(rootView, R.id.powerSteering_text, options.isNull("powerSteering") ? "无" : options.getString("powerSteering"));
        setTextView(rootView, R.id.powerWindows_text, options.isNull("powerWindows") ? "无" : options.getString("powerWindows"));
        setTextView(rootView, R.id.sunroof_text, options.isNull("sunroof") ? "无" : options.getString("sunroof"));
        setTextView(rootView, R.id.airConditioning_text, options.isNull("airConditioning") ? "无" : options.getString("airConditioning"));
        setTextView(rootView, R.id.leatherSeats_text, options.isNull("leatherSeats") ? "无" : options.getString("leatherSeats"));
        setTextView(rootView, R.id.powerSeats_text, options.isNull("powerSeats") ? "无" : options.getString("powerSeats"));
        setTextView(rootView, R.id.powerMirror_text, options.isNull("powerMirror") ? "无" : options.getString("powerMirror"));
        setTextView(rootView, R.id.reversingRadar_text, options.isNull("reversingRadar") ? "无" : options.getString("reversingRadar"));
        setTextView(rootView, R.id.reversingCamera_text, options.isNull("reversingCamera") ? "无" : options.getString("reversingCamera"));
        setTextView(rootView, R.id.ccs_text, options.isNull("ccs") ? "无" : options.getString("ccs"));
        setTextView(rootView, R.id.softCloseDoors_text, options.isNull("softCloseDoors") ? "无" : options.getString("softCloseDoors"));
        setTextView(rootView, R.id.rearPowerSeats_text, options.isNull("rearPowerSeats") ? "无" : options.getString("rearPowerSeats"));
        setTextView(rootView, R.id.ahc_text, options.isNull("ahc") ? "无" : options.getString("ahc"));
        setTextView(rootView, R.id.parkAssist_text, options.isNull("parkAssist") ? "无" : options.getString("parkAssist"));
        setTextView(rootView, R.id.clapboard_text, options.isNull("clapboard") ? "无" : options.getString("clapboard"));
    }
}
