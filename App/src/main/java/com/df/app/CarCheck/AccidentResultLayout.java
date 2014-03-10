package com.df.app.carCheck;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.Issue;
import com.df.app.entries.IssuePhoto;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.paintview.FramePaintPreviewView;
import com.df.app.paintview.PaintPreviewView;
import com.df.app.service.Adapter.IssuePhotoListAdapter;
import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.df.app.util.Helper.getBitmapHeight;
import static com.df.app.util.Helper.getBitmapWidth;

/**
 * Created by 岩 on 13-12-20.
 *
 * 查勘结果页面
 */
public class AccidentResultLayout extends LinearLayout {
    public static Issue issue;
    public static IssuePhotoListAdapter adapter;
    public static List<PhotoEntity> thisTimeNewPhoto;

    public static FramePaintPreviewView framePaintPreviewViewFront;
    public static FramePaintPreviewView framePaintPreviewViewRear;

    public static Bitmap previewBitmapFront;
    public static Bitmap previewBitmapRear;

    public static List<PosEntity> posEntitiesFront;
    public static List<PosEntity> posEntitiesRear;

    public static List<PhotoEntity> photoEntitiesFront;
    public static List<PhotoEntity> photoEntitiesRear;

    public AccidentResultLayout(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.accident_result_layout, this);

        posEntitiesFront = new ArrayList<PosEntity>();
        posEntitiesRear = new ArrayList<PosEntity>();

        photoEntitiesFront = new ArrayList<PhotoEntity>();
        photoEntitiesRear = new ArrayList<PhotoEntity>();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        previewBitmapFront = BitmapFactory.decodeFile(Common.utilDirectory + "d4_f", options);

        LayoutParams layoutParams = new LayoutParams(previewBitmapFront.getWidth(), previewBitmapFront.getHeight());
        layoutParams.gravity = Gravity.CENTER;

        framePaintPreviewViewFront = (FramePaintPreviewView) rootView.findViewById(R.id.front_preview);
        framePaintPreviewViewFront.init(previewBitmapFront, posEntitiesFront);
        framePaintPreviewViewFront.setLayoutParams(layoutParams);

