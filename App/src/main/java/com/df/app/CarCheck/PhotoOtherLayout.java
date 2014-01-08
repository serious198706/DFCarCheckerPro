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

/**
 * Created by 岩 on 13-12-26.
 */
public class PhotoOtherLayout extends LinearLayout {
    private View rootView;
    private Context context;

    public PhotoOtherLayout(Context context) {
        super(context);

        this.context = context;
        init(context);
    }

    public PhotoOtherLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PhotoOtherLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.photo_other_list, this);

        ArrayList<PhotoEntity> photoEntities = generateDummyPhoto();


        ListView otherList = (ListView) findViewById(R.id.photo_other_list);
        otherList.setAdapter(new PhotoListAdapter(context, R.id.photo_other_list, photoEntities));
    }

    public void updateUi() {
        ArrayList<PhotoEntity> photoEntities = generateDummyPhoto();

        ListView otherList = (ListView) findViewById(R.id.photo_other_list);
        otherList.setAdapter(new PhotoListAdapter(this.context, R.id.photo_other_list, photoEntities));
    }

    private ArrayList<PhotoEntity> generateDummyPhoto() {
        ArrayList<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();

        PhotoEntity photoEntity1 = new PhotoEntity();
        photoEntity1.setComment("还行");
        photoEntity1.setFileName("ot1");
        photoEntity1.setName("其他");

        PhotoEntity photoEntity2 = new PhotoEntity();
        photoEntity2.setComment("一般");
        photoEntity2.setFileName("ot2");
        photoEntity2.setName("其他");

        photoEntities.add(photoEntity1);
        photoEntities.add(photoEntity2);

        return photoEntities;
    }
}
