package com.df.app.service;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import com.df.app.R;
import com.df.app.util.Common;

import org.json.JSONObject;

/**
 * Created by 岩 on 14-1-8.
 */
public class CommitTask extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private SoapService soapService;
    private ProgressDialog mProgressDialog;

    private String jsonString;
    private String methodName;

    public CommitTask(Context context, String methodName, String jsonString) {
        this.context = context;
        this.methodName = methodName;
        this.jsonString = jsonString;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = ProgressDialog.show(context, null, "正在提交，请稍候...", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = true;

        soapService = new SoapService();
        soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE, methodName);

        success = soapService.communicateWithServer(jsonString);

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        mProgressDialog.dismiss();

        if(success) {
            try {

            } catch (Exception e) {

            }
        } else {
            Toast.makeText(context, "获取版本号失败！", Toast.LENGTH_SHORT).show();
        }
    }
}
