package com.df.app;

/**
 * Created by 岩 on 14-4-3.
 *
 * 检测到设备插入时
 */

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Toast;

public class AttachActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(this, "已检测到适配器！", Toast.LENGTH_SHORT).show();

        finish();
    }
}
