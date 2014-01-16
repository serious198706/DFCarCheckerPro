package com.df.app.CarCheck;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.MyOnClick;
import com.df.app.service.Adapter.MyViewPagerAdapter;
import com.df.app.service.SoapService;
import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-20.
 */
public class IntegratedCheckLayout extends LinearLayout {
    private View rootView;

    private ViewPager viewPager;
    private ImageView imageView;
    private TextView exteriorTab, interiorTab, itTab1, itTab2, itTab3;
    private List<View> views;
    private int offset =0;
    private int currIndex = 0;
    private int bmpW;

    private Activity activity;

    private static ExteriorLayout exteriorLayout;
    private static InteriorLayout interiorLayout;
    private static Integrated1Layout integrated1Layout;
    private static Integrated2Layout integrated2Layout;
    private static Integrated3Layout integrated3Layout;

    private int selectedColor = Color.rgb(0xAA, 0x03, 0x0A);
    private int unselectedColor = Color.rgb(0x70, 0x70, 0x70);

    public IntegratedCheckLayout(Context context) {
        super(context);
        init(context);
    }

    public IntegratedCheckLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IntegratedCheckLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.integrated_check_layout, this);
        InitViewPager(context);
        InitTextView();

//        // 获取标准化备注
//        GetStandardRemarksTask task = new GetStandardRemarksTask(rootView.getContext());
//        task.execute();
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    private void InitViewPager(Context context) {
        viewPager = (ViewPager) rootView.findViewById(R.id.vPager);
        views = new ArrayList<View>();

        exteriorLayout = new ExteriorLayout(context);
        interiorLayout = new InteriorLayout(context);
        integrated1Layout = new Integrated1Layout(context);
        integrated2Layout = new Integrated2Layout(context);
        integrated3Layout = new Integrated3Layout(context);

        views.add(exteriorLayout);
        views.add(interiorLayout);
        views.add(integrated1Layout);
        views.add(integrated2Layout);
        views.add(integrated3Layout);

        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    private void InitTextView() {
        exteriorTab = (TextView) rootView.findViewById(R.id.tabExterior);
        interiorTab = (TextView) rootView.findViewById(R.id.tabInterior);
        itTab1 = (TextView) rootView.findViewById(R.id.tabIt1);
        itTab2 = (TextView) rootView.findViewById(R.id.tabIt2);
        itTab3 = (TextView) rootView.findViewById(R.id.tabIt3);

        selectTab(0);

        exteriorTab.setOnClickListener(new MyOnClick(viewPager, 0));
        interiorTab.setOnClickListener(new MyOnClick(viewPager, 1));
        itTab1.setOnClickListener(new MyOnClick(viewPager, 2));
        itTab2.setOnClickListener(new MyOnClick(viewPager, 3));
        itTab3.setOnClickListener(new MyOnClick(viewPager, 4));
    }

    public void updateUi() {
        exteriorLayout.updateUi();
        interiorLayout.updateUi();
        integrated1Layout.updateUi();
    }

    public void saveTirePhoto() {
        integrated2Layout.saveTirePhoto();
    }

    public void updateExteriorPreview() {
        exteriorLayout.updateExteriorPreview();
    }

    public void updateInteriorPreview() {
        interiorLayout.updateInteriorPreview();
    }

    public void saveExteriorStandardPhoto() {
        exteriorLayout.saveExteriorStandardPhoto();
    }

    public void saveInteriorStandardPhoto() {
        interiorLayout.saveInteriorStandardPhoto();
    }

    public int getCooperatorId() {
        return integrated3Layout.getCooperatorId();
    }

    public String getCooperatorName() {
        return integrated3Layout.getCooperatorName();
    }

    public List<PhotoEntity> generateSketches() {
        List<PhotoEntity> temp = new ArrayList<PhotoEntity>();

        temp.add(exteriorLayout.generateSketch());
        temp.add(interiorLayout.generateSketch());
        temp.add(integrated2Layout.generateSketch());

        return temp;
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
        exteriorTab.setTextColor(currIndex == 0 ? selectedColor : unselectedColor);
        interiorTab.setTextColor(currIndex == 1 ? selectedColor : unselectedColor);
        itTab1.setTextColor(currIndex == 2 ? selectedColor : unselectedColor);
        itTab2.setTextColor(currIndex == 3 ? selectedColor : unselectedColor);
        itTab3.setTextColor(currIndex == 4 ? selectedColor : unselectedColor);
    }

    public JSONObject generateJSONObject() {
        JSONObject conditions = new JSONObject();

        try {
            JSONObject exterior = exteriorLayout.generateJSONObject();
            JSONObject interior = interiorLayout.generateJSONObject();
            JSONObject engine = integrated1Layout.generateEngineJSONObject();
            JSONObject gearbox = integrated1Layout.generateGearboxJSONObject();
            JSONObject function = integrated1Layout.generateFunctionJSONObject();
            JSONObject flooded = integrated2Layout.generateFloodedJSONObject();
            JSONObject tires = integrated2Layout.generateTiresJSONObject();

            // 综合检查 - 外观
            conditions.put("exterior", exterior);

            // 综合检查 - 内饰
            conditions.put("interior", interior);

            // 综合检查 - 发动机检查
            conditions.put("engine", engine);

            // 综合检查 - 变速箱检查
            conditions.put("gearbox", gearbox);

            // 综合检查 - 功能检查
            conditions.put("function",function);

            // 综合检查 - 泡水检查
            conditions.put("flooded", flooded);

            // 综合检查 - 轮胎
            conditions.put("tires", tires);

            // 综合检查 - 备注
            String comment1 = integrated1Layout.generateCommentString();
            String comment2 = integrated2Layout.generateCommentString();
            String comment3 = integrated3Layout.generateCommentString();

            conditions.put("comment1", comment1);
            conditions.put("comment2", comment2);
            conditions.put("comment3", comment3);

            conditions.put("comment", comment1 + ";" + comment2 + ";" + comment3);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return conditions;
    }

    public void fillInData(JSONObject conditions) {
        try {
            JSONObject exterior = conditions.getJSONObject("exterior");
            JSONObject interior = conditions.getJSONObject("interior");
            JSONObject engine = conditions.getJSONObject("engine");
            JSONObject gearbox = conditions.getJSONObject("gearbox");
            JSONObject function = conditions.getJSONObject("function");
            JSONObject flooded = conditions.getJSONObject("flooded");
            JSONObject tires = conditions.getJSONObject("tires");

            // 综合检查 - 备注
            String comment1 = conditions.getString("commont1");
            String comment2 = conditions.getString("commont2");
            String comment3 = conditions.getString("commont3");

            exteriorLayout.fillInData(exterior);
            interiorLayout.fillInData(interior);
            integrated1Layout.fillInData(engine, gearbox, function, comment1);
            integrated2Layout.fillInData(flooded, tires, comment2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public class GetStandardRemarksTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private SoapService soapService;

        public GetStandardRemarksTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = false;
            try {
                // 登录
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("UserId", MainActivity.userInfo.getId());
                jsonObject.put("Key", MainActivity.userInfo.getKey());

                soapService = new SoapService();

                // 设置soap的配置
                soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE, Common.GET_STANDARD_REMARKS);

                success = soapService.communicateWithServer(jsonObject.toString());
            } catch (JSONException e) {
                Log.d("DFCarChecker", "Json解析错误: " + e.getMessage());
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.d(Common.TAG, soapService.getResultMessage());
        }
    }
}
