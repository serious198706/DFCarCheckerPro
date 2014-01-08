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
 * Created by 岩 on 13-12-18.
 */
public class CheckUpdateTask extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private SoapService soapService;
    private ProgressDialog mProgressDialog;
    private DownloadNewVersionTask mDownloadTask;

    public CheckUpdateTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = ProgressDialog.show(context, null, "正在检测最新版本...", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = true;

        soapService = new SoapService();
        soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE, Common.GET_APP_NEW_VERSION);

        success = soapService.checkUpdate();

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        mProgressDialog.dismiss();

        if(success) {
            try {
                // {
                //   "VersionNumber":"numberValue",
                //   "DownloadAddress":""addressValue",
                //   "Description":"descValue"
                // }

                JSONObject jsonObject = new JSONObject(soapService.getResultMessage());

                final String version = jsonObject.getString("VersionNumber");
                final String appAddress = jsonObject.getString("DownloadAddress");

                PackageInfo pInfo = context.getPackageManager().getPackageInfo
                        (context.getPackageName(), 0);

                // 版本不同，升级
                if(compareVersion(pInfo.versionName, version)) {
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle(R.string.newUpdate)
                            .setMessage("检测到新版本，点击确定进行更新")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mDownloadTask = new DownloadNewVersionTask(context);
                                    mDownloadTask.execute(appAddress);
                                }
                            })
                            .create();

                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
            } catch (Exception e) {

            }
        } else {
            Toast.makeText(context, "获取版本号失败！", Toast.LENGTH_SHORT).show();
        }
    }


    // 比较版本号
    private boolean compareVersion(String localVersion, String serverVersion) {
        if(localVersion.charAt(0) < serverVersion.charAt(0))
            return true;

        if(localVersion.charAt(2) < serverVersion.charAt(2))
            return true;

        if(localVersion.charAt(4) < serverVersion.charAt(4))
            return true;

        return false;
    }
}

