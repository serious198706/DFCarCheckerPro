package com.df.app.service.AsyncTask;

/**
 * Created by 岩 on 14-1-13.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.df.app.MainActivity;
import com.df.app.entries.UserInfo;
import com.df.app.service.SoapService;
import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class LoginTask extends AsyncTask<Void, Void, Boolean> {
    public interface OnLoginFinished {
        public void onFinished(UserInfo userinfo);
        public void onFailed(String errorMsg);
    }

    private ProgressDialog progressDialog;
    private Context context;
    private SoapService soapService;
    private OnLoginFinished mCallback;
    private UserInfo userInfo;
    private String userName;
    private String password;

    public LoginTask(Context context, OnLoginFinished listener, String userName, String password) {
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

        WifiManager wifiMan = (WifiManager) context.getSystemService(
                Context.WIFI_SERVICE);
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

            // 设置soap的配置
            soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE, Common.USER_LOGIN);

            success = soapService.login(context, jsonObject.toString());

            // 登录失败，获取错误信息并显示
            if(!success) {
                Log.d("DFCarChecker", "Login error:" + soapService.getErrorMessage());
            } else {
                userInfo = new UserInfo();

                try {
                    JSONObject userJsonObject = new JSONObject(soapService.getResultMessage());

                    // 保存用户的UserId和此次登陆的Key
                    userInfo.setId(userJsonObject.getString("UserId"));
                    userInfo.setKey(userJsonObject.getString("Key"));
                    userInfo.setName(userJsonObject.getString("UserName"));
                    userInfo.setOrid(userJsonObject.getString("Orid"));
                } catch (Exception e) {
                    Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                    return false;
                }
            }
        } catch (JSONException e) {
            Log.d("DFCarChecker", "Json解析错误: " + e.getMessage());
        }

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        progressDialog.dismiss();

        if (success) {
            mCallback.onFinished(userInfo);
        } else {
            mCallback.onFailed(soapService.getErrorMessage());

        }
    }
}