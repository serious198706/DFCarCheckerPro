package com.df.library.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.df.library.util.Common;
import com.df.library.R;
import com.df.library.util.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by 岩 on 14-4-21.
 */
public class LicenseRecognise {
    public static int nMainID = 6;
    private Context context;
    private boolean isCatchPreview;
    private int WIDTH;
    private int HEIGHT;
    private boolean isCatchPicture;
    private int srcwidth;
    private int srcheight;
    private String selectPath;
    public static Handler sHandler;
    private String path;

    public LicenseRecognise(Context context, String path) {
        this.context = context;
        this.path = path;
        checkCameraParameters();
    }

    public void takePhoto() {
        writePreferences("Button", "Height", 50);
        Log.i(Common.TAG, "isCatchPreview = " + isCatchPreview + " isCatchPicture = " + isCatchPicture);
        nMainID = Helper.readMainID();
        if (isCatchPreview && isCatchPicture) {
            Intent intent = new Intent();
            intent.setClass(context, WintoneCameraActivity.class);

            Log.i(Common.TAG, "拍摄分辨率为: " + srcwidth + " * " + srcheight);
            Log.i(Common.TAG, "预览分辨率为: " + WIDTH + " * " + HEIGHT);

            intent.putExtra("srcwidth", srcwidth);
            intent.putExtra("srcheight", srcheight);
            intent.putExtra("nMainID", nMainID);
            intent.putExtra("path", path);
            ((Activity)context).startActivityForResult(intent, Common.TAKE_LICENSE_PHOTO);
            ((Activity)context).overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
        } else {
            String partpath = Environment.getExternalStorageDirectory() + "/wtimage";
            File dir = new File(partpath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            Date date = new Date();
            selectPath = partpath + "/idcard" + date.getTime() + ".jpg";
            Intent takePictureFromCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureFromCameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(new File(selectPath)));
            ((Activity)context).startActivityForResult(takePictureFromCameraIntent, 1);
            ((Activity)context).overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
        }
    }


