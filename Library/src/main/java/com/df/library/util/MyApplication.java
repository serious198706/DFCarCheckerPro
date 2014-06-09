package com.df.library.util;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 岩 on 14-3-27.
 */
public class MyApplication extends Application {

    Map<Integer,String> maps;

    @Override
    public void onCreate() {
        super.onCreate();

        maps = new HashMap<Integer, String>();
    }


    public void setValue(Integer key,String value){
        maps.put(key,value);
    }

    public String getValue(Integer key){
        return maps.get(key);
    }
}
