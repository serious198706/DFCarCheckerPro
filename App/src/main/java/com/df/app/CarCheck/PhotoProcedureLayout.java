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
import com.df.app.service.customCamera.IPhotoProcessListener;
import com.df.app.service.customCamera.PhotoProcessManager;
import com.df.app.service.customCamera.PhotoTask;
import com.df.app.util.Common;
import com.df.app.util.Helper;
import com.df.app.util.MyAlertDialog;
import com.df.app.util.PhotoUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 13-12-26.
 *
 * 手续组照片列表
 */
public class PhotoProcedureLayout extends LinearLayout implements IPhotoProcessListener {
    private Context context;

    public static PhotoListAdapter photoListAdapter;
    public static int[] photoShotCount = {0, 0, 0, 0};
    public static long[] photoNames = {0, 0, 0, 0};
    private int currentShotPart;
    private long currentTimeMillis;
    private String[] groups = getResources().getStringArray(R.array.photoForProceduresItems);

    public PhotoProcedureLayout(Context context) {
        super(context);

        this.context = context;
        init();
    }

    public PhotoProcedureLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhotoProcedureLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.photo_procedure_list, this);

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
                                        for (int i = 0; i < Common.proceduresPartArray.length; i++) {
                                            if (name.equals(Common.proceduresPartArray[i])) {
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

        ListView procedureList = (ListView) findViewById(R.id.photo_procedure_list);
        procedureList.setAdapter(photoListAdapter);

        Button button = (Button)findViewById(R.id.photoButton);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startCamera();
            }
        });
    }

    /**
     * 拍摄手续组照片
     */
    private void startCamera() {
        PhotoProcessManager.getInstance().registPhotoProcessListener(this);

        String[] itemArray = PhotoUtils.getItemArray(context, R.array.photoForProceduresItems, photoShotCount);

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

                                    Helper.startCamera(context, groups[currentShotPart], currentTimeMillis);
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

                    Helper.startCamera(context, groups[currentShotPart], currentTimeMillis);
                }
            }
        });

        contentArea.addView(listView);

        setTextView(view, R.id.title, getResources().getString(R.string.takePhotoForProcedures));

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
            saveProceduresStandardPhoto();
        }

        startCamera();
    }

    /**
     * 保存手续组照片
     */
    public void saveProceduresStandardPhoto() {
        Helper.handlePhoto(Long.toString(currentTimeMillis) + ".jpg");

        if(photoShotCount[currentShotPart] == 0) {
            PhotoEntity photoEntity = PhotoUtils.generatePhotoEntity(context, currentTimeMillis,
                    groups[currentShotPart], "procedures", Common.proceduresPartArray[currentShotPart]);

            PhotoProcedureLayout.photoListAdapter.addItem(photoEntity);

            photoShotCount[currentShotPart] = 1;
        }
        // 如果该部位已经有照片（无论是网络的还是本地的），更新照片位置，更改action
        else {
            for(PhotoEntity photoEntity : PhotoProcedureLayout.photoListAdapter.getItems()) {
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

        PhotoProcedureLayout.photoListAdapter.notifyDataSetChanged();
    }

    /**
     * 提交前的检查
     * @return
     */
    public String check() {
        int sum = 0;

        for (int aPhotoShotCount : photoShotCount) {
            sum += aPhotoShotCount;
        }

        if(sum < Common.PHOTO_MIN_PROCEDURES) {
            return "procedures";
        } else {
            return "";
        }
    }
}
