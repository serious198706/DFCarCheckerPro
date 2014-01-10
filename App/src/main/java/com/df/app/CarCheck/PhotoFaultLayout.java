package com.df.app.CarCheck;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.df.app.R;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.PhotoListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-26.
 */
public class PhotoFaultLayout extends LinearLayout {
    private static View rootView;
    private static Context context;

    public static PhotoListAdapter photoListAdapter;

    public PhotoFaultLayout(Context context) {
        super(context);

        this.context = context;
        init(context);
    }

    public PhotoFaultLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PhotoFaultLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.photo_fault_list, this);

        List<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();

        photoListAdapter = new PhotoListAdapter(context, R.id.photo_fault_list, photoEntities);

        ListView faultList = (ListView) findViewById(R.id.photo_fault_list);
        faultList.setAdapter(photoListAdapter);
    }

    private ArrayList<PhotoEntity> generateDummyPhoto() {
        ArrayList<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();

        PhotoEntity photoEntity1 = new PhotoEntity();
        photoEntity1.setComment("右前铰链处有修复痕迹");
        photoEntity1.setFileName("fa1");
        photoEntity1.setName("缺陷 - 右前铰链处");

        PhotoEntity photoEntity2 = new PhotoEntity();
        photoEntity2.setComment("水箱上支架有修复痕迹");
        photoEntity2.setFileName("fa2");
        photoEntity2.setName("缺陷 - 水箱上支架");

        photoEntities.add(photoEntity1);
        photoEntities.add(photoEntity2);

        return photoEntities;
    }
}
