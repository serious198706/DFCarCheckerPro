package com.ads.activity;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.df.library.R;
import com.df.library.util.Common;

public class BaseActivity extends Activity implements OnClickListener
{
    private static final String tag = "AdsBaseActivity";
    
    private static final int SDK_VERSION = Build.VERSION.SDK_INT;
    
    protected static final String SAVED_PREFERENCES = "AdsSavedData";
    protected static final String KEY_LAST_CONNECTED_DEV_NAME = "LastDevName";
    protected static final String KEY_LAST_CONNECTED_DEV_MAC = "LastDevAddr";
    
    protected static final String KEY_SAVED_BOXID = "SavedBoxId";
    protected static final String KEY_SAVED_RANDOMCODE = "SavedRandomCode";
    protected static final String KEY_SAVED_PASSWORD = "SavedPassword";
    
    protected static SharedPreferences mSharedata;
    
    static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    static final int WM_DO_PAIR_DEVICE = 1000;
	static final int WM_DO_CONNECT_DEVICE = 1001;
	static final int WM_DO_RECV_THREAD = 1002;
    static final int WM_DO_DLL = 1003;
    static final int WM_DO_ERROR = 1004;
    static final int CONNECTOR_TYPE_SERIAL = 0;
    static final int CONNECTOR_TYPE_BLUETOOTH = 1;
    
    static final int RESCODE_BLUETOOTH_PAIR_FAIL = 2000;
    static final int RESCODE_BLUETOOTH_CONNECT_FAIL = 2001;
    
    static final int INVALID_BLUETOOTH_DATA = -1;
    
    static ProgressDialog mProgressDialog = null;
    
    private Dialog mAboutDialog;
    final CharSequence[] mConnectorOptions = new CharSequence[2];
    
    private Handler mBaseActivtyHandler = new Handler();
    
    protected static String mBoxId = null;
    protected static String mRandomCode = null;
    protected static String mPassword = null;
    
    static String btDeviceName;
    static String btDeviceMac;
    static BluetoothDevice btDevice;
    static BluetoothSocket btSocket = null;
//    static InputStream btInStream = null;
//    static OutputStream btOutStream = null;
    static int mConnectorType = CONNECTOR_TYPE_BLUETOOTH;
    static byte[] recvBuffer = new byte[1024];
    static int recvBufLen = 0;
    static RecvThread recvThread;
    private enum RTStatus {RT_RUN, RT_STOP, RT_IDLE};
	private RTStatus rtStatus = RTStatus.RT_IDLE;
	static boolean bBTRegister = false;
    static boolean mAllowShowDialog = true;
    
    public native String getBoxId();
    public native String getRandomCode();
    public native int checkRegister(String randomCode, String password, String boxID);
    public native void setConnectorInfo(int isBluetooth, int bluetoothData);
    public native int nativeInit(Object activityObj);
    public native int runDll(String dllPath, String binPath, String datPath, String param, boolean en, boolean allowDialog);
    public native void nativeHandleWinMessage(int what, int wparam, int lparam);
    public native void setUserInput(String input);
    public native void setCdsItemSelected(int index);
    
