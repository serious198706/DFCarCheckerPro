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
 * Created by 岩 on 14-1-13.
 *
 * 获取待检车辆列表
 */

public class GetCarsWaitingListTask extends AsyncTask<Void, Void, Boolean> {
    public interface OnGetListFinish {
        public void onFinish(String result);
        public void onFailed(String error);
    }

    Context context;
    private SoapService soapService;
    private ProgressDialog progressDialog;
    private int startNumber;
    private OnGetListFinish mCallback;

    public GetCarsWaitingListTask(Context context, int startNumber, OnGetListFinish listener) {
        this.context = context;
        this.startNumber = startNumber;
        this.mCallback = listener;
    }

    @Override
    protected void onPreExecute()
    {
        progressDialog = ProgressDialog.show(context, null, "正在获取待检车辆，请稍候...", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success;

        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("StartNumber", startNumber);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("PlateType", MainActivity.userInfo.getPlateType());

            soapService = new SoapService();
            soapService.setUtils(Common.getSERVER_ADDRESS() + Common.CAR_CHECK_SERVICE, Common.GET_WAITING_CARS);

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
