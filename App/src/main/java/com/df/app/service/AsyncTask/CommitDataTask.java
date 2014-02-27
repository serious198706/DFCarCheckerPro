package com.df.app.service.AsyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.df.app.carCheck.BasicInfoLayout;
import com.df.app.MainActivity;
import com.df.app.service.SoapService;
import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 岩 on 14-1-13.
 *
 * 提交检测数据
 */

public class CommitDataTask extends AsyncTask<JSONObject, Void, Boolean> {
    public interface OnCommitDataFinished {
        public void onFinished(String result);
        public void onFailed(String result);
    }

    Context context;
    private SoapService soapService;
    private ProgressDialog progressDialog;
    private OnCommitDataFinished mCallback;

    public CommitDataTask(Context context, OnCommitDataFinished listener) {
        this.context = context;
        this.mCallback = listener;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setMessage("正在提交信息，请稍候...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(JSONObject... params) {
        boolean success;

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("CarId", BasicInfoLayout.carId);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("JsonString", params[0]);
        } catch (JSONException e) {
            Log.d(Common.TAG, "生成Json失败！");
        }

        soapService = new SoapService();
        soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE, Common.COMMIT_DATA);

        success = soapService.communicateWithServer(jsonObject.toString());

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        progressDialog.dismiss();

        if(success) {
            mCallback.onFinished(soapService.getResultMessage());
            Log.d(Common.TAG, "提交成功！" + soapService.getErrorMessage());
        } else {
            mCallback.onFailed(soapService.getErrorMessage());
            Log.d(Common.TAG, "提交失败!" + soapService.getErrorMessage());
        }

    }
}