package com.df.app;

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

import com.df.app.carsChecked.CarsCheckedListActivity;
import com.df.app.carsWaiting.CarsWaitingListActivity;
import com.df.app.procedures.InputProceduresActivity;
import com.df.app.entries.UserInfo;
import com.df.app.entries.VehicleModel;
import com.df.app.service.EncryptDecryptFile;
import com.df.app.service.AsyncTask.LogoutTask;
import com.df.app.service.VehicleModelParser;
import com.df.app.service.XmlHandler;
import com.df.app.util.Common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.df.app.util.Helper.setTextView;

public class MainActivity extends Activity {
    public static UserInfo userInfo;
    public static VehicleModel vehicleModel;

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
            userInfo = new UserInfo();
            userInfo.setId(bundle.getString("UserId"));
            userInfo.setKey(bundle.getString("Key"));
            userInfo.setName(bundle.getString("UserName"));
            userInfo.setOrid(bundle.getString("Orid"));
        }

        if(Common.getEnvironment().equals("i")) {
            setTextView(getWindow().getDecorView(), R.id.environmentText, "内网测试");
        } else if(Common.getEnvironment().equals("i_i")) {
            setTextView(getWindow().getDecorView(), R.id.environmentText, "内网测试");
        } else if(Common.getEnvironment().equals("o")) {
            setTextView(getWindow().getDecorView(), R.id.environmentText, "外网测试");
        }
    }

    private void enterInputProcedures() {
        ParseXmlTask parseXmlTask = new ParseXmlTask(this, InputProceduresActivity.class);
        parseXmlTask.execute();
    }

    private void enterCarsWaiting() {
        ParseXmlTask parseXmlTask = new ParseXmlTask(this, CarsWaitingListActivity.class);
        parseXmlTask.execute();
    }

    private void enterCarsChecked() {
        ParseXmlTask parseXmlTask = new ParseXmlTask(this, CarsCheckedListActivity.class);
        parseXmlTask.execute();
    }

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
                Log.d(Common.TAG, "注销失败！");
                finish();
            }
        });
        logoutTask.execute();
    }

    // 解析车型XML
    public void parseXml() {
        try {
            FileInputStream fis;

            String path = Common.utilDirectory;
            String zippedFile = path + "df001";

            EncryptDecryptFile.decryptFile(zippedFile, "pdAFstScKvwO0o!D");

            XmlHandler.unzip(zippedFile + ".d", path);

            File f = new File(path + "vm");
            fis = new FileInputStream(f);

            if(fis == null) {
                Toast.makeText(this, "SD卡挂载有问题!", Toast.LENGTH_LONG).show();
                Log.d(Common.TAG, "SD卡挂载有问题!");
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
            Log.d(Common.TAG, "文件不存在!");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(Common.TAG, "文件操作出错!");
        }
    }

    @Override
    public void onBackPressed() {
        quit();
    }

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
