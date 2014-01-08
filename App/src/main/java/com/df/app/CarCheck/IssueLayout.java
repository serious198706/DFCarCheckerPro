package com.df.app.CarCheck;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.df.app.R;
import com.df.app.entries.Issue;
import com.df.app.service.IssueListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 岩 on 13-12-20.
 */
public class IssueLayout extends LinearLayout {
    private View rootView;
    private SimpleAdapter adapter;
    private ArrayList<HashMap<String, String>> mylist;

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

        ArrayList<Issue> issueList = fillInDummyData();

        issueListView.setAdapter(new IssueListAdapter(context, issueList));
    }

    public void updateUi() {

    }

    private ArrayList<Issue> fillInDummyData() {
        ArrayList<Issue> issueList  = new ArrayList<Issue>();

        issueList.add(new Issue("右前门铰链处是否平整、无修复焊接痕迹", "Y", "F"));
        issueList.add(new Issue("前杠与前翼子板之间的缝隙是否整齐均匀", "N", "R"));
        issueList.add(new Issue("右前翼子板和翼子板内衬连接处的胶体是否规整", "Y", "R"));
        issueList.add(new Issue("右前翼子板内侧是否平整、无回火焊点或折痕", "N", "R"));
        issueList.add(new Issue("右前翼子板内衬板是否平整、无钣金修复痕迹", "Y", "F"));
        issueList.add(new Issue("右前减震器座是否平整、无钣金修复痕迹", "N", "R"));
        issueList.add(new Issue("引擎盖内侧封边及内衬是否整齐、无修复焊接痕迹", "Y", "R"));

        return issueList;
    }
}
