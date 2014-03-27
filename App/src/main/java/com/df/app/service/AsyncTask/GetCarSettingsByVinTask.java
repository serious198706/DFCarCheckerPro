package com.df.app.service.AsyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.df.app.MainActivity;
import com.df.app.service.SoapService;
import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 岩 on 14-1-21.
 *
 * 使用vin获取车辆配置
 */

public class GetCarSettingsByVinTask extends AsyncTask<Void, Void, Boolean> {
    public interface OnGetCarSettingsFinished {
        public void onFinished(String result);
        public void onFailed(String result);
    }

    Context context;
    String vin;
    ProgressDialog mProgressDialog;
    private SoapService soapService;
    private OnGetCarSettingsFinished mCallback;

    public GetCarSettingsByVinTask(Context context, String vin, OnGetCarSettingsFinished listener) {
        this.context = context;
        this.vin = vin;
        this.mCallback = listener;
    }

    @Override
    protected void onPreExecute()
    {
        mProgressDialog = ProgressDialog.show(context, null, "正在获取车辆信息，请稍候...", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success;

        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("Vin", vin);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());

            soapService = new SoapService();
            soapService.setUtils(Common.getSERVER_ADDRESS() + Common.CAR_CHECK_SERVICE, Common.GET_OPTIONS_BY_VIN);

            success = soapService.communicateWithServer(jsonObject.toString());
        } catch (JSONException e) {
            Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
            return false;
        }

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        mProgressDialog.dismiss();

        // 如果成功通信
        if (success) {
            mCallback.onFinished(soapService.getResultMessage());
        }
        // 如果失败
        else {
            mCallback.onFailed(soapService.getErrorMessage());
        }
    }
}