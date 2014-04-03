package com.df.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.carCheck.BasicInfoLayout;
import com.df.app.carCheck.ExteriorLayout;
import com.df.app.carCheck.Integrated2Layout;
import com.df.app.entries.Action;
import com.df.app.entries.PhotoEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by on 13-8-30.
 *
 * 各种helper
 */

public class Helper {
    /**
     * 只有一个""的空列表
     * @return
     */
    public static List<String> getEmptyStringList() {
        List<String> emptyStringList = new ArrayList<String>();
        emptyStringList.add("");

        return  emptyStringList;
    }

    /**
     * 创建一个文件uri
     */
    public static Uri getOutputMediaFileUri(String fileName){
        return Uri.fromFile(getOutputMediaFile(fileName));
    }

    /**
     * 创建一个文件用于存储照片
     * @param fileName 文件名
     * @return
     */
    private static File getOutputMediaFile(String fileName){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Common.photoDirectory);

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(Common.TAG, "failed to create directory");
                return null;
            }
        }

        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return mediaFile;
    }

    /**
     * 检查vin的合法性
     * @param vin
     * @return
     */
    public static boolean isVin(String vin) {
        boolean find = Pattern.compile("^([0123456789ABCDEFGHJKLMNPRSTUVWXYZ]){12}(\\d){5}$").matcher(vin).find();

        // 如果前12位不为数字或字母，或者后5位不为数字，则错误
        if(!find) {
            return false;
        }

        find = Pattern.compile("^[UZ]{1}$").matcher(vin.substring(9, 10)).find();
        // 如果第9位是U或者Z，则错误
        if(find) {
            return false;
        }

        String lab = "0123456789ABCDEFGHJKLMNPRSTUVWXYZ";

        int[] val = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 7, 9, 2, 3, 4, 5, 6, 7, 8, 9 };
        int[] idx = { 8, 7, 6, 5, 4, 3, 2, 10, 1, 9, 8, 7, 6, 5, 4, 3, 2 };
        int value = 0;

        for (int i = 0; i < vin.length(); i++)
        {
            if (i == 8) continue;
            value += val[lab.indexOf(vin.substring(i, i + 1))] * idx[i];
        }

        return (vin.substring(8, 9).equals(Integer.toString(value % 11).replace("10", "X")));
    }

    /**
     * 获取spinner当前选项的内容
     * @param view
     * @param spinnerId
     * @return
     */
    public static String getSpinnerSelectedText(View view, int spinnerId) {
        Spinner spinner = (Spinner)view.findViewById(spinnerId);

        return spinner.getSelectedItem().toString();
    }

    /**
     * 根据内容，设置spinner的选择项
     * @param view
     * @param spinnerId
     * @param text
     */
    public static void setSpinnerSelectionWithString(View view, int spinnerId, String text) {
        Spinner spinner = (Spinner)view.findViewById(spinnerId);

        int count = spinner.getCount();
        for(int i = 0; i < count; i++) {
            if(spinner.getItemAtPosition(i).toString().equals(text))
                spinner.setSelection(i);
        }
    }

    /**
     * 获取spinner当前选择项的index
     * @param view
     * @param spinnerId
     * @return
     */
    public static int getSpinnerSelectedIndex(View view, int spinnerId) {
        Spinner spinner = (Spinner)view.findViewById(spinnerId);

        return spinner.getSelectedItemPosition();
    }

    /**
     * 使用index设置spinner的选择项
     * @param view
     * @param spinnerId
     * @param index
     */
    public static void setSpinnerSelectionWithIndex(View view, int spinnerId, int index) {
        Spinner spinner = (Spinner)view.findViewById(spinnerId);
        spinner.setSelection(index);
    }

    /**
     * 设置textView
     * @param view
     * @param textId
     * @param string
     */
    public static void setTextView(View view, int textId, String string) {
        TextView textView = (TextView)view.findViewById(textId);

        if(string == null)
            textView.setText("无");
        else
            textView.setText(string);
    }


    /**
     * 设置editView的错误信息
     * @param view
     * @param editId
     */
    public static void setEditError(View view, int editId, String msg) {
        EditText editText = (EditText)view.findViewById(editId);
        editText.setError(msg);
    }

    /**
     * 设置editView内容
     * @param view
     * @param textId
     * @param text
     */
    public static void setEditViewText(View view, int textId, String text) {
        EditText editText = (EditText)view.findViewById(textId);
        editText.setText(text);
    }

    /**
     * 获取editView内容
     * @param view
     * @param textId
     * @return
     */
    public static String getEditViewText(View view, int textId) {
        EditText editText = (EditText)view.findViewById(textId);
        return editText.getText().toString();
    }

    /**
     * 显示或者隐藏控件
     * @param view
     * @param viewId
     * @param show
     */
    public static void showView(View view, int viewId, boolean show) {
        view.findViewById(viewId).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * 停用或启用控件
     * @param view
     * @param id
     * @param enable
     */
    public static void enableView(View view, int id, boolean enable) {
        view.findViewById(id).setEnabled(enable);
    }

    /**
     *
     * @param fileName
     * @param max
     */
    public static void setPhotoSize(String fileName, int max) {
        String path = Common.photoDirectory;

        File file = new File(path + fileName);
        Bitmap bitmap = BitmapFactory.decodeFile(path + fileName);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap newBitmap = null;

        float ratio;
        float newWidth;
        float newHeight;

        // 如果宽度小于800, 无视
        if(width > max) {
            ratio = (float)width / (float)max;
            newWidth = max;
            newHeight = height / ratio;
        } else if(height > max) {
            ratio = (float)height / (float)max;
            newWidth = width / ratio;
            newHeight = max;
        } else {
            newWidth = width;
            newHeight = height;
        }

        newBitmap = Bitmap.createScaledBitmap(bitmap, (int)newWidth, (int)newHeight, true);

        try {
            FileOutputStream ostream = new FileOutputStream(file);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);

            ostream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 获取图片宽度
    public static int getBitmapWidth(String fileName) {
        Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + fileName);
        return bitmap == null ? 0 : bitmap.getWidth();
    }

    // 获取图片高度
    public static int getBitmapHeight(String fileName) {
        Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + fileName);
        return bitmap == null ? 0 : bitmap.getHeight();
    }

    // 生成缩略图
    public static void generatePhotoThumbnail(String fileName, int max) {
        String path = Common.photoDirectory;

        Bitmap bitmap = BitmapFactory.decodeFile(path + fileName);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap newBitmap = null;

        float ratio;
        float newWidth;
        float newHeight;

        // 如果宽度小于800, 无视
        if(width > max) {
            ratio = (float)width / (float)max;
            newWidth = max;
            newHeight = height / ratio;
        } else if(height > max) {
            ratio = (float)height / (float)max;
            newWidth = width / ratio;
            newHeight = max;
        } else {
            newWidth = width;
            newHeight = height;
        }

        newBitmap = Bitmap.createScaledBitmap(bitmap, (int)newWidth, (int)newHeight, true);

        try {
            String thumbFile = path + fileName.substring(0, fileName.length() - 4) + "_t.jpg";
            FileOutputStream ostream = new FileOutputStream(thumbFile);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);

            ostream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String hexString = "0123456789ABCDEF";

    /*
     *  将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串
     */
    public static byte[] hexStr2Bytes(String paramString) {
        String str = paramString.trim().replace(" ", "").toUpperCase(Locale.US);
        int i = str.length() / 2;
        byte[] arrayOfByte = new byte[i];
        for (int j = 0;; ++j) {
            if (j >= i)
                return arrayOfByte;
            int k = 1 + j * 2;
            int l = k + 1;
            arrayOfByte[j] = (byte) (0xFF & Integer.decode("0x" + str.substring(j * 2, k) + str.substring(k, l)));
        }
    }

    /*
     *  把字节数组转换成16进制字符串
     */
    public static String bytesToHexString(byte[] bArray, int count) {
        StringBuffer sb = new StringBuffer(bArray.length);

        String sTemp;
        for (int i = 0; i < count; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    public static Bitmap drawTextToBitmap(Context context, int resourceId,  int index) {
        try {
            float scale = context.getResources().getDisplayMetrics().density;
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);

            android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
            // 保险起见，从原bitmap将bitmapConfig拷贝过来
            if(bitmapConfig == null) {
                bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
            }
            // 将imutableBitmap变成mutableBitmap
            bitmap = bitmap.copy(bitmapConfig, true);

            Canvas canvas = new Canvas(bitmap);

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            // 文字颜色
            paint.setColor(Color.WHITE);
            // 文字大小
            paint.setTextSize((int) (12 * scale));
            // 加个小影子~
            paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY);

            // 往图片上写文字
            Rect bounds = new Rect();
            String text = Integer.toString(index);
            paint.getTextBounds(text, 0, text.length(), bounds);

            int x = (bitmap.getWidth() - bounds.width()) / 5;
            int y = (bitmap.getHeight() + bounds.height()) / 4;

            canvas.drawText(text, x * scale, y * scale, paint);

            return bitmap;
        } catch (Exception e) {
            return BitmapFactory.decodeResource(context.getResources(), resourceId);
        }
    }

    public static void startCamera(Context context, String fileName, int requestCode) {
        Uri fileUri = Helper.getOutputMediaFileUri(fileName);

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // 设置拍摄的文件名
        ((Activity)context).startActivityForResult(intent, requestCode);
    }

    /**
     * 拷贝草图
     */
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }



}
