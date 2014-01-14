package com.df.app.service;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by 岩 on 13-12-20.
 */

public class MyOnClick implements View.OnClickListener
{
    int index = 0;
    ViewPager viewPager;

    public MyOnClick(ViewPager viewPager, int i){
        this.index = i ;
        this.viewPager = viewPager;
    }

    @Override
    public void onClick(View v) {
        this.viewPager.setCurrentItem(index);
    }
}

