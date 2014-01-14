package com.df.app.CarCheck;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.df.app.R;
import com.df.app.service.MyScrollView;
import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

import static com.df.app.util.Helper.getEditViewText;
import static com.df.app.util.Helper.getSpinnerSelectedText;
import static com.df.app.util.Helper.showView;

/**
 * Created by 岩 on 13-12-25.
 */
public class Integrated1Layout extends LinearLayout{
    private static String gearType;
    private static View rootView;
    private static int[] spinnerIds = {
            R.id.engineStarted_spinner,
            R.id.engineSteady_spinner,
            R.id.engineStrangeNoices_spinner,
            R.id.engineExhaustColor_spinner,
            R.id.engineFluid_spinner,
            R.id.gearMtClutch_spinner,
            R.id.gearMtShiftEasy_spinner,
            R.id.gearMtShiftSpace_spinner,
            R.id.gearAtShiftShock_spinner,
            R.id.gearAtShiftNoise_spinner,
            R.id.gearAtShiftEasy_spinner,
            R.id.engineFault_spinner,
            R.id.oilPressure_spinner,
            R.id.parkingBrake_spinner,
            R.id.waterTemp_spinner,
            R.id.tachometer_spinner,
            R.id.milometer_spinner,
            R.id.audioHorn_spinner,
            R.id.airBag_spinner,
            R.id.abs_spinner,
            R.id.powerWindows_spinner,
            R.id.sunroof_spinner,
            R.id.airConditioning_spinner,
            R.id.powerSeats_spinner,
            R.id.powerMirror_spinner,
            R.id.reversingRadar_spinner,
            R.id.reversingCamera_spinner,
            R.id.softCloseDoors_spinner,
            R.id.rearPowerSeats_spinner,
            R.id.ahc_spinner,
            R.id.parkAssist_spinner};

    public Integrated1Layout(Context context) {
        super(context);
        init(context);
    }

