package com.df.app.service.AsyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.df.app.CarCheck.BasicInfoLayout;
import com.df.app.MainActivity;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.SoapService;
import com.df.app.util.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Created by 岩 on 14-1-13.
 *
 * 保存
 */
// 提交检测数据
public class SaveDataTask extends AsyncTask<JSONObject, Void, Boolean> {
    public interface OnSaveDataFinished {
        public void onFinished();
        public void onFailed();
    }

    Context context;
    List<PhotoEntity> photoEntities;
    int carId;
    private ProgressDialog progressDialog;
    private OnSaveDataFinished mCallback;

    /**
     * 构造函数
     * @param context 上下文
     * @param carId 车辆id，以此为文件名进行本地保存
     * @param photoEntityList 所有已拍摄图片
     * @param listener 保存成功的回调函数指针
     */
    public SaveDataTask(Context context, int carId, List<PhotoEntity> photoEntityList, OnSaveDataFinished listener) {
        this.context = context;
        this.carId = carId;
        this.photoEntities = photoEntityList;
        this.mCallback = listener;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setMessage("正在保存信息，请稍候...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(JSONObject... params) {
        boolean success;

        try {
            JSONArray photos = new JSONArray();

            for(PhotoEntity photoEntity : photoEntities) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("comment", photoEntity.getComment());
                jsonObject.put("fileName", photoEntity.getFileName());
                jsonObject.put("thumbFileName", photoEntity.getThumbFileName());
                jsonObject.put("jsonString", photoEntity.getJsonString());
                jsonObject.put("name", photoEntity.getName());

                photos.put(jsonObject);
            }

            params[0].put("photos", photos);

            String jsonString = (params[0]).toString();

            // 保存文件，文件名为carId
            OutputStreamWriter write = new OutputStreamWriter(
                    new FileOutputStream(Common.savedDirectory + Integer.toString(carId)), "UTF-8");
            BufferedWriter writer=new BufferedWriter(write);

            writer.write(jsonString);
            writer.close();

            success = true;
        } catch (JSONException e) {
            e.printStackTrace();
            success = false;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        progressDialog.dismiss();

        if(success) {
            mCallback.onFinished();
        } else {
            mCallback.onFailed();
        }
    }

    @Override
    protected void onCancelled() {
        progressDialog.dismiss();
    }
}