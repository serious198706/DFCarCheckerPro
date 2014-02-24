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
 * 导入平台
 */

public class ImportPlatformTask extends AsyncTask<Void, Void, Boolean> {
    public interface OnImportFinished {
        public void onFinished(String result);
        public void onFailed(String result);
    }

    private Context context;
    private int carId;
    private final String type;
    private int sellerId;
    private SoapService soapService;
    private ProgressDialog progressDialog;
    private OnImportFinished mCallback;

    public ImportPlatformTask(Context context, int carId, String type, int sellerId, OnImportFinished listener) {
        this.context = context;
        this.carId = carId;
        this.type = type;
        this.sellerId = sellerId;
        this.mCallback = listener;
    }

    @Override
    protected void onPreExecute()
    {
        progressDialog = ProgressDialog.show(context, null, "正在导入平台，请稍候...", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success;

        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("CarId", this.carId);
            jsonObject.put("Type", this.type);
            jsonObject.put("SellerId", this.sellerId);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());

            soapService = new SoapService();
            soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE, Common.IMPORT_PLATFORM);

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

        if(success) {
            mCallback.onFinished(soapService.getResultMessage());
        } else {
            mCallback.onFailed(soapService.getErrorMessage());
        }
    }

    @Override
    protected void onCancelled() {
    }
}
