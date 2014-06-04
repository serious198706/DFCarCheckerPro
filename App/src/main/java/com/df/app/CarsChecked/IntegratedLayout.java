package com.df.app.carsChecked;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.df.app.R;
import com.df.app.carCheck.Integrated2Layout;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.paintView.ExteriorPaintPreviewView;
import com.df.app.paintView.InteriorPaintPreviewView;
import com.df.app.service.AsyncTask.DownloadImageTask;
import com.df.app.util.Common;
import com.df.app.util.MyScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import static com.df.app.util.Helper.setEditViewText;
import static com.df.app.util.Helper.setSpinnerSelectionWithString;
import static com.df.app.util.Helper.setTextView;
import static com.df.app.util.Helper.showView;

/**
 * Created by 岩 on 14-3-14.
 */
public class IntegratedLayout extends LinearLayout {
    private ExteriorPaintPreviewView exteriorPaintPreviewView;
    private InteriorPaintPreviewView interiorPaintPreviewView;
    private ArrayList<PosEntity> exPosEntities;
    private ArrayList<PhotoEntity> exPhotoEntities;
    private ArrayList<PosEntity> inPosEntities;
    private ArrayList<PhotoEntity> inPhotoEntities;
    private View rootView;
    private DownloadImageTask downloadImageTask;
    private DownloadImageTask downloadImage1Task;

    public IntegratedLayout(Context context, JSONObject conditions, JSONObject options, JSONObject photo) {
        super(context);
        init(context, conditions, options, photo);
    }

