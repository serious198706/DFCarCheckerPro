package com.df.app.carsWaiting;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.df.app.carCheck.CarCheckActivity;
import com.df.app.procedures.InputProceduresActivity;
import com.df.app.R;
import com.df.app.entries.CarsWaitingItem;
import com.df.app.service.Adapter.CarsWaitingListAdapter;
import com.df.app.service.AsyncTask.GetCarDetailTask;
import com.df.app.service.AsyncTask.GetCarsWaitingListTask;
import com.df.app.util.Common;
import com.fortysevendeg.android.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.android.swipelistview.SwipeListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by 岩 on 14-1-7.
 *
 * 待检车辆列表
 */
public class CarsWaitingActivity extends Activity {
    private SwipeListView swipeListView;
    private ArrayList<CarsWaitingItem> data;
    private CarsWaitingListAdapter adapter;

    public int startNumber = 1;
    private View footerView;
    private int lastPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars_waiting);

        swipeListView = (SwipeListView)findViewById(R.id.carsWaitingList);

        data = new ArrayList<CarsWaitingItem>();

        adapter = new CarsWaitingListAdapter(this, data, new CarsWaitingListAdapter.OnModifyProcedure() {
            @Override
            public void onModifyProcedure(CarsWaitingItem item) {
                Intent intent = new Intent(CarsWaitingActivity.this, InputProceduresActivity.class);
                intent.putExtra("jsonString", item.getJsonObject().toString());
                startActivity(intent);
            }
        });

        swipeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        swipeListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onClickFrontView(int position) {
                getCarDetail(data.get(position).getCarId(), CarCheckActivity.class);
            }

            @Override
            public void onDismiss(int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    data.remove(position);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onStartOpen(int position, int action, boolean right) {
                swipeListView.closeAnimate(lastPos);
                lastPos = position;
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
                swipeListView.closeAnimate(lastPos);
                refresh();
            }
        });

        refresh();
    }

    /**
     * 刷新列表
     */
    private void refresh() {
        GetCarsWaitingListTask getCarsWaitingListTask = new GetCarsWaitingListTask(CarsWaitingActivity.this, startNumber,
                new GetCarsWaitingListTask.OnGetListFinish() {
                    @Override
                    public void onFinish(String result) {
                        fillInData(result);
                        startNumber += 10;
                    }
                    @Override
                    public void onFailed(String error) {
                        Toast.makeText(CarsWaitingActivity.this, "获取待检车辆列表失败：" + error, Toast.LENGTH_SHORT).show();
                        Log.d("DFCarChecker", "获取待检车辆列表失败：" + error);
                    }
                });
        getCarsWaitingListTask.execute();
    }

    /**
     * 填充待检测列表
     * @param result
     */
    private void fillInData(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);

            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                CarsWaitingItem item = new CarsWaitingItem();
                item.setPlateNumber(jsonObject.getString("plateNumber"));
                item.setExteriorColor(jsonObject.getString("exteriorColor"));
                item.setCarType(jsonObject.getString("licenseModel"));
                item.setDate(jsonObject.getString("createDate"));
                item.setCarId(jsonObject.getInt("carId"));
                item.setJsonObject(jsonObject);

                data.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();

        startNumber = data.size() + 1;

        if(data.size() == 0) {
            footerView.setVisibility(View.GONE);
        } else {
            footerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 填充测试数据
     */
    private void fillInDummyData() {
        for(int i = 0; i < 5; i++) {
            CarsWaitingItem item = new CarsWaitingItem();
            item.setPlateNumber("京A12345");
            item.setExteriorColor("黑");
            item.setCarType("奥迪A100");
            item.setDate("2013-12-28 14:01:58");
            item.setCountryId("1");
            item.setBrandId("194");
            item.setManufacturerId("448");
            item.setSeriesId("6735");
            item.setModelId("14302");
            item.setCarId(i + 395);

            String jsonString = "{\"carId\":2,\"vin\":\"15844\",\"engineSerial\":\"发动机号\",\"vehicleType\":\"行驶证车辆类型\",\"userCharacter\":\"非运营\",\"mileage\":\"表征里程\",\"plateNumber\":\"京A2548\",\"licenseModel\":\"行驶证品牌型号\",\"exteriorColor\":\"红色\",\"regDate\":\"2012-12-03\",\"buildDate\":\"2012-12-03\",\"createDate\":\"2013-01-01\"}";

            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                item.setJsonObject(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            data.add(item);
        }

        adapter.notifyDataSetChanged();

        startNumber = data.size() + 1;

        if(data.size() == 0) {
            footerView.setVisibility(View.GONE);
        } else {
            footerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 点击待检列表中的某一项时，根据carId获取该车的详细信息
     * 如果该carId在本地存在，则表示已经保存过，就从本地获取详细信息
     * @param carId
     * @param activity
     */
    private void getCarDetail(final int carId, final Class activity) {
        GetCarDetailTask getCarDetailTask = new GetCarDetailTask(CarsWaitingActivity.this, carId, new GetCarDetailTask.OnGetDetailFinished() {
            @Override
            public void onFinish(String result) {
                Intent intent = new Intent(CarsWaitingActivity.this, activity);
                intent.putExtra("jsonString", result);
                intent.putExtra("carId", carId);
                intent.putExtra("activity", CarsWaitingActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailed(String result) {
                Toast.makeText(CarsWaitingActivity.this, result, Toast.LENGTH_SHORT).show();
                finish();

                Log.d(Common.TAG, result);
            }
        });

        getCarDetailTask.execute();
    }
}
