package com.df.app.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
     * 获取日期串
     * @param year
     * @param month
     * @return
     */
    public static String getDateString(String year, String month) {
        return year + "-" + (month.length() == 1 ? "0" + month : month);
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
     * 设置editView为焦点
     * @param view
     * @param editId
     */
    public static void setEditFocus(View view, int editId) {
        EditText editText = (EditText)view.findViewById(editId);
        editText.requestFocus();
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
     *  将字符串编码成16进制数字,适用于所有字符（包括中文）
     */
    public static String encode(String str) {
        // 根据默认编码获取字节数组
        byte[] bytes = str.getBytes();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        // 将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(hexString.charAt((bytes[i] & 0x0f)) + " ");
        }

        return sb.toString();
    }

    /*
     *  将16进制数字解码成字符串,适用于所有字符（包括中文）
     */
    public static String decode(String bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(
                bytes.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
                    .indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());

    }

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
            arrayOfByte[j] = (byte) (0xFF & Integer.decode(
                    "0x" + str.substring(j * 2, k) + str.substring(k, l))
                    .intValue());
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
}
