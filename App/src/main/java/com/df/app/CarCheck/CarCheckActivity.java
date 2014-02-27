package com.df.app.carCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.carsChecked.CarsCheckedActivity;
import com.df.app.carsWaiting.CarsWaitingActivity;
import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.service.AsyncTask.CommitDataTask;
import com.df.app.service.AsyncTask.GeneratePhotoEntitiesTask;
import com.df.app.service.AsyncTask.SaveDataTask;
import com.df.app.service.AsyncTask.UploadPictureTask;
import com.df.app.util.Common;
import com.df.app.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 13-12-20.
 *
 * 主activity，承载基本信息、事故排查、综合检查、拍摄照片四个模块
 */
public class CarCheckActivity extends Activity {
    private BasicInfoLayout basicInfoLayout;
    private AccidentCheckLayout accidentCheckLayout;
    private IntegratedCheckLayout integratedCheckLayout;
    private PhotoLayout photoLayout;
    private Button basicInfoButton;
    private Button accidentCheckButton;
    private Button integratedButton;
    private Button photoButton;

    private int carId;
    private List<PhotoEntity> photoEntities;

    private boolean showMenu = false;

    // 按钮内容与按钮id的map
    SparseArray<String> tabMap = new SparseArray<String>();

    private Class activity;

    // 最终json串
    private JSONObject jsonObject;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_check);

        // 填充各部分的内容
        Bundle bundle = getIntent().getExtras();

        // 获取车辆详细信息
        String jsonString = bundle.getString("jsonString");
        carId = bundle.getInt("carId");
        activity = (Class)getIntent().getSerializableExtra("activity");

        photoEntities = new ArrayList<PhotoEntity>();

        tabMap.put(R.id.basicInfo, "基本信息");
        tabMap.put(R.id.accidentCheck, "事故排查");
        tabMap.put(R.id.integratedCheck, "综合检查");
        tabMap.put(R.id.photo, "照片拍摄");

        Button navigateButton = (Button) findViewById(R.id.buttonNavi);
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenu = !showMenu;
                showNaviMenu(showMenu);
            }
        });

        basicInfoButton = (Button)findViewById(R.id.buttonBasicInfo);
        basicInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {selectTab(R.id.basicInfo);
            }
        });
        basicInfoButton.setVisibility(View.GONE);

        accidentCheckButton = (Button)findViewById(R.id.buttonAccidentCheck);
        accidentCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {selectTab(R.id.accidentCheck);
            }
        });
        accidentCheckButton.setVisibility(View.GONE);
        accidentCheckButton.setEnabled(false);

        integratedButton = (Button)findViewById(R.id.buttonIntegratedCheck);
        integratedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {selectTab(R.id.integratedCheck);
            }
        });
        integratedButton.setVisibility(View.GONE);
        integratedButton.setEnabled(false);

        photoButton = (Button)findViewById(R.id.buttonPhoto);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTab(R.id.photo);
            }
        });
        photoButton.setVisibility(View.GONE);
        photoButton.setEnabled(false);

        // 基本信息模块
        basicInfoLayout = (BasicInfoLayout)findViewById(R.id.basicInfo);
        basicInfoLayout.setUpdateUiListener(new BasicInfoLayout.OnGetCarSettings() {
            @Override
            public void onGetCarSettings() {
                accidentCheckButton.setEnabled(true);
                integratedButton.setEnabled(true);
                integratedCheckLayout.updateUi();
                photoButton.setEnabled(true);

                accidentCheckLayout.updatePreviews();
            }
        });

        // 事故排查模块
        accidentCheckLayout = (AccidentCheckLayout)findViewById(R.id.accidentCheck);

        // 综合检查模块
        integratedCheckLayout = (IntegratedCheckLayout)findViewById(R.id.integratedCheck);

        // 拍摄照片模块
        photoLayout = (PhotoLayout)findViewById(R.id.photo);

        // 设置当前标题
        setTextView(getWindow().getDecorView(), R.id.currentItem, getString(R.string.title_basicInfo));

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
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pass = checkAllFields();

                // 如果所有项目都已经填写完毕
                if(pass.equals("")) {
                    View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);
                    TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
                    TextView content = new TextView(view1.getContext());
                    content.setText(R.string.commitMsg);
                    content.setTextSize(22f);
                    contentArea.addView(content);

                    setTextView(view1, R.id.title, getResources().getString(R.string.alert));

                    AlertDialog dialog = new AlertDialog.Builder(CarCheckActivity.this)
                            .setView(view1)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    generatePhotoEntities();
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .create();

                    dialog.show();
                } else {
                    if(pass.equals("accidentCheck")) {
                        Toast.makeText(CarCheckActivity.this, "未完成事故检测！", Toast.LENGTH_SHORT).show();
                        selectTab(R.id.accidentCheck);
                    } else if(pass.equals("leftFront") || pass.equals("rightFront") ||
                            pass.equals("leftRear") || pass.equals("rightRear") ||
                            pass.equals("spare") || pass.equals("edits")) {
                        selectTab(R.id.integratedCheck);
                    } else if(pass.equals("exterior") || pass.equals("interior") ||
                            pass.equals("engine") || pass.equals("procedures") || pass.equals("agreements")) {
                        selectTab(R.id.photo);
                    } else if(pass.equals("coop")) {
                        selectTab(R.id.integratedCheck);
                    }
                }
            }
        });

        // 保存按钮
        Button saveButton = (Button)findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);
                TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
                TextView content = new TextView(view1.getContext());
                content.setText(R.string.saveMsg);
                content.setTextSize(22f);
                contentArea.addView(content);

                setTextView(view1, R.id.title, getResources().getString(R.string.alert));

                AlertDialog dialog = new AlertDialog.Builder(CarCheckActivity.this)
                        .setView(view1)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 3.生成所有检测信息的Json数据
                                generateJsonString();
                                // 4.提交检测信息
                                saveData();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();

                dialog.show();
            }
        });

