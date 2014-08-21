package com.df.app.carCheck;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.df.app.R;
import com.df.app.service.util.AppCommon;
import com.df.library.entries.Action;
import com.df.library.entries.CarSettings;
import com.df.library.entries.Issue;
import com.df.library.entries.ListedPhoto;
import com.df.library.entries.PhotoEntity;
import com.df.library.entries.PosEntity;
import com.df.app.paintView.FramePaintPreviewView;
import com.df.app.service.Adapter.IssuePhotoListAdapter;
import com.df.library.util.Common;
import com.df.library.entries.UserInfo;
import com.df.library.util.Helper;
import com.df.library.util.PhotoUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.df.library.util.Helper.getBitmapHeight;
import static com.df.library.util.Helper.getBitmapWidth;

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

    public static int fSketchIndex;
    public static int rSketchIndex;

    private View rootView;
    private int figure;

    public AccidentResultLayout(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.accident_result_layout, this);

        posEntitiesFront = new ArrayList<PosEntity>();
        posEntitiesRear = new ArrayList<PosEntity>();

        photoEntitiesFront = new ArrayList<PhotoEntity>();
        photoEntitiesRear = new ArrayList<PhotoEntity>();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        previewBitmapFront = BitmapFactory.decodeFile(AppCommon.utilDirectory + "d4_f", options);
        framePaintPreviewViewFront = (FramePaintPreviewView) rootView.findViewById(R.id.front_preview);
        framePaintPreviewViewFront.init(previewBitmapFront, posEntitiesFront);

        previewBitmapRear = BitmapFactory.decodeFile(AppCommon.utilDirectory + "d4_r", options);
        framePaintPreviewViewRear = (FramePaintPreviewView) rootView.findViewById(R.id.rear_preview);
        framePaintPreviewViewRear.init(previewBitmapRear, posEntitiesRear);
    }

    /**
     * 更新界面，主要是更新车型图片
     */
    public void updateUi() {
        // TODO 会崩
        int figure;
        
        if(CarSettings.getInstance().getFigure().equals("")) {
            figure = 0;
        } else {
            figure = Integer.parseInt(CarSettings.getInstance().getFigure());
        }
        
        setFigureImage(figure);
    }

    /**
     * 获取该视角下的最后一个点的位置数据
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

        int index = adapter.getCount();
        ListedPhoto listedPhoto = new ListedPhoto(index, photoEntity);
        adapter.addItem(listedPhoto);
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

            PosEntity posEntity = getPosEntity(flag);
            String fileName = posEntity.getImageFileName();
            int index = PhotoLayout.photoIndex++;

            photoJsonObject.put("x", posEntity.getStartX());
            photoJsonObject.put("y", posEntity.getStartY());
            photoJsonObject.put("width", getBitmapWidth(AppCommon.photoDirectory, fileName));
            photoJsonObject.put("height", getBitmapHeight(AppCommon.photoDirectory, fileName));
            photoJsonObject.put("issueId", posEntity.getIssueId());
            photoJsonObject.put("comment", posEntity.getComment());

            jsonObject.put("Group", "frame");
            jsonObject.put("Part", getPart(flag));
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("CarId", BasicInfoLayout.carId);
            jsonObject.put("UserId", UserInfo.getInstance().getId());
            jsonObject.put("Key", UserInfo.getInstance().getKey());
            jsonObject.put("Action", Action.MODIFY);
            jsonObject.put("Index", index);

            photoEntity = PhotoUtils.generatePhotoEntity(jsonObject, "结构缺陷", fileName,
                    Action.MODIFY, index, posEntity.getComment());
        } catch (JSONException e) {
            Log.d(AppCommon.TAG, e.getMessage());
        }

        return photoEntity;
    }

    /**
     * 根据车型数据调用不同的预览图
     *
     * 参数：车辆类型代码
     */
    private void setFigureImage(int figure) {
        this.figure = figure;

        previewBitmapFront = getBitmapFromFigure(figure, "fSketch");
        previewBitmapRear = getBitmapFromFigure(figure, "rSketch");

        framePaintPreviewViewFront.init(previewBitmapFront, posEntitiesFront);
        framePaintPreviewViewRear.init(previewBitmapRear, posEntitiesRear);
    }

    private Bitmap getBitmapFromFigure(int figure, String view) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeFile(AppCommon.utilDirectory + getBitmapNameFromFigure(figure, view), options);
        } catch (OutOfMemoryError e) {
            Toast.makeText(rootView.getContext(), "内存不足，请稍候重试！", Toast.LENGTH_SHORT).show();
        }

        return bitmap;
    }

    private String getBitmapNameFromFigure(int figure, String view) {
        return getNameFromFigure(figure, view);
    }

    private static String getNameFromFigure(int figure, String view) {
        return "d" + (figure == 2 || figure == 3 ? 2 : 4) + (view.equals("fSketch") ? "_f" : "_r");
    }

    /**
     * 生成草图（前视角x1，后视角x1）
     */
    public List<PhotoEntity> generateSketches() {
        List<PhotoEntity> temp = new ArrayList<PhotoEntity>();

        temp.add(generateSketch("fSketch"));
        temp.add(generateSketch("rSketch"));

        return temp;
    }

    /**
     * 根据视角生成不同草图(干净的)
     *
     * 参数:1.视角 2.草图名称
     */
    private PhotoEntity generateSketch(String sketchName) {
        Bitmap bitmap = getBitmapFromFigure(figure, sketchName);

        // 将草图文件拷贝到指定目录
        try {
            Helper.copy(new File(AppCommon.utilDirectory + getBitmapNameFromFigure(figure, sketchName)),
                    new File(AppCommon.photoDirectory + sketchName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        PhotoEntity photoEntity = new PhotoEntity();

        // 修改时，将原来的index填写进去
        int index = CarCheckActivity.isModify() ? (sketchName.equals("fSketch") ? fSketchIndex : rSketchIndex) : PhotoLayout.photoIndex++;

        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();

            photoJsonObject.put("height", bitmap.getHeight());
            photoJsonObject.put("width", bitmap.getWidth());

            jsonObject.put("Group", "frame");
            jsonObject.put("Part", sketchName);
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", UserInfo.getInstance().getId());
            jsonObject.put("Key", UserInfo.getInstance().getKey());
            jsonObject.put("CarId", BasicInfoLayout.carId);
            jsonObject.put("Action", photoEntity.getModifyAction());
            jsonObject.put("Index", photoEntity.getIndex());

            photoEntity = PhotoUtils.generatePhotoEntity(jsonObject, sketchName, sketchName,
                    CarCheckActivity.isModify() ? Action.MODIFY : Action.NORMAL, index, "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return photoEntity;
    }

    /**
     * 清空缓存
     */
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

        destroyTask();
    }

    /**
     * 修改或者半路检测时，下载前后视角草图并应用
     */
    public void fillInData(JSONObject photo) throws JSONException {
        JSONObject frame = photo.getJSONObject("frame");

        // 结构草图 - 前视角
        JSONObject fSketch = frame.getJSONObject("fSketch");

        if(fSketch != JSONObject.NULL) {
            fSketchIndex = fSketch.getInt("index");

            if(fSketchIndex >= PhotoLayout.photoIndex) {
                PhotoLayout.photoIndex = fSketchIndex + 1;
            }
        }

        // 结构草图 - 后视角
        JSONObject rSketch = frame.getJSONObject("rSketch");

        if(rSketch != JSONObject.NULL) {
            rSketchIndex = rSketch.getInt("index");

            if(rSketchIndex >= PhotoLayout.photoIndex) {
                PhotoLayout.photoIndex = rSketchIndex + 1;
            }
        }
    }

    /**
     * 退出时销毁正在执行的任务
     */
    public void destroyTask() {
//        if(downloadImageTaskF != null) {
//            downloadImageTaskF.cancel(true);
//        }
//        if(downloadImageTaskR != null) {
//            downloadImageTaskR.cancel(true);
//        }
    }
}
