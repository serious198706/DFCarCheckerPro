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
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.paintView.ExteriorPaintPreviewView;
import com.df.app.paintView.InteriorPaintPreviewView;
import com.df.app.service.AsyncTask.DownloadImageTask;
import com.df.app.util.Common;
import com.df.app.util.MyScrollView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.df.app.util.Helper.setEditViewText;
import static com.df.app.util.Helper.setSpinnerSelectionWithString;
import static com.df.app.util.Helper.setTextView;

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

    public IntegratedLayout(Context context, JSONObject conditions, JSONObject photo) {
        super(context);
        init(context, conditions, photo);
    }

    public IntegratedLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntegratedLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    private void init(Context context, JSONObject conditions, JSONObject photo) {
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
            fillInData(conditions);
            updateImage(photo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fillInData(JSONObject conditions) throws JSONException {
        JSONObject exterior = conditions.getJSONObject("exterior");

        setTextView(rootView, R.id.smooth_text, exterior.getString("smooth"));
        setTextView(rootView, R.id.glass_text, exterior.get("glass") == JSONObject.NULL ? "无" : exterior.getString("glass"));
        setTextView(rootView, R.id.screw_text, exterior.get("screw") == JSONObject.NULL ? "无" : exterior.getString("screw"));
        setTextView(rootView, R.id.needRepair_text, exterior.getString("needRepair"));
        setTextView(rootView, R.id.exterior_comment_text, exterior.get("comment") == JSONObject.NULL ? "无" : exterior.getString("comment"));

        JSONObject interior = conditions.getJSONObject("interior");

        setTextView(rootView, R.id.sealingStrip_text, interior.getString("sealingStrip"));
        setTextView(rootView, R.id.interior_comment_text, interior.get("comment") == JSONObject.NULL ? "无" : interior.getString("comment"));

        JSONObject tires = conditions.getJSONObject("tires");

        setTextView(rootView, R.id.leftFront_text, tires.get("leftFront") == JSONObject.NULL ? "" : tires.getString("leftFront"));
        setTextView(rootView, R.id.rightFront_text, tires.get("rightFront") == JSONObject.NULL ? "" : tires.getString("rightFront"));
        setTextView(rootView, R.id.leftRear_text, tires.get("leftRear") == JSONObject.NULL ? "" : tires.getString("leftRear"));
        setTextView(rootView, R.id.rightRear_text, tires.get("rightRear") == JSONObject.NULL ? "" : tires.getString("rightRear"));
        setTextView(rootView, R.id.spare_text, tires.get("spare") == JSONObject.NULL ? "" : tires.getString("spare"));
        setTextView(rootView, R.id.formatMatch_text, tires.get("formatMatch") == JSONObject.NULL ? "" : tires.getString("formatMatch"));
        setTextView(rootView, R.id.patternMatch_text, tires.get("patternMatch") == JSONObject.NULL ? "" : tires.getString("patternMatch"));

        JSONObject engine = conditions.getJSONObject("engine");

        setTextView(rootView, R.id.engine_text, engine.get("summaryPro") == JSONObject.NULL ? "正常" : engine.getString("summaryPro"));

        JSONObject gearbox = conditions.getJSONObject("gearbox");

        setTextView(rootView, R.id.gear_text, gearbox.get("summaryPro") == JSONObject.NULL ? "正常" : gearbox.getString("summaryPro"));

        JSONObject function = conditions.getJSONObject("function");

        if(function.getString("engineFault").equals("故障")) {

        }
        if(function.getString("oilPressure").equals("故障")) {

        }
        if(function.getString("parkingBrake").equals("故障")) {

        }
        if(function.getString("waterTemp").equals("故障")) {

        }
        if(function.getString("tachometer").equals("故障")) {

        }
        if(function.getString("milometer").equals("故障")) {

        }
        if(function.getString("audio").equals("故障")) {

        }

        if(function.get("abs") != JSONObject.NULL) {
            if(function.getString("abs").equals("故障")) {

            }
        }

        if(function.get("airBag") != JSONObject.NULL) {
            if(function.getString("airBag").equals("故障")) {

            }
        }

        if(function.get("powerWindows") != JSONObject.NULL) {
            if(function.getString("powerWindows").equals("故障")) {

            }
        }

        if(function.get("sunroof") != JSONObject.NULL) {
            if(function.getString("sunroof").equals("故障")) {

            }
        }

        if(function.get("airConditioning") != JSONObject.NULL) {
            if(function.getString("airConditioning").equals("故障")) {

            }
        }

        if(function.get("powerSeats") != JSONObject.NULL) {
            if(function.getString("powerSeats").equals("故障")) {

            }
        }

        if(function.get("powerMirror") != JSONObject.NULL) {
            if(function.getString("powerMirror").equals("故障")) {

            }
        }

        if(function.get("reversingRadar") != JSONObject.NULL) {
            if(function.getString("reversingRadar").equals("故障")) {

            }
        }

        if(function.get("reversingCamera") != JSONObject.NULL) {
            if(function.getString("reversingCamera").equals("故障")) {

            }
        }

        if(function.get("softCloseDoors") != JSONObject.NULL) {
            if(function.getString("softCloseDoors").equals("故障")) {

            }
        }

        if(function.get("rearPowerSeats") != JSONObject.NULL) {
            if(function.getString("rearPowerSeats").equals("故障")) {

            }
        }

        if(function.get("ahc") != JSONObject.NULL) {
            if(function.getString("ahc").equals("故障")) {

            }
        }

        if(function.get("parkAssist") != JSONObject.NULL) {
            if(function.getString("parkAssist").equals("故障")) {

            }
        }
    }

    private void updateImage(JSONObject photo) throws JSONException {
        JSONObject exterior = photo.getJSONObject("exterior");

        // 结构草图 - 前视角
        JSONObject exSketch = exterior.getJSONObject("sketch");

        if(exSketch != JSONObject.NULL) {
            String sketchUrl = exSketch.getString("photo");
            new DownloadImageTask(Common.PICTURE_ADDRESS + sketchUrl, new DownloadImageTask.OnDownloadFinished() {
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
            }).execute();
        }

        JSONObject interior = photo.getJSONObject("interior");

        // 结构草图 - 后视角
        JSONObject inSketch = interior.getJSONObject("sketch");

        if(inSketch != JSONObject.NULL) {
            String sketchUrl = inSketch.getString("photo");
            new DownloadImageTask(Common.PICTURE_ADDRESS + sketchUrl, new DownloadImageTask.OnDownloadFinished() {
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
            }).execute();
        }
    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }
}
