package com.df.app.carCheck;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.df.app.R;
import com.df.app.entries.Issue;
import com.df.app.entries.PhotoEntity;
import com.df.app.entries.PosEntity;
import com.df.app.util.MyOnClick;
import com.df.app.service.Adapter.MyViewPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-20.
 *
 * 事故排查页面，包含数据采集、问题查勘、查勘结果三个子页面
 */
public class AccidentCheckLayout extends LinearLayout implements ViewPager.OnPageChangeListener{
    private View rootView;

    private ViewPager viewPager;
    private TextView collectTab, issueTab, resultTab;
    private List<View> views;

    private CollectDataLayout collectDataLayout;
    private IssueLayout issueLayout;
    private AccidentResultLayout accidentResultLayout;
    private boolean loaded;

    private int selectedColor = Color.rgb(0xAA, 0x03, 0x0A);
    private int unselectedColor = Color.rgb(0x70, 0x70, 0x70);

    public AccidentCheckLayout(Context context) {
        super(context);
        init(context);
    }

    public AccidentCheckLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AccidentCheckLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.accident_check_layout, this);

        // 采集数据页面
        collectDataLayout = new CollectDataLayout(context, new CollectDataLayout.OnGetIssueData() {
            @Override
            public void showContent() {
                // 当确定车辆配置后，出现另外两个页面
                // 但车辆配置可能会更新，这时就不需要再次添加了
                showIssueAndResultTabs();
            }

            @Override
            public void updateUi(String result, ProgressDialog progressDialog) {
                issueLayout.updateUi(result, progressDialog);
                accidentResultLayout.updateUi();
            }
        });

        // 问题查勘页面
        issueLayout = new IssueLayout(context);

        // 查勘结果页面
        accidentResultLayout = new AccidentResultLayout(context);

        InitViewPager();
        InitTextView();
    }

    /**
     * 初始化viewPager，用来承载各个模块，可以通过滑动切换
     */
    private void InitViewPager() {
        viewPager = (ViewPager) rootView.findViewById(R.id.vPager);
        views = new ArrayList<View>();

        views.add(collectDataLayout);

        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(this);
    }

    /**
     * 初始化标签，用来标识当前模块，可以点击
     */
    private void InitTextView() {
        collectTab = (TextView) rootView.findViewById(R.id.collect);
        issueTab = (TextView) rootView.findViewById(R.id.issue);
        resultTab = (TextView) rootView.findViewById(R.id.result);

        selectTab(0);

        issueTab.setVisibility(INVISIBLE);
        resultTab.setVisibility(INVISIBLE);

        collectTab.setOnClickListener(new MyOnClick(viewPager, 0));
    }

    /**
     * 更新勘查结果的两张图片（前后视角）
     */
    public void updatePreviews() {
        accidentResultLayout.updateUi();
    }

    /**
     * 生成草图（问题查勘x1，查勘结果x2）
     */
    public List<PhotoEntity> generateSketches() {
        List<PhotoEntity> temp = new ArrayList<PhotoEntity>();

        temp.add(issueLayout.generateSketch());
        temp.addAll(accidentResultLayout.generateSketches());

        return temp;
    }

    /**
     * 当确定蓝牙设备后，更新蓝牙连接信息
     */
    public void setupBluetoothService() {
        collectDataLayout.setupBluetoothService();
    }

    public void stopBluetoothService() {
        collectDataLayout.stopBluetoothService();
    }


    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int arg0) {
        selectTab(arg0);
    }

    private void selectTab(int currIndex) {
        collectTab.setTextColor(currIndex == 0 ? selectedColor : unselectedColor);
        issueTab.setTextColor(currIndex == 1 ? selectedColor : unselectedColor);
        resultTab.setTextColor(currIndex == 2 ? selectedColor : unselectedColor);
    }

    /**
     * 获取事故的最后一张图片
     * 参数：flag 视角
     */
    public PosEntity getPosEntity(int flag) {
        return accidentResultLayout.getPosEntity(flag);
    }

    /**
     * 储存事故图片
     * 参数：flag 视角
     */
    public void saveAccidentPhoto(int flag) {
        accidentResultLayout.saveAccidentPhoto(flag);
    }

    /**
     * 生成事故排查的JSON串
     */
    public JSONObject generateJSONObject() {
        JSONObject accident = new JSONObject();

        try {
            // 测量数据
            JSONObject data = collectDataLayout.generateJSONObject();

            // 问题查勘
            JSONObject issue = issueLayout.generateJSONObject();

            accident.put("data", data);
            accident.put("issue", issue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return accident;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     */
    public void fillInData(JSONObject accident, Handler handler) {
        try {
            // 测量数据
            JSONObject data = accident.getJSONObject("data");

            collectDataLayout.fillInData(data);

            // 问题查勘
            JSONObject issue = accident.getJSONObject("issue");

            // 如果有sketch节点，表示已经获取过问题查勘了，直接赋值
            if(issue.get("sketch") != null) {
                handler.sendEmptyMessage(1);
                issueLayout.fillInData(issue);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示问题查勘和查勘结果页面
     */
    public void showIssueAndResultTabs() {
        if(!loaded) {
            views.add(issueLayout);
            views.add(accidentResultLayout);

            issueTab.setVisibility(VISIBLE);
            resultTab.setVisibility(VISIBLE);

            issueTab.setOnClickListener(new MyOnClick(viewPager, 1));
            resultTab.setOnClickListener(new MyOnClick(viewPager, 2));

            viewPager.setAdapter(new MyViewPagerAdapter(views));

            loaded = true;
        }
    }

    /**
     * 提交前的检查
     * @return
     */
    public String checkAllFields() {
        if(!loaded) {
            selectTab(0);
        }

        return loaded ? "" : "accidentCheck";
    }

    public List<Issue> getIssues() {
        return issueLayout.getIssues();
    }

    public void modifyComment(String comment) {
        issueLayout.modifyComment(comment);
    }

    public void clearCache() {
        issueLayout.clearCache();
        accidentResultLayout.clearCache();
    }

    public void fillInData(JSONObject accident, JSONObject photo, Handler handler) {
        try {
            // 测量数据
            JSONObject data = accident.getJSONObject("data");

            collectDataLayout.fillInData(data);

            // 问题查勘
            JSONObject issue = accident.getJSONObject("issue");

            // 如果有sketch节点，表示已经获取过问题查勘了，直接赋值
            if(issue.get("sketch") != null) {
                handler.sendEmptyMessage(1);
                issueLayout.fillInData(issue);
            }

            issueLayout.fillInData(issue, photo);
            accidentResultLayout.fillInData(photo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void unbindDrawables() {
        issueLayout.unbindDrawables();
    }
}
