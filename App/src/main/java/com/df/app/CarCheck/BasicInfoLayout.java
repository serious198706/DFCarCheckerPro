package com.df.app.CarCheck;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.df.app.R;
import com.df.app.entries.CarSettings;
import com.df.app.service.Adapter.MyViewPagerAdapter;
import com.df.app.service.MyOnClick;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by 岩 on 13-12-20.
 *
 * 基本信息页面，包括车辆信息、配置信息两个子页面
 */
public class BasicInfoLayout extends LinearLayout implements ViewPager.OnPageChangeListener{
    private View rootView;

    private ViewPager viewPager;
    private TextView vehicleInfoTab, optionsTab;
    private List<View> views;

    private VehicleInfoLayout vehicleInfoLayout;
    private OptionsLayout optionsLayout;

    public static CarSettings mCarSettings;

    public static int carId;

    private OnUpdateUiListener mUpdateUiCallback;

    private int selectedColor = Color.rgb(0xAA, 0x03, 0x0A);
    private int unselectedColor = Color.rgb(0x70, 0x70, 0x70);

    public BasicInfoLayout(Context context) {
        super(context);
        if(!isInEditMode())
            init(context);
    }

    public BasicInfoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BasicInfoLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context){
        rootView = LayoutInflater.from(context).inflate(R.layout.basic_info_layout, this);

        mCarSettings = new CarSettings();

        optionsLayout = new OptionsLayout(context);
        vehicleInfoLayout = new VehicleInfoLayout(context, new VehicleInfoLayout.UpdateUi() {
            @Override
            public void updateUi() {
                // optionsLayout介个页面只添加一次就好了，多添加就出事了
                if(views.size() == 1) {
                    views.add(optionsLayout);
                    optionsTab.setVisibility(VISIBLE);
                    optionsTab.setOnClickListener(new MyOnClick(viewPager, 1));
                    viewPager.setAdapter(new MyViewPagerAdapter(views));
                }

                // 更新结构、外观、内饰
                mUpdateUiCallback.updateUi();

                // 更新配置信息页面
                optionsLayout.updateUi();
            }
        });

        InitViewPager(context);
        InitTextView();
    }

    /**
     * 初始化viewPager，用来承载各个模块，可以通过滑动切换
     */
    private void InitViewPager(Context context) {
        viewPager = (ViewPager) rootView.findViewById(R.id.vPager);
        views = new ArrayList<View>();

        views.add(vehicleInfoLayout);

        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(this);
    }

    /**
     * 初始化标签，用来标识当前模块，可以点击
     */
    private void InitTextView() {
        vehicleInfoTab = (TextView) rootView.findViewById(R.id.vehicleInfoTab);
        optionsTab = (TextView) rootView.findViewById(R.id.optionsTab);
        optionsTab.setVisibility(INVISIBLE);

        selectTab(0);

        vehicleInfoTab.setOnClickListener(new MyOnClick(viewPager, 0));
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int arg0) {
        selectTab(arg0);
    }

    private void selectTab(int currIndex) {
        vehicleInfoTab.setTextColor(currIndex == 0 ? selectedColor : unselectedColor);
        optionsTab.setTextColor(currIndex == 1 ? selectedColor : unselectedColor);
    }

    /**
     * 生成基本信息的JSON串
     */
    public JSONObject generateJSONObject() {

        JSONObject features = new JSONObject();

        try {
            features.put("procedures", vehicleInfoLayout.generateJSONObject());
            features.put("options", optionsLayout.generateJSONObject());
        } catch (JSONException e) {

        }

        return features;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     */
    public void fillInData(int carId, JSONObject features) {
        try {
            this.carId = carId;

            JSONObject procedures = features.getJSONObject("procedures");

            if(features.has("options")) {
                JSONObject options = features.getJSONObject("options");
                optionsLayout.fillInData(options);
                vehicleInfoLayout.fillInData(procedures, options.getString("countryId"), options.getString("brandId"),
                        options.getString("manufacturerId"), options.getString("seriesId"), options.getString("modelId"));
            } else {
                vehicleInfoLayout.fillInData(procedures);
            }
        } catch (JSONException e) {

        }
    }

    /**
     * CarCheckActivity来实现此接口，当确定车辆配置信息后，需要更新事故、外观、内饰页面
     */
    public interface OnUpdateUiListener {
        public void updateUi();
    }

    /**
     * 因为基本信息页是在xml中静态生成的，所以要手动设置回调的监听者
     */
    public void setUpdateUiListener(OnUpdateUiListener listener) {
        this.mUpdateUiCallback = listener;
    }
}
