package com.df.app.carCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.df.app.R;
import com.df.library.entries.CarSettings;
import com.df.app.service.util.AppCommon;
import com.df.library.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.df.library.util.Helper.getEditViewText;
import static com.df.library.util.Helper.getSpinnerSelectedText;
import static com.df.library.util.Helper.setEditViewText;
import static com.df.library.util.Helper.setSpinnerSelectionWithString;
import static com.df.library.util.Helper.setTextView;

/**
 * Created by 岩 on 13-12-20.
 *
 * 配置信息
 */
public class OptionsLayout extends LinearLayout {
    private Context context;

    public interface OnLoadSettingsButtonClicked {
        public void onLoadSettings();
    }

    private View rootView;

    private CarSettings mCarSettings;
    private OnLoadSettingsButtonClicked mCallback;

    public OptionsLayout(Context context, OnLoadSettingsButtonClicked listener) {
        super(context);
        this.mCallback = listener;
        this.context = context;
        init();
    }

    public OptionsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OptionsLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        rootView = LayoutInflater.from(context).inflate(R.layout.options_layout, this);

        mCarSettings = BasicInfoLayout.mCarSettings;

        Button loadSettingsButton = (Button)findViewById(R.id.loadSettingsButton);
        loadSettingsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                View view1 = ((Activity)context).getLayoutInflater().inflate(R.layout.popup_layout, null);
                TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
                TextView content = new TextView(view1.getContext());
                content.setText(R.string.loadSettingsAlert);
                content.setTextSize(20f);
                contentArea.addView(content);

                setTextView(view1, R.id.title, getResources().getString(R.string.alert));

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setView(view1)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mCallback.onLoadSettings();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();

                dialog.show();
            }
        });


        fillInDefaultData();
    }

    /**
     * 填入默认的配置信息（都为无）
     */
    private void fillInDefaultData() {
        // 改动配置信息中的Spinner
        String carConfigs = mCarSettings.getCarConfigs();
        final String configArray[] = carConfigs.split(",");

        for(int i = 0; i < configArray.length; i++) {
            setSpinnerSelection(AppCommon.carSettingsSpinnerMap[i][0], "无");
        }
    }

    /**
     * 更新界面
     */
    public void updateUi() {
        // 设置排量EditText
        setEditViewText(rootView, R.id.displacement_edit, BasicInfoLayout.mCarSettings.getDisplacement());

        // 设置驱动方式EditText
        setEditViewText(rootView, R.id.transmission_edit, BasicInfoLayout.mCarSettings.getTransmissionText());

        // 设置车辆型号textView
        setTextView(rootView, R.id.brandText, "车辆型号：" +
                BasicInfoLayout.mCarSettings.getBrandString());

        // 改动“综合检查”里的变速器形式
        Integrated1Layout.setGearType(BasicInfoLayout.mCarSettings.getTransmissionText());
    }

    /**
     * 设置配置信息中的Spinner，并与综合检查中的Spinner产生联动
     * @param spinnerId
     * @param selection
     */
    private void setSpinnerSelection(final int spinnerId, String selection) {
        final Spinner spinner = (Spinner) rootView.findViewById(spinnerId);
        setSpinnerSelectionWithString(rootView, spinnerId, selection);

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
     * 生成配置信息的JSONObject
     * @return
     */
    public JSONObject generateJSONObject() {
        TableLayout tableLayout = (TableLayout)findViewById(R.id.bi_content_table);

        JSONObject options = new JSONObject();

        try {
            options = generateJSONObject(tableLayout);

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
            options.put("figure", Integer.parseInt(mCarSettings.getFigure()));

            options.put("displacement", getEditViewText(rootView, R.id.displacement_edit));
            options.put("category", mCarSettings.getCategory());
            options.put("transmission", getEditViewText(rootView, R.id.transmission_edit));
            options.put("spareTire", Integrated2Layout.getSpareTireSelection());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return options;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * @param options
     * @param handler
     */
    public void fillInData(JSONObject options, Handler handler) {
        try {
            TableLayout tableLayout = (TableLayout)rootView.findViewById(R.id.bi_content_table);

            List<View> views = new ArrayList<View>();
            Helper.findAllViews(tableLayout, views, Spinner.class);

            for(View view : views) {
                setSpinnerSelectionWithString(rootView, view.getId(),
                        options.isNull((String)view.getTag()) ? "无" : options.getString((String)view.getTag()));
            }

            if(options.has("displacement"))
                setEditViewText(rootView, R.id.displacement_edit, Double.toString(options.getDouble("displacement")));
            if(options.has("transmission"))
                setEditViewText(rootView, R.id.transmission_edit, options.getString("transmission"));

            // 备胎
            if(options.has("spareTire"))
                Integrated2Layout.setSpareTireSelection(options.getString("spareTire"));

            handler.sendEmptyMessage(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
