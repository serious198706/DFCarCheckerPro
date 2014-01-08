package com.df.app.CarCheck;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.renderscript.Int3;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.df.app.R;
import com.df.app.service.MyOnClick;
import com.df.app.service.MyViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-20.
 */
public class IntegratedCheckLayout extends LinearLayout {
    private View rootView;

    private ViewPager viewPager;
    private ImageView imageView;
    private TextView exteriorTab, interiorTab, itTab1, itTab2, itTab3;
    private List<View> views;
    private int offset =0;
    private int currIndex = 0;
    private int bmpW;

    private Activity activity;

    private static ExteriorLayout exteriorLayout;
    private static InteriorLayout interiorLayout;
    private static Integrated1Layout integrated1Layout;
    private static Integrated2Layout integrated2Layout;
    private static Integrated3Layout integrated3Layout;

    private int selectedColor = Color.rgb(0xAA, 0x03, 0x0A);
    private int unselectedColor = Color.rgb(0x70, 0x70, 0x70);

    public IntegratedCheckLayout(Context context) {
        super(context);
        init(context);
    }

    public IntegratedCheckLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IntegratedCheckLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.integrated_check_layout, this);
        InitViewPager(context);
        InitTextView();
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    private void InitViewPager(Context context) {
        viewPager = (ViewPager) rootView.findViewById(R.id.vPager);
        views = new ArrayList<View>();

        exteriorLayout = new ExteriorLayout(context);
        interiorLayout = new InteriorLayout(context);
        integrated1Layout = new Integrated1Layout(context);
        integrated2Layout = new Integrated2Layout(context);
        integrated3Layout = new Integrated3Layout(context);

        views.add(exteriorLayout);
        views.add(interiorLayout);
        views.add(integrated1Layout);
        views.add(integrated2Layout);
        views.add(integrated3Layout);

        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    private void InitTextView() {
        exteriorTab = (TextView) rootView.findViewById(R.id.tabExterior);
        interiorTab = (TextView) rootView.findViewById(R.id.tabInterior);
        itTab1 = (TextView) rootView.findViewById(R.id.tabIt1);
        itTab2 = (TextView) rootView.findViewById(R.id.tabIt2);
        itTab3 = (TextView) rootView.findViewById(R.id.tabIt3);

        selectTab(0);

        exteriorTab.setOnClickListener(new MyOnClick(viewPager, 0));
        interiorTab.setOnClickListener(new MyOnClick(viewPager, 1));
        itTab1.setOnClickListener(new MyOnClick(viewPager, 2));
        itTab2.setOnClickListener(new MyOnClick(viewPager, 3));
        itTab3.setOnClickListener(new MyOnClick(viewPager, 4));
    }

    public void updateUi() {
        exteriorLayout.updateUi();
        interiorLayout.updateUi();
    }

    public void updateExteriorPreview() {
        exteriorLayout.updateExteriorPreview();
    }

    public void updateInteriorPreview() {
        interiorLayout.updateInteriorPreview();
    }

    public void saveExteriorStandardPhoto() {
        exteriorLayout.saveExteriorStandardPhoto();
    }

    public void saveInteriorStandardPhoto() {
        interiorLayout.saveInteriorStandardPhoto();
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
        exteriorTab.setTextColor(currIndex == 0 ? selectedColor : unselectedColor);
        interiorTab.setTextColor(currIndex == 1 ? selectedColor : unselectedColor);
        itTab1.setTextColor(currIndex == 2 ? selectedColor : unselectedColor);
        itTab2.setTextColor(currIndex == 3 ? selectedColor : unselectedColor);
        itTab3.setTextColor(currIndex == 4 ? selectedColor : unselectedColor);
    }
}
