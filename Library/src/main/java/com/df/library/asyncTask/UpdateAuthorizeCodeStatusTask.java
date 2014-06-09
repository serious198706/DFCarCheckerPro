package com.df.library.asyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.df.library.service.SoapService;
import com.df.library.util.Common;
import com.df.library.entries.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 岩 on 2014/5/14.
 */
public class UpdateAuthorizeCodeStatusTask   extends AsyncTask<Void, Void, Boolean> {
    public interface OnUpdateFinished {
        public void onFinished(String result);

        public void onFailed(String result);
    }

    Context context;
    private SoapService soapService;
    private String authorizeCode;
    private ProgressDialog progressDialog;
    private OnUpdateFinished mCallback;

    public UpdateAuthorizeCodeStatusTask(Context context, OnUpdateFinished listener) {
        this.context = context;
        this.mCallback = listener;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = false;

        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("UserId", UserInfo.getInstance().getId());
            jsonObject.put("Key", UserInfo.getInstance().getKey());

            soapService = new SoapService();
            soapService.setUtils(Common.getSERVER_ADDRESS() + Common.CAR_CHECK_SERVICE, Common.UPDATE_AUTHORIZE_CODE_STATUS);

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
