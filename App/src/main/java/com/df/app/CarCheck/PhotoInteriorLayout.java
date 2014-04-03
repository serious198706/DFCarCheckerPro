package com.df.app.carCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
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

import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 13-12-26.
 *
 * 内饰标准照列表
 */
public class PhotoInteriorLayout extends LinearLayout {
    private Context context;

    // 记录已经拍摄的照片数
    public static int[] photoShotCount = {0, 0, 0, 0, 0, 0};
    private long[] photoNames = {0, 0, 0, 0, 0, 0};

    // 记录当前拍摄的文件名
    private long currentTimeMillis;

    // 记录当前正在拍摄的部位
    private int currentShotPart;

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
        LayoutInflater.from(context).inflate(R.layout.photo_interior_list, this);
        
        ListView interiorList = (ListView) findViewById(R.id.photo_interior_list);

        List<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();

        photoListAdapter = new PhotoListAdapter(context, photoEntities, true, false);
        interiorList.setAdapter(photoListAdapter);

        Button startCameraButton = (Button)findViewById(R.id.photoButton);
        startCameraButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startCamera();
            }
        });
    }

    /**
     * 拍摄内饰标准照
     */
    private void startCamera() {
        String[] itemArray = getResources().getStringArray(R.array.interior_camera_item);

        int length = itemArray.length;

        for(int i = 0; i < length; i++) {
            itemArray[i] += " (";
            itemArray[i] += Integer.toString(photoShotCount[i]);
            itemArray[i] += ") ";
        }

        View view1 = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.popup_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view1)
                .create();

        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        final ListView listView = new ListView(view1.getContext());
        listView.setAdapter(new ArrayAdapter<String>(view1.getContext(), android.R.layout.simple_list_item_1, itemArray));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
                dialog.dismiss();
                currentShotPart = i;

                if(photoShotCount[currentShotPart] == 1) {
                    MyAlertDialog.showAlert(context, R.string.rePhoto, R.string.alert,
                            MyAlertDialog.BUTTON_STYLE_OK_CANCEL, new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message message) {
                            switch (message.what) {
                                case MyAlertDialog.POSITIVE_PRESSED:
                                    currentTimeMillis = photoNames[currentShotPart];

                                    Toast.makeText(context, "正在拍摄" + ((TextView)view).getText() + "组", Toast.LENGTH_LONG).show();
                                    Helper.startCamera(context, Long.toString(currentTimeMillis) + ".jpg", Common.PHOTO_FOR_INTERIOR_STANDARD);
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

                    photoNames[currentShotPart] = currentTimeMillis;

                    Toast.makeText(context, "正在拍摄" + ((TextView)view).getText() + "组", Toast.LENGTH_LONG).show();
                    Helper.startCamera(context, Long.toString(currentTimeMillis) + ".jpg", Common.PHOTO_FOR_INTERIOR_STANDARD);
                }
            }
        });
        contentArea.addView(listView);

        setTextView(view1, R.id.title, getResources().getString(R.string.interior_camera));

        dialog.show();
    }

    /**
     * 保存内饰标准照
     */
    public void saveInteriorStandardPhoto() {
        Helper.setPhotoSize(Long.toString(currentTimeMillis) + ".jpg", Common.PHOTO_WIDTH);
        Helper.generatePhotoThumbnail(Long.toString(currentTimeMillis) + ".jpg", Common.THUMBNAIL_WIDTH);

        if(photoShotCount[currentShotPart] == 0) {
            PhotoEntity photoEntity = generatePhotoEntity();
            PhotoInteriorLayout.photoListAdapter.addItem(photoEntity);
            photoShotCount[currentShotPart] = 1;
        }

        PhotoInteriorLayout.photoListAdapter.notifyDataSetChanged();

        startCamera();
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

        String group = getResources().getStringArray(R.array.interior_camera_item)[currentShotPart];
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

            String currentPart = Common.interiorPartArray[currentShotPart];

            photoJsonObject.put("part", currentPart);

            jsonObject.put("Group", "interior");
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
        int sum = 0;

        for(int i : photoShotCount) {
            sum += i;
        }

        if(sum < 1) {
            return "interior";
        } else {
            return "";
        }
    }
}
