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
 * Created by 岩 on 14-1-20.
 *
 * 检查卖家姓名
 */

public class CheckSellerNameTask extends AsyncTask<Void, Void, Boolean> {
    public interface OnCheckSellerNameFinished {
        public void onFinished(String result);
        public void onFailed(String result);
    }

    Context context;
    String sellerNameValue;
    private SoapService soapService;
    private ProgressDialog progressDialog;
    private OnCheckSellerNameFinished mCallback;

    public CheckSellerNameTask(Context context, String sellerNameValue, OnCheckSellerNameFinished listener) {
        this.context = context;
        this.sellerNameValue = sellerNameValue;
        this.mCallback = listener;
    }

    @Override
    protected void onPreExecute()
    {
        progressDialog = ProgressDialog.show(context, null,
                "正在获取用户信息，请稍候...", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = false;

        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("SellerName", this.sellerNameValue);

            soapService = new SoapService();
            soapService.setUtils(Common.getSERVER_ADDRESS() + Common.CAR_CHECK_SERVICE, Common.CHECK_SELLER_NAME);

            success = soapService.communicateWithServer(jsonObject.toString());
        } catch (JSONException e) {
            Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
            return false;
        }

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        progressDialog.dismiss();

        // 成功获取
        if (success) {
            mCallback.onFinished(soapService.getResultMessage());
        } else {
            mCallback.onFailed(soapService.getErrorMessage());
        }
    }
}
