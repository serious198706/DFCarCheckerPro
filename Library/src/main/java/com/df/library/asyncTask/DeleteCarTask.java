package com.df.library.asyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.df.library.entries.PhotoEntity;
import com.df.library.service.SoapService;
import com.df.library.util.Common;
import com.df.library.entries.UserInfo;
import com.df.library.util.DeleteFiles;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 在待检列表中删除车辆，同时要删除的，还有这辆车的存储文件和照片文件
 * Created by 岩 on 14-3-12.
 */
public class DeleteCarTask extends AsyncTask<Void, Void, Boolean> {
    public interface OnDeleteFinished {
        public void onFinished(String result);
        public void onFailed(String result);
    }

    private Context context;
    private int carId;
    private String utilPath;
    private String photoPath;
    private SoapService soapService;
    private ProgressDialog progressDialog;
    private OnDeleteFinished mCallback;

    public DeleteCarTask(Context context, String utilPath, String photoPath, int carId, OnDeleteFinished listener) {
        this.context = context;
        this.carId = carId;
        this.utilPath = utilPath;
        this.photoPath = photoPath;
        this.mCallback = listener;
    }

    @Override
    protected void onPreExecute()
    {
        progressDialog = ProgressDialog.show(context, null, "正在删除，请稍候...", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = false;

        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("CarId", this.carId);
            jsonObject.put("UserId", UserInfo.getInstance().getId());
            jsonObject.put("Key", UserInfo.getInstance().getKey());

            soapService = new SoapService();
            soapService.setUtils(Common.getSERVER_ADDRESS() + Common.CAR_CHECK_SERVICE, Common.DELETE_CAR);

            success = soapService.communicateWithServer(jsonObject.toString());

            File file = new File(utilPath + Integer.toString(carId));

            // 如果本地存在这辆车的数据
            if (file.exists() && success) {

                String result = "";
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
                BufferedReader reader = new BufferedReader(read);
                String line;

                while ((line = reader.readLine()) != null) {
                    result += line;
                }

                reader.close();

                JSONObject json = new JSONObject(result);

                // 如果有照片节点，则更新照片list
                if (json.has("photos")) {
                    List<PhotoEntity> photoEntities = new ArrayList<PhotoEntity>();

                    if (json.get("photos") instanceof JSONArray) {
                        JSONArray jsonArray = json.getJSONArray("photos");

                        int length = jsonArray.length();

                        for (int i = 0; i < length; i++) {
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
                    }

                    // 删除相关文件
                    for(PhotoEntity photoEntity : photoEntities) {
                        if(!photoEntity.getFileName().equals(""))
                            DeleteFiles.deleteFile(photoPath + photoEntity.getFileName());

                        if(!photoEntity.getThumbFileName().equals(""))
                            DeleteFiles.deleteFile(photoPath + photoEntity.getThumbFileName());
                    }
                }

                success = file.delete();
            }
        } catch (JSONException e) {
            Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
            return false;
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        progressDialog.dismiss();

        if(success) {
            mCallback.onFinished(soapService.getResultMessage());
        } else {
            mCallback.onFailed(soapService.getErrorMessage());
        }
    }

    @Override
    protected void onCancelled() {
    }
}
