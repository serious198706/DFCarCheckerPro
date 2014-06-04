package com.df.app.util;

import android.content.Context;

import com.df.app.MainActivity;
import com.df.app.carCheck.BasicInfoLayout;
import com.df.app.carCheck.CarCheckActivity;
import com.df.app.carCheck.PhotoLayout;
import com.df.app.entries.Action;
import com.df.app.entries.PhotoEntity;

import org.json.JSONException;
import org.json.JSONObject;

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

    public static JSONObject generateJSONObject(String photoDataPart, String group, String part, String action, int index) {
        // 组织JsonString
        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();

            photoJsonObject.put("part", photoDataPart);

            jsonObject.put("Group", group);
            jsonObject.put("Part", part);
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("CarId", BasicInfoLayout.carId);
            jsonObject.put("Action", action);
            jsonObject.put("Index", index);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * 生成photoEntity
     * @return PhotoEntity
     */
    public static PhotoEntity generatePhotoEntity(long currentMillis, String name, String photoGroup, String photoPart, String photoDataPart) {
        return generatePhotoEntity(Long.toString(currentMillis) + ".jpg", name, photoGroup, photoPart, photoDataPart);
    }

    /**
     * 生成photoEntity
     * @return PhotoEntity
     */
    public static PhotoEntity generatePhotoEntity(String fileName, String name, String photoGroup, String photoPart, String photoDataPart) {
        String action =  CarCheckActivity.isModify() ? Action.ADD : Action.MODIFY;
        int index = PhotoLayout.photoIndex++;
        JSONObject jsonObject = generateJSONObject(photoDataPart, photoGroup, photoPart, action, index);

        return generatePhotoEntity(jsonObject, name, fileName, action, index, "");
    }

    /**
     * 生成photoEntity
     * @return PhotoEntity
     */
    public static PhotoEntity generatePhotoEntity(JSONObject jsonObject, String name, String fileName, String action, int index, String comment) {
        PhotoEntity photoEntity = new PhotoEntity();

        photoEntity.setName(name);
        photoEntity.setFileName(fileName);
        photoEntity.setIndex(index);
        photoEntity.setModifyAction(action);
        if(photoEntity.getFileName().equals("")) {
            photoEntity.setThumbFileName("");
        } else {
            photoEntity.setThumbFileName(photoEntity.getFileName().substring(0, photoEntity.getFileName().length() - 4) + "_t.jpg");
        }
        photoEntity.setComment(comment);
        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }
}
