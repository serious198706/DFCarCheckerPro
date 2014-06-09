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
 * Created by 岩 on 14-3-12.
 */
public class DeleteCarTask extends AsyncTask<Void, Void, Boolean> {
    public interface OnDeleteFinished {
        public void onFinished(String result);
        public void onFailed(String result);
    }


    private Context context;
    private int carId;
    private SoapService soapService;
    private ProgressDialog progressDialog;
    private OnDeleteFinished mCallback;

    public DeleteCarTask(Context context, int carId, OnDeleteFinished listener) {
        this.context = context;
        this.carId = carId;
        this.mCallback = listener;
    }

    @Override
    protected void onPreExecute()
    {
        progressDialog = ProgressDialog.show(context, null, "正在删除，请稍候...", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success;

        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("CarId", this.carId);
            jsonObject.put("UserId", UserInfo.getInstance().getId());
            jsonObject.put("Key", UserInfo.getInstance().getKey());

            soapService = new SoapService();
            soapService.setUtils(Common.getSERVER_ADDRESS() + Common.CAR_CHECK_SERVICE, Common.DELETE_CAR);

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
