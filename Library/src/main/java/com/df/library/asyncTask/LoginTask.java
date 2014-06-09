package com.df.library.asyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.df.library.service.SoapService;
import com.df.library.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

/**
 * Created by 岩 on 14-1-13.
 *
 * 登录
 */

public class LoginTask extends AsyncTask<Void, Void, Boolean> {
    public interface OnLoginFinished {
        public void onFinished(String result);
        public void onFailed(String error);
    }

    private ProgressDialog progressDialog;
    private Context context;
    private SoapService soapService;
    private OnLoginFinished mCallback;
    private String userName;
    private String password;

    public LoginTask(Context context, String userName, String password, OnLoginFinished listener) {
        this.context = context;
        this.mCallback = listener;
        this.userName = userName;
        this.password = password;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(context, null, "正在登录，请稍候...", false, false);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = false;

        // 获取mac地址
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        String macAddr = wifiInf.getMacAddress();

        String serialNumber = null;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serialNumber = (String) get.invoke(c, "ro.serialno");
        } catch (Exception ignored) {
            Log.d(Common.TAG, "无法获取序列号！");
        }

        try {
            // 登录
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("UserName", userName);
            jsonObject.put("Password", password);
            jsonObject.put("Key", macAddr);
            jsonObject.put("SerialNumber", serialNumber);

            soapService = new SoapService();
            soapService.setUtils(Common.getSERVER_ADDRESS() + Common.CAR_CHECK_SERVICE, Common.USER_LOGIN);

            success = soapService.communicateWithServer(jsonObject.toString());
        } catch (JSONException e) {
            Log.d("DFCarChecker", "Json解析错误: " + e.getMessage());
        }

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        progressDialog.dismiss();

        if (success) {
            mCallback.onFinished(soapService.getResultMessage());
        } else {
            mCallback.onFailed(soapService.getErrorMessage());

        }
    }
}