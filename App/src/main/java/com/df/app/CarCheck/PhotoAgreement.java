package com.df.app.carCheck;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.Action;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.Adapter.PhotoListAdapter;
import com.df.app.service.customCamera.IPhotoProcessListener;
import com.df.app.service.customCamera.PhotoProcessManager;
import com.df.app.service.customCamera.PhotoTask;
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
public class PhotoAgreement extends LinearLayout implements IPhotoProcessListener {
    private Context context;

    // adapter
    public static PhotoListAdapter photoListAdapter;

    // 已拍摄的照片数量
    public static int photoShotCount = 0;
    public static long photoName;

    // 正在拍摄的部位
    private int currentShotPart;

    // 目前的文件名
    private long currentTimeMillis;

    public PhotoAgreement(Context context) {
        super(context);

        this.context = context;
        init();
    }

    public PhotoAgreement(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhotoAgreement(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.photo_other_list, this);

        List<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();

        photoListAdapter = new PhotoListAdapter(context, photoEntities, true, false, new PhotoListAdapter.OnAction() {
            @Override
            public void onDelete(int position) {
                String msg = "确定删除协议组 - 协议照片？";

                MyAlertDialog.showAlert(context, msg, R.string.alert, MyAlertDialog.BUTTON_STYLE_OK_CANCEL,
                        new Handler(new Handler.Callback() {
                            @Override
                            public boolean handleMessage(Message message) {
                                switch (message.what) {
                                    case MyAlertDialog.POSITIVE_PRESSED:
                                        if(CarCheckActivity.isModify()) {
                                            photoListAdapter.getItem(0).setModifyAction(Action.DELETE);
                                            JSONObject jsonObject = null;
                                            try {
                                                jsonObject = new JSONObject(photoListAdapter.getItem(0).getJsonString());
                                                jsonObject.put("Action", Action.DELETE);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            photoListAdapter.getItem(0).setJsonString(jsonObject.toString());
                                        } else {
                                            photoListAdapter.clear();
                                        }

                                        photoListAdapter.notifyDataSetChanged();
                                        photoShotCount = 0;
                                        photoName = 0;
                                        break;
                                }

                                return true;
                            }
                        }));
            }

            @Override
            public void onModifyComment(int position, String comment) {

            }

            @Override
            public void onShowPhoto(int position) {

            }
        });

        ListView otherList = (ListView) findViewById(R.id.photo_other_list);
        otherList.setAdapter(photoListAdapter);

        Button button = (Button)findViewById(R.id.photoButton);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startCamera();
            }
        });
    }

    /**
     * 拍摄协议组照片
     */
    private void startCamera() {
        PhotoProcessManager.getInstance().registPhotoProcessListener(this);

        if(photoShotCount == 1) {
            MyAlertDialog.showAlert(context, R.string.rePhoto, R.string.alert,
                    MyAlertDialog.BUTTON_STYLE_OK_CANCEL, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message message) {
                    switch (message.what) {
                        case MyAlertDialog.POSITIVE_PRESSED:
                            currentTimeMillis = photoName;

                            // 如果是网络图片或者没有图片
                            if(currentTimeMillis == Common.NO_PHOTO || currentTimeMillis == Common.WEB_PHOTO) {
                                currentTimeMillis = System.currentTimeMillis();
                            }

                            Helper.startCamera(context, "协议组", currentTimeMillis);
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

            Helper.startCamera(context, "协议组", currentTimeMillis);
        }
    }

    @Override
    public void onPhotoProcessFinish(List<PhotoTask> list) {
        if(list == null) {
            return;
        }

        PhotoTask photoTask = list.get(0);

        // 如果为完成状态
        if(photoTask.getState() == PhotoTask.STATE_COMPLETE) {
            saveAgreementPhoto();
        }
    }

    /**
     * 保存协议组照片
     */
    public void saveAgreementPhoto() {
        Helper.handlePhoto(Long.toString(currentTimeMillis) + ".jpg");

        if(photoShotCount == 0) {
            PhotoEntity photoEntity = generatePhotoEntity();
            PhotoAgreement.photoListAdapter.addItem(photoEntity);
            photoShotCount = 1;
        }
        // 如果该部位已经有照片（无论是网络的还是本地的），更新照片位置，更改action
        else {
            for(PhotoEntity photoEntity : PhotoAgreement.photoListAdapter.getItems()) {
                if(photoEntity.getName().equals("协议")) {
                    photoEntity.setFileName(Long.toString(currentTimeMillis) + ".jpg");
                    photoEntity.setThumbFileName(Long.toString(currentTimeMillis) + "_t.jpg");
                    photoEntity.setModifyAction(Action.MODIFY);

                    try {
                        JSONObject jsonObject = new JSONObject(photoEntity.getJsonString());
                        jsonObject.put("Action", photoEntity.getModifyAction());
                        photoEntity.setJsonString(jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
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
        if(photoShotCount < Common.PHOTO_MIN_AGREEMENT)
            return "agreement";
        else
            return "";
    }
}
