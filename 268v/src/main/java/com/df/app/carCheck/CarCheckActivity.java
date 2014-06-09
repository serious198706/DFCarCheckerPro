package com.df.app.carCheck;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.df.app.carsChecked.CarsCheckedListActivity;
import com.df.app.R;
import com.df.app.service.util.AppCommon;
import com.df.library.entries.Action;
import com.df.library.entries.Issue;
import com.df.library.entries.PhotoEntity;
import com.df.library.entries.PosEntity;
import com.df.library.asyncTask.CommitDataTask;
import com.df.app.service.GeneratePhotoEntitiesTask;
import com.df.library.asyncTask.SaveDataTask;
import com.df.library.asyncTask.UploadPictureTask;
import com.df.library.service.views.NaviDialog;
import com.df.library.util.Common;
import com.df.library.entries.UserInfo;
import com.df.library.util.MyAlertDialog;
import com.df.library.util.PhotoParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.df.library.util.Helper.showView;

/**
 * Created by 岩 on 13-12-20.
 *
 * 主activity，承载基本信息、事故排查、综合检查、拍摄照片四个模块
 */
public class CarCheckActivity extends Activity {
    // 回头再说（考虑给每张照片添加上传成功的标志）
    private boolean pictureUploaded = true;

    private static boolean modify;

    private BasicInfoLayout basicInfoLayout;
    private AccidentCheckLayout accidentCheckLayout;
    private IntegratedCheckLayout integratedCheckLayout;
    private PhotoLayout photoLayout;
    private TransactionNotesLayout transactionNotesLayout;

    // 导航按钮
    private Button navigateButton;

    private int carId;
    private List<PhotoEntity> photoEntities;
    public static boolean saved = false;

    private List<String> filesDelete;

    // 按钮内容与按钮id的map
    SparseArray<String> tabMap = new SparseArray<String>();

    private Class activity;

    // 最终json串
    private JSONObject jsonObject;
    private boolean transactionNotesCommitted = false;

