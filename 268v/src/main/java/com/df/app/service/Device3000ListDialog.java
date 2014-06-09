package com.df.app.service;

import android.app.Dialog;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import com.df.app.R;
import com.df.app.service.util.AppCommon;
import com.df.library.entries.SerialNumber;
import com.df.library.util.Common;
import com.xinque.android.serial.driver.UsbSerialDriver;
import com.xinque.android.serial.driver.UsbSerialProber;
import com.xinque.android.serial.util.HexDump;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 14-1-6.
 *
 * df3000设备列表
 */
public class Device3000ListDialog extends Dialog {
    private static class DeviceEntry {
        public UsbDevice device;
        public UsbSerialDriver driver;

        DeviceEntry(UsbDevice device, UsbSerialDriver driver) {
            this.device = device;
            this.driver = driver;
        }
    }

    private static UsbSerialDriver sDriver;
    private Context context;
    private OnSelectDeviceFinished mCallback;

    private UsbManager mUsbManager;
    private ListView mDeviceListView;
    private ListView mGaugeListView;
    private TextView mProgressBarTitle;
    private ProgressBar mProgressBar;

    private static final int MESSAGE_REFRESH = 101;
    private static final int SEARCH = 102;
    private static final int ADAPTER_FOUND = 103;
    private static final long REFRESH_TIMEOUT_MILLIS = 3000;
    private DF3000Service DF3000Service;

    private List<DeviceEntry> mEntries = new ArrayList<DeviceEntry>();
    private List<SerialNumber> mSerialNumbers = new ArrayList<SerialNumber>();
    private ArrayAdapter<DeviceEntry> mAdapter;
    private ArrayAdapter<SerialNumber> mSerialNumberAdapter;

