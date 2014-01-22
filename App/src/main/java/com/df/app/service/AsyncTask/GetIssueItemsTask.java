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
public class GetIssueItemsTask extends AsyncTask<Void, Void, Boolean> {
    public interface OnGetIssueItemsFinished {
        public void onFinish(String result, ProgressDialog progressDialog);
        public void onFailed();
    }

    Context context;
    private SoapService soapService;
    private ProgressDialog progressDialog;
    private String result;
    private OnGetIssueItemsFinished mCallback;
    private JSONObject accidentData;

    public GetIssueItemsTask(Context context, JSONObject data, OnGetIssueItemsFinished listener) {
        this.context = context;
        this.mCallback = listener;
        this.accidentData = data;
    }

    @Override
    protected void onPreExecute()
    {
        progressDialog = ProgressDialog.show(context, null,
                "正在处理，请稍候...", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = false;

        try {
            JSONObject jsonObject = new JSONObject();

            // SeriesId + userID + key
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("SeriesId", /*CarsWaitingActivity.seriesId*/3);
            jsonObject.put("Data", accidentData);

            soapService = new SoapService();

            // 设置soap的配置
            soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE, Common.ANALYSIS_ACCIDENT_DATA);

            success = soapService.communicateWithServer(jsonObject.toString());

        } catch (JSONException e) {
            Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
            return false;
        }

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (success) {
            mCallback.onFinish(soapService.getResultMessage(), progressDialog);
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