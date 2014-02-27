package com.df.app.service;

/**
 * Created by 岩 on 13-9-6.
 *
 * df5000
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.df.app.carCheck.CollectDataLayout;
import com.df.app.util.Common;

import static com.df.app.util.Helper.bytesToHexString;


/**
 * 建立和管理蓝牙连接。使用一个线程来处理连接。
 */
public class DF5000Service {

    // 调试信息
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // 当创建服务器socket时为Activity记录名字
    private static final String NAME = "BluetoothChat";

    // UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Adapter
    private final BluetoothAdapter mAdapter;

    // 消息处理句柄
    private final Handler mHandler;

    // 监听线程
    private AcceptThread mAcceptThread;

    // 连接线程
    private ConnectThread mConnectThread;

    // 连接成功线程
    private ConnectedThread mConnectedThread;

    // 当前状态
    private int mState;

    // 连接状态
    public static final int STATE_NONE = 0; // 当前没有可用的连接
    public static final int STATE_LISTEN = 1; // 监听传入的连接
    public static final int STATE_CONNECTING = 2; // 正在连接
    public static final int STATE_CONNECTED = 3; // 已经连接
    public static final int STATE_CONNECTION_LOST = 4; // 连接断开

    public DF5000Service(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    // 设置当前的连接状态
    private synchronized void setState(int state) {
        if (D)
            Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // 通知activity更新界面
        mHandler.obtainMessage(Common.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    // 返回当前的连接状态
    public synchronized int getState() {
        return mState;
    }

    // 启动蓝牙服务
    public synchronized void start() {
        if (D)
            Log.d(TAG, "start");

        // 取消任何试图建立连接的线程
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // 取消任何正在运行连接的线程
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 启动线程监听蓝牙socket连接
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }

        setState(STATE_LISTEN);
    }

    // 连接蓝牙设备
     public synchronized void connect(BluetoothDevice device) {
        if (D)
            Log.d(TAG, "connect to: " + device);

        // 取消任何试图建立连接的线程
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // 取消任何正在运行连接的线程
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 启动线程连接的设备
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();

        setState(STATE_CONNECTING);
    }

    // 连接成功
    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device) {
        if (D)
            Log.d(TAG, "connected");

        // 取消任何正在发起连接的线程
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // 取消任何正在运行连接的线程
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 取消任何正在监听的线程
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // 启动线程管理连接和传输
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // 发送设备名称到界面
        Message msg = mHandler.obtainMessage(Common.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString("device_name", device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }


    // 停止所有的线程
    public synchronized void stop() {
        if (D)
            Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        setState(STATE_NONE);
    }


    // 使用异步方式运行ConnectedThread写数据
    public void write(byte[] out) {
        ConnectedThread r;

        // 异步方式
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }

        r.write(out);
    }


    // 连接请求失败，通知界面
    private void connectionFailed() {
        setState(STATE_LISTEN);

        Message msg = mHandler.obtainMessage(Common.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "无法连接装置");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }


    // 连接断开，通知界面
    private void connectionLost() {
        setState(STATE_LISTEN);

        Message msg = mHandler.obtainMessage(Common.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "连接强制关闭");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTION_LOST);
    }


    private class AcceptThread extends Thread {

        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {

            BluetoothServerSocket tmp = null;

            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D)
                Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;


            while (mState != STATE_CONNECTED) {
                try {

                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }


                if (socket != null) {
                    synchronized (DF5000Service.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:

                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:

                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            if (D)
                Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            if (D)
                Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;


            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");


            mAdapter.cancelDiscovery();


            try {

                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();

                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG,
                            "unable to close() socket during connection failure",
                            e2);
                }

                DF5000Service.this.start();
                return;
            }


            synchronized (DF5000Service.this) {
                mConnectThread = null;
            }


            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private static String GetSerialNumber(String str) {
        return str.substring(4, 12); // 00830009
    }

    private static String GetCheckSum(String str) {
        int serial = 0;

        for(int i = 0; i < str.length(); i+=2) {
            serial += Integer.parseInt(str.substring(i, i + 2), 16);
        }

        return  Integer.toHexString(serial);
    }


    public static String[] parsePackage(String str) {
        final String P_START = "AA0A";
        final String P_PARA = "0111";

        int count = 0;

        // data[0]为部位名称、data[1] 为数值 、data[2] 为长度
        String data[] = new String[3];
        int len = str.length();

        if (!CollectDataLayout.hasRequestCmd && len >= 14) {
            String requestCmd = P_START;

            String serialNumber = GetSerialNumber(str);
            requestCmd += serialNumber;
            requestCmd += P_PARA;

            String checkSum = GetCheckSum(requestCmd);

            if(checkSum.length() < 4) {
                // 填充零位
                for(int i = 0; i < 4 - checkSum.length(); i++) {
                    checkSum = "0" + checkSum;
                }
            }

            requestCmd += checkSum;

            CollectDataLayout.requestCmd = requestCmd;

            return null;
        }

        if (str.startsWith("AA") || str.startsWith("aa")) {
            if (str.length() >= 14) {
                // data[0]指的是什么部位数据
                data[0] = str.substring(12, 14);

                StringBuffer str1 = new StringBuffer(str.substring(14, len - 4));
                StringBuffer str2 = new StringBuffer();

                // 如果部位数据长度大于4,表示为真正的数据
                if (str1.length() >= 4) {
                    int arraylen = str1.length() / 4;
                    String[] array = new String[arraylen];
                    for (int i = 0; i < arraylen; i++) {
                        array[i] = str1.substring(i * 4, i * 4 + 4);
                    }
                    for (int i = 0; i < arraylen; i++) {
                        int a = Integer.parseInt(array[i], 16);
                        if (a > 32768) {
                            a = (a - 32768) / 10;
                        } else {
                            a = a / 10;
                        }
                        count++;
                        str2.append(String.valueOf(a)).append(",");
                    }

                    data[1] = String.valueOf(str2);
                    data[2] = String.valueOf(count);
                }
                // 如果部位数据的小于4，表示此为完结包，可以通知主界面更新界面了
                else {
                    data[1] = "";
                    data[2] = "";
                }
                return data;
            }
            return data;

        }
        return null;
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "没有创建临时sockets", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);

                    // 读取失败，或无读取数据
                    if(bytes == 0) {
                        return;
                    }

                    String newCode2 = bytesToHexString(buffer, bytes);
                    String[] array = parsePackage(newCode2);

                    if (array != null) {
                        // 处理结尾包
                        if(array[0].equalsIgnoreCase("FF")) {
                            mHandler.obtainMessage(Common.MESSAGE_READ_OVER).sendToTarget();
                        } else {
                            mHandler.obtainMessage(Common.MESSAGE_READ, 0,
                                    -1, array).sendToTarget();
                        }
                    }
                    else {
                        if(!CollectDataLayout.hasRequestCmd) {
                            mHandler.obtainMessage(Common.MESSAGE_GET_SERIAL).sendToTarget();
                        }
                    }

                    CollectDataLayout.hasRequestCmd = true;

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }


        public void write(byte[] buffer) {
            try {
                // 向设备写数据
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                // 关闭socket
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}

