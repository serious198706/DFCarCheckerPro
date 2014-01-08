package com.df.app.CarCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.df.app.R;
import com.df.app.service.Checker;
import com.df.app.service.DeviceListDialog;
import com.df.app.service.Measurement;
import com.df.app.service.MyScrollView;
import com.df.app.util.Common;
import com.xinque.android.serial.driver.UsbSerialDriver;

import java.util.HashMap;
import java.util.Map;

import static com.df.app.util.Helper.setEditViewText;
import static com.df.app.util.Helper.showView;

/**
 * Created by 岩 on 13-12-20.
 */
public class CollectDataLayout extends LinearLayout {
    private View rootView;
    private OnGetIssueData mCallback;

    private static UsbSerialDriver sDriver = null;
    private Checker checker = null;
    private boolean isOpen; // 是否打开设备

    private Button startButton;

    private ImageView carImg;

    MyScrollView scrollView;

    private static Map<int[], String> overIdMap ;
    static
    {
        overIdMap = new HashMap<int[], String>();
        overIdMap.put(new int[]{R.id.L_edit,    R.id.L_N,   1},     "L");
        overIdMap.put(new int[]{R.id.M_edit,    R.id.M_N,   2},     "M");
        overIdMap.put(new int[]{R.id.D_edit,    R.id.D_N,   3},     "D");
        overIdMap.put(new int[]{R.id.LA_edit,   R.id.LA_N,  4},     "LA");
        overIdMap.put(new int[]{R.id.E_edit,    R.id.E_N,   5},     "E");
        overIdMap.put(new int[]{R.id.LB_edit,   R.id.LB_N,  6},     "LB");
        overIdMap.put(new int[]{R.id.F_edit,    R.id.F_N,   7},     "F");
        overIdMap.put(new int[]{R.id.LC_edit,   R.id.LC_N,  8},     "LC");
        overIdMap.put(new int[]{R.id.G_edit,    R.id.G_N,   9},     "G");
        overIdMap.put(new int[]{R.id.H_edit,    R.id.H_N,   10},    "H");
        overIdMap.put(new int[]{R.id.I_edit,    R.id.I_N,   11},    "I");
        overIdMap.put(new int[]{R.id.RC_edit,   R.id.RC_N,  12},    "RC");
        overIdMap.put(new int[]{R.id.J_edit,    R.id.J_N,   13},    "J");
        overIdMap.put(new int[]{R.id.RB_edit,   R.id.RB_N,  14},    "RB");
        overIdMap.put(new int[]{R.id.K_edit,    R.id.K_N,   15},    "K");
        overIdMap.put(new int[]{R.id.RA_edit,   R.id.RA_N,  16},    "RA");
        overIdMap.put(new int[]{R.id.N_edit,    R.id.N_N,   17},    "N");
    }

    // 数据采集的控件map
    // 1. EditText的id
    // 2. CheckBox的id


    private int[] enhanceIdMap = {
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

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.collect_data_layout, this);

        Button searchDeviceButton = (Button) rootView.findViewById(R.id.searchDevices_button);
        searchDeviceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                DeviceListDialog dialog = new DeviceListDialog(rootView.getContext());

                dialog.setDialogResult(new DeviceListDialog.OnMyDialogResult() {
                    @Override
                    public void onMyDialogResult(UsbSerialDriver sDriver) {
                        CollectDataLayout.sDriver = sDriver;
                        showView(rootView, R.id.start_button, true);
                    }
                });

                dialog.show();
            }
        });

        startButton = (Button) rootView.findViewById(R.id.start_button);
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                open();
                startButton.setText(R.string.collecting);
                startButton.setEnabled(false);

                clearData();
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

        carImg = (ImageView)findViewById(R.id.standard_image);

        initEdits();

        scrollView = (MyScrollView) findViewById(R.id.root);
        scrollView.setListener(new MyScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy) {
                if(scrollView.getScrollY() > 5) {
                    showShadow(true);
                } else {
                    showShadow(false);
                }
            }
        });
    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    // AccidentCheckLayout 必须实现此接口
    public interface OnGetIssueData {
        public void showContent();
        public void updateUi();
    }

    // 填入假数据
    private void fillInDummyData() {
        for(final int[] n : overIdMap.keySet()) {
            int randomNum1 = 100 + (int)(Math.random() * 300);
            int randomNum2 = 100 + (int)(Math.random() * 300);
            int randomNum3 = 100 + (int)(Math.random() * 300);

            String data = Integer.toString(randomNum1) + "," + Integer.toString(randomNum2) + "," +
                    Integer.toString(randomNum3);

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
        }

        AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                .setTitle(R.string.alert)
                .setMessage(R.string.collect_finished)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mCallback.showContent();
                        mCallback.updateUi();
                    }
                }).create();

        dialog.show();
    }

    private void clearData() {
        for(final int[] n : overIdMap.keySet()) {
            EditText editText = (EditText) rootView.findViewById(n[0]);
            editText.setText("");
        }
    }

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

            editText.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if(!b) {
                        return;
                    }

                    int id = view.getId();

                    for(int[] n : overIdMap.keySet()) {
                        if(id == n[0]) {
                            Bitmap bitmap = BitmapFactory.decodeFile(Common.utilDirectory  + overIdMap.get(n));
                            carImg.setImageBitmap(bitmap);
                        }
                    }
                }
            });
        }
    }

    private void open() {
        if (sDriver == null) {
            Log.d(Common.TAG, "连接设备失败！！");
        }
        else {
            checker = Checker.instance(sDriver);
            new Thread() {
                @Override
                public void run() {
                isOpen = checker.connection();

                if (isOpen) {
                    checker.measurement(new Checker.OnReceiveData() {
                        @Override
                        public void onReceiveData(final Measurement measurement) {
                            final String values = measurement.toValueString();

                            ((Activity)getContext()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(measurement.getBlockId() <= 17) {
                                        for(int[] n : overIdMap.keySet()) {
                                            if(n[2] == measurement.getBlockId()) {
                                                setEditViewText(rootView, n[0], values);
                                                scrollView.smoothScrollTo(0, findViewById(n[0]).getBottom());
                                            }
                                        }
                                    } else {
                                        setEditViewText(rootView,
                                                enhanceIdMap[measurement.getBlockId() - 18],
                                                values);
                                    }

                                    if(measurement.getBlockId() == 29) {
                                        AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                                                .setTitle(R.string.alert)
                                                .setMessage(R.string.collect_finished)
                                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        mCallback.showContent();
                                                        mCallback.updateUi();

                                                        startButton.setText(R.string.start);
                                                        startButton.setEnabled(true);
                                                    }
                                                }).create();

                                        dialog.show();
                                    }
                                }
                            });
                        }
                    });
                }
                }
            }.start();
        }
    }
}
