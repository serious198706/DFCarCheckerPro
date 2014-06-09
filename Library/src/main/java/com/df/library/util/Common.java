package com.df.library.util;

import android.graphics.Color;
import android.os.Environment;

/**
 * Created by 岩 on 2014/6/4.
 */
public class Common {
    public static final String TAG = "DFCarChecker";

    public static final String NAMESPACE = "http://cheyipai";

    // 默认为外网测试
    private static String SERVER_ADDRESS = "http://114.112.88.216:9133/Services/";
    private static String PICTURE_ADDRESS = "http://i.268v.com:8092/c/";
    private static String THUMB_ADDRESS = "http://i.268v.com:8092/small/c/";
    private static String PROCEDURES_ADDRESS = "http://truetest.cheyipai.com:20002/";

    public static String getSERVER_ADDRESS() {
        return SERVER_ADDRESS;
    }

    public static String getPICTURE_ADDRESS() {
        return PICTURE_ADDRESS;
    }

    public static String getTHUMB_ADDRESS() {
        return THUMB_ADDRESS;
    }

    public static String getPROCEDURES_ADDRESS() {
        return PROCEDURES_ADDRESS;
    }

    public static final int EXTERNAL_VERSION = 50;
    public static final int INTERNAL_100_3_VERSION = 51;
    public static final int INTERNAL_100_6_VERSION = 52;
    public static final int INTERNAL_100_110_VERSION = 53;
    public static final int PRODUCT_VERSION = 54;

    private static int ENVIRONMENT = EXTERNAL_VERSION;

    public static void setEnvironment(int environment) {
        ENVIRONMENT = environment;

        switch (environment) {
            case EXTERNAL_VERSION:
                SERVER_ADDRESS = "http://114.112.88.216:9133/Services/";
                PICTURE_ADDRESS = "http://i.268v.com:8092/c/";
                THUMB_ADDRESS = "http://i.268v.com:8092/small/c/";
                PROCEDURES_ADDRESS = "http://truetest.cheyipai.com:20002/";
                break;
            case INTERNAL_100_3_VERSION:
                SERVER_ADDRESS = "http://192.168.100.3:40005/services/";
                PICTURE_ADDRESS = "http://192.168.100.6:8006/c/";
                THUMB_ADDRESS = "http://192.168.100.6:8006/small/c/";
                PROCEDURES_ADDRESS = "http://192.168.100.3:40001/";
                break;
            case INTERNAL_100_6_VERSION:
                SERVER_ADDRESS = "http://192.168.100.6:8052/services/";
                PICTURE_ADDRESS = "http://192.168.100.6:8006/c/";
                THUMB_ADDRESS = "http://192.168.100.6:8006/small/c/";
                PROCEDURES_ADDRESS = "http://192.168.100.6:8053/";
                break;
            case INTERNAL_100_110_VERSION:
                SERVER_ADDRESS = "http://192.168.100.110:40005/services/";
                PICTURE_ADDRESS = "http://192.168.100.6:8006/c/";
                THUMB_ADDRESS = "http://192.168.100.6:8006/small/c/";
                PROCEDURES_ADDRESS = "http://192.168.100.110:40001/";
                break;
            case PRODUCT_VERSION:
                SERVER_ADDRESS = "http://wcf.268v.com:8052/services/";
                PICTURE_ADDRESS = "http://i.268v.com/c/";
                THUMB_ADDRESS = "http://i.268v.com/small/c/";
                PROCEDURES_ADDRESS = "http://wcf.cheyipai.com:6080/";
                break;
        }
    }

    public static int getEnvironment() {
        return ENVIRONMENT;
    }

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
    public static final String GET_COOPERATORS = "GetCheckCooperates";
    public static final String GET_CHECKED_CARS = "ListCheckedCarsByUserId";
    public static final String GET_WAITING_CARS = "ListPendingCarsByUserId";
    public static final String GET_CAR_DETAIL = "GetCheckedCarDetailByCarId";
    public static final String IMPORT_PLATFORM = "ImportPlatform";
    public static final String CHECK_SELLER_NAME = "CheckSellerName";
    public static final String DELETE_CAR = "DeletePendingCarInfoByCarId";
    public static final String GET_AUTHORIZE_CODE = "GetAuthorizeCode";
    public static final String UPDATE_AUTHORIZE_CODE_STATUS = "UpdateAuthorizeCodeStatus";

    // 绘图类型代码
    public static final int COLOR_DIFF = 1; // 色差
    public static final int SCRATCH = 2;    // 划痕
    public static final int TRANS = 3;      // 变形
    public static final int SCRAPE = 4;     // 刮蹭
    public static final int OTHER = 5;      // 其它
    public static final int DIRTY = 6;      // 脏污
    public static final int BROKEN = 7;     // 破损
    public static final int DAMAGE = 8;     // 损伤

    public static final int ENTER_EXTERIOR_PAINT = 0;
    public static final int ENTER_INTERIOR_PAINT = 1;

    public static final int MASK_PHOTO = 10;

    public static final int PHOTO_WIDTH = 800;
    public static final int THUMBNAIL_WIDTH = 400;

    public static final int NO_PHOTO = 1;
    public static final int WEB_PHOTO = 2;

    // 绘制颜色
    public static int PAINTCOLOR = Color.rgb(0xEA, 0x55, 0x04);

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
    public static final int PHOTO_FOR_AGREEMENT_STANDARD = 16;
    public static final int PHOTO_RETAKE = 17;
    public static final int MODIFY_COMMENT = 18;
    public static final int MODIFY_PAINT_COMMENT = 19;
    public static final int PHOTO_FOR_TIRES_MODIFY = 20;
    public static final int PHOTO_FOR_OTHER_FAULT_STANDARD = 21;
    public static final int ADD_COMMENT_FOR_OTHER_FAULT_PHOTO = 22;
    public static final int TAKE_LICENSE_PHOTO = 23;

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

    // 拍摄标准照的位置
    public static final String[] exteriorPartArray = {"左前45°", "右前45°", "右侧底大边", "右后45°", "左后45°", "左侧底大边", "钥匙", "其他"};
    public static final String[] interiorPartArray = {"仪表盘", "左门+方向盘", "天窗", "后排座椅", "工作台(中控台)", "其他"};
    public static final String[] proceduresPartArray = {"车牌", "铭牌 - 行驶证 - 登记证", "车架号", "其他"};
    public static final String[] enginePartArray = {"全景", "左侧", "右侧", "其他"};
    public static final String[] tirePartArray = {"leftFront", "rightFront", "leftRear", "rightRear", "spare"};

    public static final String EXTERIOR = "exterior";
    public static final String INTERIOR = "interior";

    public static final int PHOTO_MIN_EXTERIOR = 1;
    public static final int PHOTO_MIN_INTERIOR = 1;
    public static final int PHOTO_MIN_PROCEDURES = 0;
    public static final int PHOTO_MIN_ENGINE = 3;
    public static final int PHOTO_MIN_AGREEMENT = 0;

    // 标签选中和未选中的颜色
    public static int selectedColor = Color.rgb(0xC7, 0x48, 0x10);
    public static int unselectedColor = Color.rgb(0x70, 0x70, 0x70);
}