        previewBitmapRear = BitmapFactory.decodeFile(Common.utilDirectory + "d4_r", options);
        framePaintPreviewViewRear = (FramePaintPreviewView) rootView.findViewById(R.id.rear_preview);
        framePaintPreviewViewRear.init(previewBitmapRear, posEntitiesRear);
        framePaintPreviewViewRear.setLayoutParams(layoutParams);
    }

    /**
     * 更新界面，主要是更新车型图片
     */
    public void updateUi() {
        setFigureImage(Integer.parseInt(BasicInfoLayout.mCarSettings.getFigure()));
    }

    /**
     * 获取该视角下的最后一个点的位置信息
     *
     * 参数：视角
     */
    public PosEntity getPosEntity(int flag) {
        return (flag == Common.ADD_COMMENT_FOR_ACCIDENT_FRONT_PHOTO) ?
                posEntitiesFront.get(posEntitiesFront.size() - 1) : posEntitiesRear.get(posEntitiesRear.size() - 1);
    }

    /**
     * 获取当前视角的全称，用于组织JSON串
     *
     * 参数：视角
     */
    private String getPart(int flag) {
        return (flag == Common.ADD_COMMENT_FOR_ACCIDENT_FRONT_PHOTO) ? "front" : "rear";
    }

    /**
     * 保存事故图片
     *
     * 参数：视角
     */
    public void saveAccidentPhoto(int flag) {
        PhotoEntity photoEntity = generatePhotoEntity(flag);

        List<PhotoEntity> photoEntities = getPart(flag).equals("front") ? AccidentResultLayout.photoEntitiesFront
                : AccidentResultLayout.photoEntitiesRear;

        photoEntities.add(photoEntity);
        issue.addPhoto(photoEntity);
        thisTimeNewPhoto.add(photoEntity);

        int index = adapter.getCount() + 1;
        IssuePhoto issuePhoto = new IssuePhoto(index, photoEntity.getThumbFileName(),
                photoEntity.getComment());
        adapter.addItem(issuePhoto);
        adapter.notifyDataSetChanged();

        PhotoFaultLayout.photoListAdapter.addItem(photoEntity);
        PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();
    }

    /**
     * 生成图片的实体
     *
     * 参数：视角
     */
    private PhotoEntity generatePhotoEntity(int flag) {
        PhotoEntity photoEntity = new PhotoEntity();

        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject photoJsonObject = new JSONObject();

            jsonObject.put("Group", "frame");
            jsonObject.put("Part", getPart(flag));

            PosEntity posEntity = getPosEntity(flag);

            photoJsonObject.put("x", posEntity.getStartX());
            photoJsonObject.put("y", posEntity.getStartY());
            photoJsonObject.put("width", getBitmapWidth(posEntity.getImageFileName()));
            photoJsonObject.put("height", getBitmapHeight(posEntity.getImageFileName()));
            photoJsonObject.put("issueId", posEntity.getIssueId());
            photoJsonObject.put("comment", posEntity.getComment());

            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("CarId", BasicInfoLayout.carId);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());

            photoEntity.setName("结构缺陷");
            photoEntity.setFileName(posEntity.getImageFileName());

            if(photoEntity.getFileName().equals("")) {
                photoEntity.setThumbFileName("");
            } else {
                photoEntity.setThumbFileName(posEntity.getImageFileName().substring(0, posEntity.getImageFileName().length() - 4) + "_t.jpg");
            }
            photoEntity.setComment(posEntity.getComment());
            photoEntity.setJsonString(jsonObject.toString());
        } catch (JSONException e) {
            Log.d(Common.TAG, e.getMessage());
        }

        return photoEntity;
    }

    /**
     * 根据车型信息调用不同的预览图
     *
     * 参数：车辆类型代码
     */
    private void setFigureImage(int figure) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        String path = Common.utilDirectory;

        // 默认为4门图
        String front = "d4_f";
        String rear = "d4_r";

        // 只有当figure为2、3时才是2门图
        switch (figure) {
            case 2:
            case 3:
                front = "d2_f";
                rear = "d2_r";
                break;
        }

        previewBitmapFront = BitmapFactory.decodeFile(path + front, options);

        LayoutParams layoutParams = new LayoutParams(previewBitmapFront.getWidth(), previewBitmapFront.getHeight());
        layoutParams.gravity = Gravity.CENTER;

        framePaintPreviewViewFront.init(previewBitmapFront, posEntitiesFront);
        framePaintPreviewViewFront.setLayoutParams(layoutParams);

        previewBitmapRear = BitmapFactory.decodeFile(path + rear, options);
        framePaintPreviewViewRear.init(previewBitmapRear, posEntitiesRear);
        framePaintPreviewViewRear.setLayoutParams(layoutParams);
    }

    /**
     * 生成草图（前视角x1，后视角x1）
     */
    public List<PhotoEntity> generateSketches() {
        List<PhotoEntity> temp = new ArrayList<PhotoEntity>();

        temp.add(generateSketch(framePaintPreviewViewFront, "fSketch"));
        temp.add(generateSketch(framePaintPreviewViewRear, "rSketch"));

        return temp;
    }

    /**
     * 根据视角生成不同草图
     *
     * 参数:1.视角 2.草图名称
     */
    private PhotoEntity generateSketch(PaintPreviewView view, String sketchName) {
        Bitmap bitmap = null;
        Canvas c;

        try {
            bitmap = Bitmap.createBitmap(view.getMaxWidth(),view.getMaxHeight(),
                    Bitmap.Config.ARGB_8888);
            c = new Canvas(bitmap);
            view.draw(c);

            FileOutputStream out = new FileOutputStream(Common.photoDirectory + sketchName);
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();

            photoJsonObject.put("height", bitmap.getHeight());
            photoJsonObject.put("width", bitmap.getWidth());

            jsonObject.put("Group", "frame");
            jsonObject.put("Part", sketchName);
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("CarId", BasicInfoLayout.carId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName(sketchName);
        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }

    public void clearCache() {
        issue = null;
        adapter = null;
        thisTimeNewPhoto = null;
        framePaintPreviewViewFront = null;
        framePaintPreviewViewRear = null;
        previewBitmapFront = null;
        previewBitmapRear = null;
        posEntitiesFront = null;
        posEntitiesRear = null;
        photoEntitiesFront = null;
        photoEntitiesRear = null;
    }
}
