package com.df.app.procedures;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.df.app.R;
import com.df.app.service.CameraActivity;
import com.df.app.util.Common;
import com.df.app.util.Helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

    public LicenseRecognise(Context context) {
        this.context = context;
        checkCameraParameters();
    }

    public void takePhoto() {
        writePreferences("Button", "Height", 50);
        Log.i(Common.TAG, "isCatchPreview = " + isCatchPreview + " isCatchPicture = " + isCatchPicture);
        nMainID = Helper.readMainID();
        if (isCatchPreview && isCatchPicture) {
            Intent intent = new Intent();
            intent.setClass(context, CameraActivity.class);

            Log.i(Common.TAG, "拍摄分辨率为: " + srcwidth + " * " + srcheight);
            Log.i(Common.TAG, "预览分辨率为: " + WIDTH + " * " + HEIGHT);

            intent.putExtra("srcwidth", srcwidth);
            intent.putExtra("srcheight", srcheight);
            intent.putExtra("nMainID", nMainID);
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
}
