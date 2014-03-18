package com.df.app.carsChecked;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.df.app.R;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.Adapter.PhotoListAdapter;
import com.df.app.util.Common;

import org.json.JSONArray;
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

    private String[] exteriorPartArray = {"leftFront45", "rightFront45", "right", "rightRear45", "leftRear45", "left", "other"};
    private String[] interiorPartArray = {"workbench", "steeringWheel", "dashboard", "leftDoor+steeringWheel", "rearSeats", "coDriverSeat", "other"};
    private String[] proceduresPartArray = {"plate", "procedures", "keys", "other"};
    private String[] enginePartArray = {"overview", "left", "right", "other"};
    private String[] tirePartArray = {"leftFront", "rightFront", "leftRear", "rightRear", "spare"};

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
        adapter = new PhotoListAdapter(context, exteriorPhotos, false);
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
            parseData(photo);
            load(exteriorPhotos);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void load(List<PhotoEntity> photoEntities) throws JSONException {
        adapter = new PhotoListAdapter(context, photoEntities, false);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }

    private void parseData(JSONObject photo) throws JSONException {
        parseStandard(photo.getJSONObject("exterior").getJSONArray("standard"), exteriorPhotos, R.array.exterior_camera_item, exteriorPartArray);
        parseStandard(photo.getJSONObject("interior").getJSONArray("standard"), interiorPhotos, R.array.interior_camera_item, interiorPartArray);

        if(photo.get("procedures") != JSONObject.NULL)
            parseStandard(photo.getJSONArray("procedures"), proceduresPhotos, R.array.photoForProceduresItems, proceduresPartArray);

        parseStandard(photo.getJSONArray("engineRoom"), enginePhotos, R.array.photoForEngineItems, enginePartArray);

        parseFault(photo);
        parseTire(photo.getJSONObject("tire"));

        parseAgreement(photo.getJSONArray("agreement"));
    }

    private void parseAgreement(JSONArray agreement) throws JSONException {
        for(int i = 0; i < agreement.length(); i++) {
            JSONObject temp = agreement.getJSONObject(i);

            PhotoEntity photoEntity = new PhotoEntity();

            String thumbUrl = temp.getString("photo");  //     c/1403/12/00715-zmckhyiiyoq.jpg
            thumbUrl = Common.PICTURE_ADDRESS + "small/" + thumbUrl + "?w=150";
            photoEntity.setThumbFileName(thumbUrl);

            agreementPhotos.add(photoEntity);
        }
    }

    private void parseTire(JSONObject tire) throws JSONException {
        for(int i = 0; i < tirePartArray.length; i++) {
            if(tire.get(tirePartArray[i]) != JSONObject.NULL) {
                JSONObject jsonObject = tire.getJSONObject(tirePartArray[i]);
                addTire(jsonObject);
            }
        }
    }

    private void addTire(JSONObject tire) throws JSONException{
        if(tire != JSONObject.NULL) {
            PhotoEntity photoEntity = new PhotoEntity();

            String thumbUrl = tire.getString("photo");  //     c/1403/12/00715-zmckhyiiyoq.jpg
            thumbUrl = Common.PICTURE_ADDRESS + "small/" + thumbUrl + "?w=150";
            photoEntity.setThumbFileName(thumbUrl);

            exteriorPhotos.add(photoEntity);
        }
    }

    private void parseStandard(JSONArray jsonArray, List<PhotoEntity> photoEntities, int stringArrayId, String[] partArray) throws JSONException {
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject temp = jsonArray.getJSONObject(i);

            PhotoEntity photoEntity = new PhotoEntity();

            String part = temp.getString("part");

            String[] exteriorPart = getResources().getStringArray(stringArrayId);

            for(int j = 0; j < partArray.length; j++) {
                if(part.equals(partArray[j])) {
                    photoEntity.setName(exteriorPart[j]);
                }
            }

            String thumbUrl = temp.getString("photo");  //     c/1403/12/00715-zmckhyiiyoq.jpg
            thumbUrl = Common.PICTURE_ADDRESS + "small/" + thumbUrl + "?w=150";
            photoEntity.setThumbFileName(thumbUrl);

            photoEntities.add(photoEntity);
        }
    }

    /**
     * 解析fault部分
     * @param photo
     * @throws JSONException
     */
    private void parseFault(JSONObject photo) throws JSONException {
        if(photo == JSONObject.NULL) {
            return;
        }

        if(photo.getJSONObject("exterior").get("fault") != JSONObject.NULL)
            addFault(photo.getJSONObject("exterior").getJSONArray("fault"));

        if(photo.getJSONObject("interior").get("fault") != JSONObject.NULL)
            addFault(photo.getJSONObject("interior").getJSONArray("fault"));

        if(photo.getJSONObject("frame").get("front") != JSONObject.NULL)
            addFault(photo.getJSONObject("frame").getJSONArray("front"));

        if(photo.getJSONObject("frame").get("rear") != JSONObject.NULL)
            addFault(photo.getJSONObject("frame").getJSONArray("rear"));
    }

    /**
     * 将jsonArray中的照片加入faultPhotos
     * @param jsonArray array
     * @throws JSONException
     */
    private void addFault(JSONArray jsonArray) throws JSONException {
        if(jsonArray == JSONObject.NULL) {
            return;
        }

        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject temp = jsonArray.getJSONObject(i);

            PhotoEntity photoEntity = new PhotoEntity();

            String name;
            if(temp.has("type")) {
                switch (temp.getInt("type")) {
                    case 1:
                        name = "色差";
                        break;
                    case 2:
                        name = "划痕";
                        break;
                    case 3:
                        name = "变形";
                        break;
                    case 4:
                        name = "刮蹭";
                        break;
                    case 5:
                        name = "其它";
                        break;
                    case 6:
                        name = "脏污";
                        break;
                    case 7:
                        name = "破损";
                        break;
                    default:
                        name = "";
                        break;
                }
            } else {
                name = "结构缺陷";
            }

            photoEntity.setName(name);

            String thumbUrl = temp.getString("photo");  //     c/1403/12/00715-zmckhyiiyoq.jpg
            thumbUrl = Common.PICTURE_ADDRESS + "small/" + thumbUrl + "?w=150";
            photoEntity.setThumbFileName(thumbUrl);
            photoEntity.setComment(temp.getString("comment"));

            faultPhotos.add(photoEntity);
        }
    }
}
