package com.df.app.service.AsyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.df.app.CarCheck.BasicInfoLayout;
import com.df.app.MainActivity;
import com.df.app.service.SoapService;
import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 岩 on 14-1-13.
 */
public class GetCooperatorTask extends AsyncTask<JSONObject, Void, Boolean> {
    public interface OnGetListFinish {
        public void onFinish(String result);
        public void onFailed();
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
        boolean success;

        // 组织最终json
        soapService = new SoapService();

        // 设置soap的配置
        soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE, Common.GET_COOPERATORS);

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
        } catch (JSONException e) {
            Log.d(Common.TAG, "生成Json失败！");
        }

        success = soapService.communicateWithServer(jsonObject.toString());

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        progressDialog.dismiss();

        if(success) {
            mCallback.onFinish(soapService.getResultMessage());
            Log.d(Common.TAG, "提交成功！" + soapService.getErrorMessage());
        } else {
            mCallback.onFailed();
            Log.d(Common.TAG, "提交失败!" + soapService.getErrorMessage());
        }

    }

    @Override
    protected void onCancelled() {
        progressDialog.dismiss();
    }
}
