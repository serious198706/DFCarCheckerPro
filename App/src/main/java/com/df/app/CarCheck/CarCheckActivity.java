package com.df.app.CarCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import com.df.app.R;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.service.UploadPictureTask;
import com.df.app.util.Common;
import com.df.app.util.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.df.app.util.Helper.setTextView;

public class CarCheckActivity extends Activity {
    private BasicInfoLayout basicInfoLayout;
    private AccidentCheckLayout accidentCheckLayout;
    private IntegratedCheckLayout integratedCheckLayout;
    private Button naviButton;
    private Button basicInfoButton;
    private Button accidentCheckButton;
    private Button integratedButton;
    private Button photoButton;

    private List<PhotoEntity> photoEntities;

    private boolean showMenu = false;

    Map<Integer, String> tabMap = new HashMap<Integer, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_check);

        photoEntities = new ArrayList<PhotoEntity>();

        tabMap.put(R.id.basicInfo, "基本信息");
        tabMap.put(R.id.accidentCheck, "事故排查");
        tabMap.put(R.id.integratedCheck, "综合检查");
        tabMap.put(R.id.photo, "照片拍摄");

        naviButton = (Button)findViewById(R.id.buttonNavi);
        naviButton.setOnClickListener(new View.OnClickListener() {
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
        basicInfoLayout.setUpdateUiListener(new BasicInfoLayout.OnUpdateUiListener() {
            @Override
            public void updateUi() {
                accidentCheckButton.setEnabled(true);
                integratedButton.setEnabled(true);
                integratedCheckLayout.updateUi();
                photoButton.setEnabled(true);
            }
        });

        // 事故排查模块
        accidentCheckLayout = (AccidentCheckLayout)findViewById(R.id.accidentCheck);

        // 综合检查模块
        integratedCheckLayout = (IntegratedCheckLayout)findViewById(R.id.integratedCheck);

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
                                // 提交检测信息
                                // 1.生成所有图片的Json数据
                                generatePhotoEntities();
                                // 2.上传图片
                                uploadPictures();
                                // 3.生成所有检测信息的Json数据
                                generateJsonString();
                                // 4.提交检测信息
                                commitData();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();

                dialog.show();
            }
        });
    }

    private void showNaviMenu(boolean show) {
        basicInfoButton.setVisibility(show ? View.VISIBLE : View.GONE);
        accidentCheckButton.setVisibility(show ? View.VISIBLE : View.GONE);
        integratedButton.setVisibility(show ? View.VISIBLE : View.GONE);
        photoButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void selectTab(int layoutId) {
        String title = tabMap.get(layoutId);
        setTextView(getWindow().getDecorView(), R.id.currentItem, title);

        for(int id : tabMap.keySet()) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        switch (requestCode) {
            case Common.ENTER_EXTERIOR_PAINT:
                integratedCheckLayout.updateExteriorPreview();
                break;
            case Common.ENTER_INTERIOR_PAINT:
                integratedCheckLayout.updateInteriorPreview();
                break;
            case Common.PHOTO_FOR_EXTERIOR_STANDARD:
                if(resultCode == Activity.RESULT_OK) {
                    integratedCheckLayout.saveExteriorStandardPhoto();
                }
                break;
            case Common.PHOTO_FOR_INTERIOR_STANDARD:
                if(resultCode == Activity.RESULT_OK) {
                    integratedCheckLayout.saveInteriorStandardPhoto();
                }
                break;
            case Common.PHOTO_FOR_ACCIDENT_FRONT:
            case Common.PHOTO_FOR_ACCIDENT_REAR:
            {
                requestCode = (requestCode == Common.PHOTO_FOR_ACCIDENT_FRONT) ?
                        Common.ADD_COMMENT_FOR_ACCIDENT_FRONT_PHOTO : Common.ADD_COMMENT_FOR_ACCIDENT_REAR_PHOTO;

                PosEntity posEntity = accidentCheckLayout.getPosEntity(requestCode);

                if(resultCode == Activity.RESULT_OK) {
                    // 如果确定拍摄了照片，则缩小照片尺寸
                    Helper.setPhotoSize(posEntity.getImageFileName(), 800);

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
            case Common.ADD_COMMENT_FOR_ACCIDENT_FRONT_PHOTO:
            case Common.ADD_COMMENT_FOR_ACCIDENT_REAR_PHOTO:
            {
                Bundle bundle = data.getExtras();

                PosEntity posEntity = accidentCheckLayout.getPosEntity(requestCode);
                posEntity.setComment(bundle.getString("comment"));

                accidentCheckLayout.saveAccidentPhoto(requestCode);
            }
                break;
            case Common.ADD_COMMENT_FOR_EXTERIOR_PHOTO:
                break;
            case Common.ADD_COMMENT_FOR_INTERIOR_PHOTO:
                break;
        }
    }

    // 添加所有的photoEntity
    private void generatePhotoEntities() {
        // 外观标准组
        photoEntities.addAll(PhotoExteriorLayout.photoListAdapter.getItems());

        // 内饰标准组
        photoEntities.addAll(PhotoInteriorLayout.photoListAdapter.getItems());

        // 缺陷组，包含外观缺陷、内饰缺陷、结构缺陷
        photoEntities.addAll(PhotoFaultLayout.photoListAdapter.getItems());

        // 手续组
        photoEntities.addAll(PhotoProcedureLayout.photoListAdapter.getItems());

        // 机舱组
        photoEntities.addAll(PhotoEngineLayout.photoListAdapter.getItems());

        // 其他组
        photoEntities.addAll(PhotoOtherLayout.photoListAdapter.getItems());
    }

    private void uploadPictures() {
        UploadPictureTask uploadPictureTask = new UploadPictureTask(CarCheckActivity.this, photoEntities);
        uploadPictureTask.execute();
    }

    private void generateJsonString() {
//        try {
//            JSONObject features = new JSONObject(basicInfoLayout.generateJsonString());
//            JSONObject accident = new JSONObject(accidentCheckLayout.generateJsonString());
//            JSONObject conditions = new JSONObject(integratedCheckLayout.generateJsonString());
//        } catch (JSONException e) {
//
//        }
    }

    private void commitData() {

    }

    @Override
    public void onBackPressed() {
        quitConfirm();
    }

    private void quitConfirm() {
        View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(R.string.quitCheckMsg);
        content.setTextSize(22f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, getResources().getString(R.string.alert));

        AlertDialog dialog = new AlertDialog.Builder(CarCheckActivity.this)
                .setView(view1)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();
    }
}
