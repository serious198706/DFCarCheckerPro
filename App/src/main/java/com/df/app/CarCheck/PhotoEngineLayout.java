package com.df.app.CarCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.Adapter.PhotoListAdapter;
import com.df.app.util.Common;
import com.df.app.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-26.
 */
public class PhotoEngineLayout extends LinearLayout {
    private View rootView;
    private Context context;

    public static PhotoListAdapter photoListAdapter;
    private int[] photoShotCount = {0, 0, 0, 0};
    private int currentShotPart;
    private long currentTimeMillis;

    public PhotoEngineLayout(Context context) {
        super(context);

        this.context = context;
        init(context);
    }

    public PhotoEngineLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PhotoEngineLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.photo_engine_list, this);

        List<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();

        photoListAdapter = new PhotoListAdapter(this.context, R.id.photo_engine_list, photoEntities);

        ListView engineList = (ListView) findViewById(R.id.photo_engine_list);
        engineList.setAdapter(photoListAdapter);

        Button button = (Button)findViewById(R.id.photoButton);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                starCamera();
            }
        });
    }

    private void starCamera() {
        String[] itemArray = getResources().getStringArray(R.array.photoForEngineItems);

        for(int i = 0; i < itemArray.length; i++) {
            itemArray[i] += " (";
            itemArray[i] += Integer.toString(photoShotCount[i]);
            itemArray[i] += ") ";
        }

        AlertDialog dialog = new AlertDialog.Builder(context).setTitle(R.string.takePhotoForEngine)
                .setItems(itemArray, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        currentShotPart = i;

                        String group = getResources().getStringArray(R.array.photoForEngineItems)[currentShotPart];

                        Toast.makeText(context, "正在拍摄" + group + "组", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        currentTimeMillis = System.currentTimeMillis();
                        Uri fileUri = Helper.getOutputMediaFileUri(Long.toString(currentTimeMillis) + ".jpg"); //
                        // create a
                        // file to save the image
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                        ((Activity)getContext()).startActivityForResult(intent, Common.PHOTO_FOR_ENGINE_STANDARD);
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();

        dialog.show();
    }

    public void saveExteriorStandardPhoto() {
        Helper.setPhotoSize(Long.toString(currentTimeMillis) + ".jpg", 800);
        Helper.generatePhotoThumbnail(Long.toString(currentTimeMillis) + ".jpg", 400);

        PhotoEntity photoEntity = generatePhotoEntity();

        PhotoEngineLayout.photoListAdapter.addItem(photoEntity);
        PhotoEngineLayout.photoListAdapter.notifyDataSetChanged();

        photoShotCount[currentShotPart]++;

        starCamera();
    }

    private PhotoEntity generatePhotoEntity() {
        // 组织JsonString
        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();
            String currentPart = "";

            switch (currentShotPart) {
                case 0:
                    currentPart = "overview";
                    break;
                case 1:
                    currentPart = "left";
                    break;
                case 2:
                    currentPart = "right";
                    break;
                case 3:
                    currentPart = "other";
                    break;
            }

            photoJsonObject.put("part", currentPart);

            jsonObject.put("Group", "engineRoom");
            jsonObject.put("Part", "standard");
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("CarId", BasicInfoLayout.carId);
        } catch (JSONException e) {

        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName(Long.toString(currentTimeMillis) + ".jpg");
        if(!photoEntity.getFileName().equals(""))
            photoEntity.setThumbFileName(Long.toString(currentTimeMillis) + "_t.jpg");
        else
            photoEntity.setThumbFileName("");
        photoEntity.setJsonString(jsonObject.toString());
        String group = getResources().getStringArray(R.array.photoForEngineItems)[currentShotPart];
        photoEntity.setName(group);

        return photoEntity;
    }


    private ArrayList<PhotoEntity> generateDummyPhoto() {
        ArrayList<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();

        PhotoEntity photoEntity1 = new PhotoEntity();
        photoEntity1.setComment("还行");
        photoEntity1.setFileName("en1");
        photoEntity1.setName("机舱 - 左部");

        PhotoEntity photoEntity2 = new PhotoEntity();
        photoEntity2.setComment("一般");
        photoEntity2.setFileName("en2");
        photoEntity2.setName("机舱 - 右部");

        photoEntities.add(photoEntity1);
        photoEntities.add(photoEntity2);

        return photoEntities;
    }

    public String check() {
        return "";
    }
}
