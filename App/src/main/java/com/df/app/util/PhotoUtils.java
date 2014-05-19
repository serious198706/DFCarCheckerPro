package com.df.app.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.carCheck.BasicInfoLayout;
import com.df.app.carCheck.CarCheckActivity;
import com.df.app.carCheck.PhotoLayout;
import com.df.app.entries.Action;
import com.df.app.entries.PhotoEntity;

import org.json.JSONException;
import org.json.JSONObject;

import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 14-4-4.
 */
public class PhotoUtils {
    public static String[] getItemArray(Context context, final int arrayId, int[] count) {
        String[] array = context.getResources().getStringArray(arrayId);

        int length = array.length;

        for(int i = 0; i < length; i++) {
            array[i] += " (";
            array[i] += Integer.toString(count[i]);
            array[i] += ") ";
        }

        return array;
    }

    /**
     * 生成photoEntity
     * @return
     */
    public static PhotoEntity generatePhotoEntity(Context context, long currentMillis, String name, String photoGroup, String photoDataPart) {
        PhotoEntity photoEntity = new PhotoEntity();

        photoEntity.setFileName(Long.toString(currentMillis) + ".jpg");

        if(!photoEntity.getFileName().equals(""))
            photoEntity.setThumbFileName(Long.toString(currentMillis) + "_t.jpg");
        else
            photoEntity.setThumbFileName("");

        photoEntity.setName(name);
        photoEntity.setIndex(PhotoLayout.photoIndex++);

        // 如果是走了这段代码，则一定是添加照片
        // 如果是修改模式，则Action就是add
        if(CarCheckActivity.isModify()) {
            photoEntity.setModifyAction(Action.ADD);
        } else {
            photoEntity.setModifyAction(Action.MODIFY);
        }

        // 组织JsonString
        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();

            photoJsonObject.put("part", photoDataPart);

            jsonObject.put("Group", photoGroup);
            jsonObject.put("Part", "standard");
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("CarId", BasicInfoLayout.carId);
            jsonObject.put("Action", photoEntity.getModifyAction());
            jsonObject.put("Index", photoEntity.getIndex());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }

    /**
     * 生成photoEntity
     * @return
     */
    public static PhotoEntity generatePhotoEntity(Context context, String fileName, String name, String photoGroup, String photoDataPart) {
        PhotoEntity photoEntity = new PhotoEntity();

        photoEntity.setFileName(fileName);

        if(!photoEntity.getFileName().equals(""))
            photoEntity.setThumbFileName(fileName.substring(0, fileName.length() - 4) + "_t.jpg");
        else
            photoEntity.setThumbFileName("");

        photoEntity.setName(name);
        photoEntity.setIndex(PhotoLayout.photoIndex++);

        // 如果是走了这段代码，则一定是添加照片
        // 如果是修改模式，则Action就是add
        if(CarCheckActivity.isModify()) {
            photoEntity.setModifyAction(Action.ADD);
        } else {
            photoEntity.setModifyAction(Action.MODIFY);
        }

        // 组织JsonString
        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();

            photoJsonObject.put("part", photoDataPart);

            jsonObject.put("Group", photoGroup);
            jsonObject.put("Part", "standard");
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("CarId", BasicInfoLayout.carId);
            jsonObject.put("Action", photoEntity.getModifyAction());
            jsonObject.put("Index", photoEntity.getIndex());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }
}
