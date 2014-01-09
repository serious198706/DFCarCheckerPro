package com.df.app.CarsWaiting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.df.app.CarCheck.CarCheckActivity;
import com.df.app.R;
import com.df.app.entries.CarsWaitingItem;
import com.df.app.service.CarsWaitingListAdapter;
import com.fortysevendeg.android.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.android.swipelistview.SwipeListView;

import java.util.ArrayList;

public class CarsWaitingActivity extends Activity {
    private SwipeListView swipeListView;
    private ArrayList<CarsWaitingItem> data;
    private CarsWaitingListAdapter adapter;
    private int lastPos;

    public static String countryId;
    public static String brandId;
    public static String manufacturerId;
    public static String seriesId;
    public static String modelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars_waiting);

        swipeListView = (SwipeListView)findViewById(R.id.carsWaitingList);

        data = new ArrayList<CarsWaitingItem>();

        fillInDummyData();

        adapter = new CarsWaitingListAdapter(this, data);

        swipeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        swipeListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onOpened(int position, boolean toRight) {
            }

            @Override
            public void onClosed(int position, boolean fromRight) {
            }

            @Override
            public void onListChanged() {
            }

            @Override
            public void onMove(int position, float x) {
            }

            @Override
            public void onStartOpen(int position, int action, boolean right) {
                swipeListView.closeAnimate(lastPos);
                Log.d("swipe", String.format("onStartOpen %d - action %d", position, action));

                lastPos = position;
            }

            @Override
            public void onStartClose(int position, boolean right) {
                Log.d("swipe", String.format("onStartClose %d", position));
            }

            @Override
            public void onClickFrontView(int position) {
                Log.d("swipe", String.format("onClickFrontView %d", position));

                countryId = data.get(position).getCountryId();
                brandId = data.get(position).getBrandId();
                manufacturerId = data.get(position).getManufacturerId();
                seriesId = data.get(position).getSeriesId();
                modelId = data.get(position).getModelId();

                Intent intent = new Intent(CarsWaitingActivity.this, CarCheckActivity.class);
                startActivity(intent);
            }

            @Override
            public void onClickBackView(int position) {
                Log.d("swipe", String.format("onClickBackView %d", position));
            }

            @Override
            public void onDismiss(int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    data.remove(position);
                }
                adapter.notifyDataSetChanged();
            }
        });

        swipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
        swipeListView.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL);
        swipeListView.setOffsetLeft(620);
        swipeListView.setAnimationTime(300);
        swipeListView.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.cars_waiting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillInDummyData() {
        for(int i = 0; i < 5; i++) {
            CarsWaitingItem item = new CarsWaitingItem();
            item.setPlateNumber("车牌号：京A12345");
            item.setExteriorColor("颜色：黑");
            item.setCarType("型号：奥迪A100");
            item.setStatus("状态：待检测");
            item.setDate("提交时间：2013-12-28");
            item.setCountryId("1");
            item.setBrandId("194");
            item.setManufacturerId("448");
            item.setSeriesId("6735");
            item.setModelId("14302");

            data.add(item);
        }
    }
}
