package com.df.kia.procedures;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.df.kia.R;
import com.df.library.asyncTask.GetAuthorizeCode;
import com.df.kia.service.util.AppCommon;
import com.df.library.util.Common;

import static com.df.library.util.Helper.setTextView;

/**
 * Created by 岩 on 13-12-20.
 *
 * 手续信息主页面，包括车辆识别、手续录入
 */
public class InputProceduresActivity extends Activity {
    private InputProceduresLayout inputProceduresLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procedures);

        Button homeButton = (Button) findViewById(R.id.buttonHome);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quitConfirm();
            }
        });

        LinearLayout container = (LinearLayout)findViewById(R.id.container);
        inputProceduresLayout = new InputProceduresLayout(this);
        container.addView(inputProceduresLayout);

        //inputProceduresLayout = (InputProceduresLayout)findViewById(R.id.inputProcedures);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null && bundle.containsKey("carId")) {
            inputProceduresLayout.fillInData(bundle.getInt("carId"));
        }
    }

    @Override
    public void onBackPressed() {
        if(inputProceduresLayout.canGoBack()) {
            inputProceduresLayout.goBack();
        } else {
            quitConfirm();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAuthService();
    }

    private void startAuthService(){
        GetAuthorizeCode getAuthorizeCode = new GetAuthorizeCode(this, new GetAuthorizeCode.OnGetCodeFinished() {
            @Override
            public void onFinished(String result) {
                inputProceduresLayout.startAuthService(result);
            }

            @Override
            public void onFailed(String result) {
//                Toast.makeText(InputProceduresActivity.this, "获取授权码失败！", Toast.LENGTH_SHORT).show();
//                Log.d(AppCommon.TAG, "获取授权码失败！" + result);

                // TODO 删除
                //fillInDummyAuthCode();
            }
        });

        getAuthorizeCode.execute();
    }

    private void fillInDummyAuthCode() {
        inputProceduresLayout.startAuthService("WS4NWVPTLDUY712YYZXGYYI7G");
        //inputProceduresLayout.startAuthService("WSM27VPMJDVYMBHYY37KYYA6B");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // land
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // port
        }
    }

    /**
     * 退出前的确认
     */
    private void quitConfirm() {
        View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(R.string.quitInputProcedures);
        content.setTextSize(20f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, getResources().getString(R.string.alert));

        AlertDialog dialog = new AlertDialog.Builder(InputProceduresActivity.this)
                .setView(view1)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Common.TAKE_LICENSE_PHOTO && resultCode == Activity.RESULT_OK) {
            inputProceduresLayout.updateLicensePhoto(data.getBooleanExtra("cut", true));
        }
    }

    @SuppressWarnings("unused")
    protected String getLatestImage() {
        String latestImage = null;
        String[] items = { MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, items, null,
                null, MediaStore.Images.Media._ID + " desc");
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                latestImage = cursor.getString(1);
                break;
            }
        }
        return latestImage;
    }
}
