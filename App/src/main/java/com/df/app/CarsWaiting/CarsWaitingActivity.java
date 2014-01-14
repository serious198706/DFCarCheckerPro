package com.df.app.CarsWaiting;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.df.app.CarCheck.CarCheckActivity;
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

public class CarsWaitingActivity extends Activity {
    private SwipeListView swipeListView;
    private ArrayList<CarsWaitingItem> data;
    private CarsWaitingListAdapter adapter;

    public static ProgressDialog progressDialog;

    public int startNumber = 1;
    private View footerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars_waiting);

        swipeListView = (SwipeListView)findViewById(R.id.carsWaitingList);

        data = new ArrayList<CarsWaitingItem>();

        adapter = new CarsWaitingListAdapter(this, data);

        swipeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        swipeListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onClickFrontView(int position) {
                Log.d("swipe", String.format("onClickFrontView %d", position));

                getCarDetail(data.get(position).getCarId());
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

    private void refresh() {
        GetCarsWaitingListTask getCarsWaitingListTask = new GetCarsWaitingListTask(CarsWaitingActivity.this, startNumber,
                new GetCarsWaitingListTask.OnGetListFinish() {
                    @Override
                    public void onFinish(String result) {
                        fillInData(result);
                    }
                    @Override
                    public void onFailed() {
                        // TODO 删掉！！！
                        fillInDummyData();
                    }
                });
        getCarsWaitingListTask.execute();
    }

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

                data.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();

        startNumber = data.size() + 1;
    }

    private void fillInDummyData() {
        for(int i = 0; i < 5; i++) {
            CarsWaitingItem item = new CarsWaitingItem();
            item.setPlateNumber("车牌号：京A12345");
            item.setExteriorColor("颜色：黑");
            item.setCarType("型号：奥迪A100");
            item.setDate("提交时间：2013-12-28");
            item.setCountryId("1");
            item.setBrandId("194");
            item.setManufacturerId("448");
            item.setSeriesId("6735");
            item.setModelId("14302");
            item.setCarId(i + 10);

            data.add(item);
        }

        adapter.notifyDataSetChanged();

        startNumber = data.size() + 1;
    }

    private void getCarDetail(final int carId) {
        GetCarDetailTask getCarDetailTask = new GetCarDetailTask(CarsWaitingActivity.this, carId, new GetCarDetailTask.OnGetDetailFinished() {
            @Override
            public void onFinish(String result) {
                Intent intent = new Intent(CarsWaitingActivity.this, CarCheckActivity.class);
                intent.putExtra("jsonString", result);
                intent.putExtra("carId", carId);
                startActivity(intent);
            }

            @Override
            public void onFailed(String result) {
                Toast.makeText(CarsWaitingActivity.this, result, Toast.LENGTH_SHORT).show();

                result = "{\"conditions\":{\"interior\":{\"comment\":\"\",\"sealingStrip\":\"是\"},\"engine\":{\"started\":\"是\",\"fluid\":\"是\",\"exhaustColor\":\"无色\",\"strangeNoices\":\"是\",\"pipe\":\"是\",\"steady\":\"是\"},\"exterior\":{\"screw\":\"右前门\",\"glass\":\"前挡风玻璃\",\"needRepair\":\"否\",\"smooth\":\"很好\",\"comment\":\"\"},\"tires\":{\"rightRear\":\"\",\"patternMatch\":\"是\",\"leftFront\":\"\",\"rightFront\":\"\",\"formatMatch\":\"是\",\"spare\":\"\",\"leftRear\":\"\"},\"gearbox\":{\"atShiftEasy\":\"是\",\"atShiftNoise\":\"是\",\"atShiftShock\":\"是\"},\"comment3\":\"\",\"comment\":\";;\",\"comment1\":\"\",\"flooded\":{\"ashtray\":\"是\",\"audioHorn\":\"是\",\"fuse\":\"是\",\"rearSeats\":\"是\",\"backCorner\":\"是\",\"cigarLighter\":\"是\",\"storageCorner\":\"是\",\"trunkCorner\":\"是\",\"roof\":\"是\",\"newFuse\":\"是\",\"engineRoom\":\"是\",\"ecu\":\"是\",\"seatBelts\":\"是\",\"seatSlide\":\"是\",\"discBox\":\"是\",\"spareTireGroove\":\"是\"},\"comment2\":\"\",\"function\":{\"reversingRadar\":\"正常\",\"audio\":\"正常\",\"waterTemp\":\"正常\",\"airConditioning\":\"正常\",\"oilPressure\":\"正常\",\"powerWindows\":\"正常\",\"milometer\":\"正常\",\"powerMirror\":\"正常\",\"abs\":\"正常\",\"engineFault\":\"正常\",\"sunroof\":\"正常\",\"airConditioningTemp\":\"30\",\"tachometer\":\"正常\",\"parkingBrake\":\"正常\",\"airBag\":\"正常\",\"powerSeats\":\"正常\",\"reversingCamera\":\"正常\"}},\"features\":{\"procedures\":{\"mileage\":\"2\",\"engineSerial\":\"201340346\",\"regDate\":\"2010-6\",\"vin\":\"LGBF1AE0X7R011351\",\"licenseModel\":\"奥迪牌100\",\"exteriorColor\":\"黑\",\"vehicleType\":\"小型客车、轿车\",\"plateNumber\":\"京A13519\",\"builtDate\":\"2010-3\"},\"options\":{\"series\":\"DS5\",\"model\":\"1.6T A\\/MT\",\"manufacturerId\":448,\"airBags\":\"1\",\"ccs\":\"有\",\"brandId\":194,\"powerMirror\":\"有\",\"abs\":\"有\",\"spareTire\":\"有\",\"displacement\":\"1.6\",\"sunroof\":\"有\",\"powerSeats\":\"有\",\"countryId\":1,\"reversingRadar\":\"有\",\"airConditioning\":\"有\",\"powerSteering\":\"有\",\"transmission\":\"A\\/MT\",\"country\":\"中国\",\"modelId\":14302,\"powerWindows\":\"有\",\"category\":\"轿车\",\"manufacturer\":\"长安标致雪铁龙\",\"driveType\":\"两驱\",\"brand\":\"DS\",\"leatherSeats\":\"有\",\"seriesId\":6735,\"reversingCamera\":\"有\"}},\"checkCooperatorId\":0,\"accident\":{\"data\":{\"device\":{\"type\":\"DF3000\",\"serial\":\"1124072\"},\"enhance\":{\"F1\":\"252,251\",\"L1\":\"251,251\",\"L2\":\"250\",\"M5\":\"251\",\"H1\":\"249\",\"J1\":\"252,251\",\"D2\":\"250\",\"D1\":\"249\",\"M4\":\"250\",\"M3\":\"250\",\"M2\":\"251\",\"M1\":\"249\"},\"options\":{\"hide\":\"\",\"cannotMeasure\":\"\"},\"overlap\":{\"D\":\"1023,1008,1005,1005,1003,1003,1004,1006,1003,1002,1859,9990,9990,9990,9990,9990\",\"E\":\"252,254,251,249,250,250,251,248,249,250\",\"F\":\"249,249,249,249,250,248,249,250,249,251\",\"G\":\"250,249,250,249,249,249,250,249,249\",\"RC\":\"247,247,248\",\"RB\":\"250,249,249,247,247,248\",\"L\":\"1009,1006,1006,1017,1003,1010,1007,1012,1011,105,83,88,40,36,41,9990,26,167772,167771,167771\",\"M\":\"1012,1004,1003,1006,1003,1002,1002,1003,1004,1004,1003,1014\",\"N\":\"249,249,250,250,248,249,249,249,248,248\",\"H\":\"248,250,249,250,249,250,251,251,249,251\",\"I\":\"248,249,250,250,249,249,250,249,248,250\",\"J\":\"247,247,247,246,247,247,248,247,250\",\"K\":\"249,250,248,251,253,250,251,250,251\",\"LA\":\"251,250,251,251\",\"LC\":\"249,250,249,249\",\"LB\":\"248,248,250,248,248,249\",\"RA\":\"251,250,250\"}},\"issue\":{\"sketch\":{},\"issueItem\":[]}},\"checkUserId\":\"60\",\"checkCooperatorName\":\"检测员\",\"checkUserName\":\"陈岩\"}";

                Intent intent = new Intent(CarsWaitingActivity.this, CarCheckActivity.class);
                intent.putExtra("jsonString", result);
                intent.putExtra("carId", carId);
                startActivity(intent);

                Log.d(Common.TAG, result);
            }
        });
        getCarDetailTask.execute();
    }
}
