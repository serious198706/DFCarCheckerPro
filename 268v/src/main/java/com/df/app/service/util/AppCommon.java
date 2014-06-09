package com.df.app.service.util;

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

    // 配置信息页与综合检查页spinner的联动关系
    public static final int[][] carSettingsSpinnerMap = {
            {R.id.driveType_spinner,        0,                              R.array.driveType_item},
            {R.id.transmission_spinner,     0,                              R.array.transmission_item},
            {R.id.airBag_spinner,           R.id.airBag_spinner,            R.array.airbag_number},
            {R.id.abs_spinner,              R.id.abs_spinner,               R.array.existornot},
            {R.id.powerSteering_spinner,    0,                              R.array.existornot},
            {R.id.powerWindows_spinner,     R.id.powerWindows_spinner,      R.array.powerWindows_items},
            {R.id.sunroof_spinner,          R.id.sunroof_spinner,           R.array.sunroof_items},
            {R.id.airConditioning_spinner,  R.id.airConditioning_spinner,   R.array.airConditioning_items},
            {R.id.leatherSeats_spinner,     0,                              R.array.leatherSeats_items},
            {R.id.powerSeats_spinner,       R.id.powerSeats_spinner,        R.array.powerSeats_items},
            {R.id.powerMirror_spinner,      R.id.powerMirror_spinner,       R.array.powerMirror_items},
            {R.id.reversingRadar_spinner,   R.id.reversingRadar_spinner,    R.array.reversingRadar_items},
            {R.id.reversingCamera_spinner,  R.id.reversingCamera_spinner,   R.array.reversingCamera_items},
            {R.id.ccs_spinner,              0,                              R.array.ccs_items},
            {R.id.softCloseDoors_spinner,   R.id.softCloseDoors_spinner,    R.array.existornot},
            {R.id.rearPowerSeats_spinner,   R.id.rearPowerSeats_spinner,    R.array.existornot},
            {R.id.ahc_spinner,              R.id.ahc_spinner,               R.array.existornot},
            {R.id.parkAssist_spinner,       R.id.parkAssist_spinner,        R.array.existornot},
            {R.id.clapboard_spinner,        R.id.clapboard_spinner,         R.array.existornot}
    };
}
