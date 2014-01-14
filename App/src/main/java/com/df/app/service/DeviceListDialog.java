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
import com.df.app.entries.SerialNumber;
import com.df.app.util.Common;
import com.xinque.android.serial.driver.UsbSerialDriver;
import com.xinque.android.serial.driver.UsbSerialProber;
import com.xinque.android.serial.util.HexDump;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 14-1-6.
 */
public class DeviceListDialog extends Dialog {

    private OnMyDialogResult mDialogResult;

    private static class DeviceEntry {
        public UsbDevice device;
        public UsbSerialDriver driver;

        DeviceEntry(UsbDevice device, UsbSerialDriver driver) {
            this.device = device;
            this.driver = driver;
        }
    }

    private static UsbSerialDriver sDriver;
    private static Context context;

    private UsbManager mUsbManager;
    private ListView mDeviceListView;
    private ListView mGaugeListView;
    private TextView mProgressBarTitle;
    private ProgressBar mProgressBar;

    private static final int MESSAGE_REFRESH = 101;
    private final static int SEARCH = 102;
    private static final long REFRESH_TIMEOUT_MILLIS = 5000;
    private Checker checker;

    private List<DeviceEntry> mEntries = new ArrayList<DeviceEntry>();
    private List<SerialNumber> mSerialNumbers = new ArrayList<SerialNumber>();
    private ArrayAdapter<DeviceEntry> mAdapter;
    private ArrayAdapter<SerialNumber> mSerialNumberAdapter;


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH:
                    refreshDeviceList();
                    mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
                    break;
                case SEARCH:
                    mSerialNumbers.clear();
                    mSerialNumbers.addAll(checker.getSerialNumbers());
                    mSerialNumberAdapter.notifyDataSetChanged();

                    mProgressBarTitle.setText(String.format("找到  %d 台检测设备",
                            mSerialNumbers.size()));
                    mProgressBar.setVisibility(View.INVISIBLE);
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

    public DeviceListDialog(Context context) {
        super(context);

        DeviceListDialog.context = context;
        init();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.device_list);

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
                    final LayoutInflater inflater =
                            (LayoutInflater) context.getSystemService(Context
                                    .LAYOUT_INFLATER_SERVICE);
                    row = (TwoLineListItem) inflater.inflate(android.R.layout.simple_list_item_2,
                            null);
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
                    Log.d(Common.TAG, "找不到设备.");
                    return;
                }

                showConsoleActivity(driver);
            }
        });
    }

    private void refreshDeviceList() {
        showProgressBar();

        new AsyncTask<Void, Void, List<DeviceEntry>>() {
            @Override
            protected List<DeviceEntry> doInBackground(Void... params) {
                Log.d(Common.TAG, "Refreshing device list ...");
                SystemClock.sleep(1000);
                final List<DeviceEntry> result = new ArrayList<DeviceEntry>();
                for (final UsbDevice device : mUsbManager.getDeviceList().values()) {
                    final List<UsbSerialDriver> drivers =
                            UsbSerialProber.probeSingleDevice(mUsbManager, device);
                    Log.d(Common.TAG, "找到设备: " + device);
                    if (drivers.isEmpty()) {
                        Log.d(Common.TAG, "  - No UsbSerialDriver available.");
                        result.add(new DeviceEntry(device, null));
                    } else {
                        for (UsbSerialDriver driver : drivers) {
                            Log.d(Common.TAG, "  + " + driver);
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
                mProgressBarTitle.setText(
                        String.format("找到(%s)个设备", Integer.valueOf(mEntries.size())));
                hideProgressBar();
                Log.d(Common.TAG, "Done refreshing, " + mEntries.size() + " entries found.");
            }

        }.execute((Void) null);
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBarTitle.setText(R.string.refreshing);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void showConsoleActivity(UsbSerialDriver driver) {
        mHandler.removeMessages(MESSAGE_REFRESH);

        this.sDriver = driver;

        search();

        mDeviceListView.setVisibility(View.GONE);
        mGaugeListView.setVisibility(View.VISIBLE);

        mSerialNumberAdapter = new ArrayAdapter<SerialNumber>(context,
                android.R.layout.simple_expandable_list_item_2, mSerialNumbers) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final TwoLineListItem row;
                if (convertView == null) {
                    final LayoutInflater inflater =
                            (LayoutInflater) context.getSystemService(Context
                                    .LAYOUT_INFLATER_SERVICE);
                    row = (TwoLineListItem) inflater.inflate(android.R.layout.simple_list_item_2,
                            null);
                } else {
                    row = (TwoLineListItem) convertView;
                }

                final SerialNumber serialNumber = mSerialNumbers.get(position);
                final String title = String.format("%d 号设备，序列号：%d",
                        (position+1), serialNumber.getSerialNumber());
                row.getText1().setText(title);

                final String subtitle = "点击设备";

                row.getText2().setText(subtitle);

                return row;
            }
        };

        mGaugeListView.setAdapter(mSerialNumberAdapter);
        mGaugeListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checker.selectSerial(position);
                mDialogResult.onMyDialogResult(sDriver);
                dismiss();
            }
        });
    }

    private void search(){
        Log.i(Common.TAG, "search...");
        if (sDriver == null) {
            mProgressBarTitle.setText("No serial device.");
            return;
        }

        checker = Checker.instance(sDriver);
        new Thread() {
            @Override
            public void run() {
                checker.search();
                mHandler.sendEmptyMessage(SEARCH);
            }
        }.start();
    }

    public interface OnMyDialogResult {
        public void onMyDialogResult(UsbSerialDriver sDriver);
    }

    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }
}
