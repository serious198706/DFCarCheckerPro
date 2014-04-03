package com.df.app.carCheck;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
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
import com.df.app.entries.Action;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.Adapter.PhotoListAdapter;
import com.df.app.util.Common;
import com.df.app.util.Helper;
import com.df.app.util.MyAlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-26.
 *
 * 协议组照片列表
 */
public class PhotoAgreement extends LinearLayout {
    private Context context;

    // adapter
    public static PhotoListAdapter photoListAdapter;

    // 已拍摄的照片数量
    public static int photoShotCount = 0;
    private long photoName;

    // 正在拍摄的部位
    private int currentShotPart;

    // 目前的文件名
    private long currentTimeMillis;

    public PhotoAgreement(Context context) {
        super(context);

        this.context = context;
        init(context);
    }

    public PhotoAgreement(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PhotoAgreement(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.photo_other_list, this);

        List<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();

        photoListAdapter = new PhotoListAdapter(context, photoEntities, true, false);

        ListView otherList = (ListView) findViewById(R.id.photo_other_list);
        otherList.setAdapter(photoListAdapter);

        Button button = (Button)findViewById(R.id.photoButton);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                starCamera();
            }
        });
    }

    /**
     * 拍摄协议组照片
     */
    private void starCamera() {
        if(photoShotCount == 1) {
            MyAlertDialog.showAlert(context, R.string.rePhoto, R.string.alert,
                    MyAlertDialog.BUTTON_STYLE_OK_CANCEL, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message message) {
                    switch (message.what) {
                        case MyAlertDialog.POSITIVE_PRESSED:
                            currentTimeMillis = photoName;

                            Toast.makeText(context, "正在拍摄协议组", Toast.LENGTH_LONG).show();
                            Helper.startCamera(context, Long.toString(currentTimeMillis) + ".jpg", Common.PHOTO_FOR_AGREEMENT_STANDARD);
                            break;
                        case MyAlertDialog.NEGATIVE_PRESSED:
                            break;
                    }

                    return true;
                }
            }));
        } else {
            // 使用当前毫秒数当作照片名
            currentTimeMillis = System.currentTimeMillis();

            photoName = currentTimeMillis;

            Toast.makeText(context, "正在拍摄协议组", Toast.LENGTH_LONG).show();
            Helper.startCamera(context, Long.toString(currentTimeMillis) + ".jpg", Common.PHOTO_FOR_AGREEMENT_STANDARD);
        }
    }

    /**
     * 保存协议组照片
     */
    public void saveAgreementPhoto() {
        Helper.setPhotoSize(Long.toString(currentTimeMillis) + ".jpg", Common.PHOTO_WIDTH);
        Helper.generatePhotoThumbnail(Long.toString(currentTimeMillis) + ".jpg", Common.THUMBNAIL_WIDTH);

        if(photoShotCount == 0) {
            PhotoEntity photoEntity = generatePhotoEntity();
            PhotoAgreement.photoListAdapter.addItem(photoEntity);
            photoShotCount = 1;
        }

        PhotoAgreement.photoListAdapter.notifyDataSetChanged();
    }

    /**
     * 生成photoEntity
     * @return
     */
    private PhotoEntity generatePhotoEntity() {
        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName(Long.toString(currentTimeMillis) + ".jpg");
        if(!photoEntity.getFileName().equals(""))
            photoEntity.setThumbFileName(Long.toString(currentTimeMillis) + "_t.jpg");
        else
            photoEntity.setThumbFileName("");

        String group = getResources().getStringArray(R.array.photoForOtherItems)[0];
        photoEntity.setName(group);
        photoEntity.setIndex(PhotoLayout.photoIndex++);

        // 如果是走了这段代码，则一定是添加照片
        // 如果是修改模式，则Action就是add
        if(CarCheckActivity.isModify()) {
            photoEntity.setModifyAction(Action.ADD);
        } else {
            photoEntity.setModifyAction(Action.MODIFY);
        }

        // 组织JsonString
        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();
            String currentPart = "协议";

            photoJsonObject.put("part", currentPart);

            jsonObject.put("Group", "agreement");
            jsonObject.put("Part", "standard");
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("CarId", BasicInfoLayout.carId);
            jsonObject.put("Action", photoEntity.getModifyAction());
            jsonObject.put("Index", photoEntity.getIndex());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }


    /**
     * 提交前的检查
     * @return
     */
    public String check() {
        if(photoShotCount == 0)
            return "agreement";
        else
            return "";
    }
}
