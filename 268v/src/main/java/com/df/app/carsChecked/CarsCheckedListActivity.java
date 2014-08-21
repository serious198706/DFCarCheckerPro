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

import com.df.app.MainActivity;
import com.df.app.carCheck.CarCheckActivity;
import com.df.app.R;
import com.df.app.procedures.InputProceduresActivity;
import com.df.app.service.util.AppCommon;
import com.df.library.entries.CarsCheckedItem;
import com.df.library.service.adapter.CarsCheckedListAdapter;
import com.df.library.asyncTask.CheckSellerNameTask;
import com.df.library.asyncTask.GetCarDetailTask;
import com.df.library.asyncTask.GetCarsCheckedListTask;
import com.df.library.asyncTask.ImportPlatformTask;
import com.df.library.util.Common;
import com.fortysevendeg.android.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.android.swipelistview.SwipeListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.df.library.util.Helper.setTextView;

/**
 * Created by 岩 on 14-1-2.
 *
 * 已检车辆列表
 */
public class CarsCheckedListActivity extends Activity {
    private SwipeListView swipeListView;
    private ArrayList<CarsCheckedItem> data;
    private CarsCheckedListAdapter adapter;

    private int startNumber = 1;
    private View footerView;
    private String lastSellerName;

    public static boolean modify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars_checked);

        swipeListView = (SwipeListView)findViewById(R.id.carsCheckedList);

        data = new ArrayList<CarsCheckedItem>();

        adapter = new CarsCheckedListAdapter(this, data, new CarsCheckedListAdapter.OnAction() {
            @Override
            public void onImport(int position) {
                selectPlatform(position);
            }

            @Override
            public void onModify(int position) {
                if("234".contains(data.get(position).getStatus())) {
                    showErrorDialog("该车辆当前不可修改！");
                } else {
                    choose(R.array.modify_items, position);
                }
            }
        }, new CarsCheckedListAdapter.OnEditPressed() {
            @Override
            public void onEditPressed(int position) {
                swipeListView.openAnimate(position);
            }
        });

        swipeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        swipeListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onClickFrontView(int position) {
                getCarDetail(position, CarReportActivity.class, false);
            }

            @Override
            public void onStartOpen(int position, int action, boolean right) {
                swipeListView.closeOpenedItems();
            }
        });

        swipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
        swipeListView.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL);
        swipeListView.setOffsetLeft(620);
        swipeListView.setLongClickable(false);
        swipeListView.setSwipeOpenOnLongPress(false);
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
                refresh(false);
            }
        });

        Button refreshButton = (Button)findViewById(R.id.buttonRefresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh(true);
            }
        });

        refresh(true);
    }

    /**
     * 刷新列表
     */
    private void refresh(boolean clear) {
        swipeListView.closeOpenedItems();

        if(clear) {
            startNumber = 1;
            data.clear();
        }

        adapter.notifyDataSetChanged();

        GetCarsCheckedListTask getCarsCheckedListTask = new GetCarsCheckedListTask(CarsCheckedListActivity.this, startNumber,
                new GetCarsCheckedListTask.OnGetListFinish() {
                    @Override
                    public void onFinish(String result) {
                        fillInData(result);
                    }
                    @Override
                    public void onFailed(String error) {
                        Toast.makeText(CarsCheckedListActivity.this, "获取已检车辆列表失败：" + error, Toast.LENGTH_SHORT).show();
                        Log.d("DFCarChecker", "获取已检车辆列表失败：" + error);
                    }
                });
        getCarsCheckedListTask.execute();
    }

    private void choose(final int arrayId, final int position) {
        View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view1)
                .create();

        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);

        final ListView listView = new ListView(view1.getContext());

        listView.setAdapter(new ArrayAdapter<String>(view1.getContext(), android.R.layout.simple_list_item_1,
                view1.getResources().getStringArray(arrayId)));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialog.dismiss();
                if(i == 0) {
                    CarsCheckedItem item = data.get(position);
                    Intent intent = new Intent(CarsCheckedListActivity.this, InputProceduresActivity.class);
                    intent.putExtra("carId", item.getCarId());
                    startActivity(intent);
                } else {
                    getCarDetail(position, CarCheckActivity.class, true);
                }
            }
        });

        contentArea.addView(listView);

        setTextView(view1, R.id.title, getResources().getString(R.string.selectModifyItem));

        dialog.show();
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * @param result
     */
    private void fillInData(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);

            int length = jsonArray.length();

            for(int i = 0; i < length; i++) {
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
     * 点击已检列表中的某一项时，根据carId获取该车的详细信息
     * @param position 第几条
     * @param activity 要进入的activity的class
     */
    private void getCarDetail(final int position, final Class activity, final boolean modify) {
        String ex = "";
        switch (Common.getEnvironment()) {
            case Common.INTERNAL_100_110_VERSION:
                ex = "100_100_";
                break;
            case Common.INTERNAL_100_3_VERSION:
                ex = "100_3_";
                break;
            case Common.INTERNAL_100_6_VERSION:
                ex = "100_6_";
                break;
            case Common.EXTERNAL_VERSION:
                ex = "ex_";
                break;
            case Common.PRODUCT_VERSION:
                ex = "pro_";
                break;
        }

        final int carId = data.get(position).getCarId();

        GetCarDetailTask getCarDetailTask = new GetCarDetailTask(CarsCheckedListActivity.this, carId, true, AppCommon.savedDirectory + ex, new GetCarDetailTask.OnGetDetailFinished() {
            @Override
            public void onFinish(String result) {
                Intent intent = new Intent(CarsCheckedListActivity.this, activity);
                intent.putExtra("jsonString", result);
                intent.putExtra("carId", carId);
                intent.putExtra("activity", CarsCheckedListActivity.class);
                intent.putExtra("modify", modify);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailed(String result) {
                Toast.makeText(CarsCheckedListActivity.this, result, Toast.LENGTH_SHORT).show();
                Log.d(AppCommon.TAG, result);
            }
        });

        getCarDetailTask.execute();
    }

    /**
     * 选择导入的平台（大众版、专业版）
     * @param position 条目位置
     */
    private void selectPlatform(int position) {
        final int carId = data.get(position).getCarId();

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
            content.setTextSize(20f);
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
        content.setTextSize(20f);
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
     * 显示出错信息
     */
    private void showErrorDialog(String message) {
        View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(message);
        content.setTextSize(20f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, getResources().getString(R.string.alert));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view1)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
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
                Toast.makeText(CarsCheckedListActivity.this, "导入成功！", Toast.LENGTH_LONG).show();

                refresh(true);
            }

            @Override
            public void onFailed(String result) {
                Toast.makeText(CarsCheckedListActivity.this, "导入失败！" + result, Toast.LENGTH_LONG).show();
                Log.d(AppCommon.TAG, result);
            }
        });
        importPlatformTask.execute();
    }
}
