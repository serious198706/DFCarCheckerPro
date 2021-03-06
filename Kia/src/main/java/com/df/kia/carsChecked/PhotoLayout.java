package com.df.kia.carsChecked;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.df.kia.R;
import com.df.kia.carCheck.BasicInfoLayout;
import com.df.library.entries.PhotoEntity;
import com.df.kia.service.Adapter.PhotoListAdapter;
import com.df.library.util.PhotoParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 14-3-14.
 */
public class PhotoLayout extends LinearLayout {
    private PhotoListAdapter adapter;
    private ListView listView;
    private Context context;

    private List<PhotoEntity> exteriorPhotos;
    private List<PhotoEntity> interiorPhotos;
    private List<PhotoEntity> faultPhotos;
    private List<PhotoEntity> proceduresPhotos;
    private List<PhotoEntity> enginePhotos;
    private List<PhotoEntity> agreementPhotos;

    public PhotoLayout(Context context, JSONObject photo) {
        super(context);
        this.context = context;
        init(context, photo);
    }

    public PhotoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init(Context context, JSONObject photo) {
        LayoutInflater.from(context).inflate(R.layout.car_report_photo_layout, this);

        exteriorPhotos = new ArrayList<PhotoEntity>();
        interiorPhotos = new ArrayList<PhotoEntity>();
        faultPhotos = new ArrayList<PhotoEntity>();
        proceduresPhotos = new ArrayList<PhotoEntity>();
        enginePhotos = new ArrayList<PhotoEntity>();
        agreementPhotos = new ArrayList<PhotoEntity>();

        listView = (ListView)findViewById(R.id.photoList);
        adapter = new PhotoListAdapter(context, exteriorPhotos, false, false, false, new PhotoListAdapter.OnAction() {
            @Override
            public void onDelete(int position) {

            }

            @Override
            public void onModifyComment(int position, String comment) {

            }

            @Override
            public void onShowPhoto(int position) {

            }
        });
        listView.setAdapter(adapter);

        Button exteriorButton = (Button)findViewById(R.id.exteriorPhotoButton);
        exteriorButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    load(exteriorPhotos);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Button interiorButton = (Button)findViewById(R.id.interiorPhotoButton);
        interiorButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    load(interiorPhotos);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Button faultButton = (Button)findViewById(R.id.faultPhotoButton);
        faultButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    load(faultPhotos);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Button proceduresButton = (Button)findViewById(R.id.proceduresPhotoButton);
        proceduresButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    load(proceduresPhotos);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Button engineButton = (Button)findViewById(R.id.enginePhotoButton);
        engineButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    load(enginePhotos);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Button agreementButton = (Button)findViewById(R.id.agreementPhotoButton);
        agreementButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    load(agreementPhotos);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            PhotoParser.parsePhotoData(context, BasicInfoLayout.carId, photo, exteriorPhotos, interiorPhotos, faultPhotos, proceduresPhotos,
                    enginePhotos, agreementPhotos);
            load(exteriorPhotos);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void load(List<PhotoEntity> photoEntities) throws JSONException {
        adapter = new PhotoListAdapter(context, photoEntities, false, false, false, new PhotoListAdapter.OnAction() {
            @Override
            public void onDelete(int position) {

            }

            @Override
            public void onModifyComment(int position, String comment) {

            }

            @Override
            public void onShowPhoto(int position) {

            }
        });
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }
}