    static
    {
        try
        {
            Log.d(tag, "Try to load library libads.so");
            System.loadLibrary("ads");
        } catch (UnsatisfiedLinkError e)
        {
            Log.e(tag, "Cannot load library libads.so: "+e.getMessage());
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        mSharedata = getApplicationContext().getSharedPreferences(SAVED_PREFERENCES, Context.MODE_WORLD_READABLE);
        
        btDeviceName = mSharedata.getString(KEY_LAST_CONNECTED_DEV_NAME, null);
        btDeviceMac = mSharedata.getString(KEY_LAST_CONNECTED_DEV_MAC, null);
        
        mBoxId = mSharedata.getString(KEY_SAVED_BOXID, null);
        mRandomCode = mSharedata.getString(KEY_SAVED_RANDOMCODE, null);
        mPassword = mSharedata.getString(KEY_SAVED_PASSWORD, null);
    }
    
    protected void updateSavedRegistration(String boxID, String randomCode, String password)
    {
        mBoxId = boxID;
        mRandomCode = randomCode;
        mPassword = password;
        Editor editor = mSharedata.edit();
        editor.putString(KEY_SAVED_BOXID, mBoxId);
        editor.putString(KEY_SAVED_RANDOMCODE, mRandomCode);
        editor.putString(KEY_SAVED_PASSWORD, mPassword);
        editor.commit();
    }
    
    protected void clearSavedRegistration()
    {
        mBoxId = null;
        mRandomCode = null;
        mPassword = null;
        Editor editor = mSharedata.edit();
        editor.clear();
        editor.commit();
    }
    
    @Override
    public void onStop()
    {
        super.onStop();
        if(mBluetoothConnectFaildDialog != null)
        {
            mBluetoothConnectFaildDialog.dismiss();
        }
    }
    
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.ads_menu, menu);
//        return true;
//    }
//    
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        switch (item.getItemId()) {
//            case R.id.menu_about:
//                aboutDialog();
//                return true;
//            case R.id.menu_connect:
//                showConnectOption();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
    
//    private void showConnectOption() {
//        mConnectorOptions[CONNECTOR_TYPE_BLUETOOTH] = getString(R.string.connector_type_bluetooth);
//        mConnectorOptions[CONNECTOR_TYPE_SERIAL] = getString(R.string.connector_type_serial);
//        String title = getString(R.string.connector_type);
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(title);
//        builder.setSingleChoiceItems(mConnectorOptions, mConnectorType, mConnectorTypeSelectListener);
//        AlertDialog alert = builder.create();
//        alert.show();
//    }
    
//    private DialogInterface.OnClickListener mConnectorTypeSelectListener = new DialogInterface.OnClickListener() {
//        public void onClick(DialogInterface dialog, int type) {
//            switch (type) {
//            case CONNECTOR_TYPE_SERIAL:
//                connectToConnecterViaSerial();
//                break;
//            case CONNECTOR_TYPE_BLUETOOTH:
//                BluetoothDevice[] pairedDevices = ADSApp.getPairedBluetoothDevices();
//                if(pairedDevices == null || pairedDevices.length == 0) {
//                    Toast.makeText(getApplicationContext(), getString(R.string.no_paired_bluetooth_devices), Toast.LENGTH_LONG).show();
//                    break;
//                }
//                selectOneBluetoothConnector(pairedDevices);
//                break;
//            default:
//                Log.wtf(tag, "WTF connector type!");
//                throw(new RuntimeException());
//            }
//            dialog.dismiss();
//        }
//    };
    
    private void connectToConnecterViaSerial()
    {
        mConnectorType = CONNECTOR_TYPE_SERIAL;
    }
    
    /**
     * 显示蓝牙配对列表到AlertDialog
     */
//    private void selectOneBluetoothConnector(BluetoothDevice[] pairedDevices)
//    {
//        final String[] devices = new String[pairedDevices.length];
//        
//        for(int i=0; i<pairedDevices.length; i++)
//        {
//            devices[i] = String.format("%s [%s]", pairedDevices[i].getName(), pairedDevices[i].getAddress());
//        }
//        
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(getString(R.string.select_bluetooth_device_to_connect));
//        builder.setSingleChoiceItems(devices, -1, new DialogInterface.OnClickListener()
//        {
//            @Override
//            public void onClick(DialogInterface dialog, int which)
//            {
//                markTheDeviceShouldBeConnectedLater(which);
//                dialog.dismiss();
//            }
//        });
//        AlertDialog alert = builder.create();
//        alert.show();
//    }
    
    /**
	 * 打开蓝牙设备
	 * @return true	蓝牙设备已经打开或正在打开
	 * @return false 不存在蓝牙设备
	 */
	public boolean enableBluetooth()
	{
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if(btAdapter!=null)
		{
			IntentFilter btIF = new IntentFilter();
			btIF.addAction(BluetoothDevice.ACTION_FOUND);
			btIF.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
			btIF.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
			btIF.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
			btIF.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
			registerReceiver(btReceiver, btIF);
			bBTRegister = true;
			
			if(!btAdapter.isEnabled())
			{	return btAdapter.enable();
			}else
			{	Message msg = new Message();
				msg.what = WM_DO_PAIR_DEVICE;
				msg.obj = "ADS";
				uiHandler.sendMessage(msg);
				return true;
			}
		}else
		{
			Toast.makeText(this, getString(R.string.bt_nofound), Toast.LENGTH_LONG).show();
		}
		return false;
	}
	
	public void disableBluetooth()
	{
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if(btAdapter!=null)
		{	btAdapter.disable();
		}
	}
    
    @Override
	protected void onDestroy()
	{
    	try
    	{  
    	    unregisterReceiver(btReceiver);
    	    if(mProgressDialog!=null)
    	    {
    	    	mProgressDialog.dismiss();
    	    }
    	} catch (IllegalArgumentException e)
    	{  
    	    e.printStackTrace();  
    	}  
		super.onDestroy();
	}

	/**
     * 蓝牙接收线程
     *	.由于inputStream的read()是阻塞的所以必须使用线程接收,否则程序会阻塞
     */
    class RecvThread extends Thread
	{
		@Override
		public void run()
		{
			Log.i("BUG", "RecvThread Start!");
			
			byte[] rBuff = new byte[1024];
			int rLen = 0;
			
			InputStream inStream = null;
			try
			{
				inStream = btSocket.getInputStream();
				uiHandler.sendEmptyMessage(WM_DO_DLL);
				rtStatus = RTStatus.RT_RUN;
			} catch(IOException e)
			{	// 此处异常将终止诊断线程
				e.printStackTrace();
				uiHandler.sendEmptyMessage(WM_DO_ERROR);
			}
			
			while (rtStatus == RTStatus.RT_RUN)
			{
				try
				{
//					rLen = btSocket.getInputStream().read(rBuff);
//					rLen = btInStream.read(rBuff);
					rLen = inStream.read(rBuff);
					if(rLen>0 && rLen<1024)
					{
						synchronized(recvBuffer)
						{
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
    
    /**
	 * 配对蓝牙
	 * .查找配对deviceName指定的蓝牙设备,并将mac地址设置到btAddress中
	 * .显示蓝牙设置界面完成配对操作
	 * @return true	查找到已经配对的蓝牙设备,btAddress为deviceName设备的mac地址
	 * @return false 启动蓝牙配对过程
	 */
	public boolean pairDevice(String deviceName)
	{
		// 查询是否已经匹配
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
		if (pairedDevices.size() > 0)
		{
			for (BluetoothDevice device : pairedDevices)
			{
				if (device.getName().equals(deviceName))
				{
//					btDevice = device;
					Message msg = new Message();
					msg.what = WM_DO_CONNECT_DEVICE;
					msg.obj = device;
					uiHandler.sendMessage(msg);
					return true;
				}
			}
		}
		
		// 开启蓝牙搜索并搜索蓝牙
		btAdapter.startDiscovery();
		// 配对提示
		Toast.makeText(this, getString(R.string.bt_pair_hint, deviceName), Toast.LENGTH_LONG).show();
		Intent intent =  new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);  
        startActivity(intent);
		return false;
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
	public BluetoothSocket connectBluetooth(BluetoothDevice btDevice) throws IOException
	{
		if(btDevice==null)
		{	return null;
		}
		
		if(Build.VERSION.SDK_INT > 10)
		{
			btSocket = connectBluetoothByInsecureRf(btDevice);
			if(btSocket==null)
			{
				btSocket = connectBluetoothByInvoke(btDevice);
				if(btSocket == null)
				{
					return null;
				}
			}
//			btInStream = btSocket.getInputStream();
//			btOutStream = btSocket.getOutputStream();
			Log.i(Common.TAG, "connectDevice Success!");
			return btSocket;
		}else
		{
			btSocket = connectBluetoothByRf(btDevice);
			if(btSocket == null)
			{
				btSocket = connectBluetoothByInvoke(btDevice);
				if(btSocket == null)
				{
					return null;
				}
			}
//			btInStream = btSocket.getInputStream();
//			btOutStream = btSocket.getOutputStream();
			Log.i(Common.TAG, "connectDevice Success!");
			return btSocket;
		}
	}
	
	public boolean startRecvThread()
	{
		recvThread = new RecvThread();
		recvThread.start();
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
	private BluetoothSocket connectBluetoothByInsecureRf(BluetoothDevice btDevice)
	{
		try
		{
			btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(BT_UUID);
			btSocket.connect();
//			btInStream = btSocket.getInputStream();
//			btOutStream = btSocket.getOutputStream();
			return btSocket;
		} catch (Exception e)
		{	e.printStackTrace();
			bluetoothClose();
		}
		return btSocket;
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
	private BluetoothSocket connectBluetoothByRf(BluetoothDevice btDevice)
	{
		try
		{
			btSocket = btDevice.createRfcommSocketToServiceRecord(BT_UUID);
			btSocket.connect();
//			btInStream = btSocket.getInputStream();
//			btOutStream = btSocket.getOutputStream();
			return btSocket;
		} catch (Exception e)
		{	e.printStackTrace();
			bluetoothClose();
		}
		return btSocket;
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
	private BluetoothSocket connectBluetoothByInvoke(BluetoothDevice btDevice)
	{
		try
		{
			Method method = btDevice.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
			btSocket = (BluetoothSocket) method.invoke(btDevice, 1);
			btSocket.connect();
//			btInStream = btSocket.getInputStream();
//			btOutStream = btSocket.getOutputStream();
			return btSocket;
		} catch (Exception e)
		{	e.printStackTrace();
			bluetoothClose();
		}
		return btSocket;
	}
	

	
	
    // ------------------广播接收器------------------
 	/**
 	 * 蓝牙配对广播接收器
 	 */
 	public BroadcastReceiver btReceiver = new BroadcastReceiver()
 	{  
         @Override  
         public void onReceive(Context context, Intent intent)
         {  
             String action = intent.getAction();  
             if(BluetoothDevice.ACTION_FOUND.equals(action))
             {  
//                  BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  
//                  if (device.getBondState() != BluetoothDevice.BOND_BONDED)
//                  {
//                 	 if(device.getName().equals("ADS"))
//                 	 {
//                 		 try
//                 		 {
//                 			 Message msg = new Message();
//                 			 msg.obj = device;
//                 			 msg.what = WM_PAIR;
//                 			 uiHandler.sendMessage(msg);
//                 		 }catch (Exception e)
//                 		 {
//                 			 e.printStackTrace();
//                 		 }
//                 	 }
//                  }  
             }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
             {
//             	Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
//         		if (pairedDevices.size() > 0)
//         		{
//         			for (BluetoothDevice device : pairedDevices)
//         			{
//         				if (device.getName().equals("ADS"))
//         				{
//         					btAddress = device.getAddress();
////         					uiHandler.sendEmptyMessage(WM_CONNECT_BT);
//         					return;
//         				}
//         			}
//         		}
//         		Toast.makeText(MainActivity.this, "自动配对失败,请尝试手动配对!", Toast.LENGTH_LONG).show();
             }else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
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
//                     btDevice = device;
                     uiHandler.sendEmptyMessage(WM_DO_CONNECT_DEVICE);
                     break; 
                 case BluetoothDevice.BOND_NONE: 
                     Log.d("BUG", "取消配对"); 
                 default: 
                     break; 
                 } 
             }else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
             {
                  int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                  switch(state)
                  {
                  case BluetoothAdapter.STATE_TURNING_ON:
                 	 break;
                  case BluetoothAdapter.STATE_ON:
                	  Message msg = new Message();
                	  msg.what = WM_DO_PAIR_DEVICE;
                	  msg.obj = "ADS";
                	  uiHandler.sendMessage(msg);
                 	 break;
                  case BluetoothAdapter.STATE_TURNING_OFF:
                 	 break;
                  case BluetoothAdapter.STATE_OFF:
                 	 break;
                  }
             }
         }
 	};
 	
 	/**
 	 * uiHandler
 	 */
 	Handler uiHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case WM_DO_PAIR_DEVICE:
				Log.i(Common.TAG, "WM_DO_PAIR_DEVICE 1");
				pairDevice((String)msg.obj);
				break;
			case WM_DO_CONNECT_DEVICE:
				Log.i(Common.TAG, "WM_DO_CONNECT_DEVICE 2");
				try
				{
					connectBluetooth((BluetoothDevice)msg.obj);
					uiHandler.sendEmptyMessage(WM_DO_RECV_THREAD);
				} catch (IOException e)
				{
					e.printStackTrace();
					Log.i(Common.TAG, "WM_DO_CONNECT_DEVICE Error!");
				}
				break;
			case WM_DO_RECV_THREAD:
				Log.i(Common.TAG, "WM_DO_RECV_THREAD 3");
				startRecvThread();
				break;
			case WM_DO_DLL:
				Log.i(Common.TAG, "WM_DO_DLL 4");
				startDll();
				break;
			case WM_DO_ERROR:
				Log.i(Common.TAG, "WM_DO_ERROR 5");
				break;
			}
		}
	};
	
	public void startDll(){};

    /**
     * 连接当前蓝牙设备
     */
    private BluetoothDevice tryToConnectedBluetoothDevice()
    {
    	BluetoothDevice btDevice = null;
    	BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    	Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
 		if (pairedDevices.size() > 0)
 		{
 			for (BluetoothDevice device : pairedDevices)
 			{
 				if (device.getName().equals("ADS"))
 				{
 					btDevice = btAdapter.getRemoteDevice(device.getAddress());
 					break;
 				}
 			}
 		}
 		
 		if(btDevice!=null)
 		{
 			BluetoothSocket btSocket = null;
			if(Build.VERSION.SDK_INT > 10)
			{
				btSocket = connectBluetoothByInsecureRf(btDevice);
				if(btSocket == null)
				{
					btSocket = connectBluetoothByInvoke(btDevice);
					if(btSocket == null)
					{
						return null;
					}
				}
			}else
			{
				btSocket = connectBluetoothByRf(btDevice);
				if(btSocket == null)
				{
					btSocket = connectBluetoothByInvoke(btDevice);
					if(btSocket == null)
					{
						return null;
					}
				}
			}
			Log.i(Common.TAG, "connectDevice Success!");
			try
			{
				btSocket.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			return btDevice;
 		}else
 		{
 			return null;
 		}
    }
    
    AlertDialog mBluetoothConnectFaildDialog;
    private void showBluetoothConnectFaild()
    {	// 显示蓝牙连接失败对话框
        if(mBluetoothConnectFaildDialog == null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
            builder.setTitle(R.string.app_name);
            builder.setMessage(R.string.bluetooth_connection_failed);
            builder.setPositiveButton(R.string.confirm, null);
            mBluetoothConnectFaildDialog = builder.create();
        }
        if(!mBluetoothConnectFaildDialog.isShowing())
        {
            mBluetoothConnectFaildDialog.show();
        }
    }
    
    // 查找蓝牙连接
    protected void findAndMarkLastConnectedBluetoothConnecter()
    { 
    	btDevice = tryToConnectedBluetoothDevice();
        if(btDevice == null) 
        {
            showBluetoothConnectFaild();
        } else 
        {
            markTheDeviceShouldBeConnectedLater(btDevice);
        }
    }
    
    /**
     * 将蓝牙配对连接信息保存到SharePreference中
     */
    private void saveLastConnectedBluetoothDeviceMac(BluetoothDevice d)
    {
    	btDevice = d;
        String name = btDevice.getName();
        String mac = btDevice.getAddress();
        Editor e = mSharedata.edit();
        e.putString(KEY_LAST_CONNECTED_DEV_NAME, name);
        e.putString(KEY_LAST_CONNECTED_DEV_MAC, mac);
        e.commit();
    }
    
    /**
     * 连接蓝牙设备
     */
//    private void markTheDeviceShouldBeConnectedLater(int deviceIndexOfCurrentlyPairedDevices)
//    {
//        final BluetoothDevice d = ADSApp.currentlyPairedDevices[deviceIndexOfCurrentlyPairedDevices];
//        markTheDeviceShouldBeConnectedLater(d);
//    }
    
    /**
     * 标记连接蓝牙设备
     */
    protected void markTheDeviceShouldBeConnectedLater(BluetoothDevice d)
    {
        mConnectorType = CONNECTOR_TYPE_BLUETOOTH;
//        saveLastConnectedBluetoothDeviceMac(d);
    }
    
    private Runnable mShowBluetoothConnectingProgress = new Runnable() {
        public void run() {
            setProgressBarIndeterminateVisibility(true);
        }
    };
    
    /**
     * 连接蓝牙设备
     */
    protected int connectToConnecterViaBluetooth(final BluetoothDevice d)
    {	
//    	btSocket = connectBluetooth(d);
//    	recvThread = new RecvThread();
//    	recvThread.start();
    	return 1;
    	//Called from DLL thread
//        int ret = INVALID_BLUETOOTH_DATA;
//        
//        mBaseActivtyHandler.post(mShowBluetoothConnectingProgress);
//        
//        BluetoothSocket bs = null;
//        try
//        {
//            //bs = d.createRfcommSocketToServiceRecord(MYUUID);
//        	Method m;
//    		try
//    		{
//    			m = d.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
//    			bs = (BluetoothSocket) m.invoke(d, 1);
//    		}catch(Exception e)
//    		{
//    			e.printStackTrace();
//    		}
//            bs.connect();
//            
//            btSocket = bs;
//            Class<? extends BluetoothSocket> btsocketClass = btSocket.getClass();
//                          
//            if (SDK_VERSION <= 16)
//            {
//                 Field fSocketData = btsocketClass.getDeclaredField("mSocketData");
//                 fSocketData.setAccessible(true);
//                 ret = fSocketData.getInt(btSocket);
//                 Log.i(tag, "Android 4.1 or older: Get Current Connected Bluetooth SocketData = "+ret);
//            } else
//            {
//            	 Field fSocketData = btsocketClass.getDeclaredField("mSocket");
//                 fSocketData.setAccessible(true);
//                 LocalSocket Socket = (LocalSocket) fSocketData.get(btSocket);
//                 FileDescriptor fd = Socket.getFileDescriptor();    
//                
//                 Class<? extends FileDescriptor> FileDescriptortClass = fd.getClass();
//                 Field fdescriptorData = FileDescriptortClass.getDeclaredField("descriptor");	
//                 fdescriptorData.setAccessible(true);
//                 ret = fdescriptorData.getInt(fd);
//                 ret = ret + 0xf0000000;
//                 Log.i(tag, " Android 4.2 and later: Get Current Connected Bluetooth SocketData = "+ret);
//            }
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//            try
//            {	bs.close();
//            } catch (IOException e1)
//            {
//            }
//            mBaseActivtyHandler.post(mBluetoothConnectionFailedHandler);
//            return INVALID_BLUETOOTH_DATA; // failed
//        }
//			
//        mBaseActivtyHandler.post(mBluetoothConnectionSucceedHandler);
//        return ret;
    }
    
//    protected int connectToConnecterViaBluetooth(final BluetoothDevice d)
//  {	//Called from DLL thread
//      int ret = INVALID_BLUETOOTH_DATA;
//      
//      mBaseActivtyHandler.post(mShowBluetoothConnectingProgress);
//      
////      BluetoothSocket bs = null;
//      Method m;
//		try
//		{
//			m = d.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
//			try
//			{
//				btSocket = (BluetoothSocket) m.invoke(d, 1);
//				try
//				{
//					btSocket.connect();
////					inStream = bs.getInputStream();
////					outStream = bs.getOutputStream();
//					return 1;
//				} catch (IOException e)
//				{
//					e.printStackTrace();
//				}
//			} catch (IllegalArgumentException e)
//			{
//				e.printStackTrace();
//			} catch (IllegalAccessException e)
//			{
//				e.printStackTrace();
//			} catch (InvocationTargetException e)
//			{
//				e.printStackTrace();
//			}
//		} catch (SecurityException e)
//		{
//			e.printStackTrace();
//		} catch (NoSuchMethodException e)
//		{
//			e.printStackTrace();
//		}
//			
////      mBaseActivtyHandler.post(mBluetoothConnectionSucceedHandler);
//      return 0;
//  }
    
    protected void onBluetoothConnectionFailed() {
        //Do nothing
    }
    
    private Runnable mBluetoothConnectionFailedHandler = new Runnable() {
        @Override
        public void run() {
            setProgressBarIndeterminateVisibility(false);
            onBluetoothConnectionFailed();
            showBluetoothConnectFaild();
        }
    };
    
    protected void onBluetoothConnectionSucceed() {
        //Do nothing
    }
    
    private Runnable mBluetoothConnectionSucceedHandler = new Runnable()
    {
        @Override
        public void run() {
            setProgressBarIndeterminateVisibility(false);

            //Clear this flag every time we connected successfully
            onBluetoothConnectionSucceed();
            if(mBluetoothConnectFaildDialog != null) {
                mBluetoothConnectFaildDialog.dismiss();
            }
            Toast.makeText(getApplicationContext(), getString(R.string.bluetooth_connection_succeed), Toast.LENGTH_LONG).show();
        }
    };
    
    public static void dumpByteArray(byte[] buff)
    {
        StringBuffer sb = new StringBuffer();
        for(byte b:buff)
        {	sb.append(String.format("%02X ", b));
        }
        Log.i(tag, "buff = "+sb);
    }
    public static void dumpByteArray(byte[] buff, int len)
    {
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<len;i++)
        {	sb.append(String.format("%02X ", (byte)buff[i]));
        }
        Log.i(tag, "buff = "+sb);
    }
    
    /**
     * 连接蓝牙设备
     */
    protected int blueToothConnectToMarkedDevice()
    {
        int bluetoothSocketData = INVALID_BLUETOOTH_DATA;
        if(btDevice != null)
        {
            bluetoothSocketData = connectToConnecterViaBluetooth(btDevice);
            recvThread = new RecvThread();
            recvThread.start();
            try
			{
				Thread.sleep(1000);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
        }
        return bluetoothSocketData;
    }
    
    /**
     * 关闭蓝牙Socket连接
     */
    protected void bluetoothClose()
    {
    	// btInStream
//    	try
//    	{	if(btInStream != null)
//    		{	btInStream.close();		btInStream = null;
//    		}
//    	}catch(IOException e)
//    	{	e.printStackTrace();
//    	}
    	if(btSocket==null)
    	{
    		return;
    	}
//    	try
//    	{	if(btSocket.getInputStream() != null)
//    		{	btSocket.getInputStream().close();
//    		}
//    	}catch(IOException e)
//    	{	e.printStackTrace();
//    	}
    	// Stop RecvThread
    	if(rtStatus == RTStatus.RT_RUN)
    	{	rtStatus = rtStatus.RT_STOP;
    		while(rtStatus != RTStatus.RT_IDLE)
    		{	try
    			{	Thread.sleep(5);
    			} catch (InterruptedException e)
    			{	e.printStackTrace();
    			}
    		}
    	}
    	
    	// btOutStream
//    	try
//    	{	if(btOutStream != null)
//    		{	btOutStream.close();	btOutStream = null;
//    		}
//    	}catch(IOException e)
//    	{	e.printStackTrace();
//    	}
//    	try
//    	{	if(btSocket.getOutputStream()!= null)
//    		{	btSocket.getOutputStream().close();
//    		}
//    	}catch(IOException e)
//    	{	e.printStackTrace();
//    	}
    	// btSocket
    	try
    	{	if(btSocket != null)
    		{	btSocket.close();		btSocket = null;
    		}
    	}catch(IOException e)
    	{	e.printStackTrace();
    	}
    	// close
//    	BluetoothAdapter.getDefaultAdapter().disable();
    }
    
    /**
     * 从蓝牙inputStream中读取maxReadLen数据
     */
    protected byte[] bluetoothRead(int maxReadLen)
    {
    	if(recvBufLen<=0)
    	{
    		return "".getBytes();
    	}
    	synchronized(recvBuffer)
		{
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
    protected int bluetoothWrite(byte[] buff)
    {
//        try
//        {
////        	btSocket.getOutputStream().write(buff);
////        	btSocket.getOutputStream().flush();
//        	OutputStream outStream = btSocket.getOutputStream();
//        	outStream.write(buff);
//        	outStream.flush();
//        	return 1;
//        } catch (IOException e)
//        {	e.printStackTrace();
//        }
        return 0;
    }
    
    public void showProgressDialog(String title, String message)
    {
    	if(mProgressDialog==null)
    	{
    		mProgressDialog = ProgressDialog.show(this, title, message);
    	}else
    	{
    		mProgressDialog.dismiss();
    		mProgressDialog = ProgressDialog.show(this, title, message);
    	}
    }
    
    public void hideProgressDialog()
    {
    	if(mProgressDialog!=null)
    	{
    		mProgressDialog.dismiss();
    	}
    }
    
    private void aboutDialog() {
        Context c = this;
        mAboutDialog = new Dialog(c);

        mAboutDialog.setContentView(R.layout.ads_about_dialog);
        mAboutDialog.setTitle(getString(R.string.about_title));
        Button b = (Button)mAboutDialog.findViewById(R.id.ads_about_button_close);
        b.setOnClickListener(this);
        mAboutDialog.show();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ads_about_button_close) {
            mAboutDialog.dismiss();
        }
    }

    public void showAlertFromChildThread(final String content) {
        mBaseActivtyHandler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
                builder.setMessage(content)
                       .setCancelable(false)
                       .setPositiveButton(BaseActivity.this.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }
    
    public void showAlertFromChildThread(final String content, final DialogInterface.OnClickListener onClickListener) {
        mBaseActivtyHandler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
                builder.setMessage(content)
                       .setCancelable(false)
                       .setPositiveButton(BaseActivity.this.getString(R.string.confirm), onClickListener);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }
    
}