    public IntegratedLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntegratedLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    private void init(Context context, JSONObject conditions, JSONObject options, JSONObject photo) {
        rootView = LayoutInflater.from(context).inflate(R.layout.car_report_integrated_layout, this);

        Bitmap bitmap = BitmapFactory.decodeFile(Common.utilDirectory + "r3d4");
        exPosEntities = new ArrayList<PosEntity>();
        exPhotoEntities = new ArrayList<PhotoEntity>();
        exteriorPaintPreviewView = (ExteriorPaintPreviewView) findViewById(R.id.exterior_image);
        exteriorPaintPreviewView.init(bitmap, exPosEntities);

        inPosEntities = new ArrayList<PosEntity>();
        inPhotoEntities = new ArrayList<PhotoEntity>();
        interiorPaintPreviewView = (InteriorPaintPreviewView) findViewById(R.id.interior_image);
        interiorPaintPreviewView.init(bitmap, inPosEntities);

        MyScrollView scrollView = (MyScrollView) findViewById(R.id.root);
        scrollView.setListener(new MyScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy) {
                if (scrollView.getScrollY() > 5) {
                    showShadow(true);
                } else {
                    showShadow(false);
                }
            }
        });

        // 移除输入框的焦点，避免每次输入完成后界面滚动
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        scrollView.setFocusable(true);
        scrollView.setFocusableInTouchMode(true);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
            }
        });

        try {
            fillInData(conditions, options);
            updateImage(photo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fillInData(JSONObject conditions, JSONObject options) throws JSONException {
        // 外观检查
        JSONObject exterior = conditions.getJSONObject("exterior");

        setTextView(rootView, R.id.smooth_text, exterior.getString("smooth"));
        setTextView(rootView, R.id.glass_text, exterior.get("glass") == JSONObject.NULL ? "无" : exterior.getString("glass"));
        setTextView(rootView, R.id.screw_text, exterior.get("screw") == JSONObject.NULL ? "无" : exterior.getString("screw"));
        setTextView(rootView, R.id.needRepair_text, exterior.getString("needRepair"));
        setTextView(rootView, R.id.exterior_comment_text, exterior.get("comment") == JSONObject.NULL ? "无" : exterior.getString("comment"));

        // 内饰检查
        JSONObject interior = conditions.getJSONObject("interior");

        setTextView(rootView, R.id.sealingStrip_text, interior.getString("sealingStrip"));
        setTextView(rootView, R.id.interior_comment_text, interior.get("comment") == JSONObject.NULL ? "无" : interior.getString("comment"));

        // 轮胎检查
        JSONObject tires = conditions.getJSONObject("tires");

        setTextView(rootView, R.id.leftFront_text, tires.get("leftFront") == JSONObject.NULL ? "" : tires.getString("leftFront"));
        setTextView(rootView, R.id.rightFront_text, tires.get("rightFront") == JSONObject.NULL ? "" : tires.getString("rightFront"));
        setTextView(rootView, R.id.leftRear_text, tires.get("leftRear") == JSONObject.NULL ? "" : tires.getString("leftRear"));
        setTextView(rootView, R.id.rightRear_text, tires.get("rightRear") == JSONObject.NULL ? "" : tires.getString("rightRear"));
        setTextView(rootView, R.id.spare_text, tires.get("spare") == JSONObject.NULL ? "" : tires.getString("spare"));

        showView(rootView, R.id.spareTireRow, !options.getString("spareTire").equals("无"));

        setTextView(rootView, R.id.formatMatch_text, tires.get("formatMatch") == JSONObject.NULL ? "" : tires.getString("formatMatch"));
        setTextView(rootView, R.id.patternMatch_text, tires.get("patternMatch") == JSONObject.NULL ? "" : tires.getString("patternMatch"));

        // 发动机检查
        JSONObject engine = conditions.getJSONObject("engine");

        setTextView(rootView, R.id.engine_text, engine.get("summaryPro") == JSONObject.NULL ? "正常" : engine.getString("summaryPro"));

        // 变速箱检查
        JSONObject gearbox = conditions.getJSONObject("gearbox");

        setTextView(rootView, R.id.gear_text, gearbox.get("summaryPro") == JSONObject.NULL ? "正常" : gearbox.getString("summaryPro"));

        // 功能检查
        JSONObject function = conditions.getJSONObject("function");

        String functionResult = "";
        Iterator keys = function.keys();

        while(keys.hasNext()) {
            String key = (String)keys.next();

            if(function.get(key) != JSONObject.NULL && function.getString(key).equals("故障")) {
                String name = getResources().getString(getResources().getIdentifier(key, "string", getContext().getPackageName()));
                functionResult += name.substring(0, name.length() - 1) + "故障" + ";";
            }
            // 该项在配置信息中有，但是在综合检查中没有，则可以被定性为缺失
            else if(function.get(key) == JSONObject.NULL && options.has(key) && options.get(key) != JSONObject.NULL){
                String name = getResources().getString(getResources().getIdentifier(key, "string", getContext().getPackageName()));
                functionResult += name.substring(0, name.length() - 1) + "缺失" + ";";
            }
        }

        if(functionResult.contains("气囊数")) {
            functionResult.replace("气囊数", "气囊");
        }

        setTextView(rootView, R.id.function_text, functionResult);

        // 泡水检查
        JSONObject flooded = conditions.getJSONObject("flooded");
        keys = flooded.keys();

        setTextView(rootView, R.id.flood_text, "非泡水车");

        while(keys.hasNext()) {
            String key = (String)keys.next();

            if(flooded.getString(key).equals("否")) {
                setTextView(rootView, R.id.flood_text, "疑似泡水车");
                break;
            }
        }

        // 备注
        setTextView(rootView, R.id.integrated_comment_text,
                conditions.get("comment") == JSONObject.NULL ? "无" : conditions.getString("comment"));
    }

    private void updateImage(JSONObject photo) throws JSONException {
        JSONObject exterior = photo.getJSONObject("exterior");

        // 如果有缺陷点
        if(!exterior.isNull("fault")) {
            JSONArray fault = exterior.getJSONArray("fault");

            for(int i = 0; i < fault.length(); i++) {
                JSONObject jsonObject = fault.getJSONObject(i);

                int type = jsonObject.getInt("type");

                PosEntity posEntity = new PosEntity(type);

                // 要设置max，否则在使用endx endy时会返回零
                posEntity.setMaxX(1000);
                posEntity.setMaxY(2000);

                int startX, startY, endX, endY, radius;

                if(type == Common.TRANS) {
                    radius = jsonObject.getInt("radius");
                    startX = jsonObject.getInt("startX") - radius;
                    startY = jsonObject.getInt("startY") - radius;
                    endX = jsonObject.getInt("startX") + radius;
                    endY = jsonObject.getInt("startY") + radius;
                } else {
                    startX = jsonObject.getInt("startX");
                    startY = jsonObject.getInt("startY");
                    endX = jsonObject.getInt("endX");
                    endY = jsonObject.getInt("endY");
                }

                posEntity.setStart(startX, startY);
                posEntity.setEnd(endX, endY);
                posEntity.setImageFileName(jsonObject.getString("photo"));

                exPosEntities.add(posEntity);
            }
        } else {
            exteriorPaintPreviewView.setAlpha(1.0f);
            exteriorPaintPreviewView.invalidate();
        }

        // 结构草图 - 前视角
        JSONObject exSketch = exterior.getJSONObject("sketch");

        if(exSketch != JSONObject.NULL) {
            String sketchUrl = exSketch.getString("photo");
            downloadImageTask = new DownloadImageTask(getContext(), Common.getPICTURE_ADDRESS() + sketchUrl, new DownloadImageTask.OnDownloadFinished() {
                @Override
                public void onFinish(Bitmap bitmap) {
                    ProgressBar progressBar = (ProgressBar)findViewById(R.id.exProgressBar);
                    progressBar.setVisibility(GONE);
                    exteriorPaintPreviewView.init(bitmap, exPosEntities);
                    exteriorPaintPreviewView.invalidate();
                }

                @Override
                public void onFailed() {

                }
            });
            downloadImageTask.execute();
        }

        JSONObject interior = photo.getJSONObject("interior");

        if(!interior.isNull("fault")) {
            JSONArray fault = interior.getJSONArray("fault");

            for(int i = 0; i < fault.length(); i++) {
                JSONObject jsonObject = fault.getJSONObject(i);

                int type = jsonObject.getInt("type");

                PosEntity posEntity = new PosEntity(type);

                // 要设置max，否则在使用endx endy时会返回零
                posEntity.setMaxX(1000);
                posEntity.setMaxY(2000);

                int startX, startY, endX, endY, radius;

                if(type == Common.BROKEN) {
                    radius = jsonObject.getInt("radius");
                    startX = jsonObject.getInt("startX") - radius;
                    startY = jsonObject.getInt("startY") - radius;
                    endX = jsonObject.getInt("startX") + radius;
                    endY = jsonObject.getInt("startY") + radius;
                } else {
                    startX = jsonObject.getInt("startX");
                    startY = jsonObject.getInt("startY");
                    endX = jsonObject.getInt("endX");
                    endY = jsonObject.getInt("endY");
                }

                posEntity.setStart(startX, startY);
                posEntity.setEnd(endX, endY);
                posEntity.setImageFileName(jsonObject.getString("photo"));

                inPosEntities.add(posEntity);
            }
        } else {
            interiorPaintPreviewView.setAlpha(1.0f);
            interiorPaintPreviewView.invalidate();
        }

        // 结构草图 - 后视角
        JSONObject inSketch = interior.getJSONObject("sketch");

        if(inSketch != JSONObject.NULL) {
            String sketchUrl = inSketch.getString("photo");
            downloadImage1Task = new DownloadImageTask(getContext(), Common.getPICTURE_ADDRESS() + sketchUrl, new DownloadImageTask.OnDownloadFinished() {
                @Override
                public void onFinish(Bitmap bitmap) {
                    ProgressBar progressBar = (ProgressBar)findViewById(R.id.inProgressBar);
                    progressBar.setVisibility(GONE);
                    interiorPaintPreviewView.init(bitmap, inPosEntities);
                    interiorPaintPreviewView.invalidate();
                }

                @Override
                public void onFailed() {
                    Log.d(Common.TAG, "下载后视角草图失败！");
                }
            });

            downloadImage1Task.execute();
        }
    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void destroyTask() {
        if(downloadImageTask != null) {
            downloadImageTask.cancel(true);
        }
        if(downloadImage1Task != null) {
            downloadImage1Task.cancel(true);
        }

        for(int i = 0; i < Integrated2Layout.photoShotCount.length; i++) {
            Integrated2Layout.photoShotCount[i] = 0;
        }
    }
}
