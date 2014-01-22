package com.df.app.service.AsyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.df.app.MainActivity;
import com.df.app.service.SoapService;
import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 岩 on 14-1-13.
 */
public class GetCarsCheckedListTask extends AsyncTask<Void, Void, Boolean> {
    public interface OnGetListFinish {
        public void onFinish(String result);
        public void onFailed();
    }

    Context context;
    private SoapService soapService;
    private ProgressDialog progressDialog;
    private int startNumber;
    private String result;
    private OnGetListFinish mCallback;

    public GetCarsCheckedListTask(Context context, int startNumber, OnGetListFinish listener) {
        this.context = context;
        this.startNumber = startNumber;
        this.mCallback = listener;
    }

    @Override
    protected void onPreExecute()
    {
        progressDialog = ProgressDialog.show(context, null,
                "正在获取已检车辆信息，请稍候...", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = false;

        try {
            JSONObject jsonObject = new JSONObject();

            // SeriesId + userID + key
            jsonObject.put("StartNumber", startNumber);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());

            soapService = new SoapService();

            // 设置soap的配置
            soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE, Common.GET_CHECKED_CARS);

            success = soapService.communicateWithServer(jsonObject.toString());

            // 传输失败，获取错误信息并显示
            if(!success) {
                Log.d("DFCarChecker", "获取车辆配置信息失败：" + soapService.getErrorMessage());
            } else {
                result = soapService.getResultMessage();
                startNumber += 10;
            }
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
            mCallback.onFinish(result);
        } else {
            mCallback.onFailed();
            Toast.makeText(context, "连接错误！", Toast.LENGTH_SHORT).show();
            Log.d("DFCarChecker", "连接错误: " + soapService.getErrorMessage());
        }
    }

    @Override
    protected void onCancelled() {
    }
}
