package com.df.app.CarCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.CarsWaiting.CarsWaitingActivity;
import com.df.app.MainActivity;
import com.df.app.Procedures.CarRecogniseLayout;
import com.df.app.Procedures.ProceduresLayout;
import com.df.app.R;
import com.df.app.entries.Brand;
import com.df.app.entries.CarSettings;
import com.df.app.entries.Country;
import com.df.app.entries.Manufacturer;
import com.df.app.entries.Model;
import com.df.app.entries.Series;
import com.df.app.entries.VehicleModel;
import com.df.app.service.MyViewPagerAdapter;
import com.df.app.service.MyOnClick;
import com.df.app.service.SoapService;
import com.df.app.util.Common;
import com.df.app.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.df.app.util.Helper.enableView;
import static com.df.app.util.Helper.getEditViewText;
import static com.df.app.util.Helper.setEditViewText;
import static com.df.app.util.Helper.showView;

/**
 * Created by 岩 on 13-12-20.
 */
public class BasicInfoLayout extends LinearLayout {
    private View rootView;

    private ViewPager viewPager;
    private TextView vehicleInfoTab, optionsTab;
    private List<View> views;

    private VehicleInfoLayout vehicleInfoLayout;
    private OptionsLayout optionsLayout;

    public static CarSettings mCarSettings;

    public static String uniqueId;

    private OnUpdateUiListener mUpdateUiCallback;

    private int selectedColor = Color.rgb(0xAA, 0x03, 0x0A);
    private int unselectedColor = Color.rgb(0x70, 0x70, 0x70);

    public BasicInfoLayout(Context context) {
        super(context);
        if(!isInEditMode())
            init(context);
    }

    public BasicInfoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BasicInfoLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setUpdateUiListener(OnUpdateUiListener listener) {
        this.mUpdateUiCallback = listener;
    }

    public void init(Context context){
        rootView = LayoutInflater.from(context).inflate(R.layout.basic_info_layout, this);

        UUID uuid = UUID.randomUUID();
        uniqueId = uuid.toString();

        // TODO 在初始化的时候，要向服务器请求一次车辆配置信息，以便初始化配置信息页面
        mCarSettings = new CarSettings();

        optionsLayout = new OptionsLayout(context);
        vehicleInfoLayout = new VehicleInfoLayout(context, new VehicleInfoLayout.UpdateUi() {
            @Override
            public void updateUi() {
                optionsLayout.updateUi();
                mUpdateUiCallback.updateUi();
            }
        });

        InitViewPager(context);
        InitTextView();
    }

    private void InitViewPager(Context context) {
        viewPager = (ViewPager) rootView.findViewById(R.id.vPager);
        views = new ArrayList<View>();

        views.add(vehicleInfoLayout);
        views.add(optionsLayout);

        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    private void InitTextView() {
        vehicleInfoTab = (TextView) rootView.findViewById(R.id.vehicleInfoTab);
        optionsTab = (TextView) rootView.findViewById(R.id.optionsTab);

        selectTab(0);

        vehicleInfoTab.setOnClickListener(new MyOnClick(viewPager, 0));
        optionsTab.setOnClickListener(new MyOnClick(viewPager, 1));
    }

    class MyOnPageChangeListener implements ViewPager.OnPageChangeListener
    {
        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int arg0) {
            selectTab(arg0);
        }
    }

    private void selectTab(int currIndex) {
        vehicleInfoTab.setTextColor(currIndex == 0 ? selectedColor : unselectedColor);
        optionsTab.setTextColor(currIndex == 1 ? selectedColor : unselectedColor);
    }

    // CarCheckActivity必须实现此接口
    public interface OnUpdateUiListener {
        public void updateUi();
    }
}
