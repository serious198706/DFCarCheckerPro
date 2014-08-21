package com.df.library.util;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import com.df.library.R;
import com.df.library.service.CrashHandler;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by 岩 on 14-3-27.
 */
public class MyApplication extends Application {
    private static final String tag = MyApplication.class.getSimpleName();
    public static final String LOG_TAG = "BUG";
    public static final int WM_KEY_NONE = 0xFE;
    public static final int WM_KEY_EXIT = 0xFF;
    // public static final int WM_ON_KEY_READ_CODE = 1000;
    public static final int WM_ON_MENU_ITEM_CLICK = 10000;
    public static final int WM_ON_DIALOG_OK_CLICK = 10001;
    public static final int WM_ON_DIALOG_CANCEL_CLICK = WM_KEY_EXIT;

    public static final String CN_FUNCTION_XML = "menu/CNFunctionMenu.xml";
    public static final String CN_CATEGORY_XML = "menu/CNCategory.xml";
    public static final String EN_FUNCTION_XML = "menu/ENFunctionMenu.xml";
    public static final String EN_CATEGORY_XML = "menu/ENCategory.xml";

    public static final String ADS_ACTION_PREFIX = "com.ads.action.";

    public static MyApplication INSTANCE;
    public static Context mContext;
    private static BluetoothAdapter mBluetoothAdapter;

    public MyApplication() { INSTANCE = this; }

    @Override
    public void onCreate() {
        super.onCreate();

//        // 异常处理，不需要处理时注释掉这两句即可！
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        // 注册crashHandler
//        crashHandler.init(getApplicationContext());

        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=53f1785b");

        mContext = getApplicationContext();
        buildDrawableResourceMap();
    }

    private static HashMap<String, Integer> mDrawableMap = new HashMap<String, Integer>();

    private void buildDrawableResourceMap()
    {
        try
        {
            Class<R.drawable> drawable = R.drawable.class;
            Field[] fields = drawable.getFields();
            for (Field f : fields)
            {
                mDrawableMap.put(f.getName(), f.getInt(null));
            }
        } catch (Exception e)
        {
            Log.e(tag, "Errer when build Drawable Map:" + e.getMessage());
        }
    }

    public static int getDrawableResource(String key)
    {
        Integer id = null;
        if (key != null)
        {
            id = mDrawableMap.get(key);
        }
        if (id != null)
        {
            return id;
        }
        return R.drawable.car_repair; // Default icon
    }

    public static FileDescriptor getAssetFileFd(String path) throws IOException
    {
        AssetManager am = INSTANCE.getApplicationContext().getAssets();
        AssetFileDescriptor fd = am.openFd(path);
        return fd.getFileDescriptor();
    }

    public static InputStream getAssetFileInputSream(String path)
            throws IOException
    {
        AssetManager am = INSTANCE.getApplicationContext().getAssets();
        return am.open(path);
    }

    public static boolean extractAssetFile(String filePath, String destPath)
    {
        boolean ret = false;

        try
        {
            InputStream in = getAssetFileInputSream(filePath);
            OutputStream out = new FileOutputStream(destPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            ret = true;
        } catch (IOException e)
        {
            Log.e(tag, "Error when copy asset file: " + filePath
                    + ", to destination path:" + destPath, e);
        }

        return ret;
    }

    private static void copyFile(InputStream in, OutputStream out)
            throws IOException
    {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
    }
}