    public Integrated1Layout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Integrated1Layout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.integrated1_layout, this);

        MyScrollView scrollView = (MyScrollView)findViewById(R.id.root);
        scrollView.setListener(new MyScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy) {
                if (scrollView.getScrollY() > 5) {
                    showShadow(true);
                } else {
                    showShadow(false);
                }
            }
        });

        for(int i = 0; i < spinnerIds.length; i++) {
            setSpinnerColor(spinnerIds[i], Color.RED);
        }

        EditText AirConditioningTempEdit = (EditText) rootView.findViewById(R.id.airConditioningTemp_edit);

        // 公里数只允许小数点后两位，并且小数点前只能有2位
        AirConditioningTempEdit.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable edt) {
                String temp = edt.toString();

                if (temp.contains(".")) {
                    int posDot = temp.indexOf(".");
                    if (posDot <= 0) return;
                    if (temp.length() - posDot - 1 > 2) {
                        edt.delete(posDot + 3, posDot + 4);
                    }
                } else {
                    if (temp.length() > 2) {
                        edt.clear();
                        edt.append(temp.substring(0, 2));
                    }
                }
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
        });
    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void updateUi() {

    }

    public static void setGearType(String gearType) {
        Integrated1Layout.gearType = gearType;

        // 手动档
        if(gearType.equals("MT")) {
            setGearRowVisibility(true);
        }
        // 自动档
        else {
            setGearRowVisibility(false);
        }
    }

    private static void setGearRowVisibility(boolean visibility) {
        showView(rootView, R.id.gear_manually_row, visibility);
        showView(rootView, R.id.gear_manually_row_1, visibility);
        showView(rootView, R.id.gear_manually_row_2, visibility);
        showView(rootView, R.id.gear_manually_row_3, visibility);
        showView(rootView, R.id.gear_auto_row, !visibility);
        showView(rootView, R.id.gear_auto_row_1, !visibility);
        showView(rootView, R.id.gear_auto_row_2, !visibility);
        showView(rootView, R.id.gear_auto_row_3, !visibility);
    }

    // 更新相关的Spinner
    public static void updateAssociatedSpinners(int spinnerId, String selectedItemText) {
        int interSpinnerId;

        // 在map里查找对应的spinnerID
        for(int i = 0; i < Common.carSettingsSpinnerMap.length; i++) {
            if(spinnerId == Common.carSettingsSpinnerMap[i][0]) {
                interSpinnerId = Common.carSettingsSpinnerMap[i][1];

                if(interSpinnerId == 0)
                    continue;

                // 如果基本信息里的spinner选择的是“无”，则综合检查里的也应为“无”
                if(selectedItemText.equals("无")) {
                    enableSpinner(interSpinnerId, false);
                } else {
                    enableSpinner(interSpinnerId, true);
                }
            }
        }
    }

    private static void setSpinnerColor(int spinnerId, int color) {
        Spinner spinner = (Spinner) rootView.findViewById(spinnerId);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i >= 1)
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.RED);
                else
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);

                // 当选择项为“无”时，还应为黑色字体
                if(adapterView.getSelectedItem().toString().equals("无")) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private static void enableSpinner(int spinnerId, boolean enable) {
        Spinner spinner = (Spinner) rootView.findViewById(spinnerId);

        spinner.setSelection(enable ? 0 : 2);
        spinner.setClickable(enable);
        spinner.setAlpha(enable ? 1.0f : 0.3f);
    }


    public JSONObject generateEngineJSONObject() throws JSONException{
        JSONObject engine = new JSONObject();

        engine.put("started", getSpinnerSelectedText(rootView, R.id.engineStarted_spinner));
        engine.put("steady", getSpinnerSelectedText(rootView, R.id.engineSteady_spinner));
        engine.put("strangeNoices", getSpinnerSelectedText(rootView, R.id.engineStrangeNoices_spinner));
        engine.put("exhaustColor", getSpinnerSelectedText(rootView, R.id.engineExhaustColor_spinner));
        engine.put("fluid", getSpinnerSelectedText(rootView, R.id.engineFluid_spinner));
        engine.put("pipe", getSpinnerSelectedText(rootView, R.id.pipe_spinner));

        return engine;
    }

    public JSONObject generateGearboxJSONObject() throws JSONException{
        JSONObject gearbox = new JSONObject();

        if(gearType.equals("MT")) {
            gearbox.put("mtClutch", getSpinnerSelectedText(rootView, R.id.gearMtClutch_spinner));
            gearbox.put("mtShiftEasy", getSpinnerSelectedText(rootView, R.id.gearMtShiftEasy_spinner));
            gearbox.put("mtShiftSpace", getSpinnerSelectedText(rootView, R.id.gearMtShiftSpace_spinner));
        } else {
            gearbox.put("atShiftShock", getSpinnerSelectedText(rootView, R.id.gearAtShiftShock_spinner));
            gearbox.put("atShiftNoise", getSpinnerSelectedText(rootView, R.id.gearAtShiftNoise_spinner));
            gearbox.put("atShiftEasy", getSpinnerSelectedText(rootView, R.id.gearAtShiftEasy_spinner));
        }

        return gearbox;
    }

    public JSONObject generateFunctionJSONObject() throws JSONException {
        JSONObject function = new JSONObject();

        function.put("engineFault", getSpinnerSelectedText(rootView, R.id.engineFault_spinner));
        function.put("oilPressure", getSpinnerSelectedText(rootView, R.id.oilPressure_spinner));
        function.put("parkingBrake", getSpinnerSelectedText(rootView, R.id.parkingBrake_spinner));
        function.put("waterTemp", getSpinnerSelectedText(rootView, R.id.waterTemp_spinner));
        function.put("tachometer", getSpinnerSelectedText(rootView, R.id.tachometer_spinner));
        function.put("milometer", getSpinnerSelectedText(rootView, R.id.milometer_spinner));
        function.put("audio", getSpinnerSelectedText(rootView, R.id.audioHorn_spinner));

        if(!getSpinnerSelectedText(rootView, R.id.abs_spinner).equals("无"))
            function.put("abs", getSpinnerSelectedText(rootView, R.id.abs_spinner));
        if(!getSpinnerSelectedText(rootView, R.id.airBag_spinner).equals("无"))
            function.put("airBag", getSpinnerSelectedText(rootView, R.id.airBag_spinner));
        if(!getSpinnerSelectedText(rootView, R.id.powerWindows_spinner).equals("无"))
            function.put("powerWindows", getSpinnerSelectedText(rootView, R.id.powerWindows_spinner));
        if(!getSpinnerSelectedText(rootView, R.id.sunroof_spinner).equals("无"))
            function.put("sunroof", getSpinnerSelectedText(rootView, R.id.sunroof_spinner));
        if(!getSpinnerSelectedText(rootView, R.id.airConditioning_spinner).equals("无")) {
            function.put("airConditioning", getSpinnerSelectedText(rootView, R.id.airConditioning_spinner));
            function.put("airConditioningTemp", getEditViewText(rootView, R.id.airConditioningTemp_edit));
        }
        if(!getSpinnerSelectedText(rootView, R.id.powerSeats_spinner).equals("无"))
            function.put("powerSeats", getSpinnerSelectedText(rootView, R.id.powerSeats_spinner));
        if(!getSpinnerSelectedText(rootView, R.id.powerMirror_spinner).equals("无"))
            function.put("powerMirror", getSpinnerSelectedText(rootView, R.id.powerMirror_spinner));
        if(!getSpinnerSelectedText(rootView, R.id.reversingRadar_spinner).equals("无"))
            function.put("reversingRadar", getSpinnerSelectedText(rootView, R.id.reversingRadar_spinner));
        if(!getSpinnerSelectedText(rootView, R.id.reversingCamera_spinner).equals("无"))
            function.put("reversingCamera", getSpinnerSelectedText(rootView, R.id.reversingCamera_spinner));
        if(!getSpinnerSelectedText(rootView, R.id.softCloseDoors_spinner).equals("无"))
            function.put("softCloseDoors", getSpinnerSelectedText(rootView, R.id.softCloseDoors_spinner));
        if(!getSpinnerSelectedText(rootView, R.id.rearPowerSeats_spinner).equals("无"))
            function.put("rearPowerSeats", getSpinnerSelectedText(rootView, R.id.rearPowerSeats_spinner));
        if(!getSpinnerSelectedText(rootView, R.id.ahc_spinner).equals("无"))
            function.put("ahc", getSpinnerSelectedText(rootView, R.id.ahc_spinner));
        if(!getSpinnerSelectedText(rootView, R.id.parkAssist_spinner).equals("无"))
            function.put("parkAssist", getSpinnerSelectedText(rootView, R.id.parkAssist_spinner));

        return function;
    }

    public String generateCommentString() {
        return getEditViewText(rootView, R.id.it1_comment_edit);
    }

    public void fillInData(JSONObject engine, JSONObject gearbox, JSONObject function, String comment1) {

    }
}
