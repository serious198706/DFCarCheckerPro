package com.df.app.service.AsyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.df.app.MainActivity;
import com.df.app.service.SoapService;
import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by 岩 on 14-1-13.
 */
public class LogoutTask extends AsyncTask<Void, Void, Boolean> {
    public interface OnLogoutFinished {
        public void onFinished();
        public void onFailed();
    }

    Context context;
    SoapService soapService;
    private ProgressDialog mProgressDialog;
    private OnLogoutFinished mCallback;

    public LogoutTask(Context context, OnLogoutFinished listener) {
        this.context = context;
        this.mCallback = listener;
    }

    private void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = ProgressDialog.show(context, null,
                "正在注销...", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = false;

        // 删除产生的垃圾文件
        DeleteRecursive(new File(Environment.getExternalStorageDirectory().getPath() +
                "/.cheyipai"));

        soapService = new SoapService();

        soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE, Common.USER_LOGOUT);

        JSONObject jsonObject = new JSONObject();

        try {
            if(MainActivity.userInfo != null) {
                jsonObject.put("UserId", MainActivity.userInfo.getId());
                jsonObject.put("Key", MainActivity.userInfo.getKey());
            }
        } catch (JSONException e) {

        }

        success = soapService.communicateWithServer(jsonObject.toString());

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        mProgressDialog.dismiss();

        if(success) {
            mCallback.onFinished();
            Log.d(Common.TAG, "注销成功！");
        } else {
            mCallback.onFailed();
            Log.d(Common.TAG, "注销失败！");
        }
    }

    @Override
    protected void onCancelled() {
        mProgressDialog.dismiss();
    }
}