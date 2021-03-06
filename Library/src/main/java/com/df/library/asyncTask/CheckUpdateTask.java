package com.df.library.asyncTask;

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

import com.df.library.R;
import com.df.library.service.SoapService;
import com.df.library.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

import static com.df.library.util.Helper.setTextView;

/**
 * Created by 岩 on 13-12-18.
 *
 * 检查程序更新
 */

public class CheckUpdateTask extends AsyncTask<Void, Void, Boolean> {
    private int appType;
    private Context context;
    private SoapService soapService;
    private ProgressDialog mProgressDialog;
    private DownloadNewVersionTask mDownloadTask;

    public CheckUpdateTask(Context context, int appType) {
        this.context = context;
        this.appType = appType;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = ProgressDialog.show(context, null, "正在检测最新版本...", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = false;

        // 6月13日加 在接口部分增加apptype字段，用来区分版本（专业版、起亚版）
        try {
            soapService = new SoapService();
            soapService.setUtils(Common.getSERVER_ADDRESS() + Common.CAR_CHECK_SERVICE, Common.CHECK_VERSION);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("AppType", appType);

            success = soapService.communicateWithServer(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
                    dialog.setCancelable(false);
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
        String[] localVersionArray = localVersion.split("\\.");
        String[] serverVersionArray = serverVersion.split("\\.");

        return makeVersionCode(localVersionArray) < makeVersionCode(serverVersionArray);
    }

    private int makeVersionCode(String[] array) {
        int sum = 0;

        for(int i = 0; i < array.length; i++) {
            int n = Integer.parseInt(array[i]) * (int)Math.pow((double)10, (double)array.length - i - 1);
            sum += n;
        }

        return sum;
    }
}

