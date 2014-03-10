package com.df.app.carCheck;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.Issue;
import com.df.app.entries.IssuePhoto;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.Adapter.IssueListAdapter;
import com.df.app.service.Adapter.IssuePhotoListAdapter;
import com.df.app.util.Common;
import com.df.app.util.QuickReturnListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public static IssuePhoto issuePhoto;
    public static PhotoEntity photoEntityModify;
    public static IssuePhotoListAdapter photoListAdapter;

    // 图片层
    private ArrayList<Drawable> drawableList;

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

        issueListView.setAdapter(adapter);
        issueListView.addHeaderView(header);

        imageView = (ImageView)header.findViewById(R.id.issue_image);

        issueListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
                if (absListView.getChildAt(0) == null) {
                    return;
                }

                if (absListView.getChildAt(0).getTop() < 0) {
                    showShadow(true);
                } else {
                    showShadow(false);
                }
            }
        });

        // 画图
        r = new Runnable() {
            @Override
            public void run() {
                drawBase();
                drawSketch(handelLevelNames(level1, 1));
                drawSketch(handelLevelNames(level2, 2));
            }
        };
    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void updateUi(String result, ProgressDialog progressDialog) {
        fillInData(result, progressDialog);
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * @param result
     * @param progressDialog
     */
    private void fillInData(String result, ProgressDialog progressDialog) {
        issueList.clear();

        try {
            JSONObject jsonObject = new JSONObject(result);

            // 获取覆盖件等级
            sketch = jsonObject.getJSONObject("sketch");

            level1 = sketch.getString("level1");
            level2 = sketch.getString("level2");

            // 获取问题列表
            JSONArray jsonArray = jsonObject.getJSONArray("issueItem");

            for(int i = 0; i < jsonArray.length(); i++) {
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

            r.run();

            if(progressDialog != null)
                progressDialog.dismiss();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 传入"L,E,K,LC,RB"，传出"L1, E1, K1"
    *
     */
    private String handelLevelNames(String level, int n) {
        String[] partNames = level.split(",");

        String result = "";

        for(String part : partNames) {
            // 将LA,LB,LC,RA,RB,RC过滤掉
            if(part.length() != 1) {
                continue;
            }

            part += Integer.toString(n);
            result += part;
            result += ",";
        }

        return result.length() == 0 ? result : result.substring(0, result.length() - 1);
    }

    /**
     * 绘制底图
     */
    private void drawBase() {
        Bitmap baseBitmap = BitmapFactory.decodeFile(Common.utilDirectory + "base");

        // 先添加底图
        drawableList = new ArrayList<Drawable>();
        drawableList.add(new BitmapDrawable(getResources(), baseBitmap));
    }

    /**
     * 设置漆面预览图
     * @param level
     */
    private void drawSketch(String level) {
        try {
            String[] partNames = level.split(",");

            // 根据名称添加其他图
            for(String layerName : partNames) {
                Bitmap bitmap = BitmapFactory.decodeFile(Common.utilDirectory + layerName);
                Drawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                bitmap = null;
                drawableList.add(bitmapDrawable);
            }

            // 创建LayerDrawable
            LayerDrawable layerDrawable = new LayerDrawable(drawableList.toArray(new Drawable[drawableList.size()]));

            int width = layerDrawable.getIntrinsicWidth();
            int height = layerDrawable.getIntrinsicHeight();

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            layerDrawable.setBounds(0, 0, width, height);
            layerDrawable.draw(new Canvas(bitmap));

            // 将LayerDrawable添加到imageView中
            imageView.setImageBitmap(bitmap);
        } catch (OutOfMemoryError e) {
            Toast.makeText(rootView.getContext(), "内存不足，请稍候重试！", Toast.LENGTH_SHORT).show();
            ((Activity)rootView.getContext()).finish();
        }
    }

    /**
     * 填入测试数据
     * @return
     */
    private ArrayList<Issue> fillInDummyData() {
        issueList  = new ArrayList<Issue>();

        issueList.add(new Issue(1, "右前门铰链处是否平整、无修复焊接痕迹", "F", "", "", ""));
        issueList.add(new Issue(2, "前杠与前翼子板之间的缝隙是否整齐均匀", "R", "", "", ""));
        issueList.add(new Issue(3, "右前翼子板和翼子板内衬连接处的胶体是否规整",  "R", "", "", ""));
        issueList.add(new Issue(4, "右前翼子板内侧是否平整、无回火焊点或折痕", "R", "", "", ""));
        issueList.add(new Issue(5, "右前翼子板内衬板是否平整、无钣金修复痕迹","F", "", "", ""));
        issueList.add(new Issue(6, "右前减震器座是否平整、无钣金修复痕迹", "N", "", "", ""));
        issueList.add(new Issue(7, "引擎盖内侧封边及内衬是否整齐、无修复焊接痕迹", "R", "", "", ""));

        return issueList;
    }

    /**
     * 生成事故检查JSONObject
     * @return
     * @throws JSONException
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
            FileOutputStream out = new FileOutputStream(Common.photoDirectory + "accident_sketch");
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();

            photoJsonObject.put("width", bitmap.getWidth());
            photoJsonObject.put("height", bitmap.getHeight());

            jsonObject.put("Group", "accident");
            jsonObject.put("Part", "sketch");
            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
            jsonObject.put("CarId", BasicInfoLayout.carId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName("accident_sketch");
        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * @param issue
     */
    public void fillInData(JSONObject issue) {
        fillInData(issue.toString(), null);
    }

    public List<Issue> getIssues() {
        return issueList;
    }

    public void drawSketch() {

    }

    public void modifyComment(String comment) {
        photoEntityModify.setComment(comment);
        issuePhoto.setDesc(comment);
        photoListAdapter.notifyDataSetChanged();
        PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();
    }

    public void clearCache() {
        issuePhoto = null;
        photoListAdapter = null;
        photoEntityModify = null;
    }
}
