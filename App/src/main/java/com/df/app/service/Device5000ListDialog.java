package com.df.app.service;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.df.app.R;
import com.df.app.util.Common;

import java.util.Set;

import static com.df.app.util.Helper.setEditViewText;
import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 14-1-6.
 */
public class Device5000ListDialog extends Dialog {
    private Context context;
    private Activity activity;
    private OnSelectDeviceFinished mCallback;

    // 适配器
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    @Override
    protected void onStart() {
        super.onStart();

        if (!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableIntent, Common.REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public Device5000ListDialog(Activity activity, Context context, OnSelectDeviceFinished listener) {
        super(context);

        this.activity = activity;
        this.context = context;
        this.mCallback = listener;

        init();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.df5000_device_list);

        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_item);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_item);

        // 已配对设备列表
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // 查找到的设备列表
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // 注册时发送广播给设备
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter);

        // 广播时发现已完成注册
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(mReceiver, filter);

        // 获取本地蓝牙适配器
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // 得到目前已配对的设备
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // 清空之前的查找记录
        mPairedDevicesArrayAdapter.clear();

        // 如果发现设备，将其添加到列表
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n"
                        + device.getAddress());
            }
        } else {
            // 如果未找到设备
            String noDevices = context.getResources().getString(R.string.none_paired);
            mPairedDevicesArrayAdapter.add(noDevices);
        }

        doDiscovery();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            // 停止查找设备
            mBtAdapter.cancelDiscovery();

            // 获得设备地址，以aa:bb:cc:dd:ee:ff的形式保存
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            // 将地址传回collectLayout
            mCallback.onFinished(address);
            dismiss();
        }
    };

    /**
     *
     */
    public void showPairedDevices() {
        // 得到目前已配对的设备
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // 清空之前的查找记录
        mPairedDevicesArrayAdapter.clear();

        // 如果发现设备，将其添加到列表
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n"
                        + device.getAddress());
            }
        } else {
            // 如果未找到设备
            String noDevices = context.getResources().getString(R.string.none_paired);
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    /**
     * 发现与bluetoothadapter启动装置
     */
    public void doDiscovery() {
        Log.d(Common.TAG, "doDiscovery()");

        // 设置标题 - 正在查找...
        setTextView(getWindow().getDecorView(), R.id.progressBarTitle, context.getResources().getString(R.string.scanning));

        // 如果正在查找设备，则停止
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // 查找设备
        mBtAdapter.startDiscovery();
    }

    // 接收蓝牙适配器的广播
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // 发现设备
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                // 获取蓝牙设备的信息
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // 如果已绑定了，则加入绑定列表
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }

            }
            // 查找结束
            else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                // 设置标题 - 选择设备
                setTextView(getWindow().getDecorView(), R.id.progressBarTitle, context.getResources().getString(R.string.select_device));

                // 如果发现的设备数量为0，则提示未找到设备
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = context.getResources().getString(R.string.none_found);
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

    public interface OnSelectDeviceFinished {
        public void onFinished(String address);
    }
}
