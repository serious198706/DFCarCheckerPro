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
import com.df.app.service.MyViewPagerAdapter;
import com.df.app.service.MyOnClick;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.jar.JarOutputStream;

import static com.df.app.util.Helper.getDateString;
import static com.df.app.util.Helper.getEditViewText;
import static com.df.app.util.Helper.getSpinnerSelectedText;

/**
 * Created by 岩 on 13-12-20.
 */
public class BasicInfoLayout extends LinearLayout {
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

    public void setUpdateUiListener(OnUpdateUiListener listener) {
        this.mUpdateUiCallback = listener;
    }

    public void init(Context context){
        rootView = LayoutInflater.from(context).inflate(R.layout.basic_info_layout, this);

        // TODO carId由运营处获取
        UUID uuid = UUID.randomUUID();
        carId = new Random().nextInt(10);

        mCarSettings = new CarSettings();

        optionsLayout = new OptionsLayout(context);
        vehicleInfoLayout = new VehicleInfoLayout(context, new VehicleInfoLayout.UpdateUi() {
            @Override
            public void updateUi() {
                optionsLayout.updateUi();
                mUpdateUiCallback.updateUi();
            }
        });

        InitViewPager(context);
        InitTextView();
    }

    private void InitViewPager(Context context) {
        viewPager = (ViewPager) rootView.findViewById(R.id.vPager);
        views = new ArrayList<View>();

        views.add(vehicleInfoLayout);
        views.add(optionsLayout);

        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    private void InitTextView() {
        vehicleInfoTab = (TextView) rootView.findViewById(R.id.vehicleInfoTab);
        optionsTab = (TextView) rootView.findViewById(R.id.optionsTab);

        selectTab(0);

        vehicleInfoTab.setOnClickListener(new MyOnClick(viewPager, 0));
        optionsTab.setOnClickListener(new MyOnClick(viewPager, 1));
    }

    class MyOnPageChangeListener implements ViewPager.OnPageChangeListener
    {
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
    }

    private void selectTab(int currIndex) {
        vehicleInfoTab.setTextColor(currIndex == 0 ? selectedColor : unselectedColor);
        optionsTab.setTextColor(currIndex == 1 ? selectedColor : unselectedColor);
    }

    public JSONObject generateJSONObject() {

        JSONObject features = new JSONObject();

        try {
            features.put("procedures", vehicleInfoLayout.generateJSONObject());
            features.put("options", optionsLayout.generateJSONObject());
        } catch (JSONException e) {

        }

        return features;
    }

    // CarCheckActivity必须实现此接口
    public interface OnUpdateUiListener {
        public void updateUi();
    }
}
