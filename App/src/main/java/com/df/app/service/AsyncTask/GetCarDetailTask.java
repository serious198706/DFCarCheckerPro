package com.df.app.service.AsyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.df.app.MainActivity;
import com.df.app.service.SoapService;
import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 岩 on 14-1-13.
 */
// 获取详细信息线程
public class GetCarDetailTask extends AsyncTask<Void, Void, Boolean> {
    public interface OnGetDetailFinished {
        public void onFinish(String result);
        public void onFailed(String result);
    }

    private Context context;
    private SoapService soapService;
    private ProgressDialog progressDialog;
    private OnGetDetailFinished mCallback;
    private int carId;

    public GetCarDetailTask(Context context, int carId, OnGetDetailFinished listener) {
        this.context = context;
        this.carId = carId;
        this.mCallback = listener;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(context, null,
                "正在获取车辆信息，请稍候。。", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success;

        soapService = new SoapService();

        soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE, Common.GET_CAR_DETAIL);

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("CarId", carId);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());

        } catch (JSONException e) {

        }

        success = soapService.communicateWithServer(jsonObject.toString());

        return success;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        progressDialog.dismiss();

        if(success) {
            mCallback.onFinish(soapService.getResultMessage());
        } else {
            mCallback.onFailed(soapService.getErrorMessage());
            Log.d("DFCarChecker", "获取车辆配置信息失败：" + soapService.getErrorMessage());
        }
    }
}
