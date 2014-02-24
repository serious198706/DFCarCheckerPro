package com.df.app.CarCheck;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.df.app.R;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.Adapter.PhotoListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-26.
 *
 * 内饰标准照列表
 */
public class PhotoInteriorLayout extends LinearLayout {
    private View rootView;
    private Context context;

    public static PhotoListAdapter photoListAdapter;

    public PhotoInteriorLayout(Context context) {
        super(context);

        this.context = context;
        init(context);
    }

    public PhotoInteriorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PhotoInteriorLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.photo_interior_list, this);
        ListView interiorList = (ListView) findViewById(R.id.photo_interior_list);

        List<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();

        photoListAdapter = new PhotoListAdapter(context, R.id.photo_interior_list, photoEntities);
        interiorList.setAdapter(photoListAdapter);
    }

    /**
     * 生成测试数据
     * @return
     */
    private ArrayList<PhotoEntity> generateDummyPhoto() {
        ArrayList<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();

        PhotoEntity photoEntity1 = new PhotoEntity();
        photoEntity1.setComment("还行");
        photoEntity1.setFileName("in1");
        photoEntity1.setName("内饰 - 方向盘");

        PhotoEntity photoEntity2 = new PhotoEntity();
        photoEntity2.setComment("一般");
        photoEntity2.setFileName("in2");
        photoEntity2.setName("内饰 - 工作台");

        photoEntities.add(photoEntity1);
        photoEntities.add(photoEntity2);

        return photoEntities;
    }
}
