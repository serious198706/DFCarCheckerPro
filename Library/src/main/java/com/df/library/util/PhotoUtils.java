package com.df.library.util;

import android.content.Context;
import android.graphics.Bitmap;

import com.df.library.entries.Action;
import com.df.library.entries.PhotoEntity;
import com.df.library.entries.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;

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

    public static JSONObject generateJSONObject(String photoDataPart, String group, String part, String action, int index, int carId) {
        // 组织JsonString
        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();

            photoJsonObject.put("part", photoDataPart);

            jsonObject.put("Group", group);
            jsonObject.put("Part", part);
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", UserInfo.getInstance().getId());
            jsonObject.put("Key", UserInfo.getInstance().getKey());
            jsonObject.put("CarId", carId);
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
    public static PhotoEntity generatePhotoEntity(long currentMillis, String name, String photoGroup,
                                                  String photoPart, String photoDataPart, int index, int carId, boolean isModify) {
        return generatePhotoEntity(Long.toString(currentMillis) + ".jpg", name, photoGroup, photoPart, photoDataPart, index, carId, isModify);
    }

    /**
     * 生成photoEntity
     * @return PhotoEntity
     */
    public static PhotoEntity generatePhotoEntity(String fileName, String name, String photoGroup,
                                                  String photoPart, String photoDataPart, int index, int carId, boolean isModify) {
        String action =  isModify ? Action.ADD : Action.MODIFY;
        JSONObject jsonObject = generateJSONObject(photoDataPart, photoGroup, photoPart, action, index, carId);

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

    /**
     * 生成草图
     * @return
     */
    public static PhotoEntity generateSketch(Bitmap bitmap, String path, String photoName, String action, int index, String group, int carId) {
        try {
            FileOutputStream out = new FileOutputStream(path + photoName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName(photoName);

        photoEntity.setModifyAction(action);
        photoEntity.setIndex(index);

        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();

            photoJsonObject.put("width", bitmap.getWidth());
            photoJsonObject.put("height", bitmap.getHeight());

            jsonObject.put("Group", group);
            jsonObject.put("Part", "sketch");
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", UserInfo.getInstance().getId());
            jsonObject.put("Key", UserInfo.getInstance().getKey());
            jsonObject.put("CarId", carId);
            jsonObject.put("Action", action);
            jsonObject.put("Index", index);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }
}
