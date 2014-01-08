package com.df.app.Procedures;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.df.app.CarCheck.OptionsLayout;
import com.df.app.R;
import com.df.app.entries.CarSettings;
import com.df.app.service.MyOnClick;
import com.df.app.service.MyViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by 岩 on 14-1-8.
 */
public class InputProceduresLayout extends LinearLayout {
    private View rootView;

    private ViewPager viewPager;
    private TextView carRecogniseTab, proceduresTab, optionsTab;
    private List<View> views;

    private boolean loaded = false;

    private CarRecogniseLayout carRecogniseLayout;
    private ProceduresLayout proceduresLayout;

    public static CarSettings mCarSettings;

    private OnUpdateUiListener mUpdateUiCallback;

    public static String uniqueId;

    private int selectedColor = Color.rgb(0xAA, 0x03, 0x0A);
    private int unselectedColor = Color.rgb(0x70, 0x70, 0x70);

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

        carRecogniseLayout = new CarRecogniseLayout(context, new CarRecogniseLayout.OnShowContentListener() {
            @Override
            public void showContent() {
                // 当VIN确定后，出现另外两个页面
                if(!loaded) {
                    views.add(proceduresLayout);

                    proceduresTab.setVisibility(VISIBLE);

                    proceduresTab.setOnClickListener(new MyOnClick(viewPager, 1));

                    viewPager.setAdapter(new MyViewPagerAdapter(views));

                    loaded = true;
                }
            }

            @Override
            public void updateUi() {
                proceduresLayout.updateUi();

                // TODO 删！！！
                mUpdateUiCallback.updateUi();
            }
        });

        proceduresLayout = new ProceduresLayout(context);

        InitViewPager(context);
        InitTextView();
    }

    private void InitViewPager(Context context) {
        viewPager = (ViewPager) rootView.findViewById(R.id.vPager);
        views = new ArrayList<View>();

        views.add(carRecogniseLayout);

        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    private void InitTextView() {
        carRecogniseTab = (TextView) rootView.findViewById(R.id.carRecogniseTab);
        proceduresTab = (TextView) rootView.findViewById(R.id.proceduresTab);

        selectTab(0);

        proceduresTab.setVisibility(INVISIBLE);

        carRecogniseTab.setOnClickListener(new MyOnClick(viewPager, 0));
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
        carRecogniseTab.setTextColor(currIndex == 0 ? selectedColor : unselectedColor);
        proceduresTab.setTextColor(currIndex == 1 ? selectedColor : unselectedColor);
    }

    // CarCheckActivity必须实现此接口
    public interface OnUpdateUiListener {
        public void updateUi();
    }
}
