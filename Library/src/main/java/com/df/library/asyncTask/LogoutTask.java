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

import java.io.File;

/**
 * Created by 岩 on 14-1-13.
 *
 * 注销
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

    /**
     * 递归删除所有文件
     * @param fileOrDirectory
     */
    private void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = ProgressDialog.show(context, null, "正在注销...", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success;

        // 删除文件
        //DeleteRecursive(new File(Common.utilDirectory));

        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("UserId", UserInfo.getInstance().getId());
            jsonObject.put("Key", UserInfo.getInstance().getKey());

            soapService = new SoapService();
            soapService.setUtils(Common.getSERVER_ADDRESS() + Common.CAR_CHECK_SERVICE, Common.USER_LOGOUT);

            success = soapService.communicateWithServer(jsonObject.toString());
        } catch (JSONException e) {
            Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
            return false;
        }

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        mProgressDialog.dismiss();

        if(success) {
            mCallback.onFinished();
        } else {
            mCallback.onFailed();
        }
    }
}