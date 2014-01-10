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
public class PhotoProcedureLayout extends LinearLayout {
    private View rootView;
    private Context context;

    public static PhotoListAdapter photoListAdapter;

    public PhotoProcedureLayout(Context context) {
        super(context);

        this.context = context;
        init(context);
    }

    public PhotoProcedureLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PhotoProcedureLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.photo_procedure_list, this);

        List<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();
        photoListAdapter = new PhotoListAdapter(context, R.id.photo_procedure_list, photoEntities);

        ListView procedureList = (ListView) findViewById(R.id.photo_procedure_list);
        procedureList.setAdapter(photoListAdapter);
    }

    private ArrayList<PhotoEntity> generateDummyPhoto() {
        ArrayList<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();

        PhotoEntity photoEntity1 = new PhotoEntity();
        photoEntity1.setComment("还行");
        photoEntity1.setFileName("pr1");
        photoEntity1.setName("手续 - 钥匙");

        PhotoEntity photoEntity2 = new PhotoEntity();
        photoEntity2.setComment("一般");
        photoEntity2.setFileName("pr2");
        photoEntity2.setName("手续 - 证件");

        photoEntities.add(photoEntity1);
        photoEntities.add(photoEntity2);

        return photoEntities;
    }
}