//        Button comB = (Button)findViewById(R.id.com);
//        comB.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                commitData();
//            }
//        });

        // 填充数据
        fillInData(carId, jsonString);
    }

    /**
     * 检查所有必填字段
     *
     * 使用字符串来定位目前的
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
                // 2.上传图片
                uploadPictures();
            }

            @Override
            public void onFailed() {
                Toast.makeText(CarCheckActivity.this, "处理失败！", Toast.LENGTH_SHORT).show();
                Log.d(Common.TAG, "生成草图失败！");
            }
        });

        generatePhotoEntitiesTask.execute();
    }

    /**
     * 上传所有照片
     */
    private void uploadPictures() {
        UploadPictureTask uploadPictureTask = new UploadPictureTask(CarCheckActivity.this, photoEntities, new UploadPictureTask.UploadFinished() {
            @Override
            public void OnFinish() {
                // 3.生成所有检测信息的Json数据
                generateJsonString();
                // 4.提交检测信息
                commitData();
            }
        });
        uploadPictureTask.execute();
    }

    /**
     * 提交检测数据
     */
    private void commitData() {
        CommitDataTask commitDataTask = new CommitDataTask(CarCheckActivity.this, new CommitDataTask.OnCommitDataFinished() {
            @Override
            public void onFinished(String result) {
                // 如果存在临时保存的数据，则删除之
                File file = new File(Common.savedDirectory + Integer.toString(carId));
                if(file.exists()) {
                    if(file.delete()) {
                        Toast.makeText(CarCheckActivity.this, result, Toast.LENGTH_SHORT).show();
                        clearCache();
                        Intent intent = new Intent(CarCheckActivity.this, CarsCheckedActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailed(String result) {
                Toast.makeText(CarCheckActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        });
        commitDataTask.execute(jsonObject);
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
                SaveDataTask saveDataTask = new SaveDataTask(CarCheckActivity.this, carId, photoEntities, new SaveDataTask.OnSaveDataFinished() {
                    @Override
                    public void onFinished() {
                        Toast.makeText(CarCheckActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
                        Log.d(Common.TAG, "保存成功！");
                        clearCache();
                        Intent intent = new Intent(CarCheckActivity.this, CarsWaitingActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailed() {
                        Toast.makeText(CarCheckActivity.this, "保存失败！", Toast.LENGTH_SHORT).show();
                        Log.d(Common.TAG, "保存失败！");
                    }
                });
                saveDataTask.execute(jsonObject);
            }

            @Override
            public void onFailed() {
                Toast.makeText(CarCheckActivity.this, "处理失败！", Toast.LENGTH_SHORT).show();
                Log.d(Common.TAG, "生成草图失败！");
            }
        });

        generatePhotoEntitiesTask.execute();
    }

    /**
     * 按下导航按钮，显示菜单
     */
    private void showNaviMenu(boolean show) {
        basicInfoButton.setVisibility(show ? View.VISIBLE : View.GONE);
        accidentCheckButton.setVisibility(show ? View.VISIBLE : View.GONE);
        integratedButton.setVisibility(show ? View.VISIBLE : View.GONE);
        photoButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * 根据选择的按钮，切换不同的模块
     */
    private void selectTab(int layoutId) {
        String title = tabMap.get(layoutId);
        setTextView(getWindow().getDecorView(), R.id.currentItem, title);

        for(int i = 0; i < tabMap.size(); i++) {
            int id = tabMap.keyAt(i);

            if(id != layoutId) {
                findViewById(id).setVisibility(View.GONE);
            } else {
                findViewById(id).setVisibility(View.VISIBLE);
            }
        }

        basicInfoButton.setBackgroundResource(layoutId == R.id.basicInfo ?
                R.drawable.basic_info_active : R.drawable.basic_info);
        accidentCheckButton.setBackgroundResource(layoutId == R.id.accidentCheck ?
                R.drawable.accident_active : R.drawable.accident);
        integratedButton.setBackgroundResource(layoutId == R.id.integratedCheck ?
                R.drawable.integrated_active : R.drawable.integrated);
        photoButton.setBackgroundResource(layoutId == R.id.photo ?
                R.drawable.photo_active : R.drawable.photo);

        showMenu = !showMenu;
        showNaviMenu(false);
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
            // 事故查勘照片拍摄
            case Common.PHOTO_FOR_ACCIDENT_FRONT:
            case Common.PHOTO_FOR_ACCIDENT_REAR:
            {
                requestCode = (requestCode == Common.PHOTO_FOR_ACCIDENT_FRONT) ?
                        Common.ADD_COMMENT_FOR_ACCIDENT_FRONT_PHOTO : Common.ADD_COMMENT_FOR_ACCIDENT_REAR_PHOTO;

                PosEntity posEntity = accidentCheckLayout.getPosEntity(requestCode);

                if(resultCode == Activity.RESULT_OK) {
                    // 如果确定拍摄了照片，则缩小照片尺寸
                    Helper.setPhotoSize(posEntity.getImageFileName(), 800);
                    Helper.generatePhotoThumbnail(posEntity.getImageFileName(), 400);

                    Intent intent = new Intent(CarCheckActivity.this, AddPhotoCommentActivity.class);
                    intent.putExtra("fileName", posEntity.getImageFileName());
                    startActivityForResult(intent, requestCode);
                } else {
                    // 如果取消了拍摄，则置照片名为空
                    posEntity.setImageFileName("");
                    accidentCheckLayout.saveAccidentPhoto(requestCode);
                }
            }
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
            // 外观标准照
            case Common.PHOTO_FOR_EXTERIOR_STANDARD:
                if(resultCode == Activity.RESULT_OK) {
                    photoLayout.saveExteriorStandardPhoto();
                }
                break;
            // 内饰标准照
            case Common.PHOTO_FOR_INTERIOR_STANDARD:
                if(resultCode == Activity.RESULT_OK) {
                    photoLayout.saveInteriorStandardPhoto();
                }
                break;
            // 轮胎照片
            case Common.PHOTO_FOR_TIRES:
                if(resultCode == Activity.RESULT_OK) {
                    integratedCheckLayout.saveTirePhoto();
                }
                break;
            // 手续组照片
            case Common.PHOTO_FOR_PROCEDURES_STANDARD:
                if(resultCode == Activity.RESULT_OK) {
                    photoLayout.saveProceduresStandardPhoto();
                }
                break;
            // 机舱组照片
            case Common.PHOTO_FOR_ENGINE_STANDARD:
                if(resultCode == Activity.RESULT_OK) {
                    photoLayout.saveEngineStandardPhoto();
                }
                break;
            // 其他组照片
            case Common.PHOTO_FOR_OTHER_STANDARD:
                if(resultCode == Activity.RESULT_OK) {
                    photoLayout.saveOtherStandardPhoto();
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
            // TODO 对某张照片重拍
            case Common.PHOTO_RETAKE:
                if(resultCode == Activity.RESULT_OK) {
                    Helper.setPhotoSize(PhotoLayout.reTakePhotoEntity.getFileName(), 800);
                    Helper.generatePhotoThumbnail(PhotoLayout.reTakePhotoEntity.getFileName(), 400);

                    PhotoExteriorLayout.photoListAdapter.notifyDataSetChanged();
                    PhotoInteriorLayout.photoListAdapter.notifyDataSetChanged();
                    PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();
                    PhotoProcedureLayout.photoListAdapter.notifyDataSetChanged();
                    PhotoEngineLayout.photoListAdapter.notifyDataSetChanged();
                    PhotoOtherLayout.photoListAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    /**
     * 生成最终的JSON串
     */
    private void generateJsonString() {
        try {
            jsonObject = new JSONObject();
            JSONObject features = basicInfoLayout.generateJSONObject();
            JSONObject accident = accidentCheckLayout.generateJSONObject();
            JSONObject conditions = integratedCheckLayout.generateJSONObject();

            jsonObject.put("features", features);
            jsonObject.put("accident", accident);
            jsonObject.put("conditions", conditions);
            jsonObject.put("checkUserId", MainActivity.userInfo.getId());
            jsonObject.put("checkUserName", MainActivity.userInfo.getName());
            jsonObject.put("checkCooperatorId", integratedCheckLayout.getCooperatorId());
            jsonObject.put("checkCooperatorName", integratedCheckLayout.getCooperatorName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     */
    // 将所有数据填充到所有检测模块中
    private void fillInData(int carId, String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            // 更新手续页面（一定会有）
            JSONObject features = jsonObject.getJSONObject("features");
            basicInfoLayout.fillInData(carId, features);

            // 如果有事故节点，则更新事故页面
            if(jsonObject.has("accident")) {
                JSONObject accident = jsonObject.getJSONObject("accident");
                accidentCheckLayout.fillInData(accident, new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message message) {
                        accidentCheckLayout.showIssueAndResultTabs();
                        return true;
                    }
                }));
            }

            // 如果有综合检查节点，则更新综合检查页面
            if(jsonObject.has("conditions")) {
                JSONObject conditions = jsonObject.getJSONObject("conditions");
                integratedCheckLayout.fillInData(conditions);
            }

            // 如果有照片节点，则更新照片list
            if(jsonObject.has("photos")) {
                List<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();

                JSONArray jsonArray = jsonObject.getJSONArray("photos");

                for(int i = 0; i < jsonArray.length(); i++) {
                    JSONObject entity = jsonArray.getJSONObject(i);
                    PhotoEntity photoEntity = new PhotoEntity();
                    photoEntity.setJsonString(entity.getString("jsonString"));
                    photoEntity.setComment(entity.has("comment") ? entity.getString("comment") : "");
                    photoEntity.setName(entity.has("name") ? entity.getString("name") : "");
                    photoEntity.setFileName(entity.has("fileName") ? entity.getString("fileName") : "");
                    photoEntity.setThumbFileName(entity.has("thumbFileName") ? entity.getString("thumbFileName") : "");

                    photoEntities.add(photoEntity);
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

        }
    }

    /**
     * 对PhotoEntity中，根据不同的group，做不同的处理
     * @param photoEntity
     */
    private void addPhotoEntityToGroup(PhotoEntity photoEntity) throws JSONException{
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

                if(photoPart.equals("leftFront45")) {
                    PhotoExteriorLayout.photoShotCount[0]++;
                } else if(photoPart.equals("rightFront45")) {
                    PhotoExteriorLayout.photoShotCount[1]++;
                } else if(photoPart.equals("left")) {
                    PhotoExteriorLayout.photoShotCount[2]++;
                } else if(photoPart.equals("right")) {
                    PhotoExteriorLayout.photoShotCount[3]++;
                } else if(photoPart.equals("leftRear45")) {
                    PhotoExteriorLayout.photoShotCount[4]++;
                } else if(photoPart.equals("rightRear45")) {
                    PhotoExteriorLayout.photoShotCount[5]++;
                } else if(photoPart.equals("other")) {
                    PhotoExteriorLayout.photoShotCount[6]++;
                }
            }
            // 缺陷照
            else if(part.equals("fault")) {
                PhotoFaultLayout.photoListAdapter.addItem(photoEntity);
                PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();

                JSONObject photoData = jsonObject.getJSONObject("PhotoData");

                int type = photoData.getInt("type");
                PosEntity posEntity = new PosEntity(type);

                posEntity.setStart(photoData.getInt("startX"), photoData.getInt("startY"));
                posEntity.setEnd(photoData.getInt("endX"), photoData.getInt("endY"));

                posEntity.setMaxX(1000);
                posEntity.setMaxY(2000);
                //posEntity.setMaxY(1407);
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

                if(photoPart.equals("workbench")) {
                    PhotoInteriorLayout.photoShotCount[0]++;
                } else if(photoPart.equals("steeringWheel")) {
                    PhotoInteriorLayout.photoShotCount[1]++;
                } else if(photoPart.equals("dashboard")) {
                    PhotoInteriorLayout.photoShotCount[2]++;
                } else if(photoPart.equals("leftDoor+steeringWheel")) {
                    PhotoInteriorLayout.photoShotCount[3]++;
                } else if(photoPart.equals("rearSeats")) {
                    PhotoInteriorLayout.photoShotCount[4]++;
                } else if(photoPart.equals("coDriverSeat")) {
                    PhotoInteriorLayout.photoShotCount[5]++;
                } else if(photoPart.equals("other")) {
                    PhotoInteriorLayout.photoShotCount[6]++;
                }
            }
            // 缺陷照
            else if(part.equals("fault")) {
                PhotoFaultLayout.photoListAdapter.addItem(photoEntity);
                PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();

                JSONObject photoData = jsonObject.getJSONObject("PhotoData");

                int type = photoData.getInt("type");
                PosEntity posEntity = new PosEntity(type);

                posEntity.setEnd(photoData.getInt("endX"), photoData.getInt("endY"));
                posEntity.setStart(photoData.getInt("startX"), photoData.getInt("startY"));
                posEntity.setMaxX(1000);
                posEntity.setMaxY(2000);
                //posEntity.setMaxY(1383);
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

                if(part.equals("front")) {
                    AccidentResultLayout.posEntitiesFront.add(posEntity);
                    AccidentResultLayout.framePaintPreviewViewFront.invalidate();
                    PhotoFaultLayout.photoListAdapter.addItem(photoEntity);
                    PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();
                } else if(part.equals("rear")){
                    AccidentResultLayout.posEntitiesRear.add(posEntity);
                    AccidentResultLayout.framePaintPreviewViewRear.invalidate();
                    PhotoFaultLayout.photoListAdapter.addItem(photoEntity);
                    PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();
                }
            }
        }
        // 手续组
        else if(group.equals("procedures")) {
            PhotoProcedureLayout.photoListAdapter.addItem(photoEntity);
            PhotoProcedureLayout.photoListAdapter.notifyDataSetChanged();

            JSONObject photoData = jsonObject.getJSONObject("PhotoData");
            String part = photoData.getString("part");

            if(part.equals("plate")) {
                PhotoProcedureLayout.photoShotCount[0]++;
            } else if(part.equals("procedures")) {
                PhotoProcedureLayout.photoShotCount[1]++;
            } else if(part.equals("keys")) {
                PhotoProcedureLayout.photoShotCount[2]++;
            } else if(part.equals("other")) {
                PhotoProcedureLayout.photoShotCount[3]++;
            }
        }
        // 机舱组
        else if(group.equals("engineRoom")) {
            PhotoEngineLayout.photoListAdapter.addItem(photoEntity);
            PhotoEngineLayout.photoListAdapter.notifyDataSetChanged();

            JSONObject photoData = jsonObject.getJSONObject("PhotoData");
            String part = photoData.getString("part");

            if(part.equals("overview")) {
                PhotoEngineLayout.photoShotCount[0]++;
            } else if(part.equals("left")) {
                PhotoEngineLayout.photoShotCount[1]++;
            } else if(part.equals("right")) {
                PhotoEngineLayout.photoShotCount[2]++;
            } else if(part.equals("other")) {
                PhotoEngineLayout.photoShotCount[3]++;
            }
        }
        // 协议组
        else if(group.equals("agreement")) {
            PhotoOtherLayout.photoListAdapter.addItem(photoEntity);
            PhotoOtherLayout.photoListAdapter.notifyDataSetChanged();
        }
        // 轮胎（加入外观组）
        else if(group.equals("tire")) {
            String part = jsonObject.getString("Part");

            if(!part.equals("sketch")) {
                PhotoExteriorLayout.photoListAdapter.addItem(photoEntity);
                PhotoExteriorLayout.photoListAdapter.notifyDataSetChanged();

                if(part.equals("leftFront")) {
                    Integrated2Layout.photoShotCount[0]++;
                } else if(part.equals("rightFront")) {
                    Integrated2Layout.photoShotCount[1]++;
                } else if(part.equals("leftRear")) {
                    Integrated2Layout.photoShotCount[2]++;
                } else if(part.equals("rightRear")) {
                    Integrated2Layout.photoShotCount[3]++;
                } else if(part.equals("spare")) {
                    Integrated2Layout.photoShotCount[4]++;
                }
            }
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
        View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(R.string.quitCheckMsg);
        content.setTextSize(20f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, getResources().getString(R.string.alert));

        AlertDialog dialog = new AlertDialog.Builder(CarCheckActivity.this)
                .setView(view1)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        clearCache();

                        Intent intent = new Intent(CarCheckActivity.this, activity);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();
    }

    /**
     * 退出时，清除一些静态数据
     */
    private void clearCache() {
        for(int i = 0; i < PhotoExteriorLayout.photoShotCount.length; i++) {
            PhotoExteriorLayout.photoShotCount[i] = 0;
        }
        for(int i = 0; i < PhotoInteriorLayout.photoShotCount.length; i++) {
            PhotoInteriorLayout.photoShotCount[i] = 0;
        }
        for(int i = 0; i < PhotoEngineLayout.photoShotCount.length; i++) {
            PhotoEngineLayout.photoShotCount[i] = 0;
        }
        for(int i = 0; i < PhotoProcedureLayout.photoShotCount.length; i++) {
            PhotoProcedureLayout.photoShotCount[i] = 0;
        }
        for(int i = 0; i < PhotoEngineLayout.photoShotCount.length; i++) {
            PhotoEngineLayout.photoShotCount[i] = 0;
        }
        for(int i = 0; i < Integrated2Layout.photoShotCount.length; i++) {
            Integrated2Layout.photoShotCount[i] = 0;
        }
    }
}
