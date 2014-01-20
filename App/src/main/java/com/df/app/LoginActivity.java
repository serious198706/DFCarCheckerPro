package com.df.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.df.app.entries.UserInfo;
import com.df.app.service.AsyncTask.CheckUpdateTask;
import com.df.app.service.AsyncTask.LoginTask;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
    // 用户名及密码
    private String mUserName;
    private String mPassword;

    // UI references.
    private EditText mUserNameView;
    private EditText mPasswordView;

    private LoginTask mLoginTask = null;
    private CheckUpdateTask mCheckUpdateTask;

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
        if (mLoginTask != null) {
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
            mLoginTask = new LoginTask(LoginActivity.this, new LoginTask.OnLoginFinished() {
                @Override
                public void onFinished(UserInfo userinfo) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("UserId", userinfo.getId());
                    intent.putExtra("Key", userinfo.getKey());
                    intent.putExtra("UserName", userinfo.getName());
                    intent.putExtra("Orid", userinfo.getOrid());
                    mLoginTask = null;
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailed(String errorMsg) {
                    mPasswordView.setError(errorMsg);
                    mPasswordView.requestFocus();
                    mLoginTask = null;
                }
            }, mUserName, mPassword);
            mLoginTask.execute((Void) null);
        }
    }

}
