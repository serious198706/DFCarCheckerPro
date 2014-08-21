package com.ads.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ads.adapter.BasicAdapter;
import com.ads.model.UserSelection;
import com.ads.utils.BluetoothUtil;
import com.ads.utils.FileUtil;
import com.ads.view.CdsItemView;
import com.ads.view.CustView;
import com.ads.view.DtcItemView;
import com.ads.view.PopMenuView;
import com.ads.view.StatusView;
import com.df.library.R;
import com.df.library.util.MyAlertDialog;
import com.df.library.util.MyApplication;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkingActivity extends BaseActivity implements IAdsJavaCallback
{
    public static final char DTC_FLAG = 1;
    public static final char FORMAT_FLAG = '@';
    public static final int WM_CONNECT_BLUETOOTH = 1100;

    private static final String tag = "ADSWorkingActivity";

    private String mLink;
    private String mParam;

    private Handler mActivityHandler;

    private ListView mListView;
    private TextView mStatText;
    private Button mBtnSelectall;
    private Button mBtnSelectnone;
    private Button[] mFunctionButtons;

    //    private ArrayAdapter<String> mMenuAdapter;
    private SimpleAdapter mMenuAdapter;
    private ArrayList<Map<String, Object>> mMenuList;
    private DtcAdapter mDtcAdapter;
    private CdsAdapter mCdsAdapter;

    private PopMenuView mPopMenu;
    private StatusView mStatusView;
    private CustView mCustView;

    private ProgressDialog mProgressDialog;

    private NativeWinMessageHandler mNativeMessageHandler;
    private UserSelection mUserSelection = null;
    private BluetoothUtil bluetoothUtil = null;

    private int mDllThreadExitVal = 0;

    private int nListViewFirstPos = 0, nListViewShowCount = 0;

    private List<String> dtcPrintData, cdsPrintData;
    private String boxid;
    private MyAlertDialog myAlertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // 应用启动前先关闭蓝牙以避免蓝牙设备没有得到复位
        bluetoothUtil = new BluetoothUtil(this, uiHandler, "ADS;JBM-242");
        bluetoothUtil.disableBluetooth();

        mActivityHandler = new Handler();

        setContentView(R.layout.scan_main);
        mListView = (ListView) findViewById(R.id.ads_list);
        mStatText = (TextView) findViewById(R.id.ads_stat_text);

        initFunctionButtons();
        mPopMenu = (PopMenuView) findViewById(R.id.ads_pop_menu);
        mPopMenu.setButtonsOnClickListener(mPopMenuButtonsOnClickListener);
        mStatusView = (StatusView) findViewById(R.id.ads_status_view);
        mCustView = (CustView) findViewById(R.id.ads_cust_view);

        Intent i = getIntent();
        mLink = i.getStringExtra("link");
        if(mLink == null) mLink = "DEMO"; //Default

        mLink = mLink.replace("\\", "/");

        mParam = i.getStringExtra("param");
        if(mParam == null) mParam = ""; //Default

        //mLink = "ZHONGHUA/ENG";
        //mParam = "01";

        Log.d(tag, String.format("Link=%s, Param=%s", mLink, mParam));

        mUserSelection = new UserSelection();
        mNativeMessageHandler = new NativeWinMessageHandler();

        setConnectorInfo(mConnectorType, INVALID_BLUETOOTH_DATA);//Set invalid firstly, open later
        nativeInit(this);

        myAlertDialog = new MyAlertDialog();

        findViewById(R.id.connectADS).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressDialog("", getString(R.string.bt_connecting));
                uiHandler.sendEmptyMessage(WM_CONNECT_BLUETOOTH);
            }
        });

        findViewById(R.id.getErrorCode).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                nativeHandleWinMessage(MyApplication.WM_ON_MENU_ITEM_CLICK, 0, 0);
                sendKeyEvent(0);
            }
        });

        findViewById(R.id.buttonHome).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(bluetoothUtil != null) {
            bluetoothUtil.closeBluetooth();
            bluetoothUtil.disableBluetooth();
        }
        super.onDestroy();
    }

    // 开启DLL运行线程
    public void startDll() {
        mDllThread.start();
    }

    @Override
    protected byte[] bluetoothRead(int maxReadLen)
    {
        return bluetoothUtil.bluetoothRead(maxReadLen);
    }

    @Override
    protected int bluetoothWrite(byte[] buff) {
        if(bluetoothUtil.bluetoothWrite(buff)) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }


    // DLL运行线程
    Thread mDllThread = new Thread("DllThread") {
        @Override
        public void run() {
            boxid = getBoxId();
            Log.i("BUG","BoxID:"+boxid);

            String cacheDirPath = getCacheDir().getAbsolutePath();
            String name;
            String dir;
            if(mLink.contains("/")) {
                int i = mLink.lastIndexOf("/");
                dir = mLink.substring(0, i+1);
                name = mLink.substring(i+1, mLink.length());
            } else {
                dir = "";
                name = mLink;
            }

            String dllName = name + ".so";
            String dllSrc = "dll/" + dir + dllName;
            String dllDst = cacheDirPath + "/" + dllName;

            boolean en = false;

            String binName = "cn" + name + ".bin";
            String binSrc = "bin/" + dir + binName;
            String binDst = cacheDirPath + "/" + binName;

            String datName = "Data.dat";
            String datSrc = "dat/cn" + datName;
            String datDst = cacheDirPath + "/" + datName;

            Log.d(tag, "Extracting dll file "+ dllSrc + " to "+ dllDst);
            boolean dllRead = MyApplication.extractAssetFile(dllSrc, dllDst);

            Log.d(tag, "Extracting bin file "+ binSrc + " to "+ binDst);
            boolean binRead = MyApplication.extractAssetFile(binSrc, binDst);

            Log.d(tag, "Extracting dat file "+ datSrc + " to "+ datDst);
            boolean datRead = MyApplication.extractAssetFile(datSrc, datDst);

            if(dllRead && binRead && datRead) {
                mDllThreadExitVal = runDll(dllDst, binDst, datDst, mParam, en, mAllowShowDialog);
                Log.i(tag, "Finish executing DLL "+binDst+", exit code = " + mDllThreadExitVal);

                if (mConnectorType == CONNECTOR_TYPE_BLUETOOTH
                        && btSocket != null) {
                    try {
                        btSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                mActivityHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        exitHandler.removeCallbacks(r);

                        showDialog("ADS", mLink + "\n"
                                + getString(R.string.dllquit), false);
                    }
                });
                getUserSelection(); //Wait here

            } else {
                mActivityHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        showDialog("ADS", mLink + "\n"
                                + getString(R.string.dllerror), false);

                    }
                });

                getUserSelection(); // Wait here
            }

            //DLL thread quits, so activity quits
            mActivityHandler.post(new Runnable() {
                @Override
                public void run() {
                    bluetoothUtil.disableBluetooth();
                    WorkingActivity.this.setResult(mDllThreadExitVal);
                    WorkingActivity.this.finish();
                }
            });

            File fs[] = (new File(cacheDirPath)).listFiles();
            for (File f : fs) {
                Log.i(tag, "Delete file " + f.getAbsolutePath());
                f.delete();
            }
        }
    };


    /**
     * Called from native, an message will be sent and handled in the main thread to update the UI
     */
    private Object mMsgSynchonizer = new Object();

    public void sendWinMessageFromNative(int what, int wparam, int lparam)
    {
        WinMessage wm = new WinMessage(what, wparam, lparam);
        Message m = mNativeMessageHandler.obtainMessage(0, wm);

        try
        {
            // We send the message and then wait it until it is handled
            synchronized (mMsgSynchonizer)
            { // We must get the object monitor here before we send the message
                /**
                 * mNativeMessageHandler.sendMessage(m) must be moved into the
                 * synchronized block If UI thread get the CPU just now, it will
                 * be blocked when it try to do synchronized (mMsgSynchonizer).
                 * We do this to make sure that mMsgSynchonizer.notify() won't
                 * be called before we call mMsgSynchonizer.wait()
                 */
                mNativeMessageHandler.sendMessage(m);
                mMsgSynchonizer.wait();
            }
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Called from native, wait here and return an selection code, e.g. item index or button id
     */
    public int getUserSelection() {
        Log.d(tag, "Wait for user selection .");
        int ret = MyApplication.WM_KEY_NONE;
        try {
            synchronized (mUserSelection) {
                mUserSelection.wait();
                ret = mUserSelection.consumeKeyCode();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
    }
    public int getUserSelectionTimed(int timeout) {
        int ret = MyApplication.WM_KEY_NONE;
        try {
            synchronized (mUserSelection) {
                mUserSelection.wait(timeout);
                ret = mUserSelection.consumeKeyCode();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
    }
    /**
     * Called from native, to display/dismiss a customized dialog
     */
    // private AdsDialog mDialog;
    public void showDialog(byte[] info, boolean question) {
        StringBuffer title = new StringBuffer();
        StringBuffer content = new StringBuffer();
        expInfo(info, title, content);

        showDialog(title.toString(), content.toString(), question);
    }
    public void showDialog(String title, String content, boolean question) {
        myAlertDialog._showAlert(this, content, title, MyAlertDialog.BUTTON_STYLE_OK_CANCEL, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MyAlertDialog.POSITIVE_PRESSED:
                        sendKeyEvent(MyApplication.WM_ON_DIALOG_OK_CLICK);
                        break;
                    case MyAlertDialog.NEGATIVE_PRESSED:
                        sendKeyEvent(MyApplication.WM_ON_DIALOG_CANCEL_CLICK);
                        break;
                }

                return true;
            }
        }));
    }
    public void dismissDialog() {
        myAlertDialog._dismiss();
    }

    /**
     * Called from native, to show/dismiss the status 'dialog'
     */
    public void showStatusDialog(byte[] info) {
        StringBuffer title = new StringBuffer();
        StringBuffer content = new StringBuffer();
        expInfo(info, title, content);

        showStatusDialog(title.toString(), content.toString());
    }
    public void showStatusDialog(String title, String content) {
        myAlertDialog._showAlert(this, content, title, MyAlertDialog.NO_BUTTON, null);
    }
    public void setStatusDialogProgress(int progress) {

    }
    public void dismissStatusDialog() {
        myAlertDialog._dismiss();
    }

    /**
     * Called from native, to show the input dialog
     */
    public void showInputDialog(String title, byte[] info) {
        StringBuffer txtformat = new StringBuffer();
        StringBuffer content = new StringBuffer();
        expInfo(info, txtformat, content);

        showInputDialog(title, content.toString(), txtformat.toString());
    }
    private void showInputDialog(String title, String message, String txtformat) {
        myAlertDialog._showAlert(this, message, title, MyAlertDialog.BUTTON_STYLE_OK, null);
    }
    /**
     * Called from native, to reset window title
     */
    public void setWindowTitle(String title) {
        getWindow().setTitle(title);
    }

    /**
     * Called from native, to switch list views
     */
    private static final int WIN_MNU  =   1;
    private static final int WIN_DTC  =   2;
    private static final int WIN_CDS  =   3;
    private static final int WIN_CUST =   5;
    private static final int WIN_SELECT_CDS  =    8;
    private static int mCurrentWindowView = 0;
    public void switchWindowView(int type) {
        Log.d(tag, "Switch to widow type = "+type);

        clearStatText();
        hideAllFunctionButtons();
        mCurrentWindowView = type;
        switch(type) {
            case WIN_MNU:
                if(mMenuAdapter==null)
                {
                    mMenuList = new ArrayList<Map<String, Object>>();
                    mMenuAdapter = new SimpleAdapter(getApplicationContext(), mMenuList, R.layout.menu_item,
                            new String[]{"text", "id"},
                            new int[]{R.id.ads_menu_item_text, R.id.ads_menu_item_id});
                }
                mListView.setAdapter(mMenuAdapter);
                mListView.setOnItemClickListener(mMenuOnItemClickListener);
                mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);

                mCustView.setVisibility(View.GONE);
                break;

            case WIN_DTC:
                if(mDtcAdapter==null) {
                    mDtcAdapter = new DtcAdapter();
                }
                mListView.setAdapter(mDtcAdapter);
                mListView.setOnItemClickListener(null); //DTC does not response to user click
                mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);

                break;

            case WIN_SELECT_CDS:
                if(mCdsAdapter == null) {
                    mCdsAdapter = new CdsAdapter();
                }

                mListView.setAdapter(mCdsAdapter);
                mListView.setOnItemClickListener(null); //Let the list view itself to handle the check
                mListView.setItemsCanFocus(false);
                mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                mBtnSelectall.setVisibility(View.VISIBLE);
                mBtnSelectnone.setVisibility(View.VISIBLE);
                break;

            case WIN_CDS:
                if(mCdsAdapter == null) {
                    mCdsAdapter = new CdsAdapter();
                }
                mListView.setAdapter(mCdsAdapter);
                mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
                mListView.setOnItemClickListener(null); //CDS displaying does not response to user click
                break;

            case WIN_CUST:
                mCustView.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    /**
     * For set the stat text
     */
    public void showStatText(String s) {
        mStatText.setVisibility(View.VISIBLE);
        mStatText.setText(s);
    }
    public void clearStatText() {
        mStatText.setVisibility(View.INVISIBLE);
        mStatText.setText("");
    }

    /**
     * Called from native, to invalidate and redraw the list view
     */
    public void invalidateListView() {
        mListView.invalidateViews();
    }

    /**
     * Called from native, to get the top/bottom item position
     */
    //We deliberately sleep some time here to let the UI thread finish update first.
    //Otherwise, mListView.getLastVisiblePosition() might return inconsistent value.
    public int getListViewTopItemPos()
    {
        sleep(16);

        int lastPos = mListView.getLastVisiblePosition();
        int firstPos= mListView.getFirstVisiblePosition();
        if(lastPos>0)
        {
            nListViewFirstPos = firstPos;
            nListViewShowCount= lastPos-firstPos+1;
            Log.e("NOW", "FP:"+firstPos + " LP:" + lastPos);
        }
        return nListViewFirstPos;
    }
    public int getListViewBottomItemPos()
    {
        sleep(16);

        return nListViewFirstPos+nListViewShowCount;
    }
    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
        }
    }
    /**
     * Called from native, to add an item to the menu
     */
    public void addMenuItem(String item, int id)
    {
        Log.d(tag, "Add menu id:"+id+" item:"+item);
        if(mMenuAdapter!=null)
        {
            int i = mMenuAdapter.getCount()+1;
            item = String.format("%d. %s ", i, item);
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("text", item);
            map.put("id", id);
            mMenuList.add(map);
            mMenuAdapter.notifyDataSetChanged();

            findViewById(R.id.getErrorCodeArea).setVisibility(View.VISIBLE);
            findViewById(R.id.connectStatusText).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.connectStatusText)).setText("连接成功！");
        }
    }

    /**
     * Called from native, to clear the menu items
     */
    public void clearMenuItem()
    {
        if(mMenuAdapter!=null)
        {
            mMenuList.clear();
            mMenuAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Called from native, to manipulate the popup menu box
     */
    public void showPopMenu(String title) {
        mPopMenu.clearItems();
        mPopMenu.setVisibility(View.VISIBLE);
        mPopMenu.setTitle(title);
    }

    public void addPopMenuItem(String item) {
        mPopMenu.addItem(item);
        mPopMenu.invalidateListView();
    }

    public void dismissPopMenu() {
        mPopMenu.setVisibility(View.GONE);
    }

    /**
     * Called from native, to add/clear the DTC items
     */
    public void addDtcItem(byte buff[]) {
        DTCItem dtcItem = DTCItem.createFromBuff(buff);
        if(mDtcAdapter!=null) {
            mDtcAdapter.dtcItems.add(dtcItem);
            mListView.invalidateViews();
            mListView.setVisibility(View.VISIBLE);
        }
    }
    public void clearDtcItem() {
        Log.d(tag, "Clear all dtc items");
        if(mDtcAdapter!=null) {
            mDtcAdapter.dtcItems.clear();
            mListView.invalidateViews();
        }
    }


    /**
     * Called from native, to add/clear cds items
     */
    public void clearCdsItem() {
        Log.d(tag, "Clear all Cds items");
        if(mCdsAdapter != null) {
            mCdsAdapter.cdsItems.clear();
            mListView.invalidateViews();
        }
    }
    public void addCdsItem(int id, String txt, String max, String min, String unit, String format)
    {
        Log.e("NOW", String.format("Add CDS item id=%d, txt=%s, max=%s, min=%s, unit=%s, format=%s", id, txt, max, min, unit, format));
        if(mCdsAdapter!=null)
        {
            CdsItem item = CdsItem.create(id, txt, max, min, unit, format);
            int i = mCdsAdapter.cdsItems.size()+1;
            item.name = String.format("%d. %s", i, item.name);
            mCdsAdapter.cdsItems.add(item);
        }
    }
    public int getCdsItemId(int index)
    {
        int id=0;
        if( index >= 0 && index < mCdsAdapter.cdsItems.size())
        {
            id = mCdsAdapter.cdsItems.get(index).id;
            Log.d(tag, String.format("Get CDS item id for list index %d, reture id=%d", index, id));
        }
        return id;
    }
    public void setCdsItemValue(int id, float fValue, String strValue)
    {
        Log.i("NOW", "setCdsItemValue() id:" + id + " fValue:" + fValue + " strValue:" + strValue);
        CdsItem theItem = null;
//        for(int i=mCdsAdapter.getCount()-1; i>=0; i--)
        for(int i=0;i<mCdsAdapter.getCount();i++)
        {
            theItem = mCdsAdapter.cdsItems.get(i);
            if(theItem.id == id) break;
        }
        String format = theItem.format;

        if(format.contains("|")) {
            int i = (int)fValue;
            String[] fs = format.split("\\|");
            if(i >= fs.length) {
                Log.w(tag, String.format("setCdsItemValue id=%d v=%f s=%s f=%s", id, fValue, strValue, format));
                i = fs.length-1;
            }
            theItem.value = fs[i];
        } else if (format.contains(".")) {
            String fmt = "%"+format+"f";
            theItem.value = String.format(fmt+" %s", fValue, theItem.unit);
        } else if(strValue !=null && !strValue.isEmpty()){
            theItem.value = strValue;
        } else {
            theItem.value = String.format("%f %s", fValue, format);
        }
        invalidateListView();
    }

    /**
     * Called from native, to handle the CUST view stuffs.
     */
    public void clearCustView() {
        mCustView.clearViews();
    }
    public void custShowText(String text, int pos, int color) {
        mCustView.showText(text, pos, color);
    }

    /**
     *  #define KEY_BTN1    0xF1    
     #define KEY_BTN2    0xF2
     #define KEY_BTN3    0xF3
     #define KEY_BTN4    0xF4
     #define KEY_BTN5    0xF5
     #define KEY_BTN6    0xF6
     #define KEY_BTN7    0xF7
     #define KEY_BTN8    0xF8
     #define KEY_BTN9    0xF9
     #define KEY_BTN10   0xFA
     #define KEY_BTN11   0xFB
     #define KEY_BTN12   0xFC
     #define KEY_BTN13   0xFD
     #define KEY_NONE    0xFE
     #define KEY_EXIT    0xFF
     */


    Handler exitHandler = new Handler();
    Runnable r = new Runnable() {
        @Override
        public void run() {

            sendKeyEvent(MyApplication.WM_KEY_EXIT);

            sleep(100);

            exitHandler.post(r);
        }
    };

    @Override
    public void onBackPressed()
    {
        saveListView();

        String filepath = FileUtil.createSDCardDir("/ADS/");
        StringBuffer sb = new StringBuffer();
        // 盒子号
        if(boxid!=null && boxid.length()==24)
        {
            sb.append(boxid.substring(0, 24));
        }
        sb.append("TMTMTM");
        // 故障码
        if(dtcPrintData!=null)
        {
            for(String item : dtcPrintData)
            {
                sb.append(item);
            }
        }
        sb.append("TMTMTM");
        // 数据流
        if(cdsPrintData!=null)
        {
            for(String item : cdsPrintData)
            {
                sb.append(item);
            }
        }

        filepath = filepath+"PRT.txt";
        FileUtil.saveSDCardFile(filepath, sb.toString());

        mListView.setVisibility(View.GONE);
        exitHandler.post(r);

//        sendKeyEvent(MyApplication.WM_KEY_EXIT);
//        sendKeyEvent(MyApplication.WM_KEY_EXIT);
//
//        bluetoothUtil.closeBluetooth();
//        bluetoothUtil.disableBluetooth();

//    	if (mCurrentWindowView == WIN_MNU)
//    	{
//    		String filepath = FileUtil.createSDCardDir("/ADS/");
//	    	StringBuffer sb = new StringBuffer();
//	    	// 盒子号
//	    	if(boxid!=null && boxid.length()==24)
//	    	{
//	    		sb.append(boxid.substring(0, 24));
//	    	}
//	    	sb.append("TMTMTM");
//	    	// 故障码
//	    	if(dtcPrintData!=null)
//	    	{
//		    	for(String item : dtcPrintData)
//				{
//					sb.append(item);
//				}
//	    	}
//	    	sb.append("TMTMTM");
//			// 数据流
//	    	if(cdsPrintData!=null)
//	    	{
//		    	for(String item : cdsPrintData)
//				{
//					sb.append(item);
//				}
//	    	}
//
//	    	filepath = filepath+"PRT.txt";
//			FileUtil.saveSDCardFile(filepath, sb.toString());
//    	}else if(mCurrentWindowView == WIN_DTC)
//        {
//        	saveListView();
//        }else if(mCurrentWindowView == WIN_CDS)
//        {
//        	saveListView();
//        }else if (mCurrentWindowView == WIN_SELECT_CDS)
//		{
//			SparseBooleanArray array = mListView.getCheckedItemPositions();
//			for (int i = 0; i < mCdsAdapter.cdsItems.size(); i++)
//			{
//				if (array.get(i))
//				{
//					setCdsItemSelected(mCdsAdapter.cdsItems.get(i).id);
//				}
//			}
//		}
//
//		sendKeyEvent(MyApplication.WM_KEY_EXIT);
    }

    /**
     * 保存ListView数据
     * .根据mCurrentWindowView的值确定mListView中Adapter
     * .并从Adapter中取出数据保存到文件中
     * .文件保存根据数据类型+时间戳.txt形式保存
     */
    private void saveListView()
    {
        String filepath = FileUtil.createSDCardDir("/ADS/");
        StringBuffer sb = new StringBuffer();
        switch(mCurrentWindowView)
        {
            case WIN_DTC:

                DtcAdapter dtcAdapter = (DtcAdapter)mListView.getAdapter();
                sb.setLength(0);
                if(dtcPrintData==null)
                {	dtcPrintData = new ArrayList<String>();
                }
                dtcPrintData.clear();
                for(DTCItem dtcItem : dtcAdapter.dtcItems)
                {
                    sb.append(dtcItem.code);sb.append(";");
                    sb.append(dtcItem.desc);sb.append(";");
                    sb.append(dtcItem.state);sb.append(";");
                    sb.append("&");
                    dtcPrintData.add(String.format("%s;%s;%s;&", dtcItem.code, dtcItem.desc, dtcItem.state));
                }
                break;
            case WIN_CDS:
                CdsAdapter cdsAdapter = (CdsAdapter)mListView.getAdapter();
                sb.setLength(0);
                if(cdsPrintData==null)
                {	cdsPrintData = new ArrayList<String>();
                }
                cdsPrintData.clear();
                for(CdsItem cdsItem : cdsAdapter.cdsItems)
                {
                    sb.append(cdsItem.id);sb.append(";");
                    sb.append(cdsItem.name);sb.append(";");
                    sb.append(cdsItem.value);sb.append(";");
                    sb.append(cdsItem.max);sb.append(";");
                    sb.append(cdsItem.min);sb.append(";");
                    sb.append(cdsItem.unit);sb.append(";");
                    sb.append(cdsItem.format);sb.append(";");
                    sb.append("&");
                    cdsPrintData.add(String.format("%s;%s;%s;%s;%s;%s;%s;&", cdsItem.id, cdsItem.name, cdsItem.value, cdsItem.max, cdsItem.min, cdsItem.unit, cdsItem.format));
                }
                break;
        }
    }

    private void initFunctionButtons() {

        //Special buttons for CDS view
        mBtnSelectall = (Button) findViewById(R.id.ads_btn_selectall);
        mBtnSelectnone = (Button) findViewById(R.id.ads_btn_selectnone);

        mFunctionButtons = new Button[13];
        mFunctionButtons[0] = (Button)findViewById(R.id.ads_function_btn1);
        mFunctionButtons[1] = (Button)findViewById(R.id.ads_function_btn2);
        mFunctionButtons[2] = (Button)findViewById(R.id.ads_function_btn3);
        mFunctionButtons[3] = (Button)findViewById(R.id.ads_function_btn4);
        mFunctionButtons[4] = (Button)findViewById(R.id.ads_function_btn5);
        mFunctionButtons[5] = (Button)findViewById(R.id.ads_function_btn6);
        mFunctionButtons[6] = (Button)findViewById(R.id.ads_function_btn7);
        mFunctionButtons[7] = (Button)findViewById(R.id.ads_function_btn8);
        mFunctionButtons[8] = (Button)findViewById(R.id.ads_function_btn9);
        mFunctionButtons[9] = (Button)findViewById(R.id.ads_function_btn10);
        mFunctionButtons[10] = (Button)findViewById(R.id.ads_function_btn11);
        mFunctionButtons[11] = (Button)findViewById(R.id.ads_function_btn12);
        mFunctionButtons[12] = (Button)findViewById(R.id.ads_function_btn13);
        int w = getWindowManager().getDefaultDisplay().getWidth() / 4;
        LayoutParams lp = new LayoutParams(w, LayoutParams.FILL_PARENT);
        for(Button b : mFunctionButtons) {
            b.setLayoutParams(lp);
        }
        mBtnSelectall.setLayoutParams(lp);
        mBtnSelectnone.setLayoutParams(lp);
    }

    private void hideAllFunctionButtons() { //Utility for convenience
        for(Button b : mFunctionButtons) {
            b.setVisibility(View.GONE);
        }
        mBtnSelectall.setVisibility(View.GONE);
        mBtnSelectnone.setVisibility(View.GONE);
    }

    public void showFunctionButton(int index, String text) {
        index--; //Button index is 1-15 in native
        if(index >= 0 && index <= 12) {
            mFunctionButtons[index].setVisibility(View.VISIBLE);
            mFunctionButtons[index].setText(text);
        } else {
            Log.w(tag, "Cannot show button for an index "+index);
        }
    }

    public void onClickFunctionButton(View btn) {
        int id = btn.getId();
        if (id == R.id.ads_btn_selectall || id == R.id.ads_btn_selectnone) {
            int n = mCdsAdapter.cdsItems.size();
            for (int i = 0; i < n; i++) {
                mListView.setItemChecked(i, id == R.id.ads_btn_selectall);
            }
            invalidateListView();

        } else if (id == R.id.ads_function_btn1) {
            sendKeyEvent(0xF1);

        } else if (id == R.id.ads_function_btn2) {
            sendKeyEvent(0xF2);

        } else if (id == R.id.ads_function_btn3) {
            sendKeyEvent(0xF3);

        } else if (id == R.id.ads_function_btn4) {
            sendKeyEvent(0xF4);

        } else if (id == R.id.ads_function_btn5) {
            sendKeyEvent(0xF5);

        } else if (id == R.id.ads_function_btn6) {
            sendKeyEvent(0xF6);

        } else if (id == R.id.ads_function_btn7) {
            sendKeyEvent(0xF7);

        } else if (id == R.id.ads_function_btn8) {
            sendKeyEvent(0xF8);

        } else if (id == R.id.ads_function_btn9) {
            sendKeyEvent(0xF9);

        } else if (id == R.id.ads_function_btn10) {
            sendKeyEvent(0xFA);

        } else if (id == R.id.ads_function_btn11) {
            sendKeyEvent(0xFB);

        } else if (id == R.id.ads_function_btn12) {
            sendKeyEvent(0xFC);

        } else if (id == R.id.ads_function_btn13) {
            sendKeyEvent(0xFD);

        } else {
            sendKeyEvent(0xFE);

        }
    }

    private static String getGb2312String(byte[] buff) {
        String s = null;
        try {
            s = new String(buff, "GB2312");
        } catch (UnsupportedEncodingException e) {
            s = "Cannot convert buffer to GB2312 string.";
        }
        return s;
    }

    private static void expInfo(byte[] info, StringBuffer titleBuffer, StringBuffer contentBuffer) {
        Log.d(tag, "------------expinfo dump the gb2312 string buff---------------");
        dumpByteArray(info);
        String s = getGb2312String(info);
        byte buff[] = s.getBytes();
        Log.d(tag, "------------expinfo dump the utf-8 string buff---------------");
        dumpByteArray(buff);
        Log.d(tag, "expinfo original str="+(new String(buff)));

        // 取出标题
        String content = "";
        int firstEmptyCharIdx = s.indexOf('\0');
        if(firstEmptyCharIdx!=-1)
        {
            titleBuffer.append(s.substring(0, firstEmptyCharIdx));
            // 取出内容
            content = s.substring(firstEmptyCharIdx+2, s.length());
        }else
        {
            titleBuffer.append("ERR_FORMAT");
            // 取出内容
            content = "";
        }
        Log.d(tag, "content="+content);
        content = content.replaceAll("@.*?@","");
        content = content.replace('|', '\n');
        contentBuffer.append(content);
    }

    class WinMessage {
        int what;
        int wparam;
        int lparam;
        public WinMessage(int what, int wp, int lp) {
            this.what = what;
            this.wparam = wp;
            this.lparam = lp;
        }
    }

    class NativeWinMessageHandler extends Handler {
        @Override
        public void handleMessage(Message m) {
            WinMessage wm = (WinMessage)(m.obj);
            nativeHandleWinMessage(wm.what, wm.wparam, wm.lparam);

            //Un-block the dll thread
            synchronized (mMsgSynchonizer) {
                mMsgSynchonizer.notify();
            }
        }
    }

    public void sendKeyEvent(int code) {
        synchronized (mUserSelection) {
            mUserSelection.setKeyCode(code);
            mUserSelection.notify();
        }
    }

    OnItemClickListener mMenuOnItemClickListener = new OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View v, int position, long id)
        {
            Log.d(tag, "Click on menu id = "+position);
            TextView tvText = (TextView)v.findViewById(R.id.ads_menu_item_text);
            TextView tvId= (TextView)v.findViewById(R.id.ads_menu_item_id);
            Log.i("BUG", "TEXT:"+tvText.getText().toString()+" ID:"+tvId.getText().toString());
            int pos = Integer.parseInt(tvId.getText().toString());
            nativeHandleWinMessage(MyApplication.WM_ON_MENU_ITEM_CLICK, pos, pos);
            sendKeyEvent(pos);
        }
    };

    private boolean connectBluetooth()
    {
        findViewById(R.id.connectStatusText).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.connectStatusText)).setText("正在连接...");

        bluetoothUtil = new BluetoothUtil(this, uiHandler, "ADS;JBM-242");
        if(!bluetoothUtil.enableBluetooth())
        {  	Toast.makeText(this, R.string.bt_nofound, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

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
                case WM_CONNECT_BLUETOOTH:
                    if(!connectBluetooth()) {
                        MyAlertDialog.showAlert(WorkingActivity.this, R.string.bt_connect_fail, R.string.alert, MyAlertDialog.BUTTON_STYLE_OK, new Handler());
                        hideProgressDialog();
                        findViewById(R.id.connectStatusText).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.connectStatusText)).setText(R.string.bt_fail_tip);

                        if(bluetoothUtil != null) {
                            bluetoothUtil.closeBluetooth();
                            bluetoothUtil.disableBluetooth();
                        }
//					setResult(RESCODE_BLUETOOTH_CONNECT_FAIL);
//					finish();
                    }
                    break;
                case BluetoothUtil.WM_BLUETOOTH_DEVICE_OK:
                    Log.i(MyApplication.LOG_TAG, "WM_DO_RECV_THREAD 3");
                    hideProgressDialog();
                    startDll();
                    break;
                case BluetoothUtil.WM_BLUETOOTH_ERROR:
                    Log.i(MyApplication.LOG_TAG, "WM_DO_ERROR 5");
                    break;
                case BluetoothUtil.WM_BLUETOOTH_CONNECT_FAIL:
                    MyAlertDialog.showAlert(WorkingActivity.this, R.string.bt_connect_fail, R.string.alert, MyAlertDialog.BUTTON_STYLE_OK, new Handler());
                    hideProgressDialog();
                    findViewById(R.id.connectStatusText).setVisibility(View.VISIBLE);
                    ((TextView)findViewById(R.id.connectStatusText)).setText(R.string.bt_fail_tip);

                    if(bluetoothUtil != null) {
                        bluetoothUtil.closeBluetooth();
                        bluetoothUtil.disableBluetooth();
                    }
