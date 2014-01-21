package com.df.app.CarCheck;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.df.app.R;
import com.df.app.service.MyOnClick;
import com.df.app.service.Adapter.MyViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-20.
 */
public class PhotoLayout extends LinearLayout {
    private View rootView;

    private ViewPager viewPager;
    private TextView exteriorTab, interiorTab, faultTab, procedureTab, engineTab, otherTab;
    private List<View> views;

    private PhotoExteriorLayout photoExteriorLayout;
    private PhotoInteriorLayout photoInteriorLayout;
    private PhotoFaultLayout photoFaultLayout;
    private PhotoProcedureLayout photoProcedureLayout;
    private PhotoEngineLayout photoEngineLayout;
    private PhotoOtherLayout photoOtherLayout;

    private int selectedColor = Color.rgb(0xAA, 0x03, 0x0A);
    private int unselectedColor = Color.rgb(0x70, 0x70, 0x70);

    public PhotoLayout(Context context) {
        super(context);
        init(context);
    }

    public PhotoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PhotoLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.photo_layout, this);
        InitViewPager(context);
        InitTextView();
    }

    private void InitViewPager(Context context) {
        viewPager = (ViewPager) rootView.findViewById(R.id.vPager);
        views = new ArrayList<View>();

        photoExteriorLayout = new PhotoExteriorLayout(context);
        photoInteriorLayout = new PhotoInteriorLayout(context);
        photoFaultLayout = new PhotoFaultLayout(context);
        photoProcedureLayout = new PhotoProcedureLayout(context);
        photoEngineLayout = new PhotoEngineLayout(context);
        photoOtherLayout = new PhotoOtherLayout(context);

        views.add(photoExteriorLayout);
        views.add(photoInteriorLayout);
        views.add(photoFaultLayout);
        views.add(photoProcedureLayout);
        views.add(photoEngineLayout);
        views.add(photoOtherLayout);

        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    private void InitTextView() {
        exteriorTab = (TextView) rootView.findViewById(R.id.tabExterior);
        interiorTab = (TextView) rootView.findViewById(R.id.tabInterior);
        faultTab = (TextView) rootView.findViewById(R.id.tabFault);
        procedureTab = (TextView) rootView.findViewById(R.id.tabProcedure);
        engineTab = (TextView) rootView.findViewById(R.id.tabEngine);
        otherTab = (TextView) rootView.findViewById(R.id.tabOther);

        selectTab(0);

        exteriorTab.setOnClickListener(new MyOnClick(viewPager, 0));
        interiorTab.setOnClickListener(new MyOnClick(viewPager, 1));
        faultTab.setOnClickListener(new MyOnClick(viewPager, 2));
        procedureTab.setOnClickListener(new MyOnClick(viewPager, 3));
        engineTab.setOnClickListener(new MyOnClick(viewPager, 4));
        otherTab.setOnClickListener(new MyOnClick(viewPager, 5));
    }

    public void updateUi() {
        // 更新照片队列（如果有新照片的话）
    }

    public void saveProceduresStandardPhoto() {
        photoProcedureLayout.saveProceduresStandardPhoto();
    }

    public void saveEngineStandardPhoto() {
        photoEngineLayout.saveExteriorStandardPhoto();
    }

    public void saveOtherStandardPhoto() {
        photoOtherLayout.saveOtherStandardPhoto();
    }

    public boolean checkAllFields() {
        boolean pass;

        // 外观组照片必拍
        pass = photoExteriorLayout.check();

        if(pass) {
            // 内饰组照片必拍
            pass = photoInteriorLayout.check();
        }

        if(pass) {
            // 机舱组照片必拍
            pass = photoEngineLayout.check();
        }

        if(pass) {
            // 手续组照片不是必拍
            pass = photoProcedureLayout.check();
        }

        if(pass) {
            // 其他组照片不是必拍
            pass = photoOtherLayout.check();
        }

        return false;
    }

    public void locateEmptyField() {

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
        faultTab.setTextColor(currIndex == 2 ? selectedColor : unselectedColor);
        procedureTab.setTextColor(currIndex == 3 ? selectedColor : unselectedColor);
        engineTab.setTextColor(currIndex == 4 ? selectedColor : unselectedColor);
        otherTab.setTextColor(currIndex == 5 ? selectedColor : unselectedColor);
    }
}
