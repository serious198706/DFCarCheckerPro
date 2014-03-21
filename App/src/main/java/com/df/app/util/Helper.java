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

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.carCheck.BasicInfoLayout;
import com.df.app.carCheck.ExteriorLayout;
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

    public static void parsePhotoData(Context context, JSONObject photo,
                                      List<PhotoEntity> exteriorPhotos,
                                      List<PhotoEntity> interiorPhotos,
                                      List<PhotoEntity> faultPhotos,
                                      List<PhotoEntity> proceduresPhotos,
                                      List<PhotoEntity> enginePhotos,
                                      List<PhotoEntity> agreementPhotos) throws JSONException {
        parseStandard(context, photo.getJSONObject("exterior").getJSONArray("standard"), exteriorPhotos, R.array.exterior_camera_item, Common.exteriorPartArray);
        parseStandard(context, photo.getJSONObject("interior").getJSONArray("standard"), interiorPhotos, R.array.interior_camera_item, Common.interiorPartArray);

        if(photo.get("procedures") != JSONObject.NULL)
            parseStandard(context, photo.getJSONArray("procedures"), proceduresPhotos, R.array.photoForProceduresItems, Common.proceduresPartArray);

        parseStandard(context, photo.getJSONArray("engineRoom"), enginePhotos, R.array.photoForEngineItems, Common.enginePartArray);

        parseFault(photo, faultPhotos);
        parseTire(context, photo.getJSONObject("tire"), exteriorPhotos);

        parseAgreement(photo.getJSONArray("agreement"), agreementPhotos);
    }

    private static void parseAgreement(JSONArray agreement, List<PhotoEntity> agreementPhotos) throws JSONException {
        for(int i = 0; i < agreement.length(); i++) {
            JSONObject temp = agreement.getJSONObject(i);

            PhotoEntity photoEntity = new PhotoEntity();

            String url = temp.getString("photo");  //     c/1403/12/00715-zmckhyiiyoq.jpg
            String thumbUrl;

            if(!url.equals("")) {
                thumbUrl = Common.THUMB_ADDRESS + url + "?w=150";
                url = Common.PICTURE_ADDRESS + url;
            } else {
                thumbUrl = "";
            }

            photoEntity.setFileName(url);
            photoEntity.setThumbFileName(thumbUrl);
            photoEntity.setIndex(temp.getInt("index"));
            photoEntity.setModifyAction(Action.NORMAL);
            photoEntity.setName("协议");
            makeJsonString(photoEntity, temp, "agreement", "");

            agreementPhotos.add(photoEntity);
        }
    }

    private static void parseTire(Context context, JSONObject tire, List<PhotoEntity> exteriorPhotos) throws JSONException {
        String[] tireArray = context.getResources().getStringArray(R.array.tire_items);

        for(int i = 0; i < Common.tirePartArray.length; i++) {
            if(tire.get(Common.tirePartArray[i]) != JSONObject.NULL) {
                JSONObject jsonObject = tire.getJSONObject(Common.tirePartArray[i]);
                addTire(tireArray[i], Common.tirePartArray[i], jsonObject, exteriorPhotos);
            }
        }
    }

    private static void addTire(String name, String part, JSONObject temp, List<PhotoEntity> exteriorPhotos) throws JSONException{
        if(temp != JSONObject.NULL) {
            PhotoEntity photoEntity = new PhotoEntity();

            String url = temp.getString("photo");  //     c/1403/12/00715-zmckhyiiyoq.jpg
            String thumbUrl;

            if(!url.equals("")) {
                thumbUrl = Common.THUMB_ADDRESS + url + "?w=150";
                url = Common.PICTURE_ADDRESS + url;
            } else {
                thumbUrl = "";
            }

            photoEntity.setFileName(url);
            photoEntity.setThumbFileName(thumbUrl);
            photoEntity.setIndex(temp.getInt("index"));
            photoEntity.setName(name);
            photoEntity.setModifyAction(Action.NORMAL);
            makeJsonString(photoEntity, temp, "tires", part);

            exteriorPhotos.add(photoEntity);
        }
    }

    private static void parseStandard(Context context, JSONArray jsonArray, List<PhotoEntity> photoEntities, int stringArrayId, String[] partArray) throws JSONException {
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject temp = jsonArray.getJSONObject(i);

            PhotoEntity photoEntity = new PhotoEntity();

            String part = temp.getString("part");

            String[] exteriorPart = context.getResources().getStringArray(stringArrayId);

            for(int j = 0; j < partArray.length; j++) {
                if(part.equals(partArray[j])) {
                    photoEntity.setName(exteriorPart[j]);
                }
            }

            String url = temp.getString("photo");  //     c/1403/12/00715-zmckhyiiyoq.jpg
            String thumbUrl;

            if(!url.equals("")) {
                thumbUrl = Common.THUMB_ADDRESS + url + "?w=150";
                url = Common.PICTURE_ADDRESS + url;
            } else {
                thumbUrl = "";
            }

            photoEntity.setFileName(url);
            photoEntity.setThumbFileName(thumbUrl);
            photoEntity.setIndex(temp.getInt("index"));
            photoEntity.setModifyAction(Action.NORMAL);

            String group;

            if(stringArrayId == R.array.exterior_camera_item) {
                group = "exterior";
            } else if(stringArrayId == R.array.interior_camera_item) {
                group = "interior";
            } else if(stringArrayId == R.array.photoForProceduresItems) {
                group = "procedures";
            } else if(stringArrayId == R.array.photoForEngineItems) {
                group = "engineRoom";
            } else {
                group = "";
            }

            makeJsonString(photoEntity, temp, group, "standard");

            photoEntities.add(photoEntity);
        }
    }

    /**
     * 解析fault部分
     * @param photo
     * @throws JSONException
     */
    private static void parseFault(JSONObject photo, List<PhotoEntity> faultPhotos) throws JSONException {
        if(photo == JSONObject.NULL) {
            return;
        }

        if(photo.getJSONObject("exterior").get("fault") != JSONObject.NULL)
            addFault(photo.getJSONObject("exterior").getJSONArray("fault"), faultPhotos, "exterior");

        if(photo.getJSONObject("interior").get("fault") != JSONObject.NULL)
            addFault(photo.getJSONObject("interior").getJSONArray("fault"), faultPhotos, "interior");

        if(photo.getJSONObject("frame").get("front") != JSONObject.NULL)
            addFault(photo.getJSONObject("frame").getJSONArray("front"), faultPhotos, "front");

        if(photo.getJSONObject("frame").get("rear") != JSONObject.NULL)
            addFault(photo.getJSONObject("frame").getJSONArray("rear"), faultPhotos, "rear");
    }

    /**
     * 将jsonArray中的照片加入faultPhotos
     * @param jsonArray array
     * @throws JSONException
     */
    private static void addFault(JSONArray jsonArray, List<PhotoEntity> faultPhotos, String part) throws JSONException {
        if(jsonArray == JSONObject.NULL) {
            return;
        }

        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject temp = jsonArray.getJSONObject(i);

            PhotoEntity photoEntity = new PhotoEntity();

            String name;
            String group;
            if(temp.has("type")) {
                switch (temp.getInt("type")) {
                    case 1:
                        name = "色差";
                        group = "exterior";
                        break;
                    case 2:
                        name = "划痕";
                        group = "exterior";
                        break;
                    case 3:
                        name = "变形";
                        group = "exterior";
                        break;
                    case 4:
                        name = "刮蹭";
                        group = "exterior";
                        break;
                    case 5:
                        name = "其它";
                        group = "exterior";
                        break;
                    case 6:
                        name = "脏污";
                        group = "interior";
                        break;
                    case 7:
                        name = "破损";
                        group = "interior";
                        break;
                    default:
                        name = "";
                        group = "";
                        break;
                }
            } else {
                name = "结构缺陷";
                group = "frame";
            }

            photoEntity.setName(name);

            String url = temp.getString("photo");  //     c/1403/12/00715-zmckhyiiyoq.jpg
            String thumbUrl;

            if(!url.equals("")) {
                thumbUrl = Common.THUMB_ADDRESS + url + "?w=150";
                url = Common.PICTURE_ADDRESS + url;
            } else {
                thumbUrl = "";
            }

            photoEntity.setFileName(url);
            photoEntity.setThumbFileName(thumbUrl);
            photoEntity.setComment(temp.getString("comment"));
            photoEntity.setIndex(temp.getInt("index"));
            photoEntity.setModifyAction(Action.NORMAL);
            makeJsonString(photoEntity, temp, group, part);

            faultPhotos.add(photoEntity);


        }
    }

    private static void makeJsonString(PhotoEntity photoEntity, JSONObject photoData, String group, String part) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("CarId", BasicInfoLayout.carId);
        jsonObject.put("UserId", MainActivity.userInfo.getId());
        jsonObject.put("Key", MainActivity.userInfo.getKey());
        jsonObject.put("Action", photoEntity.getModifyAction());
        jsonObject.put("Index", photoEntity.getIndex());

        if(!group.equals(""))
            jsonObject.put("Group", group);

        if(!part.equals(""))
            jsonObject.put("Part", part);

        photoData.remove("photo");
        photoData.remove("index");

        jsonObject.put("PhotoData", photoData);

        photoEntity.setJsonString(jsonObject.toString());
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
