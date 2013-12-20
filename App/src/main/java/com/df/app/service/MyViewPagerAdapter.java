package com.df.app.service;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by 岩 on 13-12-20.
 */

public class MyViewPagerAdapter extends PagerAdapter {
    private List<View> listViews;

    public MyViewPagerAdapter(List<View> listViews){
        this.listViews = listViews ;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(listViews.get(position));
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(listViews.get(position),0);
        return listViews.get(position);
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return listViews.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        // TODO Auto-generated method stub
        return arg0 == arg1;
    }

}

