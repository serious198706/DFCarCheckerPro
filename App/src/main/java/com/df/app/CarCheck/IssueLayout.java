package com.df.app.CarCheck;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.df.app.R;
import com.df.app.entries.Issue;
import com.df.app.service.Adapter.IssueListAdapter;
import com.df.app.util.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 岩 on 13-12-20.
 */
public class IssueLayout extends LinearLayout {
    private View rootView;
    private IssueListAdapter adapter;
    private ArrayList<HashMap<String, String>> mylist;
    private ArrayList<Issue> issueList;
    private ImageView imageView;

    // 图片层
    private ArrayList<Drawable> drawableList;

    // 最后会用到，所以保
    private JSONObject sketch;

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

        ListView issueListView = (ListView) findViewById(R.id.issue_list);

        issueList  = new ArrayList<Issue>();

        adapter = new IssueListAdapter(context, issueList);

        issueListView.setAdapter(adapter);

        imageView = (ImageView)findViewById(R.id.issue_image);

        issueListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
                if(absListView.getChildAt(0) == null) {
                    return;
                }

                if(absListView.getChildAt(0).getTop() < 0) {
                    showShadow(true);
                } else {
                    showShadow(false);
                }
            }
        });
    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void updateUi(String result, ProgressDialog progressDialog) {
        fillInData(result, progressDialog);
    }

    private void fillInData(String result, ProgressDialog progressDialog) {
        issueList.clear();

        try {
            JSONObject jsonObject = new JSONObject(result);

            // 获取覆盖件等级
            sketch = jsonObject.getJSONObject("sketch");

            String level1 = sketch.getString("level1");
            String level2 = sketch.getString("level2");

            // 获取问题列表
            JSONArray jsonArray = jsonObject.getJSONArray("issueItem");

            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject issueObject = jsonArray.getJSONObject(i);

                Issue issue = new Issue(issueObject.getInt("issueId"),
                        issueObject.getString("desc"), issueObject.getString("view"), "", "轻微", "是");
                issueList.add(issue);
            }

            adapter.notifyDataSetChanged();

            // 画图
            drawBase();
            drawSketch(handelLevelNames(level1, 1));
            drawSketch(handelLevelNames(level2, 2));

            progressDialog.dismiss();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 传入"L,E,K,LC,RB"，传出"L1, E1, K1"
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

    private void drawBase() {
        Bitmap baseBitmap = BitmapFactory.decodeFile(Common.utilDirectory + "base");

        // 先添加底图
        drawableList = new ArrayList<Drawable>();
        drawableList.add(new BitmapDrawable(getResources(), baseBitmap));
    }

    private void drawSketch(String level) {
        String[] partNames = level.split(",");

        // 根据名称添加其他图
        for(String layerName : partNames) {
            Bitmap bitmap = BitmapFactory.decodeFile(Common.utilDirectory + layerName);
            Drawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
            drawableList.add(bitmapDrawable);
        }

        // 创建LayerDrawable
        LayerDrawable layerDrawable = new LayerDrawable(drawableList.toArray(new Drawable[0]));

        int width = layerDrawable.getIntrinsicWidth();
        int height = layerDrawable.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        layerDrawable.setBounds(0, 0, width, height);
        layerDrawable.draw(new Canvas(bitmap));

        // 将LayerDrawable添加到imageView中
        imageView.setImageBitmap(bitmap);
    }

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

            // TODO 这儿的Select，来自collectLayout, 不来自issue，要改！！！！！！！
            issueObject.put("select", temp.getSelect());

            issueItem.put(issueObject);
        }

        issue.put("sketch", sketch);
        issue.put("issueItem", issueItem);

        return issue;
    }

    // 进入检测车辆时填充的数据
    public void fillInData(JSONObject issue) {

    }
}
