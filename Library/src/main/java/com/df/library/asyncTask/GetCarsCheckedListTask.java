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
 * 获取已检车辆列表
 */

public class GetCarsCheckedListTask extends AsyncTask<Void, Void, Boolean> {
    public interface OnGetListFinish {
        public void onFinish(String result);
        public void onFailed(String error);
    }

    Context context;
    private SoapService soapService;
    private ProgressDialog progressDialog;
    private int startNumber;
    private OnGetListFinish mCallback;

    public GetCarsCheckedListTask(Context context, int startNumber, OnGetListFinish listener) {
        this.context = context;
        this.startNumber = startNumber;
        this.mCallback = listener;
    }

    @Override
    protected void onPreExecute()
    {
        progressDialog = ProgressDialog.show(context, null, "正在获取已检车辆，请稍候...", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success;

        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("StartNumber", startNumber);
            jsonObject.put("UserId", UserInfo.getInstance().getId());
            jsonObject.put("Key", UserInfo.getInstance().getKey());

            soapService = new SoapService();
            soapService.setUtils(Common.getSERVER_ADDRESS() + Common.CAR_CHECK_SERVICE, Common.GET_CHECKED_CARS);

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

        if (success) {
            mCallback.onFinish(soapService.getResultMessage());
        } else {
            mCallback.onFailed(soapService.getErrorMessage());
        }
    }
}
