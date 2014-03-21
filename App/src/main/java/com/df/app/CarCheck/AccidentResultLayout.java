package com.df.app.carCheck;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.Action;
import com.df.app.entries.Issue;
import com.df.app.entries.ListedPhoto;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.paintView.FramePaintPreviewView;
import com.df.app.paintView.PaintPreviewView;
import com.df.app.service.Adapter.IssuePhotoListAdapter;
import com.df.app.service.AsyncTask.DownloadImageTask;
import com.df.app.util.Common;
import com.df.app.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.df.app.util.Helper.getBitmapHeight;
import static com.df.app.util.Helper.getBitmapWidth;
import static com.df.app.util.Helper.showView;

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

        previewBitmapFront = BitmapFactory.decodeFile(Common.utilDirectory + "d4_f", options);

        framePaintPreviewViewFront = (FramePaintPreviewView) rootView.findViewById(R.id.front_preview);
        framePaintPreviewViewFront.init(previewBitmapFront, posEntitiesFront);

        previewBitmapRear = BitmapFactory.decodeFile(Common.utilDirectory + "d4_r", options);
        framePaintPreviewViewRear = (FramePaintPreviewView) rootView.findViewById(R.id.rear_preview);
        framePaintPreviewViewRear.init(previewBitmapRear, posEntitiesRear);
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

            jsonObject.put("Group", "frame");
            jsonObject.put("Part", getPart(flag));

            PosEntity posEntity = getPosEntity(flag);

            photoEntity.setName("结构缺陷");
            photoEntity.setFileName(posEntity.getImageFileName());
            photoEntity.setIndex(PhotoLayout.photoIndex++);

            // 如果是走了这段代码，则一定是添加照片
            // 如果是修改模式，则Action就是add
            if(CarCheckActivity.isModify()) {
                photoEntity.setModifyAction(Action.MODIFY);
            } else {
                photoEntity.setModifyAction(Action.MODIFY);
            }

            if(photoEntity.getFileName().equals("")) {
                photoEntity.setThumbFileName("");
            } else {
                photoEntity.setThumbFileName(posEntity.getImageFileName().substring(0, posEntity.getImageFileName().length() - 4) + "_t.jpg");
            }
            photoEntity.setComment(posEntity.getComment());

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
            jsonObject.put("Action", photoEntity.getModifyAction());
            jsonObject.put("Index", photoEntity.getIndex());

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

            bitmap = BitmapFactory.decodeFile(getBitmapNameFromFigure(figure, view), options);
        } catch (OutOfMemoryError e) {
            Toast.makeText(rootView.getContext(), "内存不足，请稍候重试！", Toast.LENGTH_SHORT).show();
            ((Activity)rootView.getContext()).finish();
        }

        return bitmap;
    }

    private String getBitmapNameFromFigure(int figure, String view) {
        return Common.utilDirectory + getNameFromFigure(figure, view);
    }

    private static String getNameFromFigure(int figure, String view) {
        String name;

        if(view.equals("fSketch")) {
            name = "d4_f";

            switch (figure) {
                case 2:
                case 3:
                    name = "d2_f";
                    break;
            }
        } else {
            name = "d2_r";

            switch (figure) {
                case 2:
                case 3:
                    name = "d2_r";
                    break;
            }
        }

        return name;
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

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName(sketchName);

        // 修改时，添加Action，并且将原来的index填写进去
        if(CarCheckActivity.isModify()) {
            photoEntity.setModifyAction(Action.MODIFY);
            photoEntity.setIndex(sketchName.equals("fSketch") ? fSketchIndex : rSketchIndex);
        } else {
            photoEntity.setModifyAction(Action.NORMAL);
            photoEntity.setIndex(PhotoLayout.photoIndex++);
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
            jsonObject.put("Action", photoEntity.getModifyAction());
            jsonObject.put("Index", photoEntity.getIndex());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        photoEntity.setJsonString(jsonObject.toString());
        return photoEntity;
    }

    /**
     * 根据视角生成不同草图(干净的)
     *
     * 参数:1.视角 2.草图名称
     */
    private PhotoEntity generateSketch(String sketchName) {
        Bitmap bitmap = getBitmapFromFigure(figure, sketchName);

        try {
            Helper.copy(new File(Common.utilDirectory + getBitmapNameFromFigure(figure, "sketchName")),
                    new File(Common.photoDirectory + sketchName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName(sketchName);

        // 修改时，添加Action，并且将原来的index填写进去
        if(CarCheckActivity.isModify()) {
            photoEntity.setModifyAction(Action.MODIFY);
            photoEntity.setIndex(sketchName.equals("fSketch") ? fSketchIndex : rSketchIndex);
        } else {
            photoEntity.setModifyAction(Action.NORMAL);
            photoEntity.setIndex(PhotoLayout.photoIndex++);
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
            jsonObject.put("Action", photoEntity.getModifyAction());
            jsonObject.put("Index", photoEntity.getIndex());
        } catch (JSONException e) {
            e.printStackTrace();
        }

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

    public void fillInData(JSONObject photo) throws JSONException {
        showView(rootView, R.id.frontProgressBar, true);
        showView(rootView, R.id.rearProgressBar, true);
        JSONObject frame = photo.getJSONObject("frame");

        // 结构草图 - 前视角
        JSONObject fSketch = frame.getJSONObject("fSketch");

        if(fSketch != JSONObject.NULL) {
            fSketchIndex = fSketch.getInt("index");

            if(fSketchIndex >= PhotoLayout.photoIndex) {
                PhotoLayout.photoIndex = fSketchIndex + 1;
            }

            String fSketchUrl = fSketch.getString("photo");
            new DownloadImageTask(Common.PICTURE_ADDRESS + fSketchUrl, new DownloadImageTask.OnDownloadFinished() {
                @Override
                public void onFinish(Bitmap bitmap) {
                    showView(rootView, R.id.frontProgressBar, false);
                    framePaintPreviewViewFront.init(bitmap, posEntitiesFront);
                    framePaintPreviewViewFront.invalidate();
                }

                @Override
                public void onFailed() {

                }
            }).execute();
        }

        // 结构草图 - 后视角

        JSONObject rSketch = frame.getJSONObject("rSketch");

        if(rSketch != JSONObject.NULL) {
            rSketchIndex = rSketch.getInt("index");

            if(rSketchIndex >= PhotoLayout.photoIndex) {
                PhotoLayout.photoIndex = rSketchIndex + 1;
            }

            String rSketchUrl = rSketch.getString("photo");
            new DownloadImageTask(Common.PICTURE_ADDRESS + rSketchUrl, new DownloadImageTask.OnDownloadFinished() {
                @Override
                public void onFinish(Bitmap bitmap) {
                    showView(rootView, R.id.rearProgressBar, false);
                    framePaintPreviewViewRear.init(bitmap, posEntitiesRear);
                    framePaintPreviewViewRear.invalidate();
                }

                @Override
                public void onFailed() {
                    Log.d(Common.TAG, "下载后视角草图失败！");
                }
            }).execute();
        }
    }
}
