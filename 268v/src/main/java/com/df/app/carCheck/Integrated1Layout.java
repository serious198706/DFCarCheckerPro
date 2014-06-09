package com.df.app.carCheck;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.df.app.R;
import com.df.app.service.util.AppCommon;
import com.df.library.util.Helper;
import com.df.library.util.MyScrollView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.df.library.util.Helper.enableView;
import static com.df.library.util.Helper.getEditViewText;
import static com.df.library.util.Helper.getSpinnerSelectedIndex;
import static com.df.library.util.Helper.getSpinnerSelectedText;
import static com.df.library.util.Helper.setEditViewText;
import static com.df.library.util.Helper.setSpinnerSelectionWithString;
import static com.df.library.util.Helper.showView;

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
            R.id.safebelts_spinner,
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
            R.id.parkAssist_spinner,
            R.id.clapboard_spinner};

    public static boolean finished = false;
    private static JSONObject function;
    private static int count = 0;

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
                showShadow(scrollView.getScrollY() > 5);
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

                ((TextView) adapterView.getChildAt(0)).setTextColor(i >= 1 ? Color.RED : Color.BLACK);

                // 当选择项为“无”时，还应为黑色字体
                if(adapterView.getSelectedItem().toString().equals("无")) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        EditText airConditioningTempEdit = (EditText)findViewById(R.id.airConditioningTemp_edit);
        airConditioningTempEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 第一个输入的文字不能是'.'
                String temp = editable.toString();

                if(!temp.equals("")) {
                    if(temp.charAt(0) == '.') {
                        editable.delete(0, 1);
                    }
                }
            }
        });
        airConditioningTempEdit.setText("");

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
     * @param gearType MT/AT/...
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
     * @param spinnerId id
     * @param selectedItemText 选择的文字
     */
    public static void updateAssociatedSpinners(int spinnerId, String selectedItemText) {
        int interSpinnerId;

        // 在map里查找对应的spinnerID
        for(int[] id : AppCommon.carSettingsSpinnerMap) {
            if(spinnerId == id[0]) {
                interSpinnerId = id[1];

                if(interSpinnerId == 0)
                    continue;

                // 如果基本信息里的spinner选择的是“无”，则综合检查里的也应为“无”
                if(selectedItemText.equals("无")) {
                    enableSpinner(interSpinnerId, false);
                }
                // 如果基本信息里的spinner选择的不是“无”，则综合检查里要设置为“正常”
                else {
                    enableSpinner(interSpinnerId, true);
                    setSpinnerSelectionWithString(rootView, interSpinnerId, "正常");
                }

                if(count >= 0)
                    count++;
            }
        }

        // 当全部更新完成后，再将所有的内容设置一遍
        if(count >= 14 && function != null) {
            try {
                fillFunctionWithJSONObject(function);
                count = -1;

                enableView(rootView, R.id.airConditioningTemp_edit, getSpinnerSelectedIndex(rootView, R.id.airConditioning_spinner) <= 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置spinner选择“无”之后的颜色
     * @param spinnerId id
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
     * 根据配置信息，将该spinner启用/禁用
     * @param spinnerId id
     * @param enable 启用/禁用
     */
    private static void enableSpinner(int spinnerId, boolean enable) {
        Spinner spinner = (Spinner) rootView.findViewById(spinnerId);

        // 如果配置项为“无”，则设置spinner为“无”
        if(!enable) {
            spinner.setSelection(2);
        }
        spinner.setEnabled(enable);

        if(!enable) {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.GRAY);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } else {
            setSpinnerColor(spinnerId);
        }
    }

    /**
     * 生成jsonObject
     * @param parentView viewGroup
     * @return JSONObject
     * @throws JSONException
     */
    private JSONObject generateJSONObject(ViewGroup parentView) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        List<View> views = new ArrayList<View>();
        Helper.findAllViews(parentView, views, Spinner.class);

        for(View view : views) {
            if(view instanceof Spinner) {
                String content = getSpinnerSelectedText(rootView, view.getId());

                if(!content.equals("无")) {
                    jsonObject.put((String)view.getTag(), content);
                } else {
                    jsonObject.put((String)view.getTag(), JSONObject.NULL);
                }
            }
        }

        return jsonObject;
    }

    /**
     * 生成发动机的JSONObject
     * @return
     * @throws JSONException
     */
    public JSONObject generateEngineJSONObject() throws JSONException {
        TableLayout tableLayout = (TableLayout)findViewById(R.id.engineTable);

        JSONObject engine = generateJSONObject(tableLayout);

        return engine;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * @param engine
     * @throws JSONException
     */
    public void fillEngineWithJSONObject(JSONObject engine)  throws JSONException {
        TableLayout tableLayout = (TableLayout)rootView.findViewById(R.id.engineTable);

        List<View> views = new ArrayList<View>();
        Helper.findAllViews(tableLayout, views, Spinner.class);

        for(View view : views) {
            setSpinnerSelectionWithString(rootView, view.getId(), engine.getString((String)view.getTag()));
        }
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
        TableLayout tableLayout = (TableLayout)findViewById(R.id.functionTable);

        JSONObject function = generateJSONObject(tableLayout);

        if(!getSpinnerSelectedText(rootView, R.id.airConditioning_spinner).equals("无")) {
            function.put("airConditioning", getSpinnerSelectedText(rootView, R.id.airConditioning_spinner));
            function.put("airConditioningTemp", getEditViewText(rootView, R.id.airConditioningTemp_edit));
        } else {
            function.put("airConditioning", JSONObject.NULL);
            function.put("airConditioningTemp", JSONObject.NULL);
        }

        return function;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * @param function
     * @throws JSONException
     */
    private static void fillFunctionWithJSONObject(JSONObject function) throws JSONException{
        TableLayout tableLayout = (TableLayout)rootView.findViewById(R.id.functionTable);

        List<View> views = new ArrayList<View>();
        Helper.findAllViews(tableLayout, views, Spinner.class);

        for(View view : views) {
            if(view instanceof Spinner) {
                setSpinner(function, (String)view.getTag(), view.getId());
            }
        }

        if(function.has("airConditioning")) {
            if(function.getString("airConditioning") == null) {
                enableSpinner(R.id.airConditioning_spinner, false);
            } else {
                setSpinnerSelectionWithString(rootView, R.id.airConditioning_spinner, function.getString("airConditioning"));
                setEditViewText(rootView, R.id.airConditioningTemp_edit,
                        function.get("airConditioningTemp") == JSONObject.NULL ? "" : function.getString("airConditioningTemp"));
            }
        }

        // 把必显8项设置一下
        for(int i = 0; i < 8; i++) {
            enableSpinner(spinnerIds[i + 12], true);
        }
    }

    /**
     * 根据具体情况，设置spinner
     * @param jsonObject
     * @param temp
     * @param id
     * @throws JSONException
     */
    private static void setSpinner(JSONObject jsonObject, String temp, int id) throws JSONException{
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
        this.function = function;

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

    public static void clearCache() {
        Integrated1Layout.count = 0;
    }
}