    /**
     * 处理消息
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 刷新设备列表
                case MESSAGE_REFRESH:
                    refreshDeviceList();
                    mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
                    break;
                // 查找到设备
                case SEARCH:
                    mSerialNumbers.clear();
                    mSerialNumbers.addAll(DF3000Service.getSerialNumbers());
                    mSerialNumberAdapter.notifyDataSetChanged();
                    mProgressBarTitle.setText(String.format("找到 %d 台检测设备", mSerialNumbers.size()));
                    mProgressBar.setVisibility(View.INVISIBLE);
                    break;
                case ADAPTER_FOUND:
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

    };

    @Override
    protected void onStart() {
        super.onStart();
        mHandler.sendEmptyMessage(MESSAGE_REFRESH);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeMessages(MESSAGE_REFRESH);
    }

    public Device3000ListDialog(Context context, OnSelectDeviceFinished listener) {
        super(context);

        this.context = context;
        this.mCallback = listener;

        init();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.df3000_device_list);

        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mDeviceListView = (ListView) findViewById(R.id.deviceList);
        mGaugeListView = (ListView) findViewById(R.id.gaugeList);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBarTitle = (TextView) findViewById(R.id.progressBarTitle);

        mAdapter = new ArrayAdapter<DeviceEntry>(context,
                android.R.layout.simple_expandable_list_item_2, mEntries) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final TwoLineListItem row;
                if (convertView == null) {
                    final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    row = (TwoLineListItem) inflater.inflate(android.R.layout.simple_list_item_2, null);
                } else {
                    row = (TwoLineListItem) convertView;
                }

                final DeviceEntry entry = mEntries.get(position);
                final String title = String.format("Vendor %s Product %s",
                        HexDump.toHexString((short) entry.device.getVendorId()),
                        HexDump.toHexString((short) entry.device.getProductId()));
                row.getText1().setText(title);

                final String subtitle = entry.driver != null ?
                        entry.driver.getClass().getSimpleName() : "找不到设备";

                row.getText2().setText(subtitle);

                return row;
            }

        };

        mDeviceListView.setAdapter(mAdapter);
        mDeviceListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= mEntries.size()) {
                    return;
                }

                final DeviceEntry entry = mEntries.get(position);
                final UsbSerialDriver driver = entry.driver;
                if (driver == null) {
                    Log.d(AppCommon.TAG, "找不到设备.");
                    return;
                }

                showConsoleActivity(driver);
            }
        });
    }

    /**
     * 刷新设备列表
     */
    private void refreshDeviceList() {
        showProgressBar();

        new AsyncTask<Void, Void, List<DeviceEntry>>() {
            @Override
            protected List<DeviceEntry> doInBackground(Void... params) {
                Log.d(AppCommon.TAG, "正在连接适配器...");
                SystemClock.sleep(1000);

                // 根据usbManager返回的结果，更新适配器列表
                final List<DeviceEntry> result = new ArrayList<DeviceEntry>();

                for (final UsbDevice device : mUsbManager.getDeviceList().values()) {
                    final List<UsbSerialDriver> drivers = UsbSerialProber.probeSingleDevice(mUsbManager, device);
                    Log.d(AppCommon.TAG, "找到适配器: " + device);

                    if (drivers.isEmpty()) {
                        Log.d(AppCommon.TAG, "  - No UsbSerialDriver available.");
                        result.add(new DeviceEntry(device, null));
                    } else {
                        for (UsbSerialDriver driver : drivers) {
                            Log.d(AppCommon.TAG, "  + " + driver);
                            result.add(new DeviceEntry(device, driver));
                        }
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(List<DeviceEntry> result) {
                mEntries.clear();
                mEntries.addAll(result);
                mAdapter.notifyDataSetChanged();

                Log.d(AppCommon.TAG, "查找完毕, 找到" + mEntries.size() + "个适配器");

                // 未查找到适配器
                if(result.size() != 0) {
                    mProgressBarTitle.setText(String.format("找到(%s)个适配器", mEntries.size()));
                    hideProgressBar();
                    mHandler.removeMessages(MESSAGE_REFRESH);
                    mHandler.sendEmptyMessage(ADAPTER_FOUND);
                }
            }
        }.execute();
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBarTitle.setText("正在连接适配器...");
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示已查找到的适配器设备
     * @param driver
     */
    private void showConsoleActivity(UsbSerialDriver driver) {
        mHandler.removeMessages(MESSAGE_REFRESH);

        sDriver = driver;

        // 选定此适配器，并查找gauge
        search();

        mDeviceListView.setVisibility(View.GONE);
        mGaugeListView.setVisibility(View.VISIBLE);

        mSerialNumberAdapter = new ArrayAdapter<SerialNumber>(context, android.R.layout.simple_expandable_list_item_2, mSerialNumbers) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final TwoLineListItem row;
                if (convertView == null) {
                    final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    row = (TwoLineListItem) inflater.inflate(android.R.layout.simple_list_item_2, null);
                } else {
                    row = (TwoLineListItem) convertView;
                }

                final SerialNumber serialNumber = mSerialNumbers.get(position);
                final String title = String.format("%d 号设备，序列号：%d", (position + 1), serialNumber.getSerialNumber());
                row.getText1().setText(title);

                final String subtitle = "点击连接此设备";
                row.getText2().setText(subtitle);

                return row;
            }
        };

        mGaugeListView.setAdapter(mSerialNumberAdapter);
        mGaugeListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DF3000Service.selectSerial(position);
                mCallback.onFinished(sDriver, mSerialNumbers.get(position).getSerialNumber());
                dismiss();
            }
        });
    }

    /**
     * 使用适配器查找gauge设备
     */
    private void search(){
        Log.d(AppCommon.TAG, "查找gauge设备...");

        if (sDriver == null) {
            mProgressBarTitle.setText("未找到设备");
            return;
        }

        // 获取df3000service的实例
        DF3000Service = com.df.app.service.DF3000Service.instance(sDriver);

        // 开始查找设备
        new Thread() {
            @Override
            public void run() {
                DF3000Service.search();
                mHandler.sendEmptyMessage(SEARCH);
            }
        }.start();
    }

    /**
     * CollectDataLayout实现此接口，当选择设备完成后调用此接口
     */
    public interface OnSelectDeviceFinished {
        public void onFinished(UsbSerialDriver sDriver, Long serialNumber);
    }
}
