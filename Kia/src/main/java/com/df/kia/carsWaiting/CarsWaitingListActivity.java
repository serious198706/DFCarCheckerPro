package com.df.kia.carsWaiting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.kia.R;
import com.df.kia.carCheck.CarCheckActivity;
import com.df.library.entries.CarsWaitingItem;
import com.df.kia.procedures.InputProceduresActivity;
import com.df.library.service.adapter.CarsWaitingListAdapter;
import com.df.library.asyncTask.DeleteCarTask;
import com.df.library.asyncTask.GetCarDetailTask;
import com.df.library.asyncTask.GetCarsWaitingListTask;
import com.df.kia.service.util.AppCommon;
import com.fortysevendeg.android.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.android.swipelistview.SwipeListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.df.library.util.Helper.setTextView;

/**
 * Created by 岩 on 14-1-7.
 *
 * 待检车辆列表
 */
public class CarsWaitingListActivity extends Activity {
    private SwipeListView swipeListView;
    private ArrayList<CarsWaitingItem> data;
    private CarsWaitingListAdapter adapter;

    public int startNumber = 1;
    private View footerView;
    private int lastPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars_waiting);

        swipeListView = (SwipeListView)findViewById(R.id.carsWaitingList);

        data = new ArrayList<CarsWaitingItem>();

        adapter = new CarsWaitingListAdapter(this, data, new CarsWaitingListAdapter.OnAction() {
            @Override
            public void onEditPressed(int position) {
                swipeListView.openAnimate(position);
            }

            @Override
            public void onModifyProcedure(int positon) {
                CarsWaitingItem item = data.get(positon);
                Intent intent = new Intent(CarsWaitingListActivity.this, InputProceduresActivity.class);
                intent.putExtra("carId", item.getCarId());
                startActivity(intent);
            }

            @Override
            public void onDeleteCar(final int position) {
                View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);
                TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
                TextView content = new TextView(view1.getContext());
                content.setText(R.string.confirmDeleteCar);
                content.setTextSize(20f);
                contentArea.addView(content);

                setTextView(view1, R.id.title, getResources().getString(R.string.alert));

                AlertDialog dialog = new AlertDialog.Builder(CarsWaitingListActivity.this)
                        .setView(view1)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                CarsWaitingItem item = data.get(position);
                                deleteCar(item.getCarId());
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();

                dialog.show();
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
                swipeListView.closeOpenedItems();
                lastPos = position;
            }
        });

        swipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
        swipeListView.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL);
        swipeListView.setLongClickable(false);
        swipeListView.setSwipeOpenOnLongPress(false);
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

                swipeListView.closeOpenedItems();

                refresh();
            }
        });

        refresh();
    }

    /**
     * 刷新列表
     */
    private void refresh() {
        GetCarsWaitingListTask getCarsWaitingListTask = new GetCarsWaitingListTask(CarsWaitingListActivity.this, startNumber,
                new GetCarsWaitingListTask.OnGetListFinish() {
                    @Override
                    public void onFinish(String result) {
                        fillInData(result);
                        startNumber += 10;
                    }
                    @Override
                    public void onFailed(String error) {
                        Toast.makeText(CarsWaitingListActivity.this, "获取待检车辆列表失败：" + error, Toast.LENGTH_SHORT).show();
                        Log.d("DFCarChecker", "获取待检车辆列表失败：" + error);
                        lastPos = -1;
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

//        for(int i = 0; i < data.size(); i++)
//            swipeListView.closeAnimate(i);

//        // 如果待检车辆为0, 表示没有保存的数据，删掉相关目录下的文件(正式环境下）
//        if(data.size() == 0) {
//            DeleteFiles.deleteFiles(AppCommon.photoDirectory);
//            DeleteFiles.deleteFiles(AppCommon.savedDirectory);
//        }
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
        GetCarDetailTask getCarDetailTask = new GetCarDetailTask(CarsWaitingListActivity.this, carId, false, AppCommon.savedDirectory, new GetCarDetailTask.OnGetDetailFinished() {
            @Override
            public void onFinish(String result) {
                Intent intent = new Intent(CarsWaitingListActivity.this, activity);
                intent.putExtra("jsonString", result);
                intent.putExtra("carId", carId);
                intent.putExtra("activity", CarsWaitingListActivity.class);
                intent.putExtra("modify", false);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailed(String result) {
                Toast.makeText(CarsWaitingListActivity.this, result, Toast.LENGTH_SHORT).show();
                Log.d(AppCommon.TAG, result);
            }
        });

        getCarDetailTask.execute();
    }

    /**
     * 删除车辆
     */
    private void deleteCar(int carId) {
        DeleteCarTask deleteCarTask = new DeleteCarTask(CarsWaitingListActivity.this, carId, new DeleteCarTask.OnDeleteFinished() {
            @Override
            public void onFinished(String result) {
                Toast.makeText(CarsWaitingListActivity.this, "删除成功！", Toast.LENGTH_SHORT).show();

                startNumber = 1;
                data.clear();
                adapter.notifyDataSetChanged();
                swipeListView.closeOpenedItems();
                refresh();
            }

            @Override
            public void onFailed(String result) {
                Toast.makeText(CarsWaitingListActivity.this, "删除失败！" + result, Toast.LENGTH_SHORT).show();
                Log.d(AppCommon.TAG, "删除失败！" + result);
            }
        });
        deleteCarTask.execute();
    }
}
