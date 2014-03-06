package com.df.app.carCheck;

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

import static com.df.app.util.Helper.enableView;
import static com.df.app.util.Helper.getEditViewText;
import static com.df.app.util.Helper.getSpinnerSelectedText;
import static com.df.app.util.Helper.setEditViewText;
import static com.df.app.util.Helper.setSpinnerSelectionWithString;
import static com.df.app.util.Helper.showView;

/**
 * Created by 岩 on 13-12-25.
 *
 * 综合检查一，主要包括发动机检查、变速箱检查、功能检查
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

    public static boolean finished = false;
    private JSONObject function;

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

        for (int spinnerId : spinnerIds) {
            setSpinnerColor(spinnerId);
        }

        Spinner airConditioningSpinner = (Spinner)rootView.findViewById(R.id.airConditioning_spinner);
        airConditioningSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                enableView(rootView, R.id.airConditioningTemp_edit, i <= 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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

    public void updateUi()  {
    }

    /**
     * 根据车辆的驱动方式，显示不同的变速箱问题
     * @param gearType
     */
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

    /**
     * 更新相关的Spinner
     * @param spinnerId
     * @param selectedItemText
     */
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

    /**
     * 设置spinner选择“无”之后的颜色
     * @param spinnerId
     */
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

    /**
     * 根据配置信息，将该spinner无效化
     * @param spinnerId
     * @param enable
     */
    private static void enableSpinner(int spinnerId, boolean enable) {
        Spinner spinner = (Spinner) rootView.findViewById(spinnerId);

        // 如果配置项为“无”，则设置spinner为“无”
        if(!enable) {
            spinner.setSelection(2);
        }
        spinner.setClickable(enable);
        spinner.setAlpha(enable ? 1.0f : 0.3f);
    }

    /**
     * 生成发动机的JSONObject
     * @return
     * @throws JSONException
     */
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

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * @param engine
     * @throws JSONException
     */
    public void fillEngineWithJSONObject(JSONObject engine)  throws JSONException {
        setSpinnerSelectionWithString(rootView, R.id.engineStarted_spinner, engine.getString("started"));
        setSpinnerSelectionWithString(rootView, R.id.engineSteady_spinner, engine.getString("steady"));
        setSpinnerSelectionWithString(rootView,R.id.engineStrangeNoices_spinner, engine.getString("strangeNoices"));
        setSpinnerSelectionWithString(rootView,R.id.engineExhaustColor_spinner, engine.getString("exhaustColor"));
        setSpinnerSelectionWithString(rootView, R.id.engineFluid_spinner, engine.getString("fluid"));
        setSpinnerSelectionWithString(rootView, R.id.pipe_spinner, engine.getString("pipe"));
    }

    /**
     * 生成变速箱的JSONObject
     * @return
     * @throws JSONException
     */
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

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * @param gearbox
     * @throws JSONException
     */
    private void fillGearboxWithJSONObject(JSONObject gearbox) throws JSONException {
        if(gearbox.has("mtClutch")) {
            setSpinnerSelectionWithString(rootView,R.id.gearMtClutch_spinner, gearbox.getString("mtClutch"));
            setSpinnerSelectionWithString(rootView,R.id.gearMtShiftEasy_spinner, gearbox.getString("mtShiftEasy"));
            setSpinnerSelectionWithString(rootView,R.id.gearMtShiftSpace_spinner, gearbox.getString("mtShiftSpace"));
        }

        if(gearbox.has("atShiftShock")) {
            setSpinnerSelectionWithString(rootView,R.id.gearAtShiftShock_spinner, gearbox.getString("atShiftShock"));
            setSpinnerSelectionWithString(rootView,R.id.gearAtShiftNoise_spinner, gearbox.getString("atShiftNoise"));
            setSpinnerSelectionWithString(rootView,R.id.gearAtShiftEasy_spinner, gearbox.getString("atShiftEasy"));
        }
    }

    /**
     * 生成功能检查的JSONObject
     * @return
     * @throws JSONException
     */
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
        else
            function.put("abs", JSONObject.NULL);
        if(!getSpinnerSelectedText(rootView, R.id.airBag_spinner).equals("无"))
            function.put("airBag", getSpinnerSelectedText(rootView, R.id.airBag_spinner));
        else
            function.put("airBag", JSONObject.NULL);
        if(!getSpinnerSelectedText(rootView, R.id.powerWindows_spinner).equals("无"))
            function.put("powerWindows", getSpinnerSelectedText(rootView, R.id.powerWindows_spinner));
        else
            function.put("powerWindows", JSONObject.NULL);
        if(!getSpinnerSelectedText(rootView, R.id.sunroof_spinner).equals("无"))
            function.put("sunroof", getSpinnerSelectedText(rootView, R.id.sunroof_spinner));
        else
            function.put("sunroof", JSONObject.NULL);
        if(!getSpinnerSelectedText(rootView, R.id.airConditioning_spinner).equals("无")) {
            function.put("airConditioning", getSpinnerSelectedText(rootView, R.id.airConditioning_spinner));
            function.put("airConditioningTemp", Integer.parseInt(getEditViewText(rootView, R.id.airConditioningTemp_edit)));
        } else {
            function.put("airConditioning", JSONObject.NULL);
            function.put("airConditioningTemp", 0);
        }
        if(!getSpinnerSelectedText(rootView, R.id.powerSeats_spinner).equals("无"))
            function.put("powerSeats", getSpinnerSelectedText(rootView, R.id.powerSeats_spinner));
        else
            function.put("powerSeats", JSONObject.NULL);
        if(!getSpinnerSelectedText(rootView, R.id.powerMirror_spinner).equals("无"))
            function.put("powerMirror", getSpinnerSelectedText(rootView, R.id.powerMirror_spinner));
        else
            function.put("powerMirror", JSONObject.NULL);
        if(!getSpinnerSelectedText(rootView, R.id.reversingRadar_spinner).equals("无"))
            function.put("reversingRadar", getSpinnerSelectedText(rootView, R.id.reversingRadar_spinner));
        else
            function.put("reversingRadar", JSONObject.NULL);
        if(!getSpinnerSelectedText(rootView, R.id.reversingCamera_spinner).equals("无"))
            function.put("reversingCamera", getSpinnerSelectedText(rootView, R.id.reversingCamera_spinner));
        else
            function.put("reversingCamera", JSONObject.NULL);
        if(!getSpinnerSelectedText(rootView, R.id.softCloseDoors_spinner).equals("无"))
            function.put("softCloseDoors", getSpinnerSelectedText(rootView, R.id.softCloseDoors_spinner));
        else
            function.put("softCloseDoors", JSONObject.NULL);
        if(!getSpinnerSelectedText(rootView, R.id.rearPowerSeats_spinner).equals("无"))
            function.put("rearPowerSeats", getSpinnerSelectedText(rootView, R.id.rearPowerSeats_spinner));
        else
            function.put("rearPowerSeats", JSONObject.NULL);
        if(!getSpinnerSelectedText(rootView, R.id.ahc_spinner).equals("无"))
            function.put("ahc", getSpinnerSelectedText(rootView, R.id.ahc_spinner));
        else
            function.put("ahc", JSONObject.NULL);
        if(!getSpinnerSelectedText(rootView, R.id.parkAssist_spinner).equals("无"))
            function.put("parkAssist", getSpinnerSelectedText(rootView, R.id.parkAssist_spinner));
        else
            function.put("parkAssist", JSONObject.NULL);

        return function;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * @param function
     * @throws JSONException
     */
    private void fillFunctionWithJSONObject(JSONObject function) throws JSONException{
        setSpinnerSelectionWithString(rootView, R.id.engineFault_spinner, function.getString("engineFault"));
        setSpinnerSelectionWithString(rootView, R.id.oilPressure_spinner, function.getString("oilPressure"));
        setSpinnerSelectionWithString(rootView, R.id.parkingBrake_spinner, function.getString("parkingBrake"));
        setSpinnerSelectionWithString(rootView, R.id.waterTemp_spinner, function.getString("waterTemp"));
        setSpinnerSelectionWithString(rootView, R.id.tachometer_spinner, function.getString("tachometer"));
        setSpinnerSelectionWithString(rootView, R.id.milometer_spinner, function.getString("milometer"));
        setSpinnerSelectionWithString(rootView, R.id.audio_spinner, function.getString("audio"));

        setSpinner(function, "abs", R.id.abs_spinner);
        setSpinner(function, "airBag", R.id.airBag_spinner);
        setSpinner(function, "powerWindows", R.id.powerWindows_spinner);
        setSpinner(function, "sunroof", R.id.sunroof_spinner);

        if(function.has("airConditioning")) {
            if(function.getString("airConditioning") == null) {
                enableSpinner(R.id.airConditioning_spinner, false);
            } else {
                setSpinnerSelectionWithString(rootView, R.id.airConditioning_spinner, function.getString("airConditioning"));
                setEditViewText(rootView, R.id.airConditioningTemp_edit, Integer.toString(function.getInt("airConditioningTemp")));
            }
        }

        setSpinner(function, "powerSeats", R.id.powerSeats_spinner);
        setSpinner(function, "powerMirror", R.id.powerMirror_spinner);
        setSpinner(function, "reversingRadar", R.id.reversingRadar_spinner);
        setSpinner(function, "reversingCamera", R.id.reversingCamera_spinner);
        setSpinner(function, "softCloseDoors", R.id.softCloseDoors_spinner);
        setSpinner(function, "rearPowerSeats", R.id.rearPowerSeats_spinner);
        setSpinner(function, "ahc", R.id.ahc_spinner);
        setSpinner(function, "parkAssist", R.id.parkAssist_spinner);
    }

    private void setSpinner(JSONObject jsonObject, String temp, int id) throws JSONException{
        if(jsonObject.has(temp)) {
            if(jsonObject.isNull(temp))
                enableSpinner(id, false);
            else
                setSpinnerSelectionWithString(rootView, id, jsonObject.isNull(temp) ? "无" : jsonObject.getString(temp));
        }
    }

    /**
     * 生成综合一备注的JSONObject
     * @return
     */
    public String generateCommentString() {
        return getEditViewText(rootView, R.id.it1_comment_edit);
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * @param comment
     */
    private void fillCommentWithString(String comment) {
        setEditViewText(rootView, R.id.it1_comment_edit, comment);
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * @param engine
     * @param gearbox
     * @param function
     * @param comment1
     * @throws JSONException
     */
    public void fillInData(JSONObject engine, JSONObject gearbox, JSONObject function, String comment1) throws JSONException {
        fillEngineWithJSONObject(engine);
        fillGearboxWithJSONObject(gearbox);
        fillFunctionWithJSONObject(function);
        fillCommentWithString(comment1);

        finished = true;
    }

    /**
     * 提交前的检查（暂无检查内容）
     * @return
     */
    public String checkAllFields() {
        return "";
    }
}
