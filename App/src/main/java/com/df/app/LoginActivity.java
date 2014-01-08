package com.df.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.df.app.entries.UserInfo;
import com.df.app.service.CheckUpdateTask;
import com.df.app.service.SoapService;
import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // Values for email and password at the time of the login attempt.
    private String mUserName;
    private String mPassword;

    // UI references.
    private EditText mUserNameView;
    private EditText mPasswordView;

    // 用户信息：id、key
    public static UserInfo userInfo;
    private CheckUpdateTask mCheckUpdateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Set up the login form.
        mUserNameView = (EditText) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        TextView appVersionText = (TextView) findViewById(R.id.appVersion_text);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            appVersionText.setText("V" + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            appVersionText.setText("");
            e.printStackTrace();
        }

        mCheckUpdateTask = new CheckUpdateTask(LoginActivity.this);
        mCheckUpdateTask.execute();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mUserName = mUserNameView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_password_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mUserName)) {
            mUserNameView.setError(getString(R.string.error_username_required));
            focusView = mUserNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //kick off a background task to perform the user login attempt.
            mAuthTask = new UserLoginTask(LoginActivity.this);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog progressDialog;
        private Context context;
        private SoapService soapService;

        public UserLoginTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(context, null, "正在登录，请稍候...", false, false);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = false;

            WifiManager wifiMan = (WifiManager) context.getSystemService(
                    Context.WIFI_SERVICE);
            WifiInfo wifiInf = wifiMan.getConnectionInfo();
            String macAddr = wifiInf.getMacAddress();

            String serialNumber = null;

            try {
                Class<?> c = Class.forName("android.os.SystemProperties");
                Method get = c.getMethod("get", String.class);
                serialNumber = (String) get.invoke(c, "ro.serialno");
            } catch (Exception ignored) {
                Log.d(Common.TAG, "无法获取序列号！");
            }


            try {
                // 登录
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("UserName", mUserName);
                jsonObject.put("Password", mPassword);
                jsonObject.put("Key", macAddr);
                jsonObject.put("SerialNumber", serialNumber);

                soapService = new SoapService();

                // 设置soap的配置
                soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE, Common.USER_LOGIN);

                success = soapService.login(context, jsonObject.toString());

                // 登录失败，获取错误信息并显示
                if(!success) {
                    Log.d("DFCarChecker", "Login error:" + soapService.getErrorMessage());
                } else {
                    userInfo = new UserInfo();

                    try {
                        JSONObject userJsonObject = new JSONObject(soapService.getResultMessage());

                        // 保存用户的UserId和此次登陆的Key
                        userInfo.setId(userJsonObject.getString("UserId"));
                        userInfo.setKey(userJsonObject.getString("Key"));
                    } catch (Exception e) {
                        Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                        return false;
                    }
                }
            } catch (JSONException e) {
                Log.d("DFCarChecker", "Json解析错误: " + e.getMessage());
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            progressDialog.dismiss();
            mAuthTask = null;

            if (success) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("UserId", userInfo.getId());
                intent.putExtra("Key", userInfo.getKey());
                startActivity(intent);
                finish();
            } else {
                mPasswordView.setError(soapService.getErrorMessage());
                mPasswordView.requestFocus();
            }
        }
    }



}
