package com.ads.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.df.library.R;
import com.df.library.util.Common;

public class BluetoothUtil
{
	public static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final int WM_BLUETOOTH_DEVICE_ON = 1000;
	public static final int WM_BLUETOOTH_DEVICE_BONDED = 1001;
	public static final int WM_BLUETOOTH_DEVICE_OK = 1002;
	public static final int WM_BLUETOOTH_ERROR = 1003;
	public static final int WM_BLUETOOTH_CONNECT_FAIL = 1004;
	public static final int WM_BLUETOOTH_CONNECT_OVERTIME = 1005;
	public static final int REQCODE_BLUETOOTH_SETTING = 2000;
    
	private BluetoothAdapter btAdapter = null;
	private BluetoothDevice btDevice = null;
	private BluetoothSocket btSocket = null;
	private InputStream inStream = null;
	private OutputStream outStream = null;
	private Context context = null;
	private Handler handler = null;
	private String btName;
	
	private enum RTStatus {RT_RUN, RT_STOP, RT_IDLE};
	private RTStatus rtStatus = RTStatus.RT_IDLE;
	
	private final static int MAX_BUFF_SIZE = 1024;
	private byte[] recvBuffer = null;
    private int recvBufLen = 0;
	
	/**
	 * 打开蓝牙设备
	 * @return true	蓝牙设备已经打开或正在打开
	 * @return false 不存在蓝牙设备
	 */
	public boolean enableBluetooth()
	{
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if(btAdapter!=null)
		{
			IntentFilter btIF = new IntentFilter();
			btIF.addAction(BluetoothDevice.ACTION_FOUND);
			btIF.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
			btIF.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
			btIF.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
			btIF.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
			context.registerReceiver(btReceiver, btIF);
			
			if(!btAdapter.isEnabled())
			{	// 打开蓝牙状态会触发蓝牙广播,在广播中进行配对
				return btAdapter.enable();
			}else
			{	// 蓝牙已经打开的,在这里进行配对
				if(!pairBluetooth())
				{
					doPairBluetooth();
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 关闭蓝牙设备
	 */
	public void disableBluetooth()
	{
		closeBluetooth();
		// 关闭蓝牙状态广播监听器
		try
		{  
			context.unregisterReceiver(btReceiver);  
		} catch (IllegalArgumentException e)
		{ 	e.printStackTrace();  
		}
		// 关闭蓝牙设备
		if(btAdapter!=null)
		{	btAdapter.disable();	btAdapter = null;
		}
	}
	
	/**
	 * 关闭蓝牙通讯
	 */
	public void closeBluetooth()
	{
    	try
    	{	
    		if(inStream != null)
    		{	inStream.close();		inStream = null;
    		}
    	}catch(IOException e)
    	{	e.printStackTrace();
    	}
    	
		try
		{
			if (outStream != null)
			{	outStream.close();		outStream = null;
			}
		} catch (IOException e)
		{	e.printStackTrace();
		}
		
		try
		{
			if(btSocket != null)
			{	btSocket.close();		btSocket = null;
			}
		}catch(IOException e)
    	{	e.printStackTrace();
    	}
	}
	
	/**
	 * 配对蓝牙
	 * .查找配对deviceName指定的蓝牙设备,并将mac地址设置到btAddress中
	 * .显示蓝牙设置界面完成配对操作
	 * @return true	查找到已经配对的蓝牙设备,btAddress为deviceName设备的mac地址
	 * @return false 启动蓝牙配对过程
	 */
	public boolean pairBluetooth()
	{
		// 查询是否已经匹配
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
		String[] btNames;
		if (pairedDevices.size() > 0)
		{
			if(btName.indexOf(';')!=-1)
			{	btNames = btName.split(";");
			}else
			{	btNames = new String[1];
				btNames[0] = btName;
			}
			for (BluetoothDevice device : pairedDevices)
			{
				for(String name : btNames)
				{
					if(device.getName().equals(name))
					{
						btDevice = device;
						connectBluetooth();
						return true;
					}
				}
			}
		}
		return false;
		
		// 开启蓝牙搜索并搜索蓝牙
//		btAdapter.startDiscovery();
//		// 配对提示
//		Toast.makeText(context, context.getString(R.string.bt_pair_hint), Toast.LENGTH_LONG).show();
//		Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
//        Activity workActivity = (Activity)context;
//        workActivity.startActivityForResult(intent, REQCODE_BLUETOOTH_SETTING);
//		return false;
	}
	
	public void doPairBluetooth()
	{
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		// 开启蓝牙搜索并搜索蓝牙
		btAdapter.startDiscovery();
		// 配对提示
		Toast.makeText(context, context.getString(R.string.bt_pair_hint), Toast.LENGTH_LONG).show();
		Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
		Activity workActivity = (Activity)context;
		workActivity.startActivityForResult(intent, REQCODE_BLUETOOTH_SETTING);
	}
	
	
	
	/**
	 * 连接蓝牙设备
	 * .connectBluetoothByRf()
	 * .connectBluetoothByInsecureRf()
	 * .connectBluetoothByInvoke()
	 * @return true	连接蓝牙设备成功
	 * @return false 连接蓝牙设备失败
	 * @throws java.io.IOException
	 */
	public boolean connectBluetooth()
	{
		Log.i(Common.TAG, "connectBluetooth() enter!");
		// 如果20s没有连接成功,将接收到连接失败消息
		handler.sendEmptyMessageDelayed(WM_BLUETOOTH_CONNECT_OVERTIME, 20*1000);
		if(android.os.Build.VERSION.SDK_INT > 10)
		{
			if(!connectBluetoothByInsecureRf())
			{
				if(!connectBluetoothByInvoke())
				{
					handler.sendEmptyMessage(WM_BLUETOOTH_CONNECT_FAIL);
					return false;
				}
			}
		}else
		{
			if(!connectBluetoothByRf())
			{
				if(!connectBluetoothByInvoke())
				{
					handler.sendEmptyMessage(WM_BLUETOOTH_CONNECT_FAIL);
					return false;
				}
			}
		}
		handler.removeMessages(WM_BLUETOOTH_CONNECT_OVERTIME);
		
		Log.i(Common.TAG, "connectBluetooth() success!");
		new RecvThread().start();
		handler.sendEmptyMessage(WM_BLUETOOTH_DEVICE_OK);
		return true;
	}
	
	/**
	 * 使用connectBluetoothByInsecureRf()方式创建Bluetooth Socket
	 * @return true:成功
	 * .设置btSocket
	 * .设置inStream
	 * .设置outStream
	 * @return false:失败
	 * .关闭btSocket/inStream/outStream
	 */
	private boolean connectBluetoothByInsecureRf()
	{
		try
		{
			btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(BT_UUID);
			btSocket.connect();
			inStream = btSocket.getInputStream();
			outStream = btSocket.getOutputStream();
			return true;
		} catch (Exception e)
		{	e.printStackTrace();
			closeBluetooth();
		}
		return false;
	}
	
	/**
	 * 使用createRfcommSocketToServiceRecord()方式创建Bluetooth Socket
	 * @return true:成功
	 * .设置btSocket
	 * .设置inStream
	 * .设置outStream
	 * @return false:失败
	 * .关闭btSocket/inStream/outStream
	 */
	private boolean connectBluetoothByRf()
	{
		try
		{
			btSocket = btDevice.createRfcommSocketToServiceRecord(BT_UUID);
			btSocket.connect();
			inStream = btSocket.getInputStream();
			outStream = btSocket.getOutputStream();
			return true;
		} catch (Exception e)
		{	e.printStackTrace();
			closeBluetooth();
		}
		return false;
	}
	
	/**
	 * 使用invoke createRfcommSocket()方式创建Bluetooth Socket
	 * @return true:成功
	 * .设置btSocket
	 * .设置inStream
	 * .设置outStream
	 * @return false:失败
	 * .关闭btSocket/inStream/outStream
	 */
	private boolean connectBluetoothByInvoke()
	{
		try
		{
			Method method = btDevice.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
			btSocket = (BluetoothSocket) method.invoke(btDevice, 1);
			btSocket.connect();
			inStream = btSocket.getInputStream();
			outStream = btSocket.getOutputStream();
			return true;
		} catch (Exception e)
		{	e.printStackTrace();
			closeBluetooth();
		}
		return false;
	}
	
	/**
     * 从蓝牙inputStream中读取maxReadLen数据
     */
    public byte[] bluetoothRead(int maxReadLen)
    {
    	synchronized(recvBuffer)
		{
    		if(recvBufLen<=0)
        	{	return "".getBytes();
        	}
    		
    		byte[] buff = "".getBytes();
    		if(recvBufLen<=maxReadLen)
    		{
    			buff = new byte[recvBufLen];
    			System.arraycopy(recvBuffer, 0, buff, 0, recvBufLen);
    			recvBufLen = 0;
    		}else
    		{
    			buff = new byte[maxReadLen];
    			System.arraycopy(recvBuffer, 0, buff, 0, maxReadLen);
    			byte[] tmpBuf = new byte[1024];
    			System.arraycopy(recvBuffer, maxReadLen, tmpBuf, 0, recvBufLen-maxReadLen);
    			System.arraycopy(tmpBuf, 0, recvBuffer, 0, recvBufLen-maxReadLen);
    			recvBufLen -= maxReadLen;
    		}
    		return buff;
		}
    }
    
    /**
     * 将数据发送到outputStream
     */
    public boolean bluetoothWrite(byte[] buff)
    {
        try
        {
        	outStream.write(buff);
        	outStream.flush();
        	return true;
        } catch (IOException e)
        {	e.printStackTrace();
        }
        return false;
    }
	
    // -----------蓝牙数据接收线程-----------
    class RecvThread extends Thread
	{
		@Override
		public void run()
		{
			Log.i("BUG", "RecvThread Start!");
			
			byte[] rBuff = new byte[1024];
			int rLen = 0;
			try
			{
				inStream = btSocket.getInputStream();
//				uiHandler.sendEmptyMessage(WM_DO_DLL);
				rtStatus = RTStatus.RT_RUN;
			} catch(IOException e)
			{	// 此处异常将终止诊断线程
				e.printStackTrace();
//				uiHandler.sendEmptyMessage(WM_DO_ERROR);
			}
			
			while (rtStatus == RTStatus.RT_RUN)
			{
				try
				{
					rLen = inStream.read(rBuff);
					if(rLen > 0 && rLen < MAX_BUFF_SIZE)
					{
						synchronized(recvBuffer)
						{
							if(recvBufLen+rLen > MAX_BUFF_SIZE)
							{	recvBufLen = 0;
							}
							System.arraycopy(rBuff, 0, recvBuffer, recvBufLen, rLen);
							recvBufLen+=rLen;
						}
					} 
				} catch (IOException e)
				{	// 此处异常将终止诊断线程,多是由于用户关闭btSocket造成
					e.printStackTrace();
					break;
				}
			}
			Log.i("BUG", "RecvThread Exit!");
			rtStatus = RTStatus.RT_IDLE;
		}
	}
	// -----------蓝牙状态广播接收器-----------
	public BroadcastReceiver btReceiver = new BroadcastReceiver()
 	{  
         @Override  
         public void onReceive(Context context, Intent intent)
         {  
             String action = intent.getAction();  
             if(BluetoothDevice.ACTION_FOUND.equals(action))
             {  
            	 doActionFound(intent);
             }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
             {
            	 doActionDiscoveryFinished();
             }else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
             { 
            	 doActionBondStateChanged(intent);
             }else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
             {
            	 doActionStateChanged(intent);
             }
         }
 	};
	private void doActionFound(Intent intent)
	{
//		BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//		if (device.getBondState() != BluetoothDevice.BOND_BONDED)
//		{
//			if (device.getName().equals("ADS"))
//			{
//				try
//				{
//					Message msg = new Message();
//					msg.obj = device;
//					msg.what = WM_PAIR;
//					uiHandler.sendMessage(msg);
//				} catch (Exception e)
//				{
//					e.printStackTrace();
//				}
//			}
//		}
	}
 	private void doActionDiscoveryFinished()
 	{
//     	Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
// 		if (pairedDevices.size() > 0)
// 		{
// 			for (BluetoothDevice device : pairedDevices)
// 			{
// 				if (device.getName().equals("ADS"))
// 				{
// 					btAddress = device.getAddress();
//// 					uiHandler.sendEmptyMessage(WM_CONNECT_BT);
// 					return;
// 				}
// 			}
// 		}
// 		Toast.makeText(MainActivity.this, "自动配对失败,请尝试手动配对!", Toast.LENGTH_LONG).show();
 	}
 	private void doActionBondStateChanged(Intent intent)
 	{
 		BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); 
        switch (device.getBondState())
        { 
        case BluetoothDevice.BOND_BONDING: 
            Log.d("BUG", "正在配对......"); 
            break; 
        case BluetoothDevice.BOND_BONDED: 
            Log.d("BUG", "完成配对");
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            btAdapter.cancelDiscovery();
            btDevice = device;
            connectBluetooth();
            break; 
        case BluetoothDevice.BOND_NONE: 
            Log.d("BUG", "取消配对"); 
        default: 
            break; 
        } 
 	}
 	private void doActionStateChanged(Intent intent)
 	{
 		int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
        switch(state)
        {
        case BluetoothAdapter.STATE_TURNING_ON:
        	// 蓝牙打开,正在打开
        	break;
        case BluetoothAdapter.STATE_ON:
        	// 蓝牙已经打开
        	Log.i(Common.TAG, "bluetooth open ok!");
			if(!pairBluetooth())
			{	doPairBluetooth();
			}
       	 	break;
        case BluetoothAdapter.STATE_TURNING_OFF:
        	// 蓝牙正在关闭
       	 	break;
        case BluetoothAdapter.STATE_OFF:
        	// 蓝牙已经关闭
       	 	break;
        }
 	}
 	
 	public BluetoothUtil(Context context, Handler handler, String btName)
	{
		this.context = context;
		this.handler = handler;
		this.btName = btName;
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		recvBuffer = new byte[MAX_BUFF_SIZE];
	}
}
