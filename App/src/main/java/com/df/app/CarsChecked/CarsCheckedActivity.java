package com.df.app.carsChecked;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.carCheck.CarCheckActivity;
import com.df.app.R;
import com.df.app.entries.CarsCheckedItem;
import com.df.app.service.Adapter.CarsCheckedListAdapter;
import com.df.app.service.AsyncTask.CheckSellerNameTask;
import com.df.app.service.AsyncTask.GetCarDetailTask;
import com.df.app.service.AsyncTask.GetCarsCheckedListTask;
import com.df.app.service.AsyncTask.ImportPlatformTask;
import com.df.app.util.Common;
import com.fortysevendeg.android.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.android.swipelistview.SwipeListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 14-1-2.
 *
 * 已检车辆列表
 */
public class CarsCheckedActivity extends Activity {
    private SwipeListView swipeListView;
    private ArrayList<CarsCheckedItem> data;
    private CarsCheckedListAdapter adapter;

    private int startNumber = 1;
    private View footerView;
    private String lastSellerName;
    private int lastPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars_checked);

        swipeListView = (SwipeListView)findViewById(R.id.carsCheckedList);

        data = new ArrayList<CarsCheckedItem>();

        adapter = new CarsCheckedListAdapter(this, data, new CarsCheckedListAdapter.OnImport() {
            @Override
            public void onImport(int carId) {
                selectPlatform(carId);
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
                refresh();
            }
        });

        refresh();
    }

    /**
     * 刷新列表
     */
    private void refresh() {
        GetCarsCheckedListTask getCarsCheckedListTask = new GetCarsCheckedListTask(CarsCheckedActivity.this, startNumber,
                new GetCarsCheckedListTask.OnGetListFinish() {
                    @Override
                    public void onFinish(String result) {
                        startNumber += 10;
                        fillInData(result);
                    }
                    @Override
                    public void onFailed(String error) {
                        Toast.makeText(CarsCheckedActivity.this, "获取已检车辆列表失败：" + error, Toast.LENGTH_SHORT).show();
                        Log.d("DFCarChecker", "获取已检车辆列表失败：" + error);

                        // TODO 测试用
                        fillInDummyData();
                    }
                });
        getCarsCheckedListTask.execute();
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * @param result
     */
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
                item.setCarId(jsonObject.getInt("CarId"));

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

    /**
     * 填充测试数据
     */
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

    /**
     * 点击已检列表中的某一项时，根据carId获取该车的详细信息
     * @param carId
     * @param activity
     */
    private void getCarDetail(final int carId, final Class activity) {
        GetCarDetailTask getCarDetailTask = new GetCarDetailTask(CarsCheckedActivity.this, carId, new GetCarDetailTask.OnGetDetailFinished() {
            @Override
            public void onFinish(String result) {
                Intent intent = new Intent(CarsCheckedActivity.this, activity);
                intent.putExtra("jsonString", result);
                intent.putExtra("carId", carId);
                intent.putExtra("activity", CarsCheckedActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailed(String result) {
                Toast.makeText(CarsCheckedActivity.this, result, Toast.LENGTH_SHORT).show();
//
//                result = "{\"conditions\":{\"interior\":{\"comment\":\"\",\"sealingStrip\":\"是\"},\"engine\":{\"started\":\"是\",\"fluid\":\"是\",\"exhaustColor\":\"无色\",\"strangeNoices\":\"是\",\"pipe\":\"是\",\"steady\":\"是\"},\"exterior\":{\"screw\":\"右前门\",\"glass\":\"前挡风玻璃\",\"needRepair\":\"否\",\"smooth\":\"很好\",\"comment\":\"\"},\"tires\":{\"rightRear\":\"\",\"patternMatch\":\"是\",\"leftFront\":\"\",\"rightFront\":\"\",\"formatMatch\":\"是\",\"spare\":\"\",\"leftRear\":\"\"},\"gearbox\":{\"atShiftEasy\":\"是\",\"atShiftNoise\":\"是\",\"atShiftShock\":\"是\"},\"comment3\":\"\",\"comment\":\";;\",\"comment1\":\"\",\"flooded\":{\"ashtray\":\"是\",\"audioHorn\":\"是\",\"fuse\":\"是\",\"rearSeats\":\"是\",\"backCorner\":\"是\",\"cigarLighter\":\"是\",\"storageCorner\":\"是\",\"trunkCorner\":\"是\",\"roof\":\"是\",\"newFuse\":\"是\",\"engineRoom\":\"是\",\"ecu\":\"是\",\"seatBelts\":\"是\",\"seatSlide\":\"是\",\"discBox\":\"是\",\"spareTireGroove\":\"是\"},\"comment2\":\"\",\"function\":{\"reversingRadar\":\"正常\",\"audio\":\"正常\",\"waterTemp\":\"正常\",\"airConditioning\":\"正常\",\"oilPressure\":\"正常\",\"powerWindows\":\"正常\",\"milometer\":\"正常\",\"powerMirror\":\"正常\",\"abs\":\"正常\",\"engineFault\":\"正常\",\"sunroof\":\"正常\",\"airConditioningTemp\":\"30\",\"tachometer\":\"正常\",\"parkingBrake\":\"正常\",\"airBag\":\"正常\",\"powerSeats\":\"正常\",\"reversingCamera\":\"正常\"}},\"features\":{\"procedures\":{\"mileage\":\"2\",\"engineSerial\":\"201340346\",\"regDate\":\"2010-6\",\"vin\":\"LGBF1AE0X7R011351\",\"licenseModel\":\"奥迪牌100\",\"exteriorColor\":\"黑\",\"vehicleType\":\"小型客车、轿车\",\"plateNumber\":\"京A13519\",\"builtDate\":\"2010-3\"},\"options\":{\"series\":\"DS5\",\"model\":\"1.6T A\\/MT\",\"manufacturerId\":448,\"airBags\":\"1\",\"ccs\":\"有\",\"brandId\":194,\"powerMirror\":\"有\",\"abs\":\"有\",\"spareTire\":\"有\",\"displacement\":\"1.6\",\"sunroof\":\"有\",\"powerSeats\":\"有\",\"countryId\":1,\"reversingRadar\":\"有\",\"airConditioning\":\"有\",\"powerSteering\":\"有\",\"transmission\":\"A\\/MT\",\"country\":\"中国\",\"modelId\":14302,\"powerWindows\":\"有\",\"category\":\"轿车\",\"manufacturer\":\"长安标致雪铁龙\",\"driveType\":\"两驱\",\"brand\":\"DS\",\"leatherSeats\":\"有\",\"seriesId\":6735,\"reversingCamera\":\"有\"}},\"checkCooperatorId\":0,\"accident\":{\"data\":{\"device\":{\"type\":\"DF3000\",\"serial\":\"1124072\"},\"enhance\":{\"F1\":\"252,251\",\"L1\":\"251,251\",\"L2\":\"250\",\"M5\":\"251\",\"H1\":\"249\",\"J1\":\"252,251\",\"D2\":\"250\",\"D1\":\"249\",\"M4\":\"250\",\"M3\":\"250\",\"M2\":\"251\",\"M1\":\"249\"},\"options\":{\"hide\":\"\",\"cannotMeasure\":\"\"},\"overlap\":{\"D\":\"1023,1008,1005,1005,1003,1003,1004,1006,1003,1002,1859,9990,9990,9990,9990,9990\",\"E\":\"252,254,251,249,250,250,251,248,249,250\",\"F\":\"249,249,249,249,250,248,249,250,249,251\",\"G\":\"250,249,250,249,249,249,250,249,249\",\"RC\":\"247,247,248\",\"RB\":\"250,249,249,247,247,248\",\"L\":\"1009,1006,1006,1017,1003,1010,1007,1012,1011,105,83,88,40,36,41,9990,26,167772,167771,167771\",\"M\":\"1012,1004,1003,1006,1003,1002,1002,1003,1004,1004,1003,1014\",\"N\":\"249,249,250,250,248,249,249,249,248,248\",\"H\":\"248,250,249,250,249,250,251,251,249,251\",\"I\":\"248,249,250,250,249,249,250,249,248,250\",\"J\":\"247,247,247,246,247,247,248,247,250\",\"K\":\"249,250,248,251,253,250,251,250,251\",\"LA\":\"251,250,251,251\",\"LC\":\"249,250,249,249\",\"LB\":\"248,248,250,248,248,249\",\"RA\":\"251,250,250\"}},\"issue\":{\"sketch\":{},\"issueItem\":[]}},\"checkUserId\":\"60\",\"checkCooperatorName\":\"检测员\",\"checkUserName\":\"陈岩\"}";
//
//                Intent intent = new Intent(CarsWaitingActivity.this, activity);
//                intent.putExtra("jsonString", result);
//                intent.putExtra("carId", carId);
//                startActivity(intent);

                Log.d(Common.TAG, result);
            }
        });

        getCarDetailTask.execute();
    }

    /**
     * 选择导入的平台（大众版、专业版）
     * @param carId
     */
    private void selectPlatform(final int carId) {
        View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view1)
                .create();

        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        final ListView listView = new ListView(view1.getContext());
        listView.setAdapter(new ArrayAdapter<String>(view1.getContext(), android.R.layout.simple_list_item_1,
                view1.getResources().getStringArray(R.array.platform_items)));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i) {
                    case 1:
                        // 导入大众版平台，先输入卖家id
                        checkSellerName(carId);
                        break;
                    case 0:
                        // 导入专业版平台，sellerId为0
                        importPlatform(carId, "pro", 0);
                        break;
                }

                dialog.dismiss();
            }
        });
        contentArea.addView(listView);

        setTextView(view1, R.id.title, getResources().getString(R.string.selectPlatform));

        dialog.show();
    }

    /**
     * 输入卖家id
     * @param carId
     */
    private void checkSellerName(final int carId) {
        View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        final EditText editText = new EditText(view1.getContext());
        editText.setText(lastSellerName);
        contentArea.addView(editText);

        setTextView(view1, R.id.title, getResources().getString(R.string.inputSellerName));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view1)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        lastSellerName = editText.getText().toString();
                        getSellerInfo(carId, editText.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();
    }

    /**
     * 获取卖家信息
     * @param carId
     * @param sellerId
     */
    private void getSellerInfo(final int carId, String sellerId) {
        CheckSellerNameTask checkSellerNameTask = new CheckSellerNameTask(this, sellerId, new CheckSellerNameTask.OnCheckSellerNameFinished() {
            @Override
            public void onFinished(String result) {
                showSellerName(carId, result);
            }

            @Override
            public void onFailed(String result) {
                showErrorDialog(carId);
            }
        });
        checkSellerNameTask.execute();
    }

    /**
     * 显示卖家详细信息
     * @param carId
     * @param result
     */
    private void showSellerName(final int carId, String result) {
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(result);
            String companyName = jsonObject.getString("CompanyName");
            String realName = jsonObject.getString("RealName");
            final int sellerId = jsonObject.getInt("SellerId");

            String message = getResources().getString(R.string.importPlatformConfirm) +
                    "\n" +
                    "公司：" + companyName + "\n" +
                    "真实姓名：" + realName;

            View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);
            TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
            TextView content = new TextView(view1.getContext());
            content.setText(message);
            content.setTextSize(22f);
            contentArea.addView(content);

            setTextView(view1, R.id.title, getResources().getString(R.string.alert));

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(view1)
                    .setPositiveButton(R.string.okAndImport, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            importPlatform(carId, "pub", sellerId);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create();

            dialog.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示出错信息
     * @param carId
     */
    private void showErrorDialog(final int carId) {
        String message = "未找到卖家，请检查用户名！";

        View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(message);
        content.setTextSize(22f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, getResources().getString(R.string.alert));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view1)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkSellerName(carId);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();
    }

    /**
     * 导入平台
     * @param carId
     * @param type
     * @param sellerId
     */
    private void importPlatform(int carId, String type, int sellerId) {
        ImportPlatformTask importPlatformTask = new ImportPlatformTask(this, carId, type, sellerId, new ImportPlatformTask.OnImportFinished() {
            @Override
            public void onFinished(String result) {
                lastSellerName = "";
                Toast.makeText(CarsCheckedActivity.this, "导入成功！", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailed(String result) {
                Toast.makeText(CarsCheckedActivity.this, "导入失败！" + result, Toast.LENGTH_LONG).show();
                Log.d(Common.TAG, result);
            }
        });
        importPlatformTask.execute();
    }
}
