package com.df.app.carCheck;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.df.app.R;
import com.df.app.service.util.AppCommon;
import com.df.library.entries.Action;
import com.df.library.entries.PhotoEntity;
import com.df.app.service.Adapter.PhotoListAdapter;
import com.df.library.carCheck.AddPhotoCommentActivity;
import com.df.library.entries.UserInfo;
import com.df.library.service.customCamera.IPhotoProcessListener;
import com.df.library.service.customCamera.PhotoProcessManager;
import com.df.library.service.customCamera.PhotoTask;
import com.df.library.util.Common;
import com.df.library.util.Helper;
import com.df.library.util.MyAlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-26.
 *
 * 缺陷组照片列表
 */
public class PhotoFaultLayout extends LinearLayout implements IPhotoProcessListener {

    public static PhotoListAdapter photoListAdapter;

    public static List<PhotoEntity> photoEntities;

    private Context context;

    // 目前的文件名
    private long currentTimeMillis;

    public PhotoFaultLayout(Context context) {
        super(context);

        this.context = context;
        init();
    }

    public PhotoFaultLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhotoFaultLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        photoEntities = new ArrayList<PhotoEntity>();

        LayoutInflater.from(context).inflate(R.layout.photo_fault_list, this);

        List<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();

        photoListAdapter = new PhotoListAdapter(context, photoEntities, true, true, false, new PhotoListAdapter.OnAction() {
            @Override
            public void onDelete(final int position) {
                String msg = "确定删除缺陷组 - " + photoListAdapter.getItem(position).getName() + "照片？";

                MyAlertDialog.showAlert(context, msg, R.string.alert, MyAlertDialog.BUTTON_STYLE_OK_CANCEL,
                        new Handler(new Handler.Callback() {
                            @Override
                            public boolean handleMessage(Message message) {
                                switch (message.what) {
                                    case MyAlertDialog.POSITIVE_PRESSED: {
                                        if(CarCheckActivity.isModify()) {
                                            photoListAdapter.getItem(position).setModifyAction(Action.DELETE);

                                            try {
                                                JSONObject jsonObject = new JSONObject(photoListAdapter.getItem(position).getJsonString());
                                                jsonObject.put("Action", Action.DELETE);
                                                photoListAdapter.getItem(position).setJsonString(jsonObject.toString());
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            photoListAdapter.removeItem(position);
                                        }
                                    }

                                        photoListAdapter.notifyDataSetChanged();
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

        ListView faultList = (ListView) findViewById(R.id.photo_fault_list);
        faultList.setAdapter(photoListAdapter);
        faultList.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        Button button = (Button)findViewById(R.id.photoButton);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                starCamera();
            }
        });
    }

    /**
     * 拍摄其他缺陷组照片
     */
    private void starCamera() {
        PhotoProcessManager.getInstance().registPhotoProcessListener(this);

        // 使用当前毫秒数当作照片名
        currentTimeMillis = System.currentTimeMillis();

        Helper.startCamera(context, AppCommon.photoDirectory, "其他缺陷", currentTimeMillis);
    }

    @Override
    public void onPhotoProcessFinish(List<PhotoTask> list) {
        if(list == null) {
            return;
        }

        PhotoTask photoTask = list.get(0);

        // 如果为完成状态
        if(photoTask.getState() == PhotoTask.STATE_COMPLETE)
            addCommentForOtherFaultPhoto();

    }

    /**
     * 保存其他缺陷组照片
     */
    public void saveOtherFaultPhoto(String comment) {
        PhotoEntity photoEntity = generatePhotoEntity(comment);

        photoEntities.add(photoEntity);

        photoListAdapter.addItem(photoEntity);
        photoListAdapter.notifyDataSetChanged();
    }

    /**
     * 给其他缺陷组照片添加备注
     */
    public void addCommentForOtherFaultPhoto() {
        Helper.handlePhoto(AppCommon.photoDirectory, Long.toString(currentTimeMillis) + ".jpg");

        Intent intent = new Intent(context, AddPhotoCommentActivity.class);
        intent.putExtra("fileName", AppCommon.photoDirectory + Long.toString(currentTimeMillis) + ".jpg");
        ((Activity)context).startActivityForResult(intent, Common.ADD_COMMENT_FOR_OTHER_FAULT_PHOTO);
    }

    /**
     * 生成photoEntity
     * @return
     */
    private PhotoEntity generatePhotoEntity(String comment) {
        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName(Long.toString(currentTimeMillis) + ".jpg");
        if(!photoEntity.getFileName().equals(""))
            photoEntity.setThumbFileName(Long.toString(currentTimeMillis) + "_t.jpg");
        else
            photoEntity.setThumbFileName("");

        photoEntity.setName("其他缺陷");
        photoEntity.setComment(comment);
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

            photoJsonObject.put("comment", comment);

            jsonObject.put("Group", "otherFault");
            jsonObject.put("Part", "standard");
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", UserInfo.getInstance().getId());
            jsonObject.put("Key", UserInfo.getInstance().getKey());
            jsonObject.put("CarId", BasicInfoLayout.carId);
            jsonObject.put("Action", photoEntity.getModifyAction());
            jsonObject.put("Index", photoEntity.getIndex());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }
}
