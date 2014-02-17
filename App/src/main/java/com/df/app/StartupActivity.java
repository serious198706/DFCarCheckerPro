package com.df.app;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import com.df.app.util.Common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StartupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SetDirectory();
        Intent intent = new Intent(StartupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * -- Check to see if the sdCard is mounted and create a directory w/in it
     * ========================================================================
     **/
    private void SetDirectory() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            // 创建utils路径
            File utilDirectory = new File(Common.utilDirectory);
            utilDirectory.mkdirs();

            // 将文件拷入相关路径
            CopyAssets(); // Then run the method to copy the file.
            Log.d(Common.TAG, "路径util创建成功");

            // 创建临时保存路径
            File savedDirectory = new File(Common.savedDirectory);
            savedDirectory.mkdirs();
            Log.d(Common.TAG, "路径saved创建成功");

            // 创建照片路径
            File photoDirectory = new File(Common.photoDirectory);
            photoDirectory.mkdirs();
            Log.d(Common.TAG, "路径photo创建成功");
        } else if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED_READ_ONLY)) {
            Log.d(Common.TAG, "未检测到SD卡");
        }

    }

    /**
     * -- Copy the file from the assets folder to the sdCard
     * ===========================================================
     **/
    private void CopyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e(Common.TAG, e.getMessage());
        }
        for (int i = 0; i < files.length; i++) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(files[i]);
                out = new FileOutputStream(Common.utilDirectory + files[i]);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (Exception e) {
                Log.e(Common.TAG, e.getMessage());
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
