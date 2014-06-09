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
 * Created by 岩 on 14-1-13.
 *
 * 获取从检人员
 */

public class GetCooperatorTask extends AsyncTask<JSONObject, Void, Boolean> {
    public interface OnGetListFinish {
        public void onFinish(String result);
        public void onFailed(String result);
    }

    Context context;
    private SoapService soapService;
    private ProgressDialog progressDialog;
    private OnGetListFinish mCallback;

    public GetCooperatorTask(Context context, OnGetListFinish listener) {
        this.context = context;
        this.mCallback = listener;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setMessage("正在获取从检人员列表，请稍候...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(JSONObject... params) {
        boolean success = false;

        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("UserId", UserInfo.getInstance().getId());
            jsonObject.put("Key", UserInfo.getInstance().getKey());

            soapService = new SoapService();
            soapService.setUtils(Common.getSERVER_ADDRESS() + Common.CAR_CHECK_SERVICE, Common.GET_COOPERATORS);

            success = soapService.communicateWithServer(jsonObject.toString());

        } catch (JSONException e) {
            Log.d(Common.TAG, "生成Json失败！");
        }

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        progressDialog.dismiss();

        if(success) {
            mCallback.onFinish(soapService.getResultMessage());
        } else {
            mCallback.onFailed(soapService.getResultMessage());
        }

    }

    @Override
    protected void onCancelled() {
        progressDialog.dismiss();
    }
}
