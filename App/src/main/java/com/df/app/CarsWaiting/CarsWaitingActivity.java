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
                Log.d("swipe", String.format("onClickFrontView %d", position));

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

                        // TODO 测试，留着
                        //fillInDummyData();
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
//
//                result = "{\"conditions\":{\"interior\":{\"comment\":\"\",\"sealingStrip\":\"是\"},\"engine\":{\"started\":\"是\",\"fluid\":\"是\",\"exhaustColor\":\"无色\",\"strangeNoices\":\"是\",\"pipe\":\"是\",\"steady\":\"是\"},\"exterior\":{\"screw\":\"右前门\",\"glass\":\"前挡风玻璃\",\"needRepair\":\"否\",\"smooth\":\"很好\",\"comment\":\"\"},\"tires\":{\"rightRear\":\"\",\"patternMatch\":\"是\",\"leftFront\":\"\",\"rightFront\":\"\",\"formatMatch\":\"是\",\"spare\":\"\",\"leftRear\":\"\"},\"gearbox\":{\"atShiftEasy\":\"是\",\"atShiftNoise\":\"是\",\"atShiftShock\":\"是\"},\"comment3\":\"\",\"comment\":\";;\",\"comment1\":\"\",\"flooded\":{\"ashtray\":\"是\",\"audioHorn\":\"是\",\"fuse\":\"是\",\"rearSeats\":\"是\",\"backCorner\":\"是\",\"cigarLighter\":\"是\",\"storageCorner\":\"是\",\"trunkCorner\":\"是\",\"roof\":\"是\",\"newFuse\":\"是\",\"engineRoom\":\"是\",\"ecu\":\"是\",\"seatBelts\":\"是\",\"seatSlide\":\"是\",\"discBox\":\"是\",\"spareTireGroove\":\"是\"},\"comment2\":\"\",\"function\":{\"reversingRadar\":\"正常\",\"audio\":\"正常\",\"waterTemp\":\"正常\",\"airConditioning\":\"正常\",\"oilPressure\":\"正常\",\"powerWindows\":\"正常\",\"milometer\":\"正常\",\"powerMirror\":\"正常\",\"abs\":\"正常\",\"engineFault\":\"正常\",\"sunroof\":\"正常\",\"airConditioningTemp\":\"30\",\"tachometer\":\"正常\",\"parkingBrake\":\"正常\",\"airBag\":\"正常\",\"powerSeats\":\"正常\",\"reversingCamera\":\"正常\"}},\"features\":{\"procedures\":{\"mileage\":\"2\",\"engineSerial\":\"201340346\",\"regDate\":\"2010-6\",\"vin\":\"LGBF1AE0X7R011351\",\"licenseModel\":\"奥迪牌100\",\"exteriorColor\":\"黑\",\"vehicleType\":\"小型客车、轿车\",\"plateNumber\":\"京A13519\",\"builtDate\":\"2010-3\"},\"options\":{\"series\":\"DS5\",\"model\":\"1.6T A\\/MT\",\"manufacturerId\":448,\"airBags\":\"1\",\"ccs\":\"有\",\"brandId\":194,\"powerMirror\":\"有\",\"abs\":\"有\",\"spareTire\":\"有\",\"displacement\":\"1.6\",\"sunroof\":\"有\",\"powerSeats\":\"有\",\"countryId\":1,\"reversingRadar\":\"有\",\"airConditioning\":\"有\",\"powerSteering\":\"有\",\"transmission\":\"A\\/MT\",\"country\":\"中国\",\"modelId\":14302,\"powerWindows\":\"有\",\"category\":\"轿车\",\"manufacturer\":\"长安标致雪铁龙\",\"driveType\":\"两驱\",\"brand\":\"DS\",\"leatherSeats\":\"有\",\"seriesId\":6735,\"reversingCamera\":\"有\"}},\"checkCooperatorId\":0,\"accident\":{\"data\":{\"device\":{\"type\":\"DF3000\",\"serial\":\"1124072\"},\"enhance\":{\"F1\":\"252,251\",\"L1\":\"251,251\",\"L2\":\"250\",\"M5\":\"251\",\"H1\":\"249\",\"J1\":\"252,251\",\"D2\":\"250\",\"D1\":\"249\",\"M4\":\"250\",\"M3\":\"250\",\"M2\":\"251\",\"M1\":\"249\"},\"options\":{\"hide\":\"\",\"cannotMeasure\":\"\"},\"overlap\":{\"D\":\"1023,1008,1005,1005,1003,1003,1004,1006,1003,1002,1859,9990,9990,9990,9990,9990\",\"E\":\"252,254,251,249,250,250,251,248,249,250\",\"F\":\"249,249,249,249,250,248,249,250,249,251\",\"G\":\"250,249,250,249,249,249,250,249,249\",\"RC\":\"247,247,248\",\"RB\":\"250,249,249,247,247,248\",\"L\":\"1009,1006,1006,1017,1003,1010,1007,1012,1011,105,83,88,40,36,41,9990,26,167772,167771,167771\",\"M\":\"1012,1004,1003,1006,1003,1002,1002,1003,1004,1004,1003,1014\",\"N\":\"249,249,250,250,248,249,249,249,248,248\",\"H\":\"248,250,249,250,249,250,251,251,249,251\",\"I\":\"248,249,250,250,249,249,250,249,248,250\",\"J\":\"247,247,247,246,247,247,248,247,250\",\"K\":\"249,250,248,251,253,250,251,250,251\",\"LA\":\"251,250,251,251\",\"LC\":\"249,250,249,249\",\"LB\":\"248,248,250,248,248,249\",\"RA\":\"251,250,250\"}},\"issue\":{\"sketch\":{},\"issueItem\":[]}},\"checkUserId\":\"60\",\"checkCooperatorName\":\"检测员\",\"checkUserName\":\"陈岩\"}";
//
//                Intent intent = new Intent(CarsWaitingActivity.this, activity);
//                intent.putExtra("jsonString", result);
//                intent.putExtra("carId", carId);
//                startActivity(intent);
                finish();

                Log.d(Common.TAG, result);
            }
        });

        getCarDetailTask.execute();
    }
}
