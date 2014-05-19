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
 * Created by 岩 on 2014/5/14.
 */
public class GetAuthorizeCode  extends AsyncTask<Void, Void, Boolean> {
    public interface OnGetCodeFinished {
        public void onFinished(String result);
        public void onFailed(String result);
    }

    Context context;
    private SoapService soapService;
    private ProgressDialog progressDialog;
    private OnGetCodeFinished mCallback;

    public GetAuthorizeCode(Context context, OnGetCodeFinished listener) {
        this.context = context;
        this.mCallback = listener;
    }

    @Override
    protected void onPreExecute()
    {
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = false;

        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());

            soapService = new SoapService();
            soapService.setUtils(Common.getSERVER_ADDRESS() + Common.CAR_CHECK_SERVICE, Common.GET_AUTHORIZE_CODE);

            success = soapService.communicateWithServer(jsonObject.toString());
        } catch (JSONException e) {
            Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
            return false;
        }

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {

        // 成功获取
        if (success) {
            mCallback.onFinished(soapService.getResultMessage());
        } else {
            mCallback.onFailed(soapService.getErrorMessage());
        }
    }
}
