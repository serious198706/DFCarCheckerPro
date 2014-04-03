package com.df.app.service.AsyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.df.app.carCheck.AccidentCheckLayout;
import com.df.app.carCheck.IntegratedCheckLayout;
import com.df.app.carCheck.PhotoAgreement;
import com.df.app.carCheck.PhotoEngineLayout;
import com.df.app.carCheck.PhotoExteriorLayout;
import com.df.app.carCheck.PhotoFaultLayout;
import com.df.app.carCheck.PhotoInteriorLayout;
import com.df.app.carCheck.PhotoProcedureLayout;
import com.df.app.entries.PhotoEntity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 14-1-22.
 *
 * 生成所有需要上传的照片实体
 */
public class GeneratePhotoEntitiesTask extends AsyncTask<JSONObject, Void, Boolean> {
    public interface OnGenerateFinished {
        public void onFinished(List<PhotoEntity> photoEntities);
        public void onFailed();
    }

    Context context;
    private ProgressDialog progressDialog;
    private OnGenerateFinished mCallback;
    private List<PhotoEntity> photoEntities;
    private AccidentCheckLayout accidentCheckLayout;
    private IntegratedCheckLayout integratedCheckLayout;
    private boolean generateSketch;

    /**
     * 构造方法
     * @param context 上下文
     * @param photoEntities 照片队列
     * @param accidentCheckLayout 事故查勘页面句柄
     * @param integratedCheckLayout 综合检查页面句柄
     * @param generateSketch 是否生成草图
     * @param listener 生成完成后的回调函数指针
     */
    public GeneratePhotoEntitiesTask(Context context, List<PhotoEntity> photoEntities, AccidentCheckLayout accidentCheckLayout,
                                     IntegratedCheckLayout integratedCheckLayout, boolean generateSketch, OnGenerateFinished listener) {
        this.context = context;
        this.photoEntities = photoEntities;
        this.accidentCheckLayout = accidentCheckLayout;
        this.integratedCheckLayout = integratedCheckLayout;
        this.mCallback = listener;
        this.generateSketch = generateSketch;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setMessage("正在处理，请稍候...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(JSONObject... params) {
        boolean success;

        photoEntities.clear();

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
        photoEntities.addAll(PhotoAgreement.photoListAdapter.getItems());

        // 所有草图
        if(generateSketch)
            photoEntities.addAll(generateSketches());

//        try {
//            for(int i = 0; i < photoEntities.size(); i++) {
//                PhotoEntity photoEntity = photoEntities.get(i);
//                JSONObject jsonObject = new JSONObject(photoEntity.getJsonString());
//
//                // 给每张图片设置序列号
//                jsonObject.put("Index", i + 1);
//
//                // 更新photoEntity
//                photoEntity.setJsonString(jsonObject.toString());
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        progressDialog.dismiss();

        if(success) {
            mCallback.onFinished(this.photoEntities);
        } else {
            mCallback.onFailed();
        }
    }

    /**
     * 生成草图（事故排查x3，综合检查x3）
     */
    private List<PhotoEntity> generateSketches() {
        List<PhotoEntity> temp = new ArrayList<PhotoEntity>();

        temp.addAll(accidentCheckLayout.generateSketches());
        temp.addAll(integratedCheckLayout.generateSketches());

        return temp;
    }
}