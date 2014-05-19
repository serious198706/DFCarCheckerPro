package com.df.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.df.app.service.AsyncTask.CheckUpdateTask;
import com.df.app.service.AsyncTask.LoginTask;
import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 岩 on 13-12-18.
 *
 * 登录界面
 */
public class LoginActivity extends Activity {

    // UI references.
    private EditText mUserNameView;
    private EditText mPasswordView;

    private LoginTask mLoginTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // 配置输入框
        mUserNameView = (EditText) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_ACTION_GO) {
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

            String versionText = "V" + pInfo.versionName + " - ";

            switch (Common.getEnvironment()) {
                case Common.EXTERNAL_VERSION:
                    versionText += "外网测试";
                    break;
                case Common.INTERNAL_100_3_VERSION:
                    versionText += "内网测试(100.3)";
                    break;
                case Common.INTERNAL_100_6_VERSION:
                    versionText += "内网测试(100.6)";
                    break;
                case Common.INTERNAL_100_110_VERSION:
                    versionText += "内网测试(100.110)";
                    break;
                case Common.PRODUCT_VERSION:
                    versionText = versionText.substring(0, versionText.length() - 3);
                    break;
            }

            appVersionText.setText(versionText);
        } catch (PackageManager.NameNotFoundException e) {
            appVersionText.setText("");
            e.printStackTrace();
        }

        CheckUpdateTask mCheckUpdateTask = new CheckUpdateTask(LoginActivity.this);
        mCheckUpdateTask.execute();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mLoginTask != null) {
            return;
        }

        // Reset errors.
        mUserNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String mUserName = mUserNameView.getText().toString();
        String mPassword = mPasswordView.getText().toString();

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
            // 出错时，让相应的控件获取焦点
            focusView.requestFocus();
        } else {
            // 进行登录
            mLoginTask = new LoginTask(LoginActivity.this, mUserName, mPassword, new LoginTask.OnLoginFinished() {
                @Override
                public void onFinished(String result) {
                    try {
                        JSONObject userJsonObject = new JSONObject(result);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("UserId", userJsonObject.getString("UserId"));
                        intent.putExtra("Key", userJsonObject.getString("Key"));
                        intent.putExtra("UserName", userJsonObject.getString("UserName"));
                        intent.putExtra("Orid", userJsonObject.getString("Orid"));
                        if(userJsonObject.has("PlateType"))
                            intent.putExtra("PlateType", userJsonObject.getString("PlateType"));
                        mLoginTask = null;
                        startActivity(intent);
                        finish();
                    } catch (JSONException e) {
                        Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                    }
                }

                @Override
                public void onFailed(String error) {
                    mLoginTask = null;
                    // 登录失败，获取错误信息并显示
                    Log.d(Common.TAG, "登录时出现错误：" + error);

                    mPasswordView.setError(error);
                    mPasswordView.requestFocus();
                }
            });
            mLoginTask.execute();
        }
    }

}
