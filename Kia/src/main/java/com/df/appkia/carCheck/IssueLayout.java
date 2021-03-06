package com.df.appkia.carCheck;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.df.appkia.R;
import com.df.appkia.service.Adapter.IssueListAdapter;
import com.df.appkia.service.Adapter.IssuePhotoListAdapter;
import com.df.appkia.service.util.AppCommon;
import com.df.library.asyncTask.DownloadImageTask;
import com.df.library.asyncTask.DrawIssueImageTask;
import com.df.library.entries.Action;
import com.df.library.entries.Issue;
import com.df.library.entries.ListedPhoto;
import com.df.library.entries.PhotoEntity;
import com.df.library.entries.UserInfo;
import com.df.library.util.Common;
import com.df.library.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.df.library.util.Helper.showView;

/**
 * Created by 岩 on 13-12-20.
 *
 * 问题查勘
 */
public class IssueLayout extends LinearLayout {
    private View rootView;
    private IssueListAdapter adapter;
    private ArrayList<HashMap<String, String>> mylist;
    private ArrayList<Issue> issueList;
    private ImageView imageView;
    private ImageView imageViewHome;

    public static ListedPhoto listedPhoto;
    public static PhotoEntity photoEntityModify;
    public static IssuePhotoListAdapter photoListAdapter;

    public static int sketchIndex;
    public static int sketchHomeIndex;

    // 最后会用到，所以保存起来
    private JSONObject sketch;

    // 上部，消失部分
    private View header;

    // 问题列表
    private ListView issueListView;

    // 因爲畫圖佔用時間太長，耗費資源太多，所以放到後面執行，寫在一個線程裏
    private Runnable r;
    private String level1;
    private String level2;
    private DownloadImageTask downloadImageTask;
    private DownloadImageTask downloadHomeImageTask;

    public IssueLayout(Context context) {
        super(context);
        init(context);
    }