//				setResult(RESCODE_BLUETOOTH_CONNECT_FAIL);
//				finish();
                    break;
                case BluetoothUtil.WM_BLUETOOTH_CONNECT_OVERTIME:
                    sendEmptyMessage(RESCODE_BLUETOOTH_CONNECT_FAIL);
                    break;
            }
        }
    };



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == BluetoothUtil.REQCODE_BLUETOOTH_SETTING)
        {
            // 验证配对
            if(!bluetoothUtil.pairBluetooth())
            {
                Log.i("BUG", "PairBluetooth fail");
                setResult(RESCODE_BLUETOOTH_PAIR_FAIL);
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    static class DTCItem {
        String code = "N/A";
        String desc = "N/A";
        String state = "N/A";
        private DTCItem() {}
        public static DTCItem createFromBuff(byte buff[]) {
            dumpByteArray(buff);
            DTCItem item = new DTCItem();
            String s = getGb2312String(buff);
            int len = s.length();

            int codeStartIdx = s.indexOf(DTC_FLAG);
            int codeEndIdx = s.indexOf(DTC_FLAG, codeStartIdx+1);

            if(codeStartIdx < codeEndIdx) {
                item.code = s.substring(codeStartIdx, codeEndIdx).trim();
            }

            int descAndStateSpliter = s.indexOf('|', codeEndIdx+1);
            if(descAndStateSpliter == -1) { //Does not contains '|'
                if(codeEndIdx < len) {
                    item.desc = s.substring(codeEndIdx+1, len).trim().replaceAll("@.*?@","");
                }
            } else {
                if(codeEndIdx < descAndStateSpliter) {
                    item.desc = s.substring(codeEndIdx+1, descAndStateSpliter).trim();
                }
                if(descAndStateSpliter < len) {
                    item.state = s.substring(descAndStateSpliter+1, len).trim();
                    item.state = item.state.replace('|', '\n');
                }
            }
            return item;
        }
    }

    class DtcAdapter extends BasicAdapter {
        ArrayList<DTCItem> dtcItems = new ArrayList<DTCItem>();
        @Override
        public int getCount() {
            return dtcItems.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DtcItemView v = (DtcItemView)convertView;
            if(v == null) {
                v = new DtcItemView(WorkingActivity.this);
            }
            DTCItem item = dtcItems.get(position);
            v.setContent(item.code, item.desc, item.state);
            return v;
        }
    }

    OnClickListener mPopMenuButtonsOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.ads_pop_menu_btn_start) {
                sendKeyEvent(mPopMenu.getSelectedItemId());

            } else if (i == R.id.ads_pop_menu_btn_exit) {
                sendKeyEvent(MyApplication.WM_KEY_EXIT);
                dismissPopMenu();

            }

        }
    };

    static class CdsItem {
        int id;
        String name;
        String value;
        String max;
        String min;
        String unit;
        String format;

        private CdsItem(){};

        public static CdsItem create(int id, String name, String max, String min, String unit, String format) {
            CdsItem item = new CdsItem();
            item.id = id;
            item.name = name;
            item.max = max;
            item.min = min;
            item.unit = unit;
            item.format = format;
            return item;
        }
    }

    class CdsAdapter extends BasicAdapter
    {
        ArrayList<CdsItem> cdsItems = new ArrayList<CdsItem>();

        @Override
        public int getCount() {
            return cdsItems.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CdsItemView v = (CdsItemView) convertView;
            if(v == null)
            {
                v = new CdsItemView(WorkingActivity.this);
            }
            CdsItem item = cdsItems.get(position);
            v.setName(item.name);
            if(mCurrentWindowView == WIN_CDS)
            {
                v.showValue(item.value);
            } else if (mCurrentWindowView == WIN_SELECT_CDS)
            {
                v.showCheckBox();
            }
            return v;
        }

    }

}
