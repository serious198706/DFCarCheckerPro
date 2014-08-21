package com.df.app.service.util;

import android.content.Context;
import android.os.Environment;

import com.df.app.R;

/**
 * Created by 岩 on 13-12-18.
 *
 * 常量
 */
public class AppCommon {
    public static final String utilDirectory = Environment.getExternalStorageDirectory().getPath() +
            "/.cheyipaiPro/";
    public static final String photoDirectory = Environment.getExternalStorageDirectory().getPath() +
            "/Pictures/DFCarCheckerPro/";
    public static final String savedDirectory = utilDirectory +
            "saved/";

    public static final String licenseUtilPath = Environment.getExternalStorageDirectory().toString() + "/AndroidWT";
    public static final String licensePhotoPath = photoDirectory + "license.jpg";

    public static final String TAG = "DFCarCheckerPro";

    public static int appType = 1;
}
