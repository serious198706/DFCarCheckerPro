package com.df.app.CarCheck;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import static com.df.app.util.Helper.setEditViewText;
import static com.df.app.util.Helper.setSpinnerSelectionWithString;
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
            R.id.pipe_spinner,
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
            R.id.audio_spinner,
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
            setSpinnerColor(spinnerIds[i]);
        }

        EditText AirConditioningTempEdit = (EditText) rootView.findViewById(R.id.airConditioningTemp_edit);

        // 移除输入框的焦点，避免每次输入完成后界面滚动
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        scrollView.setFocusable(true);
        scrollView.setFocusableInTouchMode(true);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
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

    private static void setSpinnerColor(int spinnerId) {
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


    public JSONObject generateEngineJSONObject() throws JSONException {
        JSONObject engine = new JSONObject();

        engine.put("started", getSpinnerSelectedText(rootView, R.id.engineStarted_spinner));
        engine.put("steady", getSpinnerSelectedText(rootView, R.id.engineSteady_spinner));
        engine.put("strangeNoices", getSpinnerSelectedText(rootView, R.id.engineStrangeNoices_spinner));
        engine.put("exhaustColor", getSpinnerSelectedText(rootView, R.id.engineExhaustColor_spinner));
        engine.put("fluid", getSpinnerSelectedText(rootView, R.id.engineFluid_spinner));
        engine.put("pipe", getSpinnerSelectedText(rootView, R.id.pipe_spinner));

        return engine;
    }

    public void fillEngineWithJSONObject(JSONObject engine)  throws JSONException {
        setSpinnerSelectionWithString(rootView, R.id.engineStarted_spinner, engine.getString("started"));
        setSpinnerSelectionWithString(rootView, R.id.engineSteady_spinner, engine.getString("steady"));
        setSpinnerSelectionWithString(rootView,R.id.engineStrangeNoices_spinner, engine.getString("strangeNoices"));
        setSpinnerSelectionWithString(rootView,R.id.engineExhaustColor_spinner, engine.getString("exhaustColor"));
        setSpinnerSelectionWithString(rootView, R.id.engineFluid_spinner, engine.getString("fluid"));
        setSpinnerSelectionWithString(rootView, R.id.pipe_spinner, engine.getString("pipe"));
    }

    public JSONObject generateGearboxJSONObject() throws JSONException {
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

    private void fillGearboxWithJSONObject(JSONObject gearbox) throws JSONException {
        if(gearbox.get("mtClutch") != null) {
            setSpinnerSelectionWithString(rootView,R.id.gearMtClutch_spinner, gearbox.getString("mtClutch"));
            setSpinnerSelectionWithString(rootView,R.id.gearMtShiftEasy_spinner, gearbox.getString("mtShiftEasy"));
            setSpinnerSelectionWithString(rootView,R.id.gearMtShiftSpace_spinner, gearbox.getString("mtShiftSpace"));
        }

        if(gearbox.get("atShiftShock") != null) {
            setSpinnerSelectionWithString(rootView,R.id.gearAtShiftShock_spinner, gearbox.getString("atShiftShock"));
            setSpinnerSelectionWithString(rootView,R.id.gearAtShiftNoise_spinner, gearbox.getString("atShiftNoise"));
            setSpinnerSelectionWithString(rootView,R.id.gearAtShiftEasy_spinner, gearbox.getString("atShiftEasy"));
        }
    }

    public JSONObject generateFunctionJSONObject() throws JSONException {
        JSONObject function = new JSONObject();

        function.put("engineFault", getSpinnerSelectedText(rootView, R.id.engineFault_spinner));
        function.put("oilPressure", getSpinnerSelectedText(rootView, R.id.oilPressure_spinner));
        function.put("parkingBrake", getSpinnerSelectedText(rootView, R.id.parkingBrake_spinner));
        function.put("waterTemp", getSpinnerSelectedText(rootView, R.id.waterTemp_spinner));
        function.put("tachometer", getSpinnerSelectedText(rootView, R.id.tachometer_spinner));
        function.put("milometer", getSpinnerSelectedText(rootView, R.id.milometer_spinner));
        function.put("audio", getSpinnerSelectedText(rootView, R.id.audio_spinner));

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
            function.put("airConditioningTemp", Integer.parseInt(getEditViewText(rootView, R.id.airConditioningTemp_edit)));
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

    private void fillFunctionWithJSONObject(JSONObject function) throws JSONException{
        setSpinnerSelectionWithString(rootView, R.id.engineFault_spinner, function.getString("engineFault"));
        setSpinnerSelectionWithString(rootView, R.id.oilPressure_spinner, function.getString("oilPressure"));
        setSpinnerSelectionWithString(rootView, R.id.parkingBrake_spinner, function.getString("parkingBrake"));
        setSpinnerSelectionWithString(rootView, R.id.waterTemp_spinner, function.getString("waterTemp"));
        setSpinnerSelectionWithString(rootView, R.id.tachometer_spinner, function.getString("tachometer"));
        setSpinnerSelectionWithString(rootView, R.id.milometer_spinner, function.getString("milometer"));
        setSpinnerSelectionWithString(rootView, R.id.audio_spinner, function.getString("audio"));

        if(function.has("abs"))
            setSpinnerSelectionWithString(rootView, R.id.abs_spinner, function.getString("abs"));
        else
            enableSpinner(R.id.abs_spinner, false);
        if(function.has("airBag"))
            setSpinnerSelectionWithString(rootView, R.id.airBag_spinner, function.getString("airBag"));
        else
            enableSpinner(R.id.airBag_spinner, false);
        if(function.has("powerWindows"))
            setSpinnerSelectionWithString(rootView, R.id.powerWindows_spinner, function.getString("powerWindows"));
        else
            enableSpinner(R.id.powerWindows_spinner, false);
        if(function.has("sunroof"))
            setSpinnerSelectionWithString(rootView, R.id.sunroof_spinner, function.getString("sunroof"));
        else
            enableSpinner(R.id.sunroof_spinner, false);
        if(function.has("airConditioning"))
            setSpinnerSelectionWithString(rootView, R.id.airConditioning_spinner, function.getString("airConditioning"));
        else
            enableSpinner(R.id.airConditioning_spinner, false);
        if(function.has("powerSeats"))
            setSpinnerSelectionWithString(rootView, R.id.powerSeats_spinner, function.getString("powerSeats"));
        else
            enableSpinner(R.id.powerSeats_spinner, false);
        if(function.has("powerMirror"))
            setSpinnerSelectionWithString(rootView, R.id.powerMirror_spinner, function.getString("powerMirror"));
        else
            enableSpinner(R.id.powerMirror_spinner, false);
        if(function.has("reversingRadar"))
            setSpinnerSelectionWithString(rootView, R.id.reversingRadar_spinner, function.getString("reversingRadar"));
        else
            enableSpinner(R.id.reversingRadar_spinner, false);
        if(function.has("reversingCamera"))
            setSpinnerSelectionWithString(rootView, R.id.reversingCamera_spinner, function.getString("reversingCamera"));
        else
            enableSpinner(R.id.reversingCamera_spinner, false);
        if(function.has("softCloseDoors"))
            setSpinnerSelectionWithString(rootView, R.id.softCloseDoors_spinner, function.getString("softCloseDoors"));
        else
            enableSpinner(R.id.softCloseDoors_spinner, false);
        if(function.has("rearPowerSeats"))
            setSpinnerSelectionWithString(rootView, R.id.rearPowerSeats_spinner, function.getString("rearPowerSeats"));
        else
            enableSpinner(R.id.rearPowerSeats_spinner, false);
        if(function.has("ahc"))
            setSpinnerSelectionWithString(rootView, R.id.ahc_spinner, function.getString("ahc"));
        else
            enableSpinner(R.id.ahc_spinner, false);
        if(function.has("parkAssist"))
            setSpinnerSelectionWithString(rootView, R.id.parkAssist_spinner, function.getString("parkAssist"));
        else
            enableSpinner(R.id.parkAssist_spinner, false);
    }

    public String generateCommentString() {
        return getEditViewText(rootView, R.id.it1_comment_edit);
    }

    private void fillCommentWithString(String comment) {
        setEditViewText(rootView, R.id.it1_comment_edit, comment);
    }

    public void fillInData(JSONObject engine, JSONObject gearbox, JSONObject function, String comment1) throws JSONException {
        fillEngineWithJSONObject(engine);
        fillGearboxWithJSONObject(gearbox);
        fillFunctionWithJSONObject(function);
        fillCommentWithString(comment1);
    }

    public String checkAllFields() {
        return "";
    }
}
