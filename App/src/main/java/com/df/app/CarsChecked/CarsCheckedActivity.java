package com.df.app.CarsChecked;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.CarCheck.CarCheckActivity;
import com.df.app.R;
import com.df.app.entries.CarsCheckedItem;
import com.df.app.service.Adapter.CarsCheckedListAdapter;
import com.df.app.service.AsyncTask.CheckSellerNameTask;
import com.df.app.service.AsyncTask.GetCarsCheckedListTask;
import com.df.app.service.AsyncTask.ImportPlatformTask;
import com.fortysevendeg.android.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.android.swipelistview.SwipeListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.df.app.util.Helper.setEditViewText;
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

        setTextView(view1, R.id.title, getResources().getString(R.string.alert));

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
            }
        });
        importPlatformTask.execute();
    }
}
