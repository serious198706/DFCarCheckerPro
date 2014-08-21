package com.df.app.carCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.R;
import com.df.app.service.util.AppCommon;
import com.df.library.asyncTask.GetIssueItemsTask;
import com.df.app.service.DF3000Service;
import com.df.app.service.DF5000Service;
import com.df.app.service.Device3000ListDialog;
import com.df.library.entries.CarSettings;
import com.df.library.entries.Measurement;
import com.df.app.service.Device5000ListDialog;
import com.df.library.util.MyAlertDialog;
import com.df.library.util.MyScrollView;
import com.df.library.util.Common;
import com.df.library.util.Helper;
import com.xinque.android.serial.driver.UsbSerialDriver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.df.library.util.Helper.getEditViewText;
import static com.df.library.util.Helper.getSpinnerSelectedText;
import static com.df.library.util.Helper.setEditViewText;
import static com.df.library.util.Helper.setSpinnerSelectionWithIndex;
import static com.df.library.util.Helper.setTextView;
import static com.df.library.util.Helper.showView;

/**
 * Created by 岩 on 13-12-20.
 *
 * 数据采集模块
 */
public class CollectDataLayout extends LinearLayout {
    private View rootView;
    private OnGetIssueData mCallback;

    private boolean isOpen; // 是否打开设备

    // 设备类型
    private int currentDeviceType = Common.DF3000;

    private Button startButton;
    private Button searchDeviceButton;
    private Button getIssueButton;

    private ImageView carImg;
    private ImageView collapseBar;

    MyScrollView scrollView;

    // 是否已经采集数据
    private boolean dataCollected = false;
    public static boolean hasRequestCmd;
    public static String requestCmd;

    // 是否已连接的标志符
    private boolean isConnected;

    // 线程句柄
    private Thread t = null;

    // 是否已经获取到序列号
    private boolean hasSerial;

    private String[] materialArray = new String[29];

    // 覆盖件map
    // 1. EditText的id
    // 2. CheckBox的id
    // 3. 序号
    // 4. 名称
    private static Map<int[], String> overIdMap ;
    static {
        overIdMap = new HashMap<int[], String>();
        overIdMap.put(new int[]{R.id.L_edit,    R.id.L_N,   1,  9},     "L");
        overIdMap.put(new int[]{R.id.M_edit,    R.id.M_N,   2,  9},     "M");
        overIdMap.put(new int[]{R.id.D_edit,    R.id.D_N,   3,  9},     "D");
        overIdMap.put(new int[]{R.id.LA_edit,   R.id.LA_N,  4,  3},     "LA");
        overIdMap.put(new int[]{R.id.E_edit,    R.id.E_N,   5,  9},     "E");
        overIdMap.put(new int[]{R.id.LB_edit,   R.id.LB_N,  6,  6},     "LB");
        overIdMap.put(new int[]{R.id.F_edit,    R.id.F_N,   7,  9},     "F");
        overIdMap.put(new int[]{R.id.LC_edit,   R.id.LC_N,  8,  3},     "LC");
        overIdMap.put(new int[]{R.id.G_edit,    R.id.G_N,   9,  9},     "G");
        overIdMap.put(new int[]{R.id.H_edit,    R.id.H_N,   10, 9},    "H");
        overIdMap.put(new int[]{R.id.I_edit,    R.id.I_N,   11, 9},    "I");
        overIdMap.put(new int[]{R.id.RC_edit,   R.id.RC_N,  12, 3},    "RC");
        overIdMap.put(new int[]{R.id.J_edit,    R.id.J_N,   13, 9},    "J");
        overIdMap.put(new int[]{R.id.RB_edit,   R.id.RB_N,  14, 6},    "RB");
        overIdMap.put(new int[]{R.id.K_edit,    R.id.K_N,   15, 9},    "K");
        overIdMap.put(new int[]{R.id.RA_edit,   R.id.RA_N,  16, 3},    "RA");
        overIdMap.put(new int[]{R.id.N_edit,    R.id.N_N,   17, 9},    "N");
    }

