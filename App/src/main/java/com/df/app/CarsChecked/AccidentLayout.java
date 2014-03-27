package com.df.app.carsChecked;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.df.app.R;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.paintView.FramePaintPreviewView;
import com.df.app.service.AsyncTask.DownloadImageTask;
import com.df.app.util.Common;
import com.df.app.util.MyScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by 岩 on 14-3-14.
 */
public class AccidentLayout extends LinearLayout {
    private FramePaintPreviewView framePaintPreviewViewFront;
    private ArrayList<PosEntity> posEntitiesFront;
    private ArrayList<PosEntity> posEntitiesRear;
    private ArrayList<PhotoEntity> photoEntitiesFront;
    private ArrayList<PhotoEntity> photoEntitiesRear;
    private Bitmap previewBitmapFront;
    private Bitmap previewBitmapRear;
    private FramePaintPreviewView framePaintPreviewViewRear;
    private View rootView;
    private DownloadImageTask downloadImageTask;
    private DownloadImageTask downloadImageTaskF;
    private DownloadImageTask downloadImageTaskR;

    public AccidentLayout(Context context, JSONObject accident, JSONObject photo) {
        super(context);
        init(context, accident, photo);
    }

    public AccidentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AccidentLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init(Context context, JSONObject accident, JSONObject photo) {
        rootView = LayoutInflater.from(context).inflate(R.layout.car_report_accident_layout, this);

        posEntitiesFront = new ArrayList<PosEntity>();
        posEntitiesRear = new ArrayList<PosEntity>();

        photoEntitiesFront = new ArrayList<PhotoEntity>();
        photoEntitiesRear = new ArrayList<PhotoEntity>();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        previewBitmapFront = BitmapFactory.decodeFile(Common.utilDirectory + "d4_f", options);

        framePaintPreviewViewFront = (FramePaintPreviewView) findViewById(R.id.front_preview);
        framePaintPreviewViewFront.init(previewBitmapFront, posEntitiesFront);

        previewBitmapRear = BitmapFactory.decodeFile(Common.utilDirectory + "d4_r", options);

        framePaintPreviewViewRear = (FramePaintPreviewView) findViewById(R.id.rear_preview);
        framePaintPreviewViewRear.init(previewBitmapRear, posEntitiesRear);

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
            fillInData(accident);
            updateImages(photo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fillInData(JSONObject accident) throws JSONException {
        JSONObject issues = accident.getJSONObject("issue");

        JSONArray jsonArray = issues.getJSONArray("issueItem");

        TableLayout tableLayout = (TableLayout)findViewById(R.id.issueResultTable);

        int length = jsonArray.length();

        for(int i = 0; i < length; i++) {
            JSONObject issueObject = jsonArray.getJSONObject(i);

            if(issueObject.get("summary") != JSONObject.NULL) {
                TableRow tableRow = new TableRow(rootView.getContext());
                TextView textView = new TextView(rootView.getContext());
                textView.setText(issueObject.getString("summary"));
                textView.setTextSize(20);
                textView.setTextColor(Color.rgb(0x55, 0x55, 0x55));
                tableRow.addView(textView);
                tableLayout.addView(tableRow);
            }
        }
    }

    private void updateImages(JSONObject photo) throws JSONException {
        JSONObject accident = photo.getJSONObject("accident");

        JSONObject accidentSketch = accident.getJSONObject("sketch");
        if(accidentSketch != JSONObject.NULL) {
            String accidentSketchUrl = accidentSketch.getString("photo");
            downloadImageTask = new DownloadImageTask(Common.getPICTURE_ADDRESS() + accidentSketchUrl, new DownloadImageTask.OnDownloadFinished() {
                @Override
                public void onFinish(Bitmap bitmap) {
                    ProgressBar progressBar = (ProgressBar)findViewById(R.id.issueImageProgressBar);
                    progressBar.setVisibility(GONE);
                    ImageView imageView = (ImageView)findViewById(R.id.issue_image);
                    imageView.setImageBitmap(bitmap);
                }

                @Override
                public void onFailed() {

                }
            });
            downloadImageTask.execute();
        }

        JSONObject frame = photo.getJSONObject("frame");

        // 结构草图 - 前视角
        JSONObject fSketch = frame.getJSONObject("fSketch");

        if(fSketch != JSONObject.NULL) {
            String fSketchUrl = fSketch.getString("photo");
            downloadImageTaskF = new DownloadImageTask(Common.getPICTURE_ADDRESS() + fSketchUrl, new DownloadImageTask.OnDownloadFinished() {
                @Override
                public void onFinish(Bitmap bitmap) {
                    ProgressBar progressBar = (ProgressBar)findViewById(R.id.frontProgressBar);
                    progressBar.setVisibility(GONE);
                    framePaintPreviewViewFront.init(bitmap, posEntitiesFront);
                    framePaintPreviewViewFront.invalidate();
                }

                @Override
                public void onFailed() {

                }
            });
            downloadImageTaskF.execute();
        }

        // 结构草图 - 后视角
        JSONObject rSketch = frame.getJSONObject("rSketch");

        if(fSketch != JSONObject.NULL) {
            String rSketchUrl = rSketch.getString("photo");
            downloadImageTaskR = new DownloadImageTask(Common.getPICTURE_ADDRESS() + rSketchUrl, new DownloadImageTask.OnDownloadFinished() {
                @Override
                public void onFinish(Bitmap bitmap) {
                    ProgressBar progressBar = (ProgressBar)findViewById(R.id.rearProgressBar);
                    progressBar.setVisibility(GONE);
                    framePaintPreviewViewRear.init(bitmap, posEntitiesRear);
                    framePaintPreviewViewRear.invalidate();
                }

                @Override
                public void onFailed() {
                    Log.d(Common.TAG, "下载后视角草图失败！");
                }
            });
            downloadImageTaskR.execute();
        }
    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void destroyTask() {
        if(downloadImageTask != null) {
            downloadImageTask.cancel(true);
        }
        if(downloadImageTaskF != null) {
            downloadImageTaskF.cancel(true);
        }
        if(downloadImageTaskR != null) {
            downloadImageTaskR.cancel(true);
        }
    }
}
