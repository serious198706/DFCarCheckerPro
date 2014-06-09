package com.df.kia.carCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;

import com.df.kia.R;
import com.df.library.entries.Action;
import com.df.library.entries.PhotoEntity;
import com.df.kia.service.Adapter.PhotoListAdapter;
import com.df.library.service.customCamera.IPhotoProcessListener;
import com.df.library.service.customCamera.PhotoProcessManager;
import com.df.library.service.customCamera.PhotoTask;
import com.df.kia.service.util.AppCommon;
import com.df.library.util.Common;
import com.df.library.util.Helper;
import com.df.library.util.MyAlertDialog;
import com.df.library.util.PhotoUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.df.library.util.Helper.setTextView;

/**
 * Created by 岩 on 13-12-26.
 *
 * 内饰标准照列表
 */
public class PhotoInteriorLayout extends LinearLayout implements IPhotoProcessListener {
    private Context context;

    // 记录已经拍摄的照片数
    public static int[] photoShotCount = {0, 0, 0, 0, 0, 0};
    public static long[] photoNames = {0, 0, 0, 0, 0, 0};

    // 记录当前拍摄的文件名
    private long currentTimeMillis;

    // 记录当前正在拍摄的部位
    private int currentShotPart;

    private String[] groups = getResources().getStringArray(R.array.photoForInteriorItems);
    
    public static PhotoListAdapter photoListAdapter;
    

    public PhotoInteriorLayout(Context context) {
        super(context);

        this.context = context;
        init();
    }

    public PhotoInteriorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhotoInteriorLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.photo_interior_list, this);
        
        ListView interiorList = (ListView) findViewById(R.id.photo_interior_list);

        List<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();

        photoListAdapter = new PhotoListAdapter(context, photoEntities, true, false, new PhotoListAdapter.OnAction() {
            @Override
            public void onDelete(final int position) {
                PhotoEntity photoEntity = photoListAdapter.getItem(position);

                final String name = photoEntity.getName();

                String msg = "确定删除内饰组 - " + name + "照片？";

                MyAlertDialog.showAlert(context, msg, R.string.alert, MyAlertDialog.BUTTON_STYLE_OK_CANCEL,
                        new Handler(new Handler.Callback() {
                            @Override
                            public boolean handleMessage(Message message) {
                                switch (message.what) {
                                    case MyAlertDialog.POSITIVE_PRESSED:{
                                        // 如果为修改模式，则将该照片的action置为delete，不真正删除
                                        if(CarCheckActivity.isModify()) {
                                            photoListAdapter.getItem(position).setModifyAction(Action.DELETE);

                                            try {
                                                JSONObject jsonObject = new JSONObject(photoListAdapter.getItem(position).getJsonString());
                                                jsonObject.put("Action", Action.DELETE);
                                                photoListAdapter.getItem(position).setJsonString(jsonObject.toString());
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        // 正常模式则直接删除图片
                                        else {
                                            photoListAdapter.removeItem(position);
                                        }

                                        int index = 0;

                                        // 找到要删除的照片的位置
                                        for (int i = 0; i < Common.interiorPartArray.length; i++) {
                                            if (name.equals(Common.interiorPartArray[i])) {
                                                index = i;
                                            }
                                        }

                                        // 将对应的照片名称与计数器归零
                                        photoNames[index] = 0;
                                        photoShotCount[index] = 0;
                                        photoListAdapter.notifyDataSetChanged();
                                    }
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
        PhotoProcessManager.getInstance().registPhotoProcessListener(this);

        String[] itemArray = PhotoUtils.getItemArray(context, R.array.photoForInteriorItems, photoShotCount);

        View view = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.popup_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .create();

        TableLayout contentArea = (TableLayout)view.findViewById(R.id.contentArea);
        final ListView listView = new ListView(view.getContext());
        listView.setAdapter(new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1, itemArray));
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

                                    // 如果是网络图片或者没有图片
                                    if(currentTimeMillis == Common.NO_PHOTO || currentTimeMillis == Common.WEB_PHOTO) {
                                        currentTimeMillis = System.currentTimeMillis();
                                    }

                                    Helper.startCamera(context, AppCommon.photoDirectory, groups[currentShotPart], currentTimeMillis);
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

                    Helper.startCamera(context, AppCommon.photoDirectory, groups[currentShotPart], currentTimeMillis);
                }
            }
        });
        contentArea.addView(listView);

        setTextView(view, R.id.title, getResources().getString(R.string.interior_camera));

        dialog.show();
    }

    @Override
    public void onPhotoProcessFinish(List<PhotoTask> list) {
        if(list == null) {
            return;
        }

        PhotoTask photoTask = list.get(0);

        // 如果为完成状态
        if(photoTask.getState() == PhotoTask.STATE_COMPLETE) {
            saveInteriorStandardPhoto();
        }

        startCamera();
    }
    /**
     * 保存内饰标准照
     */
    public void saveInteriorStandardPhoto() {
        Helper.handlePhoto(AppCommon.photoDirectory, Long.toString(currentTimeMillis) + ".jpg");

        if(photoShotCount[currentShotPart] == 0) {
            PhotoEntity photoEntity = PhotoUtils.generatePhotoEntity(currentTimeMillis,
                    groups[currentShotPart], "interior", "standard", Common.interiorPartArray[currentShotPart],
                    PhotoLayout.photoIndex++, BasicInfoLayout.carId, CarCheckActivity.isModify());
            
            PhotoInteriorLayout.photoListAdapter.addItem(photoEntity);
            photoShotCount[currentShotPart] = 1;
        }
        // 如果该部位已经有照片（无论是网络的还是本地的），更新照片位置，更改action
        else {
            for(PhotoEntity photoEntity : PhotoInteriorLayout.photoListAdapter.getItems()) {
                if(photoEntity.getName().equals(groups[currentShotPart])) {
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

        PhotoInteriorLayout.photoListAdapter.notifyDataSetChanged();
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

        if(sum < Common.PHOTO_MIN_INTERIOR) {
            return "interior";
        } else {
            return "";
        }
    }
}
