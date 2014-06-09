package com.df.app.carCheck;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.R;
import com.df.library.entries.ListedPhoto;
import com.df.library.entries.PhotoEntity;
import com.df.app.service.Adapter.PaintPhotoListAdapter;
import com.df.app.service.Adapter.PhotoListAdapter;
import com.df.library.util.Common;
import com.df.library.util.MyOnClick;
import com.df.library.service.views.MyViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-20.
 *
 * 照片列表，包括外观组标准照、内饰组标准照、缺陷组、机舱组、手续组和协议组
 */
public class PhotoLayout extends LinearLayout implements ViewPager.OnPageChangeListener {
    public static int photoIndex;

    public static PaintPhotoListAdapter paintPhotoListAdapter;
    public static ListedPhoto listedPhoto;
    public static PhotoListAdapter photoListAdapter;

    private View rootView;

    private ViewPager viewPager;
    private TextView exteriorTab, interiorTab, faultTab, procedureTab, engineTab, otherTab;

    public static PhotoExteriorLayout photoExteriorLayout;
    public static PhotoInteriorLayout photoInteriorLayout;
    public static PhotoFaultLayout photoFaultLayout;
    public static PhotoProcedureLayout photoProcedureLayout;
    public static PhotoEngineLayout photoEngineLayout;
    public static PhotoAgreement photoAgreement;

    public static PhotoEntity reTakePhotoEntity;
    public static PhotoEntity commentModEntity;

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
        List<View> views = new ArrayList<View>();

        photoExteriorLayout = new PhotoExteriorLayout(context);
        photoInteriorLayout = new PhotoInteriorLayout(context);
        photoFaultLayout = new PhotoFaultLayout(context);
        photoProcedureLayout = new PhotoProcedureLayout(context);
        photoEngineLayout = new PhotoEngineLayout(context);
        photoAgreement = new PhotoAgreement(context);

        views.add(photoExteriorLayout);
        views.add(photoInteriorLayout);
        views.add(photoFaultLayout);
        views.add(photoProcedureLayout);
        views.add(photoEngineLayout);
        views.add(photoAgreement);

        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(this);
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

    /**
     * 保存缺陷组照片
     */
    public void saveOtherFaultPhoto(String comment) {
        photoFaultLayout.saveOtherFaultPhoto(comment);
    }

    /**
     * 提交前的检查
     * @return
     */
    public String checkAllFields() {
        String currentField;

        currentField = photoExteriorLayout.check();

        if(!currentField.equals("")) {
            if(currentField.equals("exterior_1")) {
                Toast.makeText(rootView.getContext(), "外观组未拍摄左前45°或右前45°照片！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(rootView.getContext(), "外观组照片拍摄数量不足！", Toast.LENGTH_SHORT).show();
            }

            viewPager.setCurrentItem(0);

            return currentField;
        }

        currentField = photoInteriorLayout.check();

        if(!currentField.equals("")) {
            Toast.makeText(rootView.getContext(), "内饰组照片拍摄数量不足！", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(1);

            return currentField;
        }

        // 机舱组照片必拍
        currentField = photoEngineLayout.check();

        if(!currentField.equals("")) {
            if(currentField.contains("engine_")) {
                int index = Integer.parseInt(currentField.substring(currentField.length() - 1, currentField.length()));

                Toast.makeText(rootView.getContext(), "机舱组未拍摄" + Common.enginePartArray[index] + "照片！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(rootView.getContext(), "机舱组照片拍摄数量不足！", Toast.LENGTH_SHORT).show();
            }

            viewPager.setCurrentItem(4);

            return currentField;
        }

        // 协议组照片必拍
        currentField = photoAgreement.check();

        if(!currentField.equals("")) {
            Toast.makeText(rootView.getContext(), "协议组照片拍摄数量不足！", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(5);

            return currentField;
        }

        return currentField;
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
        exteriorTab.setTextColor(currIndex == 0 ? Common.selectedColor : Common.unselectedColor);
        interiorTab.setTextColor(currIndex == 1 ? Common.selectedColor : Common.unselectedColor);
        faultTab.setTextColor(currIndex == 2 ? Common.selectedColor : Common.unselectedColor);
        procedureTab.setTextColor(currIndex == 3 ? Common.selectedColor : Common.unselectedColor);
        engineTab.setTextColor(currIndex == 4 ? Common.selectedColor : Common.unselectedColor);
        otherTab.setTextColor(currIndex == 5 ? Common.selectedColor : Common.unselectedColor);
    }

    public void clearCache() {
        PhotoExteriorLayout.photoListAdapter = null;
        for(int i = 0; i < PhotoExteriorLayout.photoShotCount.length; i++) {
            PhotoExteriorLayout.photoShotCount[i] = 0;
            PhotoExteriorLayout.photoNames[i] = 0;
        }

        PhotoInteriorLayout.photoListAdapter = null;
        for(int i = 0; i < PhotoInteriorLayout.photoShotCount.length; i++) {
            PhotoInteriorLayout.photoShotCount[i] = 0;
            PhotoInteriorLayout.photoNames[i] = 0;
        }

        PhotoEngineLayout.photoListAdapter = null;
        for(int i = 0; i < PhotoEngineLayout.photoShotCount.length; i++) {
            PhotoEngineLayout.photoShotCount[i] = 0;
            PhotoEngineLayout.photoNames[i] = 0;
        }

        PhotoProcedureLayout.photoListAdapter = null;
        for(int i = 0; i < PhotoProcedureLayout.photoShotCount.length; i++) {
            PhotoProcedureLayout.photoShotCount[i] = 0;
            PhotoProcedureLayout.photoNames[i] = 0;
        }

        for(int i = 0; i < Integrated2Layout.photoShotCount.length; i++) {
            Integrated2Layout.photoShotCount[i] = 0;
        }

        PhotoAgreement.photoListAdapter = null;
        PhotoAgreement.photoShotCount = 0;
        PhotoAgreement.photoName = 0;

        PhotoLayout.photoIndex = 1;
    }

    public static void notifyDataSetChanged() {
        PhotoExteriorLayout.photoListAdapter.notifyDataSetChanged();
        PhotoInteriorLayout.photoListAdapter.notifyDataSetChanged();
        PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();
        PhotoProcedureLayout.photoListAdapter.notifyDataSetChanged();
        PhotoEngineLayout.photoListAdapter.notifyDataSetChanged();
        PhotoAgreement.photoListAdapter.notifyDataSetChanged();
    }
}