    public void checkCameraParameters() {
        // 读取支持的预览尺寸
        Camera camera = null;
        try {
            camera = Camera.open();
            if (camera != null) {
                // 读取支持的预览尺寸,优先选择640后320
                Camera.Parameters parameters = camera.getParameters();
                List<Integer> SupportedPreviewFormats = parameters.getSupportedPreviewFormats();

                Log.i(Common.TAG,"preview-size-values:" + parameters.get("preview-size-values"));
                List<Camera.Size> previewSizes = splitSize(parameters.get("preview-size-values"),camera);//parameters.getSupportedPreviewSizes();
                for(int i=0;i<previewSizes.size();i++){
                    if(previewSizes.get(i).width == 640 && previewSizes.get(i).height == 480){
                        isCatchPreview = true;
                        WIDTH = 640;
                        HEIGHT = 480;
                        break;
                    }
                    if(previewSizes.get(i).width == 320 && previewSizes.get(i).height == 240) {//640 //480
                        isCatchPreview = true;
                        WIDTH = 320;
                        HEIGHT = 240;
                    }
                }
                Log.i(Common.TAG, "isCatchPreview="+isCatchPreview);

                // 读取支持的相机尺寸,优先选择1280后1600后2048
                List<Integer> SupportedPictureFormats = parameters.getSupportedPictureFormats();

                Log.i(Common.TAG,"picture-size-values:" + parameters.get("picture-size-values"));
                List<Camera.Size> PictureSizes = splitSize(parameters.get("picture-size-values"),camera);//parameters.getSupportedPictureSizes();
                for(int i=0;i<PictureSizes.size();i++){
                    if(PictureSizes.get(i).width == 2048 && PictureSizes.get(i).height == 1536){
                        if(isCatchPicture == true) {
                            break;
                        }
                        isCatchPicture = true;
                        srcwidth = 2048;
                        srcheight = 1536;
                    }
                    if(PictureSizes.get(i).width == 1600 && PictureSizes.get(i).height == 1200){
                        isCatchPicture = true;
                        srcwidth = 1600;
                        srcheight = 1200;
                    }
                    if(PictureSizes.get(i).width == 1280 && PictureSizes.get(i).height == 960) {
                        isCatchPicture = true;
                        srcwidth = 1280;
                        srcheight = 960;
                        break;
                    }
                }
                Log.i(Common.TAG, "isCatchPicture="+isCatchPicture);
            }
            camera.release();
            camera = null;
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(camera != null) {
                try{
                    camera.release();
                    camera = null;
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private ArrayList<Camera.Size> splitSize(String str,Camera camera) {
        if (str == null)
            return null;
        StringTokenizer tokenizer = new StringTokenizer(str, ",");
        ArrayList<Camera.Size> sizeList = new ArrayList<Camera.Size>();
        while (tokenizer.hasMoreElements()) {
            Camera.Size size = strToSize(tokenizer.nextToken(),camera);
            if (size != null)
                sizeList.add(size);
        }
        if (sizeList.size() == 0)
            return null;
        return sizeList;
    }

    private Camera.Size strToSize(String str,Camera camera) {
        if (str == null)
            return null;
        int pos = str.indexOf('x');
        if (pos != -1) {
            String width = str.substring(0, pos);
            String height = str.substring(pos + 1);
            return camera.new Size(Integer.parseInt(width), Integer.parseInt(height));
        }
        return null;
    }

    protected void writePreferences(String perferencesName, String key, int value) {
        SharedPreferences preferences = context.getSharedPreferences(perferencesName, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }



    @SuppressWarnings("unused")
    protected String getLatestImage() {
        String latestImage = null;
        String[] items = { MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA };
        Cursor cursor = ((Activity)context).managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, items, null,
                null, MediaStore.Images.Media._ID + " desc");
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                latestImage = cursor.getString(1);
                break;
            }
        }
        return latestImage;
    }

    public static Object parseFields(Intent data) {
        // 读识别返回值
        int ReturnAuthority = data.getIntExtra("ReturnAuthority", -100000);//取激活状态
        int ReturnInitIDCard = data.getIntExtra("ReturnInitIDCard", -100000);//取初始化返回值
        int ReturnLoadImageToMemory = data.getIntExtra( "ReturnLoadImageToMemory", -100000);//取读图像的返回值
        int ReturnRecogIDCard = data.getIntExtra("ReturnRecogIDCard",  -100000);//取识别的返回值

        Log.d(Common.TAG, ReturnAuthority + ", " + ReturnInitIDCard + ", " + ReturnLoadImageToMemory + ", " + ReturnRecogIDCard);

        Log.i(Common.TAG, "ReturnLPFileName:" + data.getStringExtra("ReturnLPFileName"));

        if (ReturnAuthority == 0 && ReturnInitIDCard == 0
                && ReturnLoadImageToMemory == 0 && ReturnRecogIDCard > 0) {
            /**
             0	保留
             1	号牌号码
             2	车辆类型
             3	所有人
             4	住址
             5	品牌型号
             6	车辆识别代号
             7	发动机号码
             8	注册日期
             9	发证日期
             10	使用性质
             */

            String[] fieldValue = (String[]) data.getSerializableExtra("GetRecogResult");
            String fields[] = {fieldValue[1], fieldValue[5], fieldValue[2], fieldValue[10], fieldValue[7], fieldValue[6]};

            return fields;
        } else {
            String str = "";
            if (ReturnAuthority == -100000) {
                str = "未识别   代码： " + ReturnAuthority;
            } else if (ReturnAuthority != 0) {
                str = "激活失败 代码：" + ReturnAuthority;
            } else if (ReturnInitIDCard != 0) {
                str = "识别初始化失败 代码：" + ReturnInitIDCard;
            } else if (ReturnLoadImageToMemory != 0) {
                if (ReturnLoadImageToMemory == 3) {
                    str = "识别载入图像失败，请重新识别 代码：" + ReturnLoadImageToMemory;
                } else if(ReturnLoadImageToMemory == 1){
                    str = "识别载入图像失败，识别初始化失败,请重试 代码：" + ReturnLoadImageToMemory;
                } else {
                    str = "识别载入图像失败 代码：" + ReturnLoadImageToMemory;
                }
            } else if (ReturnRecogIDCard != 0) {
                str = "识别失败 代码：" + ReturnRecogIDCard;
            }

            return str;
        }
    }
}