    public IssueLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IssueLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.issue_layout, this);

        issueListView = (ListView) findViewById(R.id.issue_list);
        header = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.issue_header, null);

        issueList  = new ArrayList<Issue>();

        adapter = new IssueListAdapter(context, issueList);

        issueListView.addHeaderView(header);
        issueListView.setAdapter(adapter);

        imageView = (ImageView)header.findViewById(R.id.issue_image);
        imageViewHome = (ImageView)header.findViewById(R.id.issue_home_image);

        issueListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
                if (absListView.getChildAt(0) == null) {
                    return;
                }

                showShadow(absListView.getChildAt(0).getTop() < 0);
            }
        });


    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void updateUi(String result, ProgressDialog progressDialog) {
        fillInData(result);
        drawImage(progressDialog);
    }

    /**
     * 生成事故检查JSONObject
     * @return
     * @throws org.json.JSONException
     */
    public JSONObject generateJSONObject() throws JSONException {
        // 问题查勘
        JSONObject issue = new JSONObject();

        // 示例图
        JSONObject sketch = this.sketch;

        // 问题条目
        JSONArray issueItem = new JSONArray();

        for(Issue temp : issueList) {
            JSONObject issueObject = new JSONObject();
            issueObject.put("desc", temp.getDesc());
            issueObject.put("issueId", temp.getId());
            issueObject.put("summary", "");
            issueObject.put("serious", temp.getSerious());
            issueObject.put("view", temp.getView());
            issueObject.put("select", temp.getSelect());

            issueItem.put(issueObject);
        }

        issue.put("sketch", sketch);
        issue.put("issueItem", issueItem);

        return issue;
    }

    /**
     * 生成事故检查的漆面草图
     * @return
     */
    public PhotoEntity generateSketch() {
        Bitmap bitmap = null;

        try {
            bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
            FileOutputStream out = new FileOutputStream(AppCommon.photoDirectory + "accident_sketch");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName("accident_sketch");

        // 如果是修改模式下，就是modify
        if(CarCheckActivity.isModify()) {
            photoEntity.setModifyAction(Action.MODIFY);
            photoEntity.setIndex(sketchIndex);
        } else {
            photoEntity.setModifyAction(Action.NORMAL);
            photoEntity.setIndex(PhotoLayout.photoIndex++);
        }

        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();

            photoJsonObject.put("width", bitmap.getWidth());
            photoJsonObject.put("height", bitmap.getHeight());

            jsonObject.put("Group", "accident");
            jsonObject.put("Part", "sketch");
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", UserInfo.getInstance().getId());
            jsonObject.put("Key", UserInfo.getInstance().getKey());
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
     * 生成事故检查的漆面草图
     * @return
     */
    public PhotoEntity generateHomeSketch() {
        Bitmap bitmap = null;

        try {
            bitmap = ((BitmapDrawable)imageViewHome.getDrawable()).getBitmap();
            FileOutputStream out = new FileOutputStream(AppCommon.photoDirectory + "accident_sketch_home");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName("accident_sketch_home");

        // 如果是修改模式下，就是modify
        if(CarCheckActivity.isModify()) {
            photoEntity.setModifyAction(Action.MODIFY);
            photoEntity.setIndex(sketchHomeIndex);
        } else {
            photoEntity.setModifyAction(Action.NORMAL);
            photoEntity.setIndex(PhotoLayout.photoIndex++);
        }

        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();

            photoJsonObject.put("width", bitmap.getWidth());
            photoJsonObject.put("height", bitmap.getHeight());

            jsonObject.put("Group", "accidentHome");
            jsonObject.put("Part", "sketch");
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", UserInfo.getInstance().getId());
            jsonObject.put("Key", UserInfo.getInstance().getKey());
            jsonObject.put("CarId", BasicInfoLayout.carId);
            jsonObject.put("Action", photoEntity.getModifyAction());
            jsonObject.put("Index", photoEntity.getIndex());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }

    private void drawImage(final ProgressDialog progressDialog) {
        DrawIssueImageTask drawIssueImageTask = new DrawIssueImageTask(getContext(), level1, level2, AppCommon.utilDirectory, new DrawIssueImageTask.OnDrawFinished() {
            @Override
            public void onFinished(Bitmap accident, Bitmap accidentHome) {
                imageView.setImageBitmap(accident);
                imageViewHome.setImageBitmap(accidentHome);

                if(progressDialog != null) {
                    progressDialog.dismiss();
                    ProgressBar progressBar = (ProgressBar)findViewById(R.id.issueImageProgressBar);
                    progressBar.setVisibility(GONE);
                }
            }
        });drawIssueImageTask.execute();


//        Handler handler = new Handler();
//
//        // 画图
//        r = new Runnable() {
//            @Override
//            public void run() {
//                drawBase();
//                drawSketch(imageView, handelLevelNames(level1, 1));
//                drawSketch(imageView, handelLevelNames(level2, 2));
//
//                drawableList.clear();
//                drawBase();
//                drawSketch(imageViewHome, handelLevelNames(level2, 2));
//
//                if(progressDialog != null) {
//                    progressDialog.dismiss();
//                    ProgressBar progressBar = (ProgressBar)findViewById(R.id.issueImageProgressBar);
//                    progressBar.setVisibility(GONE);
//                }
//            }
//        };
//
//
////        thread = new Thread(r);
////        thread.start();
//
//        handler.postDelayed(r, 3000);
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * @param issue
     */
    public void fillInData(JSONObject issue) throws JSONException {
        fillInData(issue.toString());
        drawImage(null);
    }

    public void fillInData(JSONObject issue, JSONObject photo) throws JSONException {
        fillInData(issue.toString());
        updateImage(photo);
    }

    private void fillInData(String result) {
        issueList.clear();

        try {
            JSONObject jsonObject = new JSONObject(result);

            // 获取覆盖件等级
            sketch = jsonObject.getJSONObject("sketch");

            level1 = sketch.getString("level1");
            level2 = sketch.getString("level2");

            // 获取问题列表
            JSONArray jsonArray = jsonObject.getJSONArray("issueItem");

            int length = jsonArray.length();

            for(int i = 0; i < length; i++) {
                JSONObject issueObject = jsonArray.getJSONObject(i);

                Issue issue = new Issue(issueObject.getInt("issueId"),
                        issueObject.getString("desc"), issueObject.getString("view"), "", "", "");

                if(issueObject.has("select")) {
                    issue.setSelect(issueObject.getString("select"));
                }

                if(issueObject.has("serious") && !issueObject.getString("serious").equals("")) {
                    issue.setSerious(issueObject.getString("serious"));
                }

                issueList.add(issue);
            }

            adapter.notifyDataSetChanged();

            for(PhotoEntity photoEntity : AccidentResultLayout.photoEntitiesFront) {
                if(PhotoFaultLayout.photoListAdapter.getItem(photoEntity) != null) {
                    PhotoFaultLayout.photoListAdapter.removeItem(photoEntity);
                }
            }

            for(PhotoEntity photoEntity : AccidentResultLayout.photoEntitiesRear) {
                if(PhotoFaultLayout.photoListAdapter.getItem(photoEntity) != null) {
                    PhotoFaultLayout.photoListAdapter.removeItem(photoEntity);
                }
            }

            PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();

            AccidentResultLayout.photoEntitiesFront.clear();
            AccidentResultLayout.photoEntitiesRear.clear();
            AccidentResultLayout.posEntitiesFront.clear();
            AccidentResultLayout.posEntitiesRear.clear();
            AccidentResultLayout.framePaintPreviewViewFront.invalidate();
            AccidentResultLayout.framePaintPreviewViewRear.invalidate();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public List<Issue> getIssues() {
        return issueList;
    }

    public void modifyComment(String comment) {
        Helper.updateComment(photoEntityModify, comment);
        Helper.updateComment(listedPhoto.getPhotoEntity(), comment);

        photoListAdapter.notifyDataSetChanged();
        PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();
    }

    public void clearCache() {
        listedPhoto = null;
        photoListAdapter = null;
        photoEntityModify = null;

        if(downloadImageTask != null) {
            downloadImageTask.cancel(true);
        }
    }

    private void updateImage(JSONObject photo) throws JSONException {
        showView(rootView, R.id.issueImageProgressBar, true);
        JSONObject accident = photo.getJSONObject("accident");

        JSONObject accidentSketch = accident.getJSONObject("sketch");
        if(accidentSketch != JSONObject.NULL) {
            sketchIndex = accidentSketch.getInt("index");
            String accidentSketchUrl = accidentSketch.getString("photo");
            downloadImageTask = new DownloadImageTask(getContext(), Common.getPICTURE_ADDRESS() + accidentSketchUrl, new DownloadImageTask.OnDownloadFinished() {
                @Override
                public void onFinish(Bitmap bitmap) {
                    showView(rootView, R.id.issueImageProgressBar, false);
                    ImageView imageView = (ImageView)findViewById(R.id.issue_image);
                    imageView.setImageBitmap(bitmap);
                }

                @Override
                public void onFailed() {

                }
            });
            downloadImageTask.execute();
        }

        if(photo.has("accidentHome")) {
            JSONObject accidentHome = photo.getJSONObject("accidentHome");
            JSONObject accidentHomeSketch = accidentHome.getJSONObject("sketch");
            if(accidentHomeSketch != JSONObject.NULL) {
                sketchHomeIndex = accidentHomeSketch.getInt("index");
                String accidentHomeSketchUrl = accidentHomeSketch.getString("photo");
                downloadHomeImageTask = new DownloadImageTask(getContext(), Common.getPICTURE_ADDRESS() + accidentHomeSketchUrl, new DownloadImageTask.OnDownloadFinished() {
                    @Override
                    public void onFinish(Bitmap bitmap) {
                        showView(rootView, R.id.issueImageProgressBar, false);
                        ImageView imageView = (ImageView)findViewById(R.id.issue_home_image);
                        imageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onFailed() {

                    }
                });
                downloadHomeImageTask.execute();
            }
        }
    }

    public void unbindDrawables() {
        unbindDrawables(findViewById(R.id.issue_image));
        unbindDrawables(findViewById(R.id.issue_home_image));
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
}
