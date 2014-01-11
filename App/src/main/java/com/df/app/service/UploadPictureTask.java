package com.df.app.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.df.app.entries.PhotoEntity;
import com.df.app.util.Common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 14-1-9.
 */
// 上传图片
public class UploadPictureTask extends AsyncTask<Void, Integer, Boolean> {
    private SoapService soapService;
    private int total;
    private int complete;
    private Context context;
    private List<PhotoEntity> photoEntityList;

    private ProgressDialog progressDialog;

    public UploadPictureTask(Context context, List<PhotoEntity> photoEntityList) {
        this.photoEntityList = photoEntityList;
        this.context = context;

        total = photoEntityList.size();
        complete = 0;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setMessage("正在上传照片，请稍候...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMax(total);
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = false;

        soapService = new SoapService();

        // 设置soap的配置
        soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE, Common.UPLOAD_PICTURE);

        for(int i = 0; i < photoEntityList.size(); i++) {
            PhotoEntity photoEntity = photoEntityList.get(i);

            // 获取照片的物理路径
            Bitmap bitmap = null;
            String path = Common.photoDirectory;
            String fileName = photoEntity.getFileName();

            Log.d(Common.TAG, "正在上传...");
            Log.d(Common.TAG, photoEntity.getJsonString());

            // 如果照片名为空串，表示要上传空照片
            if(fileName.equals("")) {
                success = soapService.uploadPicture(photoEntity.getJsonString());
            } else {
                bitmap = BitmapFactory.decodeFile(path + fileName);

                success = soapService.uploadPicture(bitmap, photoEntity.getJsonString());
            }

            if(success) {
                // 如果成功上传，推动进度条
                Log.d(Common.TAG, "上传成功！");
                publishProgress(i);
            } else {
               i--;
            }
        }

        return success;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        progressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if(success) {
            progressDialog.dismiss();
            Toast.makeText(context, "全部上传成功！！", Toast.LENGTH_SHORT).show();
            Log.d(Common.TAG, "全部上传成功！");
        } else {
            progressDialog.dismiss();
            Toast.makeText(context, "上传失败！！", Toast.LENGTH_SHORT).show();
            Log.d(Common.TAG, "上传图片失败！");
        }

    }

    @Override
    protected void onCancelled() {

    }
}