    // 所有edit id
    private static int[] overIds = {
            R.id.L_edit,
            R.id.M_edit,
            R.id.D_edit,
            R.id.LA_edit,
            R.id.E_edit,
            R.id.LB_edit,
            R.id.F_edit,
            R.id.LC_edit,
            R.id.G_edit,
            R.id.H_edit,
            R.id.I_edit,
            R.id.RC_edit,
            R.id.J_edit,
            R.id.RB_edit,
            R.id.K_edit,
            R.id.RA_edit,
            R.id.N_edit,
            R.id.M1_edit,
            R.id.D1_edit,
            R.id.D2_edit,
            R.id.M3_edit,
            R.id.M5_edit,
            R.id.F1_edit,
            R.id.M2_edit,
            R.id.L1_edit,
            R.id.L2_edit,
            R.id.M4_edit,
            R.id.H1_edit,
            R.id.J1_edit
    };

    // 加强件控件map
    // 1. EditText的id
    // 2. 此部位的名称
    private static SparseArray<String> enhanceIdMap;
    static {
        enhanceIdMap = new SparseArray<String>();
        enhanceIdMap.put(R.id.M1_edit, "M1");
        enhanceIdMap.put(R.id.D1_edit, "D1");
        enhanceIdMap.put(R.id.D2_edit, "D2");
        enhanceIdMap.put(R.id.M3_edit, "M3");
        enhanceIdMap.put(R.id.M5_edit, "M5");
        enhanceIdMap.put(R.id.F1_edit, "F1");
        enhanceIdMap.put(R.id.M2_edit, "M2");
        enhanceIdMap.put(R.id.L1_edit, "L1");
        enhanceIdMap.put(R.id.L2_edit, "L2");
        enhanceIdMap.put(R.id.M4_edit, "M4");
        enhanceIdMap.put(R.id.H1_edit, "H1");
        enhanceIdMap.put(R.id.J1_edit, "J1");
    }

    // 蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;

    // USB驱动
    private static UsbSerialDriver sDriver = null;

    // 与设备进行交互的服务
    private DF3000Service mDF3000Service = null;
    private DF5000Service mDF5000Service = null;

    // 选择设备的界面
    private Device3000ListDialog df3000Dialog;
    private Device5000ListDialog df5000Dialog;

    // 连接上的设备，包含设备的所有信息
    private static BluetoothDevice device;
    private String deviceSerial = "";

    public CollectDataLayout(Context context, OnGetIssueData listener) {
        super(context);
        init(context);
        this.mCallback = listener;
    }

    public CollectDataLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CollectDataLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(final Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.collect_data_layout, this);

        for(int i = 0; i < materialArray.length; i++) {
            materialArray[i] = "";
        }

        // 查找设备按钮
        searchDeviceButton = (Button) rootView.findViewById(R.id.searchDevices_button);
        searchDeviceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String deviceType = getSpinnerSelectedText(rootView, R.id.device_type_spinner);

