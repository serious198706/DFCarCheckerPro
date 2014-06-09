package com.df.kia;

/**
 * Created by 岩 on 13-12-18.
 *
 * 主界面
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.kia.carsChecked.CarsCheckedListActivity;
import com.df.kia.carsWaiting.CarsWaitingListActivity;
import com.df.library.entries.UserInfo;
import com.df.library.entries.VehicleModel;
import com.df.kia.procedures.InputProceduresActivity;
import com.df.library.asyncTask.LogoutTask;
import com.df.library.service.EncryptDecryptFile;
import com.df.library.service.VehicleModelParser;
import com.df.library.service.XmlHandler;
import com.df.kia.service.util.AppCommon;
import com.df.library.util.Common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.df.library.util.Helper.setTextView;

public class MainActivity extends Activity {
    public static VehicleModel vehicleModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button inputProceduresButton = (Button)findViewById(R.id.buttonInputProcedures);
        inputProceduresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterInputProcedures();
            }
        });

        Button carsWaitingButton = (Button)findViewById(R.id.buttonCarsWaiting);
        carsWaitingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterCarsWaiting();
            }
        });

        Button carsCheckedButton = (Button)findViewById(R.id.buttonCarsChecked);
        carsCheckedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterCarsChecked();
            }
        });

        Button quitButton = (Button)findViewById(R.id.buttonQuit);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quit();
            }
        });

        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            UserInfo userInfo = UserInfo.getInstance();
            userInfo.setId(bundle.getString("UserId"));
            userInfo.setKey(bundle.getString("Key"));
            userInfo.setName(bundle.getString("UserName"));
            userInfo.setOrid(bundle.getString("Orid"));
            userInfo.setPlateType(bundle.getString("PlateType"));
        }

        switch (Common.getEnvironment()) {
            case Common.EXTERNAL_VERSION:
                setTextView(getWindow().getDecorView(), R.id.environmentText, "外网测试");
                break;
            case Common.INTERNAL_100_3_VERSION:
                setTextView(getWindow().getDecorView(), R.id.environmentText, "内网测试(100.3)");
                break;
            case Common.INTERNAL_100_6_VERSION:
                setTextView(getWindow().getDecorView(), R.id.environmentText, "内网测试(100.6)");
                break;
            case Common.INTERNAL_100_110_VERSION:
                setTextView(getWindow().getDecorView(), R.id.environmentText, "内网测试(100.110)");
                break;
            case Common.PRODUCT_VERSION:
                break;
        }
    }

    /**
     * 进入手续录入
     */
    private void enterInputProcedures() {
        if(vehicleModel != null) {
            Intent intent = new Intent(this, InputProceduresActivity.class);
            startActivity(intent);
        } else {
            ParseXmlTask parseXmlTask = new ParseXmlTask(this, InputProceduresActivity.class);
            parseXmlTask.execute();
        }
    }

    /**
     * 进入待检车辆
     */
    private void enterCarsWaiting() {
        if(vehicleModel != null) {
            Intent intent = new Intent(this, CarsWaitingListActivity.class);
            startActivity(intent);
        } else {
            ParseXmlTask parseXmlTask = new ParseXmlTask(this, CarsWaitingListActivity.class);
            parseXmlTask.execute();
        }
    }

    /**
     * 进入已检车辆
     */
    private void enterCarsChecked() {
        if(vehicleModel != null) {
            Intent intent = new Intent(this, CarsCheckedListActivity.class);
            startActivity(intent);
        } else {
            ParseXmlTask parseXmlTask = new ParseXmlTask(this, CarsCheckedListActivity.class);
            parseXmlTask.execute();
        }
    }

    /**
     * 退出
     */
    private void quit() {
        View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(R.string.quitMsg);
        content.setTextSize(20f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, getResources().getString(R.string.alert));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view1)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        logout();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();
    }

    /**
     * 注销
     */
    private void logout() {
        LogoutTask logoutTask = new LogoutTask(this, new LogoutTask.OnLogoutFinished() {
            @Override
            public void onFinished() {
                Toast.makeText(MainActivity.this, "注销成功！", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed() {
                Toast.makeText(MainActivity.this, "注销失败！", Toast.LENGTH_SHORT).show();
                Log.d(AppCommon.TAG, "注销失败！");
                finish();
            }
        });
        logoutTask.execute();
    }

    /**
     *  解析车型XML
     */
    public void parseXml() {
        try {
            FileInputStream fis;

            String path = AppCommon.utilDirectory;
            String zippedFile = path + "df001";

            EncryptDecryptFile.decryptFile(zippedFile, "pdAFstScKvwO0o!D");

            XmlHandler.unzip(zippedFile + ".d", path);

            File f = new File(path + "vm");
            fis = new FileInputStream(f);

            if(fis == null) {
                Toast.makeText(this, "SD卡挂载有问题!", Toast.LENGTH_LONG).show();
                Log.d(AppCommon.TAG, "SD卡挂载有问题!");
            } else {
                // 解析
                VehicleModelParser parser = new VehicleModelParser();
                vehicleModel = parser.parseVehicleModelXml(fis);

                // delete vm
                f.delete();

                // delete df001.d
                f = new File(zippedFile + ".d");
                f.delete();
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "文件不存在!", Toast.LENGTH_LONG).show();
            Log.d(AppCommon.TAG, "文件不存在!");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        quit();
    }

    /**
     *  解析车型XML
     */
    public class ParseXmlTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private ProgressDialog progressDialog;
        private Class activity;

        public ParseXmlTask(Context context, Class activity) {
            this.context = context;
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(context, null, "请稍候...", false, false, null);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            parseXml();
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            progressDialog.dismiss();

            Intent intent = new Intent(this.context, activity);
            startActivity(intent);
        }
    }
}
