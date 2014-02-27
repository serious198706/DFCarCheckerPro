package com.df.app.util;

import android.os.Environment;

import com.df.app.R;

/**
 * Created by 岩 on 13-12-18.
 *
 * 常量
 */
public class Common {
    public static String utilDirectory = Environment.getExternalStorageDirectory().getPath() +
            "/.cheyipaiPro/";
    public static String photoDirectory = Environment.getExternalStorageDirectory().getPath() +
            "/Pictures/DFCarCheckerPro/";
    public static String savedDirectory = utilDirectory +
            "saved/";

    public static String TAG = "DFCarCheckerPro";

    public static String NAMESPACE = "http://cheyipai";

    // 地址
    public static final String SERVER_ADDRESS = "http://192.168.100.6:8052/services/";
    //public static final String SERVER_ADDRESS = "http://192.168.8.33:810/Services/";
    public static final String PICTURE_ADDRESS = "http://192.168.100.6:8006/";

    // 服务名称
    public static final String CAR_CHECK_SERVICE = "CarCheckService.svc";

    // SoapAction
    public static final String SOAP_ACTION = "http://cheyipai/ICarCheckService/";

    // 方法名称
    public static final String GET_OPTIONS_BY_SERIESIDANDMODELID = "GetOptionsBySeriesIdAndModelId";
    public static final String GET_OPTIONS_BY_VIN = "GetCarConfigInfoByVin";
    public static final String USER_LOGIN = "UserLogin";
    public static final String USER_LOGOUT = "UserLogout";
    public static final String GET_APP_NEW_VERSION = "GetAppNewVersion";
    public static final String GET_STANDARD_REMARKS = "GetStandardRemarks";
    public static final String UPLOAD_PICTURE = "UploadPictureTagKey";
    public static final String ANALYSIS_ACCIDENT_DATA = "AnalysisAccidentData";
    public static final String COMMIT_DATA = "SubmitCarCheckData";
    //public static final String SAVE_DATA = "SaveCarCheckData";
    public static final String GET_COOPERATORS = "GetCheckCooperates";
    public static final String GET_CHECKED_CARS = "ListCheckedCarsByUserId";
    public static final String GET_WAITING_CARS = "ListPendingCarsByUserId";
    public static final String GET_CAR_DETAIL = "GetCheckedCarDetailByCarId";
    public static final String IMPORT_PLATFORM = "ImportPlatform";
    public static final String CHECK_SELLER_NAME = "CheckSellerName";

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

    // 拍摄照片的request code
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
    public static final int PHOTO_FOR_PROCEDURES_STANDARD = 14;
    public static final int PHOTO_FOR_ENGINE_STANDARD = 15;
    public static final int PHOTO_FOR_OTHER_STANDARD = 16;
    public static final int PHOTO_RETAKE = 17;

    // DF5000消息代码
    // 为BluetoothService处理程序定义的消息类型
    public static final int MESSAGE_STATE_CHANGE = 101;
    public static final int MESSAGE_READ = 102;
    public static final int MESSAGE_WRITE = 103;
    public static final int MESSAGE_DEVICE_NAME = 104;
    public static final int MESSAGE_TOAST = 105;
    public static final int MESSAGE_READ_OVER = 106;
    public static final int MESSAGE_GET_SERIAL = 107;

    // 提供给系统蓝牙activity的标志值
    public static final int REQUEST_CONNECT_DEVICE = 108;
    public static final int REQUEST_ENABLE_BT = 109;

    // 获取设备序列号的指令
    public static final String CMD_GET_SERIAL = "aa057f012e";

    // 设备类型代码
    public static final int DF3000 = 200;
    public static final int DF5000 = 201;
}