                if(deviceType.equals("DF3000")) {
                    currentDeviceType = Common.DF3000;
                    df3000Dialog = new Device3000ListDialog(rootView.getContext(), new Device3000ListDialog.OnSelectDeviceFinished() {
                        @Override
                        public void onFinished(UsbSerialDriver sDriver, Long serialNumber) {
                            CollectDataLayout.sDriver = sDriver;
                            showView(rootView, R.id.start_button, true);
                            deviceSerial = Long.toString(serialNumber);
                            Toast.makeText(context, "已连接" + deviceSerial, Toast.LENGTH_SHORT).show();
                        }
                    });
                    df3000Dialog.show();
                } else {
                    currentDeviceType = Common.DF5000;
                    df5000Dialog = new Device5000ListDialog((Activity) rootView.getContext(), rootView.getContext(), new Device5000ListDialog.OnSelectDeviceFinished() {
                        @Override
                        public void onFinished(String address) {

                            // 使用默认蓝牙适配器
                            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                            // 初始化Socket
                            if (mBluetoothAdapter == null) {
                                Toast.makeText(rootView.getContext(), R.string.not_connected, Toast.LENGTH_LONG).show();
                                showView(rootView, R.id.start_button, false);
                            }

                            // 根据设备地址连接设备
                            device = mBluetoothAdapter.getRemoteDevice(address);
                            if(mDF5000Service == null) {
                                mDF5000Service = new DF5000Service(getContext(), df5000Handler);
                            }

                            mDF5000Service.connect(device);
                        }
                    });
                    df5000Dialog.show();
                }
            }
        });

        // 开始采集数据按钮
        startButton = (Button) rootView.findViewById(R.id.start_button);
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dataCollected) {
                    View view1 = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.popup_layout, null);
                    TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
                    TextView content = new TextView(view1.getContext());
                    content.setText(R.string.reCollect_alert);
                    content.setTextSize(20f);
                    contentArea.addView(content);

                    setTextView(view1, R.id.title, getResources().getString(R.string.alert));

                    AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                            .setView(view1)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startCollect();
                                }
                            }).create();

                    dialog.show();
                } else {
                    startCollect();
                }
            }
        });

        // 当checkbox选中时，将对应的edittext特殊化
        for(final int[] n : overIdMap.keySet()) {
            CheckBox checkBox = (CheckBox)findViewById(n[1]);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b) {
                        findViewById(n[0]).setAlpha(0.3f);
                    } else {
                        findViewById(n[0]).setAlpha(1.0f);
                    }
                }
            });
        }

        // 初始化基准点图片
        carImg = (ImageView)findViewById(R.id.standard_image);
        Bitmap bitmap = BitmapFactory.decodeFile(AppCommon.utilDirectory + "L");
        carImg.setImageBitmap(bitmap);
        bitmap = null;

        // 隐藏图片的bar
        collapseBar = (ImageView)findViewById(R.id.collapseBar);
        collapseBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showImage();
            }
        });

        // 显示阴影
        scrollView = (MyScrollView) findViewById(R.id.root);
        scrollView.setListener(new MyScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy) {
                showShadow(scrollView.getScrollY() > 5);
            }
        });

        // 点击无法测量CheckBox时
        CheckBox cannotMeasureB = (CheckBox)findViewById(R.id.bn);
        cannotMeasureB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                findViewById(R.id.LB_edit).setVisibility(b ? INVISIBLE : VISIBLE);
                findViewById(R.id.LB_N).setVisibility(b ? INVISIBLE : VISIBLE);
                findViewById(R.id.RB_edit).setVisibility(b ? INVISIBLE : VISIBLE);
                findViewById(R.id.RB_N).setVisibility(b ? INVISIBLE : VISIBLE);
            }
        });

        // 初始化所有的EditText控件
        initEdits();

        // 填写测试数据
        Button dummyRecordButton = (Button)findViewById(R.id.dummyRecordButton);
        dummyRecordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                fillInDummyData();
            }
        });

        // 生产环境下，将录入测试数据的按钮干掉
        if(Common.getEnvironment() == Common.PRODUCT_VERSION)
            dummyRecordButton.setVisibility(GONE);

        getIssueButton = (Button)findViewById(R.id.getIssueButton);
        getIssueButton.setVisibility(GONE);
        getIssueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getIssueItems();
            }
        });

        // 注册设备拔出时的broadcastreceiver
        context.registerReceiver(broadcastReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "失去与设备的连接！", Toast.LENGTH_SHORT).show();
            startButton.setVisibility(GONE);
            getIssueButton.setVisibility(GONE);
        }
    };

    /**
     * 开始采集数据
     */
    private void startCollect() {
            switch (currentDeviceType) {
                case Common.DF3000:
                    collectData();
                    break;
                case Common.DF5000:
                    collectData(requestCmd);
                    break;
            }

            startButton.setText(R.string.collecting);
            updateButton(false);

            // 清空已填写的数据
            clearData();
    }

    /**
     * 显示阴影
     */
    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    /**
     * 显示或隐藏图片
     */
    private void showImage() {
        Bitmap bitmap = BitmapFactory.decodeResource(rootView.getContext().getResources(), carImg.getVisibility() == VISIBLE ?
                R.drawable.expand : R.drawable.collapse);
        collapseBar.setImageBitmap(bitmap);
        carImg.setVisibility(carImg.getVisibility() == VISIBLE ? GONE : VISIBLE);
    }

    /**
     * 显示或隐藏图片（连接设备后）
     * @param b
     */
    private void showImage(boolean b) {
        Bitmap bitmap = BitmapFactory.decodeResource(rootView.getContext().getResources(), R.drawable.expand);
        collapseBar.setImageBitmap(bitmap);
        carImg.setVisibility(GONE);
    }

    /**
     * 更新按钮状态
     * @param status
     */
    private void updateButton(boolean status) {
        startButton.setEnabled(status);
        searchDeviceButton.setEnabled(status);
        getIssueButton.setEnabled(status);
    }

    /**
     * 填入测试数据
     */
    private void  fillInDummyData() {
        for(final int[] n : overIdMap.keySet()) {
            String data = "";
            for(int i = 0; i < 9; i++) {
                int randomNum = 100 + (int)(Math.random() * 300);
                data += randomNum + ",";
            }

            data = data.substring(0, data.length() - 1);

            EditText editText = (EditText) rootView.findViewById(n[0]);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String data = editable.toString();
                    int length = data.split(",").length;

                    CheckBox checkBox = (CheckBox)findViewById(n[1]);
                    checkBox.setText("(" + Integer.toString(length) + ")");
                }
            });

            editText.setText(data);

            materialArray[n[2]] = "";
        }

        View view1 = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(R.string.collect_finished);
        content.setTextSize(20f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, getResources().getString(R.string.alert));

        AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                .setView(view1)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getIssueButton.setVisibility(VISIBLE);
                    }
                }).create();

        dialog.show();
    }

    /**
     * 清空已填入的数据
     */
    private void clearData() {
        for(int[] n : overIdMap.keySet()) {
            EditText editText = (EditText) rootView.findViewById(n[0]);
            editText.setText("");

            CheckBox checkBox = (CheckBox) rootView.findViewById(n[1]);
            checkBox.setChecked(false);
            checkBox.setText("(0)");
        }

        for(int i = 0; i < enhanceIdMap.size(); i++) {
            EditText editText = (EditText) rootView.findViewById(enhanceIdMap.keyAt(i));
            editText.setText("");
        }
    }

    /**
     * 初始化所有EditText
     */
    private void initEdits() {
        for(final int[] n : overIdMap.keySet()) {

            EditText editText = (EditText) rootView.findViewById(n[0]);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String data = editable.toString();

                    if(!data.equals("")) {
                        int length = data.split(",").length;

                        CheckBox checkBox = (CheckBox) findViewById(n[1]);
                        checkBox.setText("(" + Integer.toString(length) + ")");
                    }
                }
            });

            // 点击某个EditText时，显示对应的基准点图片
            editText.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if(!b) {
                        return;
                    }

                    int id = view.getId();

                    for(int[] n : overIdMap.keySet()) {
                        if(id == n[0]) {
                            Bitmap bitmap = BitmapFactory.decodeFile(AppCommon.utilDirectory  + overIdMap.get(n));
                            carImg.setImageBitmap(bitmap);
                        }
                    }
                }
            });
        }
    }

    /**
     * DF3000采集线程
     */
    Runnable df3000Runnable = new Runnable() {
        @Override
        public void run() {
            isOpen = mDF3000Service.connection();

            // 当接收到数据时的回调方法
            com.df.app.service.DF3000Service.OnReceiveData onReceiveData = new DF3000Service.OnReceiveData() {
                @Override
                public void onReceiveData(final Measurement measurement) {
                    final String values = measurement.toValueString();

                    ((Activity)getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(measurement.getBlockId() <= 17) {
                                setEditViewText(rootView,
                                        overIds[measurement.getBlockId() - 1], values);
                            } else {
                                setEditViewText(rootView,
                                        enhanceIdMap.keyAt(measurement.getBlockId() - 18), values);
                            }

                            materialArray[measurement.getBlockId() - 1] = "";

                            // 当数据全部采集完毕后，弹出完成提示
                            if(measurement.getBlockId() == 29) {
                                MyAlertDialog.showAlert(getContext(), R.string.collect_finished,
                                        R.string.alert, MyAlertDialog.BUTTON_STYLE_OK,
                                        new Handler(new Handler.Callback() {
                                            @Override
                                            public boolean handleMessage(Message message) {
                                                getIssueButton.setVisibility(VISIBLE);

                                                startButton.setText("重新获取数据");
                                                updateButton(true);

                                                dataCollected = true;

                                                return true;
                                            }
                                        }));
                            }
                        }
                    });
                }
            };

            // 如果没有连接设备，则返回
            if(!isOpen) {
                return;
            } else {
                mDF3000Service.startCollect(onReceiveData);
            }
        }
    };

    /**
     * 采集数据（DF3000）
     */
    private void collectData() {
        if (sDriver == null) {
            Log.d(AppCommon.TAG, "连接设备失败！！");
        }
        else {
            mDF3000Service = com.df.app.service.DF3000Service.instance(sDriver);

            Thread collectThread = new Thread(df3000Runnable);
            collectThread.start();
        }
    }

    /**
     * 初始化BluetoothService
     */
    public void setupBluetoothService() {
        mDF5000Service = new DF5000Service(rootView.getContext(), df5000Handler);
        df5000Dialog.doDiscovery();
        df5000Dialog.showPairedDevices();
    }

    /**
     * 停止BluetoothService
     */
    public void stopBluetoothService() {
        df5000Dialog.dismiss();
    }

    /**
     * 采集数据响应（DF5000）
     */
    private final Handler df5000Handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 状态发生改变，比如失去连接
                case Common.MESSAGE_STATE_CHANGE:

                    Log.d(AppCommon.TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);

                    switch (msg.arg1) {
                        // 已连接
                        case com.df.app.service.DF5000Service.STATE_CONNECTED:
                            showView(rootView, R.id.start_button, true);
                            showImage(true);
                            isConnected = true;
                            break;
                        // 正在连接
                        case com.df.app.service.DF5000Service.STATE_CONNECTING:
                            break;
                        // 监听中
                        case com.df.app.service.DF5000Service.STATE_LISTEN:
                            break;
                        // 待机
                        case com.df.app.service.DF5000Service.STATE_NONE:
                            break;
                        // 失去连接
                        case com.df.app.service.DF5000Service.STATE_CONNECTION_LOST:
                            Toast.makeText(rootView.getContext(), R.string.lost_connection, Toast.LENGTH_LONG).show();
                            startButton.setVisibility(GONE);
                            updateButton(true);
                            startButton.setText("开始获取数据");
                            isConnected = false;
                            // 关闭蓝牙
                            mDF5000Service.stop();
                            break;
                    }
                    break;
                // 写数据
                case Common.MESSAGE_WRITE:
                    break;
                // 读数据
                case Common.MESSAGE_READ:
                    String array[] = (String[]) msg.obj;

                    // 将接收的数据填入EditText
                    if (!TextUtils.isEmpty(array[1]) && !TextUtils.isEmpty(array[0])) {
                        int index = Integer.parseInt(array[0], 16);
                        setEditViewText(rootView, overIds[index - 1], array[1]);
                        materialArray[index - 1] = array[3];
                    }

                    break;
                // 数据读取完毕
                case Common.MESSAGE_READ_OVER:
                    View view1 = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.popup_layout, null);
                    TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
                    TextView content = new TextView(view1.getContext());
                    content.setText(R.string.collect_finished);
                    content.setTextSize(20f);
                    contentArea.addView(content);

                    setTextView(view1, R.id.title, getResources().getString(R.string.alert));

                    AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                            .setView(view1)

                                    // 当点击弹出的确定按钮后，获取问题查勘的内容
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getIssueButton.setVisibility(VISIBLE);
                                    startButton.setText("重新获取数据");
                                    updateButton(true);

                                    dataCollected = true;
                                }
                            })
                            .setCancelable(false)
                            .create();

                    dialog.show();
                    break;
                // 获取设备名称
                case Common.MESSAGE_DEVICE_NAME:
                    // 保存设备的名称
                    String mConnectedDeviceName = msg.getData().getString("device_name");
                    Toast.makeText(rootView.getContext(), "已连接 " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    deviceSerial = mConnectedDeviceName;

                    if (!isConnected) {
                        if (t == null) {
                            t = new Thread() {
                                public void run() {
                                    try {
                                        sleep(700);
                                        Message msg = Message.obtain();
                                        msg.what = Common.MESSAGE_GET_SERIAL;
                                        sendMessage(msg);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            t.start();
                        }
                    }

                    isConnected = true;
                    break;
                // 显示Toast信息
                case Common.MESSAGE_TOAST:
                    Toast.makeText(rootView.getContext(), msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();

                    hasRequestCmd = false;
                    hasSerial = false;
                    t = null;
                    isConnected = false;

                    showView(rootView, R.id.start_button, false);
                    break;
                // 获取序列号
                case Common.MESSAGE_GET_SERIAL:
                    if (!hasSerial) {
                        collectData(Common.CMD_GET_SERIAL);
                        hasSerial = true;
                    }
                    // 已经获取了序列号
                    else {
                        showView(rootView, R.id.start_button, true);
                    }

                    break;
            }
        }
    };


    /**
     * 采集数据（DF5000）
     */
    private void collectData(String message) {
        // 是否已经连接到设备
        if (mDF5000Service.getState() != com.df.app.service.DF5000Service.STATE_CONNECTED) {
            Toast.makeText(rootView.getContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // 发送数据
        if (!TextUtils.isEmpty(message) && message.length() > 0) {
            byte[] send = Helper.hexStr2Bytes(message);

            // 向设备写数据
            mDF5000Service.write(send);
        }
    }

    /**
     * 生成数据采集JSON串
     */
    public JSONObject generateJSONObject(boolean b) throws JSONException {
        JSONObject data = new JSONObject();

        // 覆盖件数据
        JSONObject overlap = new JSONObject();

        for(int[] n : overIdMap.keySet()) {
            String value;

            if(materialArray[n[2]].equals("Fe") && currentDeviceType == Common.DF5000) {
                value = handleFeValues(getEditViewText(rootView, n[0]));
            } else {
                value = getEditViewText(rootView, n[0]);
            }

            overlap.put(overIdMap.get(n), value);
        }

        // 保存时，要将每个部位的材料保存
        // 提交时，每个部位的材料不需要加入
        if(!b) {
            String materialString = "";

            for(String material : materialArray) {
                materialString += material;
                materialString += ",";
            }

            materialString = materialString.substring(0, materialString.length() - 1);

            data.put("materials", materialString);
        }

        // 当B柱无法测量时
        CheckBox bn = (CheckBox)findViewById(R.id.bn);
        if(bn.isChecked()) {
            overlap.put("LB", "");
            overlap.put("RB", "");
        }

        // 加强件数据
        JSONObject enhance = new JSONObject();

        for(int i = 0; i < enhanceIdMap.size(); i++) {
            enhance.put(enhanceIdMap.valueAt(i), getEditViewText(rootView, enhanceIdMap.keyAt(i)));
        }

        // 选项
        JSONObject options = new JSONObject();

        // 无法测量的部位（打勾的部位）
        String cannotMeasure = "";
        for(int[] n : overIdMap.keySet()) {
            CheckBox checkBox = (CheckBox)findViewById(n[1]);
            if(checkBox.isChecked()) {
                cannotMeasure += overIdMap.get(n);
                cannotMeasure += ",";
            }
        }

        if(cannotMeasure.length() > 0) {
            cannotMeasure = cannotMeasure.substring(0, cannotMeasure.length() - 1);
        }

        options.put("cannotMeasure", cannotMeasure);

        // 隐藏的部位
        String hide = ((CheckBox)findViewById(R.id.a)).isChecked() ? "LA,RA" : "";
        hide += ((CheckBox)findViewById(R.id.b)).isChecked() ? (hide.equals("") ? "" : ",") + "LB,RB" : "" ;

        options.put("hide", hide);

        // 设备信息
        JSONObject device = new JSONObject();

        device.put("type", getSpinnerSelectedText(rootView, R.id.device_type_spinner));
        device.put("serial", deviceSerial);

        data.put("overlap", overlap);
        data.put("enhance", enhance);
        data.put("options", options);
        data.put("device", device);

        return data;
    }

    /**
     * 对df5000产生的数据进行铁的数据补偿
     *
     * 原因：df5000对铁的测量数据比df3000的测量数据大出约14%
     *      但如果高出太多，则需要有一个限定值，目前为46。
     * @param value 原数据
     * @return 新数据
     */
    private String handleFeValues(String value) {
        String[] array = value.split(",");
        String result = "";

        for(String str : array) {
            double sValue = Double.parseDouble(str);

            sValue = (sValue - sValue / 1.14) > 46.0 ? sValue - 46.0 : sValue / 1.14;

            result += Integer.toString(((int) sValue));
            result += ",";
        }

        if(result.length() >= 2) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    /**
     * 检查每一个域的数量是否足够
     */
    private String checkFields() {
        for(int[] n : overIdMap.keySet()) {
            CheckBox checkBox = (CheckBox)findViewById(n[1]);
            String numString = checkBox.getText().toString();

            int num = Integer.parseInt(numString.substring(1, numString.length() - 1));

            if(num < n[3]) {
                String result = overIdMap.get(n);
                return result;
            }
        }

        return "";
    }

    /**
     * 获取问题查勘的内容
     */
    private void getIssueItems() {
        String check;

        // 如果为正式环境，则要进行项的检查
        if(Common.getEnvironment() == Common.PRODUCT_VERSION) {
            check = checkFields();
        } else {
            check = "";
        }

        if(!check.equals("")) {
            int resId = getResources().getIdentifier(check, "string", rootView.getContext().getPackageName());
            String name = getResources().getString(resId);

            Toast.makeText(rootView.getContext(), name + "采集数量不足！", Toast.LENGTH_SHORT).show();
        } else {
            // 启动获取issue数据线程
            try {
                GetIssueItemsTask getIssueItemsTask = new GetIssueItemsTask(rootView.getContext(),
                        Integer.parseInt(CarSettings.getInstance().getSeries().id),
                        VehicleInfoLayout.getExteriorColor(), VehicleInfoLayout.getBuiltDate(),
                        generateJSONObject(true), new GetIssueItemsTask.OnGetIssueItemsFinished() {
                    @Override
                    public void onFinish(String result, ProgressDialog progressDialog) {
                        mCallback.showContent();
                        mCallback.updateUi(result, progressDialog);

                        startButton.setText(R.string.start);
                        updateButton(true);
                    }

                    @Override
                    public void onFailed(String error, ProgressDialog progressDialog) {
                        progressDialog.dismiss();
                        Toast.makeText(rootView.getContext(), "获取问题失败: " + error, Toast.LENGTH_SHORT).show();
                        Log.d("DFCarChecker", "获取问题失败: " + error);
                    }
                });
                getIssueItemsTask.execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     */
    public void fillInData(JSONObject data) {
        try {
            JSONObject overlap = data.getJSONObject("overlap");
            JSONObject enhance = data.getJSONObject("enhance");
            JSONObject options = data.getJSONObject("options");
            JSONObject device = data.getJSONObject("device");

            String cannotMeasure = options.getString("cannotMeasure");

            // 覆盖件填充数据
            for(int[] n : overIdMap.keySet()) {
                String s = overlap.getString(overIdMap.get(n));
                setEditViewText(rootView, n[0], s);

                // 如果无法测量部位里含有该部位，则要置相应的checkbox为checked
                if(cannotMeasure.contains(overIdMap.get(n))) {
                    CheckBox checkBox = (CheckBox)findViewById(n[1]);
                    checkBox.setChecked(true);
                }
            }

            // 加强件填充数据
            for(int i = 0; i < enhanceIdMap.size(); i++) {
                String s = enhance.get(enhanceIdMap.valueAt(i)) == JSONObject.NULL ? "" : enhance.getString(enhanceIdMap.valueAt(i));
                setEditViewText(rootView, enhanceIdMap.keyAt(i), s);
            }

            // 各部位的材料
            if(data.has("materials")) {
                String[] temp = data.getString("materials").split(",");
                System.arraycopy(temp, 0, materialArray, 0, temp.length);
            }

            // 隐藏部位
            String hide = options.getString("hide");

            // 如果有LA，则置a为checked
            ((CheckBox)findViewById(R.id.a)).setChecked(hide.contains("LA"));

            // 如果有LB，则置b为checked
            ((CheckBox)findViewById(R.id.b)).setChecked(hide.contains("LB"));

            // 如果其他字段都有,但是LB与RB没有，则置bn为checked
            if(!getEditViewText(rootView, R.id.L_edit).equals("") &&
                    getEditViewText(rootView, R.id.LB_edit).equals("") &&
                    getEditViewText(rootView, R.id.RB_edit).equals("")) {
                CheckBox checkBox = (CheckBox)findViewById(R.id.bn);
                checkBox.setChecked(true);
            }

            setSpinnerSelectionWithIndex(rootView, R.id.device_type_spinner,
                    device.getString("type").equals("DF3000") ? 0 : 1);
            currentDeviceType = device.getString("type").equals("DF3000") ? Common.DF3000 : Common.DF5000;

            deviceSerial = device.getString("serial");
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注销broadReceiver
     */
    public void unRegisterBroadReceiver() {
        try {
            getContext().unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * AccidentCheckLayout来实现此接口，当采集完数据后，需要显示并更新问题查勘和查勘结果页面
     */
    public interface OnGetIssueData {
        public void showContent();
        public void updateUi(String result, ProgressDialog progressDialog);
    }
}
