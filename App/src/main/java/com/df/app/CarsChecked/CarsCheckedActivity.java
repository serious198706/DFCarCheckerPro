package com.df.app.CarsChecked;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.df.app.R;
import com.df.app.entries.CarsCheckedItem;
import com.df.app.service.Adapter.CarsCheckedListAdapter;
import com.df.app.service.AsyncTask.GetCarsCheckedListTask;
import com.fortysevendeg.android.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.android.swipelistview.SwipeListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CarsCheckedActivity extends Activity {
    private SwipeListView swipeListView;
    private ArrayList<CarsCheckedItem> data;
    private CarsCheckedListAdapter adapter;
    private int lastPos;

    private int startNumber = 1;
    private View footerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars_checked);

        swipeListView = (SwipeListView)findViewById(R.id.carsCheckedList);

        data = new ArrayList<CarsCheckedItem>();

        adapter = new CarsCheckedListAdapter(this, data);

        swipeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        swipeListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onClickFrontView(int position) {
                Log.d("swipe", String.format("onClickFrontView %d", position));
            }
        });

        swipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
        swipeListView.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL);
        swipeListView.setOffsetLeft(620);
        swipeListView.setAnimationTime(300);
        swipeListView.setAdapter(adapter);
        footerView =  ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.footer, null, false);

        swipeListView.addFooterView(footerView);

        Button homeButton = (Button)findViewById(R.id.buttonHome);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        Button loadMoreButton = (Button) findViewById(R.id.loadMore);
        loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });

        Button refreshButton = (Button)findViewById(R.id.buttonRefresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNumber = 1;
                data.clear();
                adapter.notifyDataSetChanged();
                refresh();
            }
        });

        refresh();
    }

    private void refresh() {
        GetCarsCheckedListTask getCarsCheckedListTask = new GetCarsCheckedListTask(CarsCheckedActivity.this, startNumber,
                new GetCarsCheckedListTask.OnGetListFinish() {
                    @Override
                    public void onFinish(String result) {
                        fillInData(result);
                    }
                    @Override
                    public void onFailed() {
                        // TODO 删掉这儿！
                        fillInDummyData();
                    }
                });
        getCarsCheckedListTask.execute();
    }

    private void fillInData(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);

            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                CarsCheckedItem item = new CarsCheckedItem();
                item.setPlateNumber(jsonObject.getString("PlateNumber"));
                item.setExteriorColor(jsonObject.getString("ExteriorColor"));
                item.setCarType(jsonObject.getString("Model"));
                item.setLevel(jsonObject.getString("Score"));
                item.setStatus(jsonObject.getString("Status"));
                item.setDate(jsonObject.getString("CreatedDate"));

                data.add(item);
            }

            adapter.notifyDataSetChanged();

            startNumber = data.size() + 1;

            if(data.size() == 0) {
                footerView.setVisibility(View.GONE);
            } else {
                footerView.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fillInDummyData() {
        for(int i = 0; i < 5; i++) {
            CarsCheckedItem item = new CarsCheckedItem();
            item.setPlateNumber("车牌号：京A12345");
            item.setExteriorColor("颜色：黑");
            item.setCarType("型号：奥迪A100");
            item.setLevel("等级：80B");
            item.setStatus("状态：待提交");
            item.setDate("提交时间：2013-12-28");

            data.add(item);
        }

        adapter.notifyDataSetChanged();
    }
}