    NaviDialog naviDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_check);

        // 填充各部分的内容
        Bundle bundle = getIntent().getExtras();

        // 获取车辆详细信息
        final String jsonString = bundle.getString("jsonString");
        carId = bundle.getInt("carId");
        activity = (Class)getIntent().getSerializableExtra("activity");
        modify = bundle.getBoolean("modify");

        photoEntities = new ArrayList<PhotoEntity>();

        tabMap.put(R.id.basicInfo, "基本信息");
        tabMap.put(R.id.accidentCheck, "事故排查");
        tabMap.put(R.id.integratedCheck, "综合检查");
        tabMap.put(R.id.photo, "照片拍摄");
        tabMap.put(R.id.transactionNotes, "交易备注");

        naviDialog = new NaviDialog(this, new NaviDialog.OnChoice() {
            @Override
            public void onChoise(int index) {
                selectTab(index);
                naviDialog.dismiss();
            }
        });

        Window dialogWindow = naviDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);

        lp.x = 0; // 新位置X坐标
        lp.y = 100; // 新位置Y坐标
        lp.dimAmount = 0f; // 背景不变暗

        dialogWindow.setAttributes(lp);

        // 导航按钮
        navigateButton = (Button) findViewById(R.id.buttonNavi);
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                naviDialog.show();
            }
        });

        // 基本信息模块
        basicInfoLayout = (BasicInfoLayout)findViewById(R.id.basicInfo);
        basicInfoLayout.setUpdateUiListener(new BasicInfoLayout.OnGetCarSettings() {
            @Override
            public void onGetCarSettings() {
                accidentCheckLayout.updatePreviews();
                integratedCheckLayout.updateUi();
            }
        });

        // 事故排查模块
        accidentCheckLayout = (AccidentCheckLayout)findViewById(R.id.accidentCheck);

        // 综合检查模块
        integratedCheckLayout = (IntegratedCheckLayout)findViewById(R.id.integratedCheck);

        // 拍摄照片模块
        photoLayout = (PhotoLayout)findViewById(R.id.photo);

        // 交易备注模块
        transactionNotesLayout = (TransactionNotesLayout)findViewById(R.id.transactionNotes);
        transactionNotesLayout.updateUi(carId, modify);

        // 设置当前标题
        navigateButton.setText( getString(R.string.title_basicInfo));

        // 返回主页面按钮
        Button homeButton = (Button)findViewById(R.id.buttonHome);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quitConfirm();
            }
        });

        // 提交按钮
        Button commitButton = (Button)findViewById(R.id.buttonCommit);
        commitButton.setOnClickListener(mCommitListener);

        // 保存按钮
        Button saveButton = (Button)findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(generateJsonString(false))
                    // 保存检测数据
                    saveData();
                else
                    Toast.makeText(CarCheckActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
            }
        });

        // 测试按钮
        Button comB = (Button)findViewById(R.id.com);
        comB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commitData();
            }
        });
        comB.setVisibility(View.GONE);

        // 如果是测试模式
        if(modify) {
            showView(getWindow().getDecorView(), R.id.buttonSave, false);
        }

        // 填入已保存的或者已检测的数据
        fillInData(carId, jsonString);
    }

    // 提交按钮监听事件
    View.OnClickListener mCommitListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String pass = checkAllFields();

            // 如果所有项目都已经填写完毕
            if(pass.equals("")) {
                MyAlertDialog.showAlert(CarCheckActivity.this, R.string.commitMsg, R.string.alert, MyAlertDialog.BUTTON_STYLE_OK_CANCEL,
                        new Handler(new Handler.Callback() {
                            @Override
                            public boolean handleMessage(Message message) {
                                switch (message.what) {
                                    case MyAlertDialog.POSITIVE_PRESSED:
                                        if (modify)
                                            findModifiedPicture();
                                        else
                                            generatePhotoEntities();
                                        break;
                                    case MyAlertDialog.NEGATIVE_PRESSED:
                                        break;
                                }

                                return true;
                            }
                        }));
            } else {
                if(pass.equals("accidentCheck")) {
                    Toast.makeText(CarCheckActivity.this, "未完成事故检测！", Toast.LENGTH_SHORT).show();
                    selectTab(1);
                } else if(pass.equals("leftFront") || pass.equals("rightFront") ||
                        pass.equals("leftRear") || pass.equals("rightRear") ||
                        pass.equals("spare") || pass.equals("edits")) {
                    selectTab(2);
                } else if(pass.equals("exterior") || pass.contains("interior") ||
                        pass.contains("engine") || pass.equals("procedures") || pass.equals("agreements") ||
                        pass.contains("exterior")) {
                    selectTab(3);
                } else if(pass.equals("coop")) {
                    selectTab(2);
                }
            }
        }
    };

    /**
     * 检查所有必填字段
     *
     * 使用字符串来定位缺失的位置
     */
    private String checkAllFields() {
        String currentField;

        // 检查事故检查模块
        currentField = accidentCheckLayout.checkAllFields();

        if(currentField.equals("")) {
            currentField = integratedCheckLayout.checkAllFields();
        }

        if(currentField.equals("")) {
            currentField = photoLayout.checkAllFields();
        }

        return currentField;
    }


    /**
     * 将所有的照片实体进行聚合
     */
    // 添加所有的photoEntity
    private void generatePhotoEntities() {
        GeneratePhotoEntitiesTask generatePhotoEntitiesTask = new GeneratePhotoEntitiesTask(this, photoEntities,
                accidentCheckLayout, integratedCheckLayout, true, new GeneratePhotoEntitiesTask.OnGenerateFinished() {
            @Override
            public void onFinished(List<PhotoEntity> photoEntities) {
//                // 找出所有需要删除的文件
//                filesDelete = new ArrayList<String>();

//                for(PhotoEntity photoEntity : photoEntities) {
//                    // 不需要判断文件名
//                    filesDelete.add(photoEntity.getFileName());
//                    filesDelete.add(photoEntity.getThumbFileName());
//                }

                // 2.上传图片
                uploadPictures(photoEntities);
            }

            @Override
            public void onFailed() {
                Toast.makeText(CarCheckActivity.this, "处理失败！", Toast.LENGTH_SHORT).show();
                Log.d(AppCommon.TAG, "生成草图失败！");
            }
        });

        generatePhotoEntitiesTask.execute();
    }

    /**
     * 上传所有照片
     */
    private void uploadPictures(List<PhotoEntity> photoEntities) {
        UploadPictureTask uploadPictureTask = new UploadPictureTask(CarCheckActivity.this, AppCommon.photoDirectory, photoEntities, new UploadPictureTask.UploadFinished() {
            @Override
            public void onFinish() {
                // 4.提交检测数据
                commitData();
            }

            @Override
            public void onCancel() {
                // 取消上传过程
                Toast.makeText(CarCheckActivity.this, "取消上传", Toast.LENGTH_SHORT).show();
            }
        });
        uploadPictureTask.execute();
    }

    /**
     * 在修改模式中，找到需要修改的图片（Action不为NORMAL的图片）
     */
    private void findModifiedPicture() {
        GeneratePhotoEntitiesTask generatePhotoEntitiesTask = new GeneratePhotoEntitiesTask(this, photoEntities,
                accidentCheckLayout, integratedCheckLayout, true, new GeneratePhotoEntitiesTask.OnGenerateFinished() {
            @Override
            public void onFinished(List<PhotoEntity> photoEntities) {
                // 找出需要修改的图片
                List<PhotoEntity> photoEntitiesMod = new ArrayList<PhotoEntity>();

                for(PhotoEntity photoEntity : photoEntities) {
                    if(!photoEntity.getModifyAction().equals(Action.NORMAL)) {
                        photoEntitiesMod.add(photoEntity);
                    }
                }

                // 上传
                uploadPictures(photoEntitiesMod);
            }

            @Override
            public void onFailed() {
                Toast.makeText(CarCheckActivity.this, "处理失败！", Toast.LENGTH_SHORT).show();
                Log.d(AppCommon.TAG, "生成草图失败！");
            }
        });

        generatePhotoEntitiesTask.execute();
    }

    /**
     * 提交检测数据
     */
    private void commitData() {
        // 3.生成所有检测的Json数据
        generateJsonString(true);

        final CommitDataTask commitDataTask = new CommitDataTask(CarCheckActivity.this, carId, new CommitDataTask.OnCommitDataFinished() {
            @Override
            public void onFinished(String result) {
                Toast.makeText(CarCheckActivity.this, result, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CarCheckActivity.this, CarsCheckedListActivity.class);
                startActivity(intent);
                finish();

                // 如果存在临时保存的数据，则删除之
                try {
                    File file = new File(AppCommon.savedDirectory + Integer.toString(carId));
                    if(file.exists()) {
                        file.delete();
                    }

                    //TODO 删除相关文件
//                    DeleteFiles deleteFiles = new DeleteFiles(AppCommon.photoDirectory, filesDelete);
//                    deleteFiles.deleteFiles();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(String result) {
                Toast.makeText(CarCheckActivity.this, result, Toast.LENGTH_SHORT).show();
                transactionNotesCommitted = false;
            }
        });

        transactionNotesLayout.commitTransactionNotes(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    // 交易备注提交成功
                    case 0:
                        if(!transactionNotesCommitted) {
                            commitDataTask.execute(jsonObject);
                            transactionNotesCommitted = true;
                        }

                        break;
                    // 交易备注提交失败
                    case 1:
                        Toast.makeText(CarCheckActivity.this, "交易备注提交失败！", Toast.LENGTH_SHORT).show();
                        break;
                }

                return true;
            }
        }));
    }

    /**
     * 临时保存
     *
     * 临时保存的策略是将数据全部保存到本地，以carId命名，包括检测信息等，还有照片与点的对应关系等等
     *
     * 以json形式保存成文件
     */
    private void saveData() {
        GeneratePhotoEntitiesTask generatePhotoEntitiesTask = new GeneratePhotoEntitiesTask(this, photoEntities,
                accidentCheckLayout, integratedCheckLayout, false, new GeneratePhotoEntitiesTask.OnGenerateFinished() {
            @Override
            public void onFinished(List<PhotoEntity> photoEntities) {
                SaveDataTask saveDataTask = new SaveDataTask(CarCheckActivity.this, carId, AppCommon.savedDirectory, photoEntities, new SaveDataTask.OnSaveDataFinished() {
                    @Override
                    public void onFinished() {
                        Toast.makeText(CarCheckActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
                        Log.d(AppCommon.TAG, "保存成功！");
                    }

                    @Override
                    public void onFailed() {
                        Toast.makeText(CarCheckActivity.this, "保存失败！", Toast.LENGTH_SHORT).show();
                        Log.d(AppCommon.TAG, "保存失败！");
                    }
                });
                saveDataTask.execute(jsonObject);
            }

            @Override
            public void onFailed() {
                Toast.makeText(CarCheckActivity.this, "处理失败！", Toast.LENGTH_SHORT).show();
                Log.d(AppCommon.TAG, "生成草图失败！");
            }
        });

        generatePhotoEntitiesTask.execute();
    }

    /**
     * 在切换大项时自动保存
     */
    private void autoSaveData() {
        GeneratePhotoEntitiesTask generatePhotoEntitiesTask = new GeneratePhotoEntitiesTask(this, photoEntities,
                accidentCheckLayout, integratedCheckLayout, false, new GeneratePhotoEntitiesTask.OnGenerateFinished() {
            @Override
            public void onFinished(List<PhotoEntity> photoEntities) {
                SaveDataTask saveDataTask = new SaveDataTask(CarCheckActivity.this, carId, AppCommon.savedDirectory, photoEntities, new SaveDataTask.OnSaveDataFinished() {
                    @Override
                    public void onFinished() {
                        Log.d(AppCommon.TAG, "自动保存成功！");
                    }

                    @Override
                    public void onFailed() {
                        Log.d(AppCommon.TAG, "自动保存失败！");
                    }
                });
                saveDataTask.execute(jsonObject);
            }

            @Override
            public void onFailed() {
                Log.d(AppCommon.TAG, "生成草图失败！");
            }
        });

        generatePhotoEntitiesTask.execute();
    }

    /**
     * 根据选择的按钮，切换不同的模块
     */
    private void selectTab(int index) {
        String title = tabMap.valueAt(index);
        navigateButton.setText(title);

        int length = tabMap.size();

        for(int i = 0; i < length; i++) {
            if(i == index) {
                findViewById(tabMap.keyAt(i)).setVisibility(View.VISIBLE);
            } else {
                findViewById(tabMap.keyAt(i)).setVisibility(View.GONE);
            }
        }

        if(!modify) {
            // 每切换一次大标签，自动保存，修改时不保存
            generateJsonString(false);
            autoSaveData();
        }
    }

    /**
     * 处理子页面启动activity时的返回值，主要处理拍摄照片的返回值、添加照片备注页面、蓝牙请求页面的返回值
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        switch (requestCode) {
            // 外观缺陷照
            case Common.ENTER_EXTERIOR_PAINT:
                integratedCheckLayout.updateExteriorPreview();
                break;
            // 内饰缺陷照
            case Common.ENTER_INTERIOR_PAINT:
                integratedCheckLayout.updateInteriorPreview();
                break;
            // 事故查勘照片添加备注
            case Common.ADD_COMMENT_FOR_ACCIDENT_FRONT_PHOTO:
            case Common.ADD_COMMENT_FOR_ACCIDENT_REAR_PHOTO:
                {
                    Bundle bundle = data.getExtras();

                    PosEntity posEntity = accidentCheckLayout.getPosEntity(requestCode);
                    posEntity.setComment(bundle.getString("COMMENT"));

                    accidentCheckLayout.saveAccidentPhoto(requestCode);
                }
                break;
            // 外观内饰照片备注
            case Common.ADD_COMMENT_FOR_EXTERIOR_AND_INTERIOR_PHOTO:
                break;
            // 修改备注
            case Common.MODIFY_COMMENT: {
                    Bundle bundle = data.getExtras();

                    if(bundle.containsKey("COMMENT"))
                        accidentCheckLayout.modifyComment(bundle.getString("COMMENT"));
                }
                break;
            // 其他缺陷添加备注
            case Common.ADD_COMMENT_FOR_OTHER_FAULT_PHOTO: {
                    Bundle bundle = data.getExtras();

                    if(bundle.containsKey("COMMENT"))
                        photoLayout.saveOtherFaultPhoto(bundle.getString("COMMENT"));
                }
                break;
            // 打开蓝牙请求页面的返回值
            case Common.REQUEST_ENABLE_BT:
                // 如果允许打开蓝牙
                if (resultCode == Activity.RESULT_OK) {
                    accidentCheckLayout.setupBluetoothService();
                }
                // 不允许打开蓝牙（真是有病。。）
                else {
                    Toast.makeText(CarCheckActivity.this, R.string.need_bluetooth, Toast.LENGTH_SHORT).show();
                    accidentCheckLayout.stopBluetoothService();
                }
                break;
        }
    }

    /**
     * 生成最终的JSON串
     * @param b
     */
    private boolean generateJsonString(boolean b) {
        try {
            jsonObject = new JSONObject();
            JSONObject features = basicInfoLayout.generateJSONObject();
            JSONObject accident = accidentCheckLayout.generateJSONObject(b);
            JSONObject conditions = integratedCheckLayout.generateJSONObject();

            jsonObject.put("features", features);
            jsonObject.put("accident", accident);
            jsonObject.put("conditions", conditions);
            jsonObject.put("checkUserId", UserInfo.getInstance().getId());
            jsonObject.put("checkUserName", UserInfo.getInstance().getName());
            jsonObject.put("checkCooperatorId", integratedCheckLayout.getCooperatorId());
            jsonObject.put("checkCooperatorName", integratedCheckLayout.getCooperatorName());

            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     */
    private void fillInData(int carId, String jsonString) {
        try {
            final JSONObject jsonObject = new JSONObject(jsonString);
            if(jsonObject.has("accident")) {
                saved = true;
            }

            // 更新手续页面（一定会有）
            JSONObject features = jsonObject.getJSONObject("features");
            basicInfoLayout.fillInData(carId, features, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message message) {
                    // 如果有综合检查节点，则更新综合检查页面
                    // 为什么要用Handler？因为在填入配置信息的时候，要先填入OptionsLayout的内容，再填入Integrated1Layout的内容，
                    // 先后顺序是固定的，等待OptionsLayout填写完成后，再填写Integrated1Layout
                    if(jsonObject.has("conditions")) {
                        integratedCheckLayout.fillInData(jsonObject);
                    }

                    return false;
                }
            }));

            // 如果有事故节点，则更新事故页面
            if(jsonObject.has("accident")) {
                JSONObject accident = jsonObject.getJSONObject("accident");

                if(modify) {
                    accidentCheckLayout.fillInData(accident, jsonObject.getJSONObject("photos"), new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message message) {
                            accidentCheckLayout.showIssueAndResultTabs();
                            return true;
                        }
                    }));
                } else {
                    accidentCheckLayout.fillInData(accident, new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message message) {
                            accidentCheckLayout.showIssueAndResultTabs();
                            return true;
                        }
                    }));
                }
            }

            // 如果有照片节点，则更新照片list
            if(jsonObject.has("photos")) {
                List<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();

                if(jsonObject.get("photos") instanceof JSONArray) {
                    JSONArray jsonArray = jsonObject.getJSONArray("photos");

                    int length = jsonArray.length();

                    for(int i = 0; i < length; i++) {
                        JSONObject entity = jsonArray.getJSONObject(i);
                        PhotoEntity photoEntity = new PhotoEntity();
                        photoEntity.setJsonString(entity.getString("jsonString"));
                        photoEntity.setComment(entity.has("comment") ? entity.getString("comment") : "");
                        photoEntity.setName(entity.has("name") ? entity.getString("name") : "");
                        photoEntity.setFileName(entity.has("fileName") ? entity.getString("fileName") : "");
                        photoEntity.setThumbFileName(entity.has("thumbFileName") ? entity.getString("thumbFileName") : "");
                        photoEntity.setIndex(entity.has("index") ? entity.getInt("index") : 0);

                        photoEntities.add(photoEntity);
                    }
                } else {
                    parsePhoto(jsonObject.getJSONObject("photos"), photoEntities);
                }

                for(PhotoEntity photoEntity : photoEntities) {
                    addPhotoEntityToGroup(photoEntity);
                }
            }

            // 如果有从检名字节点
            if(jsonObject.has("checkCooperatorName")) {
                integratedCheckLayout.fillInData(jsonObject.getString("checkCooperatorName"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改时，解析图片
     */
    private void parsePhoto(JSONObject photos, List<PhotoEntity> photoEntities)  throws JSONException {
        List<PhotoEntity> exteriorPhotos = new ArrayList<PhotoEntity>();
        List<PhotoEntity> interiorPhotos = new ArrayList<PhotoEntity>();
        List<PhotoEntity> faultPhotos = new ArrayList<PhotoEntity>();
        List<PhotoEntity> proceduresPhotos = new ArrayList<PhotoEntity>();
        List<PhotoEntity> enginePhotos = new ArrayList<PhotoEntity>();
        List<PhotoEntity> agreementPhotos = new ArrayList<PhotoEntity>();

        PhotoParser.parsePhotoData(CarCheckActivity.this, carId, photos,
                exteriorPhotos, interiorPhotos, faultPhotos, proceduresPhotos, enginePhotos, agreementPhotos);

        photoEntities.addAll(exteriorPhotos);
        photoEntities.addAll(interiorPhotos);
        photoEntities.addAll(faultPhotos);
        photoEntities.addAll(proceduresPhotos);
        photoEntities.addAll(enginePhotos);
        photoEntities.addAll(agreementPhotos);
    }

    /**
     * 对PhotoEntity中，根据不同的group，做不同的处理
     * @param photoEntity
     */
    private void addPhotoEntityToGroup(PhotoEntity photoEntity) throws JSONException{
        if(photoEntity.getIndex() >= PhotoLayout.photoIndex) {
            PhotoLayout.photoIndex = photoEntity.getIndex() + 1;
        }

        JSONObject jsonObject = new JSONObject(photoEntity.getJsonString());
        String group = jsonObject.getString("Group");

        // 外观组
        if(group.equals("exterior")) {
            String part = jsonObject.getString("Part");

            // 标准照
            if(part.equals("standard")) {
                PhotoExteriorLayout.photoListAdapter.addItem(photoEntity);
                PhotoExteriorLayout.photoListAdapter.notifyDataSetChanged();

                JSONObject photoData = jsonObject.getJSONObject("PhotoData");
                String photoPart = photoData.getString("part");

                for(int i = 0; i < Common.exteriorPartArray.length; i++) {
                    if(photoPart.equals(Common.exteriorPartArray[i])) {
                        PhotoExteriorLayout.photoShotCount[i] = 1;
                        if(photoEntity.getFileName().equals("")) {
                            PhotoExteriorLayout.photoNames[i] = Common.NO_PHOTO;
                        } else if(photoEntity.getFileName().contains("http")) {
                            PhotoExteriorLayout.photoNames[i] = Common.WEB_PHOTO;
                        } else {
                            PhotoExteriorLayout.photoNames[i] = Long.parseLong(
                                    photoEntity.getFileName().substring(0, photoEntity.getFileName().length() - 4));
                        }
                    }
                }
            }
            // 缺陷照
            else if(part.equals("fault")) {
                ExteriorLayout.photoEntities.add(photoEntity);
                PhotoFaultLayout.photoListAdapter.addItem(photoEntity);
                PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();

                JSONObject photoData = jsonObject.getJSONObject("PhotoData");

                int type = photoData.getInt("type");
                PosEntity posEntity = new PosEntity(type);

                // 设置边界
                posEntity.setMaxX(1000);
                posEntity.setMaxY(2000);

                int startX, startY, endX, endY, radius;

                if(type == Common.TRANS) {
                    radius = photoData.getInt("radius");
                    startX = photoData.getInt("startX") - radius;
                    startY = photoData.getInt("startY") - radius;
                    endX = photoData.getInt("startX") + radius;
                    endY = photoData.getInt("startY") + radius;
                } else {
                    startX = photoData.getInt("startX");
                    startY = photoData.getInt("startY");
                    endX = photoData.getInt("endX");
                    endY = photoData.getInt("endY");
                }

                posEntity.setStart(startX, startY);
                posEntity.setEnd(endX, endY);
                posEntity.setComment(photoData.getString("comment"));

                ExteriorLayout.posEntities.add(posEntity);
                ExteriorLayout.updateExteriorPreview();
            }
        }
        // 内饰组
        else if(group.equals("interior")) {
            String part = jsonObject.getString("Part");

            // 标准照
            if(part.equals("standard")) {
                PhotoInteriorLayout.photoListAdapter.addItem(photoEntity);
                PhotoInteriorLayout.photoListAdapter.notifyDataSetChanged();

                JSONObject photoData = jsonObject.getJSONObject("PhotoData");
                String photoPart = photoData.getString("part");

                for(int i = 0; i < Common.interiorPartArray.length; i++) {
                    if(photoPart.equals(Common.interiorPartArray[i])) {
                        PhotoInteriorLayout.photoShotCount[i] = 1;

                        if(photoEntity.getFileName().equals("")) {
                            PhotoInteriorLayout.photoNames[i] = Common.NO_PHOTO;
                        } else if(photoEntity.getFileName().contains("http")) {
                            PhotoInteriorLayout.photoNames[i] = Common.WEB_PHOTO;
                        } else {
                            PhotoInteriorLayout.photoNames[i] = Long.parseLong(
                                    photoEntity.getFileName().substring(0, photoEntity.getFileName().length() - 4));
                        }
                    }
                }
            }
            // 缺陷照
            else if(part.equals("fault")) {
                InteriorLayout.photoEntities.add(photoEntity);
                PhotoFaultLayout.photoListAdapter.addItem(photoEntity);
                PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();

                JSONObject photoData = jsonObject.getJSONObject("PhotoData");

                int type = photoData.getInt("type");
                PosEntity posEntity = new PosEntity(type);

                posEntity.setEnd(photoData.getInt("endX"), photoData.getInt("endY"));
                posEntity.setStart(photoData.getInt("startX"), photoData.getInt("startY"));
                posEntity.setMaxX(1000);
                posEntity.setMaxY(2000);

                int startX, startY, endX, endY, radius;

                if(type == Common.BROKEN) {
                    radius = photoData.getInt("radius");
                    startX = photoData.getInt("startX") - radius;
                    startY = photoData.getInt("startY") - radius;
                    endX = photoData.getInt("startX") + radius;
                    endY = photoData.getInt("startY") + radius;
                } else {
                    startX = photoData.getInt("startX");
                    startY = photoData.getInt("startY");
                    endX = photoData.getInt("endX");
                    endY = photoData.getInt("endY");
                }

                posEntity.setStart(startX, startY);
                posEntity.setEnd(endX, endY);
                posEntity.setComment(photoData.getString("comment"));

                InteriorLayout.posEntities.add(posEntity);
                InteriorLayout.updateInteriorPreview();
            }
        }
        // 结构组
        else if(group.equals("frame")) {
            String part = jsonObject.getString("Part");

            if(!part.equals("fSketch") && !part.equals("rSketch")) {
                JSONObject photoData = jsonObject.getJSONObject("PhotoData");

                PosEntity posEntity = new PosEntity(Common.COLOR_DIFF);

                posEntity.setStart(photoData.getInt("x"), photoData.getInt("y"));
                posEntity.setIssueId(photoData.getInt("issueId"));
                posEntity.setComment(photoData.getString("comment"));

                List<Issue> issues = accidentCheckLayout.getIssues();

                for(Issue issue : issues) {
                    if(issue.getId() == photoData.getInt("issueId")) {
                        issue.getPosEntities().add(posEntity);
                        issue.getPhotoEntities().add(photoEntity);
                    }
                }

                if(part.equals("front")) {
                    AccidentResultLayout.posEntitiesFront.add(posEntity);
                    AccidentResultLayout.photoEntitiesFront.add(photoEntity);
                    AccidentResultLayout.framePaintPreviewViewFront.invalidate();
                } else if(part.equals("rear")){
                    AccidentResultLayout.posEntitiesRear.add(posEntity);
                    AccidentResultLayout.photoEntitiesRear.add(photoEntity);
                    AccidentResultLayout.framePaintPreviewViewRear.invalidate();
                }

                PhotoFaultLayout.photoListAdapter.addItem(photoEntity);
                PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();
            }
        }
        // 手续组
        else if(group.equals("procedures")) {
            PhotoProcedureLayout.photoListAdapter.addItem(photoEntity);
            PhotoProcedureLayout.photoListAdapter.notifyDataSetChanged();

            JSONObject photoData = jsonObject.getJSONObject("PhotoData");
            String photoPart = photoData.getString("part");

            for(int i = 0; i < Common.proceduresPartArray.length; i++) {
                if(photoPart.equals(Common.proceduresPartArray[i])) {
                    PhotoProcedureLayout.photoShotCount[i] = 1;

                    if(photoEntity.getFileName().equals("")) {
                        PhotoProcedureLayout.photoNames[i] = Common.NO_PHOTO;
                    } else if(photoEntity.getFileName().contains("http")) {
                        PhotoProcedureLayout.photoNames[i] = Common.WEB_PHOTO;
                    } else {
                        PhotoProcedureLayout.photoNames[i] = Long.parseLong(
                                photoEntity.getFileName().substring(0, photoEntity.getFileName().length() - 4));
                    }
                }
            }
        }
        // 机舱组
        else if(group.equals("engineRoom")) {
            PhotoEngineLayout.photoListAdapter.addItem(photoEntity);
            PhotoEngineLayout.photoListAdapter.notifyDataSetChanged();

            JSONObject photoData = jsonObject.getJSONObject("PhotoData");
            String photoPart = photoData.getString("part");

            for(int i = 0; i < Common.enginePartArray.length; i++) {
                if(photoPart.equals(Common.enginePartArray[i])) {
                    PhotoEngineLayout.photoShotCount[i] = 1;

                    if(photoEntity.getFileName().equals("")) {
                        PhotoEngineLayout.photoNames[i] = Common.NO_PHOTO;
                    } else if(photoEntity.getFileName().contains("http")) {
                        PhotoEngineLayout.photoNames[i] = Common.WEB_PHOTO;
                    } else {
                        PhotoEngineLayout.photoNames[i] = Long.parseLong(
                                photoEntity.getFileName().substring(0, photoEntity.getFileName().length() - 4));
                    }
                }
            }
        }
        // 协议组
        else if(group.equals("agreement")) {
            PhotoAgreement.photoShotCount++;
            PhotoAgreement.photoListAdapter.addItem(photoEntity);
            PhotoAgreement.photoListAdapter.notifyDataSetChanged();

            if(photoEntity.getFileName().equals("")) {
                PhotoAgreement.photoName = Common.NO_PHOTO;
            } else if(photoEntity.getFileName().contains("http")) {
                PhotoAgreement.photoName = Common.WEB_PHOTO;
            } else {
                PhotoAgreement.photoName = Long.parseLong(
                        photoEntity.getFileName().substring(0, photoEntity.getFileName().length() - 4));
            }
        }
        // 轮胎（加入外观组）
        else if(group.equals("tire")) {
            String photoPart = jsonObject.getString("Part");

            if(!photoPart.equals("sketch")) {
                PhotoExteriorLayout.photoListAdapter.addItem(photoEntity);
                PhotoExteriorLayout.photoListAdapter.notifyDataSetChanged();

                for(int i = 0; i < Common.tirePartArray.length; i++) {
                    if(photoPart.equals(Common.tirePartArray[i])) {
                        Integrated2Layout.photoShotCount[i] = 1;
                        if(Integrated2Layout.buttons[i] != null)
                            Integrated2Layout.buttons[i].setBackgroundResource(R.drawable.tire_pressed);
                    }
                }

                if(Integrated2Layout.photoEntityMap != null) {
                    Integrated2Layout.photoEntityMap.put(photoPart, photoEntity);

                    if(photoPart.equals("leftFront")) {
                        Integrated2Layout.leftFrontIndex = photoEntity.getIndex();
                    } else if(photoPart.equals("rightFront")) {
                        Integrated2Layout.rightFrontIndex = photoEntity.getIndex();
                    } else if(photoPart.equals("leftRear")) {
                        Integrated2Layout.leftRearIndex = photoEntity.getIndex();
                    } else if(photoPart.equals("rightRear")) {
                        Integrated2Layout.rightRearIndex = photoEntity.getIndex();
                    } else if(photoPart.equals("spare")) {
                        Integrated2Layout.spareIndex = photoEntity.getIndex();
                    }
                }
            }
        }
        // 其他缺陷
        else if(group.equals("otherFault")) {
            PhotoFaultLayout.photoListAdapter.addItem(photoEntity);
            PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        quitConfirm();
    }

    /**
     * 退出前的确认，如果确定退出，关闭activity，并做一些销毁的操作
     */
    private void quitConfirm() {
        MyAlertDialog.showAlert(CarCheckActivity.this, R.string.quitCheckMsg, R.string.alert, MyAlertDialog.BUTTON_STYLE_OK_CANCEL,
                new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message message) {
                        switch (message.what) {
                            case MyAlertDialog.POSITIVE_PRESSED:
                                Intent intent = new Intent(CarCheckActivity.this, activity);
                                startActivity(intent);
                                finish();
                                break;
                            case MyAlertDialog.NEGATIVE_PRESSED:
                                break;
                        }

                        return true;
                    }
                }));
    }

    /**
     * 退出时，清除一些静态数据
     */
    private void clearCache() {
        saved = false;
        basicInfoLayout.clearCache();
        accidentCheckLayout.clearCache();
        integratedCheckLayout.clearCache();
        photoLayout.clearCache();
        accidentCheckLayout.unbindDrawables();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        clearCache();
        System.gc();
    }

    /**
     * 当前是否为修改模式
     */
    public static boolean isModify() { return modify; }
}
