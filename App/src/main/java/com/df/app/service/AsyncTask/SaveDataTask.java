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

import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by 岩 on 14-1-13.
 */
// 提交检测数据
public class SaveDataTask extends AsyncTask<JSONObject, Void, Boolean> {
    public interface OnSaveDataFinished {
        public void onFinished();
        public void onFailed();
    }

    Context context;
    List<PhotoEntity> photoEntities;
    private SoapService soapService;
    private ProgressDialog progressDialog;
    private OnSaveDataFinished mCallback;

    public SaveDataTask(Context context, OnSaveDataFinished listener) {
        this.context = context;
        this.mCallback = listener;
    }

    public SaveDataTask(Context context, List<PhotoEntity> photoEntityList, OnSaveDataFinished listener) {
        this.context = context;
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

        JSONArray photos = new JSONArray();

        try {
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
        } catch (JSONException e) {

        }

        String jsonString = (params[0]).toString();

        FileOutputStream outputStream;

        try {
            outputStream = new FileOutputStream(Common.savedDirectory + "1");
            outputStream.write(jsonString.getBytes());
            outputStream.close();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }

//        soapService = new SoapService();
//
//        // 设置soap的配置
//        soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE, Common.SAVE_DATA);
//
//        JSONObject jsonObject = new JSONObject();
//
//        try {
//            jsonObject.put("CarId", BasicInfoLayout.carId);
//            jsonObject.put("UserId", MainActivity.userInfo.getId());
//            jsonObject.put("Key", MainActivity.userInfo.getKey());
//            jsonObject.put("JsonString", params[0]);
//        } catch (JSONException e) {
//            Log.d(Common.TAG, "生成Json失败！");
//        }
//
//        success = soapService.communicateWithServer(jsonObject.toString());

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        progressDialog.dismiss();

        if(success) {
            mCallback.onFinished();
            Log.d(Common.TAG, "保存成功！");
        } else {
            mCallback.onFailed();
            Log.d(Common.TAG, "提交失败!");
        }

    }

    @Override
    protected void onCancelled() {
        progressDialog.dismiss();
    }
}