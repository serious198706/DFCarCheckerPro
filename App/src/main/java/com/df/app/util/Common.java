package com.df.app.util;

import android.os.Environment;

import com.df.app.R;

/**
 * Created by 岩 on 13-12-18.
 */
public class Common {



    public static String utilDirectory = Environment.getExternalStorageDirectory().getPath() +
            "/.cheyipaiPro/";
    public static String photoDirectory = Environment.getExternalStorageDirectory().getPath() +
            "/Pictures/DFCarCheckerPro/";

    public static String TAG = "DFCarCheckerPro";

    public static String NAMESPACE = "http://cheyipai";

    public static final String SERVER_ADDRESS = "http://192.168.100.6:8052/services/";
    //public static final String SERVER_ADDRESS = "http://192.168.8.33:810/Services/";
    public static final String PICTURE_ADDRESS = "http://192.168.100.6:8006/";

//    public static final String SERVER_ADDRESS = "http://192.168.100.6:50/";
//    public static final String PICTURE_ADDRESS = "http://192.168.100.6:8006/";

    // 调用地址
    public static final String CAR_CHECK_SERVICE = "CarCheckService.svc";
//    public static final String USER_MANAGE_SERVICE = "UserManageService.svc";
//    public static final String REPORT_SERVICE = "ReportService.svc";

    // Soap Action
    public static final String SOAP_ACTION = "http://cheyipai/ICarCheckService/";

    // 方法名称
    public static final String GET_OPTIONS_BY_SERIESIDANDMODELID = "GetOptionsBySeriesIdAndModelId";
    public static final String GET_OPTIONS_BY_VIN = "GetCarConfigInfoByVin";
    public static final String USER_LOGIN = "UserLogin";
    public static final String GET_APP_NEW_VERSION = "GetAppNewVersion";
    public static final String GET_STANDARD_REMARKS = "GetStandardRemarks";
    public static final String UPLOAD_PICTURE = "UploadPictureTagKey";

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
            {R.id.clapboard_spinner,        0,                              R.array.existornot}
    };

    // 绘图类型代码
    public static final int COLOR_DIFF = 1; // 色差
    public static final int SCRATCH = 2;    // 划痕
    public static final int TRANS = 3;      // 变形
    public static final int SCRAPE = 4;     // 刮蹭
    public static final int OTHER = 5;      // 其它
    public static final int DIRTY = 6;      // 脏污
    public static final int BROKEN = 7;     // 破损

    public static final int ENTER_EXTERIOR_PAINT = 0;
    public static final int ENTER_INTERIOR_PAINT = 1;

    public static final int PHOTO_FOR_EXTERIOR_STANDARD = 2;
    public static final int PHOTO_FOR_INTERIOR_STANDARD = 3;
    public static final int PHOTO_FOR_ACCIDENT_FRONT = 4;
    public static final int PHOTO_FOR_ACCIDENT_REAR = 5;
    public static final int PHOTO_FOR_EXTERIOR_FAULT = 6;
    public static final int PHOTO_FOR_INTERIOR_FAULT = 7;
    public static final int ADD_COMMENT_FOR_ACCIDENT_FRONT_PHOTO = 8;
    public static final int ADD_COMMENT_FOR_ACCIDENT_REAR_PHOTO = 9;
    public static final int ADD_COMMENT_FOR_EXTERIOR_AND_INTERIOR_PHOTO = 10;
    public static final int ADD_COMMENT_FOR_INTERIOR_PHOTO = 11;
    public static final int PHOTO_COMMENT_ADDED = 12;
    public static final int PHOTO_FOR_TIRES = 13;
}
