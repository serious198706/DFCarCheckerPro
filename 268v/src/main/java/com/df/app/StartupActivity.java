package com.df.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import com.df.app.service.util.AppCommon;
import com.df.library.util.Common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StartupActivity extends Activity {
    private int environment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        environment = bundle.getInt("environment");

        Common.setEnvironment(environment);

        SetDirectory();
        Intent intent = new Intent(StartupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 检查存储、设置路径、建立文件夹
     **/
    private void SetDirectory() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            // 创建utils路径
            File utilDirectory = new File(AppCommon.utilDirectory);
            utilDirectory.mkdirs();

            // 将文件拷入相关路径
            CopyAssets(); // Then run the method to copy the file.
            Log.d(AppCommon.TAG, "路径util创建成功");

            // 创建临时保存路径
            File savedDirectory = new File(AppCommon.savedDirectory);
            savedDirectory.mkdirs();
            Log.d(AppCommon.TAG, "路径saved创建成功");

            // 创建照片路径
            File photoDirectory = new File(AppCommon.photoDirectory);
            photoDirectory.mkdirs();
            Log.d(AppCommon.TAG, "路径photo创建成功");
        } else if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED_READ_ONLY)) {
            Log.d(AppCommon.TAG, "未检测到SD卡");
        }
    }

    /**
     * 将所有Assets拷贝到指定路径
     **/
    private void CopyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < files.length; i++) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(files[i]);
                out = new FileOutputStream(AppCommon.utilDirectory + files[i]);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (Exception e) {
                Log.e(AppCommon.TAG, e.getMessage());
            }
        }
    }

    /**
     * 拷贝文件
     */
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
