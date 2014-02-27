package com.df.app.service.AsyncTask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.R;
import com.df.app.service.SoapService;
import com.df.app.util.Common;

import org.json.JSONObject;

import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 13-12-18.
 *
 * 检查程序更新
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
        boolean success;

        soapService = new SoapService();
        soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE, Common.GET_APP_NEW_VERSION);

        success = soapService.communicateWithServer();

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
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                    View view1 = inflater.inflate(R.layout.popup_layout, null);
                    TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
                    TextView content = new TextView(view1.getContext());
                    content.setText("检测到新版本，点击确定进行更新");
                    content.setTextSize(20f);
                    contentArea.addView(content);

                    setTextView(view1, R.id.title, context.getResources().getString(R.string.newUpdate));

                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setView(view1)
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
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "获取版本号失败！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 比较版本号
     * @param localVersion 本地版本号
     * @param serverVersion 服务器版本号
     * @return 是否需要升级
     */
    private boolean compareVersion(String localVersion, String serverVersion) {
        String[] localVersionArray = localVersion.split(".");
        String[] serverVersionArray = serverVersion.split(".");

        if(Integer.parseInt(localVersionArray[0]) < Integer.parseInt(serverVersionArray[0])) {
            return true;
        }

        if(Integer.parseInt(localVersionArray[1]) < Integer.parseInt(serverVersionArray[1])) {
            return true;
        }

        if(Integer.parseInt(localVersionArray[2]) < Integer.parseInt(serverVersionArray[2])) {
            return true;
        }

        return false;
    }
}

