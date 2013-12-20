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
import com.df.app.service.MyOnClick;
import com.df.app.service.MyViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-20.
 */
public class AccidentCheckLayout extends LinearLayout {
    private View rootView;

    private ViewPager viewPager;
    private ImageView imageView;
    private TextView textView1,textView2,textView3;
    private List<View> views;
    private int offset =0;
    private int currIndex = 0;
    private int bmpW;

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
        InitImageView();
        InitViewPager(context);
        InitTextView();
    }

    private void InitViewPager(Context context) {
        viewPager = (ViewPager) rootView.findViewById(R.id.vPager);
        views = new ArrayList<View>();

        views.add(new CollectDataLayout(context));
        views.add(new IssueLayout(context));
        views.add(new AccidentResultLayout(context));

        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    private void InitTextView() {
        textView1 = (TextView) rootView.findViewById(R.id.tv_tab_meinv);
        textView2 = (TextView) rootView.findViewById(R.id.tv_tab_luoli);
        textView3 = (TextView) rootView.findViewById(R.id.tv_tab_qiche);
        textView1.setOnClickListener(new MyOnClick(viewPager, 0));
        textView2.setOnClickListener(new MyOnClick(viewPager, 1));
        textView3.setOnClickListener(new MyOnClick(viewPager, 2));
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

        imageView.setImageMatrix(matrix);// ÉèÖÃ¶¯»­³õÊ¼Î»ÖÃ
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
            Animation animation = new TranslateAnimation(one * currIndex, one * arg0, 0, 0);
            currIndex = arg0;
            animation.setFillAfter(true); // 动画完成后位置发生变化
            animation.setDuration(300);
            imageView.startAnimation(animation);
        }
    }
}
