package com.df.app.procedures;

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
import com.df.app.util.Common;
import com.df.app.util.MyOnClick;
import com.df.app.service.Adapter.MyViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by 岩 on 14-1-8.
 *
 * 手续信息主页面，包括车辆识别、手续录入
 */
public class InputProceduresLayout extends LinearLayout implements ViewPager.OnPageChangeListener {
    private View rootView;

    private ViewPager viewPager;
    private MyViewPagerAdapter adapter;
    private TextView carRecogniseTab, proceduresTab, optionsTab;
    private List<View> views;

    private boolean loaded = false;

    private CarRecogniseLayout carRecogniseLayout;
    private ProceduresWebLayout proceduresWebLayout;

    public static CarSettings mCarSettings;

    private OnUpdateUiListener mUpdateUiCallback;

    public static String uniqueId;

    public InputProceduresLayout(Context context) {
        super(context);
        init(context);
    }

    public InputProceduresLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public InputProceduresLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context){
        rootView = LayoutInflater.from(context).inflate(R.layout.input_procedures_layout, this);

        UUID uuid = UUID.randomUUID();
        uniqueId = uuid.toString();

        // 车辆配置信息
        mCarSettings = new CarSettings();

        carRecogniseLayout = new CarRecogniseLayout(context, new CarRecogniseLayout.OnShowContent() {
                @Override
            public void showContent(String vin, String plateNumber, String licenseModel, String vehicleType, String useCharacter, String engineSerial, String seriesId, String modelId) {
                // 更新手续页面
                proceduresWebLayout.updateUi(vin, plateNumber, licenseModel, vehicleType, useCharacter, engineSerial, seriesId, modelId);

                // 当VIN确定后，出现另外两个页面
                if(!loaded) {
                    views.add(proceduresWebLayout);
                    adapter.notifyDataSetChanged();

                    proceduresTab.setVisibility(VISIBLE);
                    proceduresTab.setOnClickListener(new MyOnClick(viewPager, 1));

                    loaded = true;
                }
            }

            @Override
            public void modify(String carId) {
                proceduresWebLayout.updateUi(carId);

                // 当VIN确定后，出现另外两个页面
                if(!loaded) {
                    views.add(proceduresWebLayout);
                    adapter.notifyDataSetChanged();

                    proceduresTab.setVisibility(VISIBLE);
                    proceduresTab.setOnClickListener(new MyOnClick(viewPager, 1));

                    loaded = true;
                }
            }
        }, new CarRecogniseLayout.OnHideContent() {
            @Override
            public void hideContent() {
                views.remove(proceduresWebLayout);
                adapter.notifyDataSetChanged();

                proceduresTab.setVisibility(INVISIBLE);
                proceduresTab.setOnClickListener(null);

                proceduresWebLayout.showContent(false);

                loaded = false;
            }
        });

        proceduresWebLayout = new ProceduresWebLayout(context);

        InitViewPager(context);
        InitTextView();
    }

    private void InitViewPager(Context context) {
        views = new ArrayList<View>();
        adapter = new MyViewPagerAdapter(views);
        viewPager = (ViewPager) rootView.findViewById(R.id.vPager);

        views.add(carRecogniseLayout);
        adapter.notifyDataSetChanged();

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(this);
    }

    private void InitTextView() {
        carRecogniseTab = (TextView) rootView.findViewById(R.id.carRecogniseTab);
        proceduresTab = (TextView) rootView.findViewById(R.id.proceduresTab);

        selectTab(0);

        proceduresTab.setVisibility(INVISIBLE);

        carRecogniseTab.setOnClickListener(new MyOnClick(viewPager, 0));
    }

    public boolean canGoBack() {
        return proceduresWebLayout.canGoBack();
    }

    public void goBack() {
        proceduresWebLayout.goBack();
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
        carRecogniseTab.setTextColor(currIndex == 0 ? Common.selectedColor : Common.unselectedColor);
        proceduresTab.setTextColor(currIndex == 1 ? Common.selectedColor : Common.unselectedColor);
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     */
    public void fillInData(int carId) {
        carRecogniseLayout.fillInData(carId);
        carRecogniseTab.setVisibility(GONE);
        views.remove(0);
        adapter.notifyDataSetChanged();
    }

    public void startAuthService(String authCode) {
        carRecogniseLayout.startAuthService(authCode);
    }

    public void updateLicensePhoto(boolean cut) {
        carRecogniseLayout.updateLicensePhoto(cut);
    }

    /**
     * CarCheckActivity必须实现此接口
     */
    public interface OnUpdateUiListener {
        public void updateUi();
    }
}
