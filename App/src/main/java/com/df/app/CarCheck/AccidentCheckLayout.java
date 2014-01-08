package com.df.app.CarCheck;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
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
public class AccidentCheckLayout extends LinearLayout{
    private View rootView;

    private ViewPager viewPager;
    private ImageView imageView;
    private TextView collectTab, issueTab, resultTab;
    private List<View> views;
    private int offset =0;
    private int currIndex = 0;
    private int bmpW;

    private CollectDataLayout collectDataLayout;
    private IssueLayout issueLayout;
    private AccidentResultLayout accidentResultLayout;
    private boolean loaded;

    private int selectedColor = Color.rgb(0xAA, 0x03, 0x0A);
    private int unselectedColor = Color.rgb(0x70, 0x70, 0x70);

    public AccidentCheckLayout(Context context) {
        super(context);
        init(context);
    }

    public AccidentCheckLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AccidentCheckLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.accident_check_layout, this);

        collectDataLayout = new CollectDataLayout(context, new CollectDataLayout.OnGetIssueData() {
            @Override
            public void showContent() {
                // 当VIN确定后，出现另外两个页面
                if(!loaded) {
                    views.add(issueLayout);
                    views.add(accidentResultLayout);

                    issueTab.setVisibility(VISIBLE);
                    resultTab.setVisibility(VISIBLE);

                    issueTab.setOnClickListener(new MyOnClick(viewPager, 1));
                    resultTab.setOnClickListener(new MyOnClick(viewPager, 2));

                    viewPager.setAdapter(new MyViewPagerAdapter(views));

                    loaded = true;
                }
            }

            @Override
            public void updateUi() {
                issueLayout.updateUi();
                accidentResultLayout.updateUi();
            }
        });

        issueLayout = new IssueLayout(context);
        accidentResultLayout = new AccidentResultLayout(context);

        InitViewPager(context);
        InitTextView();
    }

    private void InitViewPager(Context context) {
        viewPager = (ViewPager) rootView.findViewById(R.id.vPager);
        views = new ArrayList<View>();

        views.add(collectDataLayout);

        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    private void InitTextView() {
        collectTab = (TextView) rootView.findViewById(R.id.collect);
        issueTab = (TextView) rootView.findViewById(R.id.issue);
        resultTab = (TextView) rootView.findViewById(R.id.result);

        selectTab(0);

        issueTab.setVisibility(INVISIBLE);
        resultTab.setVisibility(INVISIBLE);

        collectTab.setOnClickListener(new MyOnClick(viewPager, 0));
    }

    class MyOnPageChangeListener implements ViewPager.OnPageChangeListener
    {
        int one = offset * 2 + bmpW ;

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
        collectTab.setTextColor(currIndex == 0 ? selectedColor : unselectedColor);
        issueTab.setTextColor(currIndex == 1 ? selectedColor : unselectedColor);
        resultTab.setTextColor(currIndex == 2 ? selectedColor : unselectedColor);
    }
}
