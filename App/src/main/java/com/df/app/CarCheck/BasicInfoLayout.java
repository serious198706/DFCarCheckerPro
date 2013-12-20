package com.df.app.CarCheck;

import android.app.Activity;
import android.content.Context;
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
import com.df.app.service.MyViewPagerAdapter;
import com.df.app.service.MyOnClick;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-20.
 */
public class BasicInfoLayout extends LinearLayout implements CarRecogniseLayout.OnShowContentListener {
    private View rootView;

    private ViewPager viewPager;
    private ImageView imageView;
    private TextView carRecogniseTab, proceduresTab, optionsTab;
    private List<View> views;
    private int offset = 0;
    private int currIndex = 0;
    private int bmpW;
    private View carRecogniseView, proceduresView, optionsView;

    public BasicInfoLayout(Context context) {
        super(context);
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

    @Override
    public void showContent() {

    }

    public void init(Context context){
        rootView = LayoutInflater.from(context).inflate(R.layout.basic_info_layout, this);
        InitImageView();
        InitViewPager(context);
        InitTextView();
    }

    private void InitViewPager(Context context) {
        viewPager = (ViewPager) rootView.findViewById(R.id.vPager);
        views = new ArrayList<View>();

        views.add(new CarRecogniseLayout(context, new CarRecogniseLayout.OnShowContentListener() {
            @Override
            public void showContent() {
                // 当VIN确定后，出现另外两个页面
                views.add(new ProceduresLayout(rootView.getContext()));
                views.add(new OptionsLayout(rootView.getContext()));

                proceduresTab.setVisibility(VISIBLE);
                optionsTab.setVisibility(VISIBLE);

                proceduresTab.setOnClickListener(new MyOnClick(viewPager, 1));
                optionsTab.setOnClickListener(new MyOnClick(viewPager, 2));

                viewPager.setAdapter(new MyViewPagerAdapter(views));
            }
        }));

        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    private void InitTextView() {
        carRecogniseTab = (TextView) rootView.findViewById(R.id.carRecogniseTab);
        proceduresTab = (TextView) rootView.findViewById(R.id.proceduresTab);
        optionsTab = (TextView) rootView.findViewById(R.id.optionsTab);

        proceduresTab.setVisibility(INVISIBLE);
        optionsTab.setVisibility(INVISIBLE);

        carRecogniseTab.setOnClickListener(new MyOnClick(viewPager, 0));
    }

    private void InitImageView() {
        imageView = (ImageView) findViewById(R.id.iv_bottom_line);

        bmpW = imageView.getLayoutParams().width;

        DisplayMetrics dm = new DisplayMetrics();
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);

        int screenW = dm.widthPixels;
        offset = (screenW / 3 - bmpW) / 2;

        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);

        imageView.setImageMatrix(matrix);
    }

    class MyOnPageChangeListener implements ViewPager.OnPageChangeListener
    {
        int one = offset * 2 + bmpW; // 每次需要滑动的距离

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int arg0) {
            Animation animation = new TranslateAnimation(one * currIndex, one * arg0, 0, 0);
            currIndex = arg0;
            animation.setFillAfter(true); // 动画完成后位置发生变化
            animation.setDuration(300);
            imageView.startAnimation(animation);
        }
    }
}
