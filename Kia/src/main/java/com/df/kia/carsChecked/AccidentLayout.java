package com.df.kia.carsChecked;

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

import com.df.kia.R;
import com.df.library.entries.PhotoEntity;
import com.df.library.entries.PosEntity;
import com.df.kia.paintView.FramePaintPreviewView;
import com.df.library.asyncTask.DownloadImageTask;
import com.df.kia.service.util.AppCommon;
import com.df.library.util.Common;
import com.df.library.util.MyScrollView;

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

        previewBitmapFront = BitmapFactory.decodeFile(AppCommon.utilDirectory + "d4_f", options);

        framePaintPreviewViewFront = (FramePaintPreviewView) findViewById(R.id.front_preview);
        framePaintPreviewViewFront.init(previewBitmapFront, posEntitiesFront);

        previewBitmapRear = BitmapFactory.decodeFile(AppCommon.utilDirectory + "d4_r", options);

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
        scrollView.setOnTouchListener(new OnTouchListener() {
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

        JSONArray newJsonArray = new JSONArray();

        // 找出所有需要显示的条目
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject issueObject = jsonArray.getJSONObject(i);

            if(issueObject.get("summary") != JSONObject.NULL) {
                newJsonArray.put(issueObject);
            }
        }

        TableLayout tableLayout = (TableLayout)findViewById(R.id.issueResultTable);

        int length = newJsonArray.length();

        // 一行两条
        int count = length % 2 == 0 ? length / 2 : length / 2 + 1;

        for(int i = 0; i < count; i++) {
            TableRow tableRow = new TableRow(rootView.getContext());

            for(int j = 0; j < 2 && (i * 2 + j < length); j++) {
                JSONObject issueObject = newJsonArray.getJSONObject(i * 2 + j);

                if(issueObject != null && issueObject.get("summary") != JSONObject.NULL) {
                    TextView textView = new TextView(rootView.getContext());
                    textView.setText("●" + issueObject.getString("summary"));
                    textView.setTextSize(20);
                    textView.setTextColor(Color.rgb(0x55, 0x55, 0x55));
                    textView.setPadding(8, 0, 8, 0);
                    tableRow.addView(textView);
                }
            }

            tableLayout.addView(tableRow);
        }
//
//        for(int i = 0; i < length; i++) {
//            JSONObject issueObject = jsonArray.getJSONObject(i);
//
//            if(issueObject.get("summary") != JSONObject.NULL) {
//                TableRow tableRow = new TableRow(rootView.getContext());
//                TextView textView = new TextView(rootView.getContext());
//                textView.setText(issueObject.getString("summary"));
//                textView.setTextSize(20);
//                textView.setTextColor(Color.rgb(0x55, 0x55, 0x55));
//                tableRow.addView(textView);
//                tableLayout.addView(tableRow);
//            }
//        }
    }



    private void updateImages(JSONObject photo) throws JSONException {
        JSONObject accident = photo.getJSONObject("accident");

        JSONObject accidentSketch = accident.getJSONObject("sketch");
        if(accidentSketch != JSONObject.NULL) {
            String accidentSketchUrl = accidentSketch.getString("photo");
            downloadImageTask = new DownloadImageTask(getContext(), Common.getPICTURE_ADDRESS() + accidentSketchUrl, new DownloadImageTask.OnDownloadFinished() {
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

        // 结构缺陷位置 - 前视角
        if(!frame.isNull("front")) {
            JSONArray frontPosArray = frame.getJSONArray("front");

            for(int i = 0; i < frontPosArray.length(); i++) {
                JSONObject temp = frontPosArray.getJSONObject(i);

                PosEntity posEntity = new PosEntity(Common.COLOR_DIFF);
                posEntity.setStart(temp.getInt("x"), temp.getInt("y"));
                posEntity.setEnd(0, 0);
                posEntity.setImageFileName(temp.getString("photo"));
                posEntitiesFront.add(posEntity);
            }
        } else {
            framePaintPreviewViewFront.setAlpha(1.0f);
            framePaintPreviewViewFront.invalidate();
        }

        // 结构缺陷位置 - 后视角
        if(!frame.isNull("rear")) {
            JSONArray rearPosArray = frame.getJSONArray("rear");

            for(int i = 0; i < rearPosArray.length(); i++) {
                JSONObject temp = rearPosArray.getJSONObject(i);

                PosEntity posEntity = new PosEntity(Common.COLOR_DIFF);
                posEntity.setStart(temp.getInt("x"), temp.getInt("y"));
                posEntity.setEnd(0, 0);
                posEntity.setImageFileName(temp.getString("photo"));
                posEntitiesRear.add(posEntity);
            }
        } else {
            framePaintPreviewViewRear.setAlpha(1.0f);
            framePaintPreviewViewRear.invalidate();
        }

        // 结构草图 - 前视角
        JSONObject fSketch = frame.getJSONObject("fSketch");

        if(fSketch != JSONObject.NULL) {
            String fSketchUrl = fSketch.getString("photo");
            downloadImageTaskF = new DownloadImageTask(getContext(), Common.getPICTURE_ADDRESS() + fSketchUrl, new DownloadImageTask.OnDownloadFinished() {
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
            downloadImageTaskR = new DownloadImageTask(getContext(), Common.getPICTURE_ADDRESS() + rSketchUrl, new DownloadImageTask.OnDownloadFinished() {
                @Override
                public void onFinish(Bitmap bitmap) {
                    ProgressBar progressBar = (ProgressBar)findViewById(R.id.rearProgressBar);
                    progressBar.setVisibility(GONE);
                    framePaintPreviewViewRear.init(bitmap, posEntitiesRear);
                    framePaintPreviewViewRear.invalidate();
                }

                @Override
                public void onFailed() {
                    Log.d(AppCommon.TAG, "下载后视角草图失败！");
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
