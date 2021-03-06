package com.df.app.carCheck;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.R;
import com.df.app.service.util.AppCommon;
import com.df.library.carCheck.Integrated1Layout;
import com.df.library.carCheck.Integrated3Layout;
import com.df.library.entries.PhotoEntity;
import com.df.library.service.views.MyViewPagerAdapter;
import com.df.library.entries.UserInfo;
import com.df.library.util.MyOnClick;
import com.df.library.service.SoapService;
import com.df.library.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-20.
 *
 * 综合检查，包括外观检查、内饰检查、综合一、二、三检查
 */
public class IntegratedCheckLayout extends LinearLayout implements ViewPager.OnPageChangeListener {
    private View rootView;

    private ViewPager viewPager;
    private TextView exteriorTab, interiorTab, itTab1, itTab2, itTab3;

    private static ExteriorLayout exteriorLayout;
    private static InteriorLayout interiorLayout;
    private static Integrated1Layout integrated1Layout;
    private static Integrated2Layout integrated2Layout;
    private static Integrated3Layout integrated3Layout;

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

    private void InitViewPager(Context context) {
        viewPager = (ViewPager) rootView.findViewById(R.id.vPager);
        List<View> views = new ArrayList<View>();

        exteriorLayout = new ExteriorLayout(context);
        interiorLayout = new InteriorLayout(context);
        integrated1Layout = new Integrated1Layout(context, BasicInfoLayout.carId);
        integrated2Layout = new Integrated2Layout(context);
        integrated3Layout = new Integrated3Layout(context);

        views.add(exteriorLayout);
        views.add(interiorLayout);
        views.add(integrated1Layout);
        views.add(integrated2Layout);
        views.add(integrated3Layout);

        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(this);
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

    /**
     * 获取车辆配置信息后，更新外观、内饰和综合一的界面
     */
    public void updateUi() {
        exteriorLayout.updateUi();
        interiorLayout.updateUi();
        integrated1Layout.updateUi();
    }

    /**
     * 更新外观草图
     */
    public void updateExteriorPreview() {
        ExteriorLayout.updateExteriorPreview();
    }

    /**
     * 更新内饰草图
     */
    public void updateInteriorPreview() {
        InteriorLayout.updateInteriorPreview();
    }

    /**
     * 保存轮胎照
     */
    public void saveTirePhoto() {
        integrated2Layout.saveTirePhoto();
    }

    /**
     * 获取从检id
     * @return
     */
    public int getCooperatorId() {
        return integrated3Layout.getCooperatorId();
    }

    /**
     * 获取从检名字
     * @return
     */
    public String getCooperatorName() {
        return integrated3Layout.getCooperatorName();
    }

    /**
     * 获取草图Bitmap
     * @return
     */
    public Bitmap getExteriorSketch() { return exteriorLayout.getSketch(); }
    public Bitmap getInteriorSketch() { return interiorLayout.getSketch(); }
    public Bitmap getTireSketch() { return integrated2Layout.getSketch(); }

    /**
     * 生成外观、内饰、轮胎的草图
     * @return
     */
    public List<PhotoEntity> generateSketches() {
        List<PhotoEntity> temp = new ArrayList<PhotoEntity>();

        temp.add(exteriorLayout.generateSketch());
        temp.add(interiorLayout.generateSketch());
        temp.add(integrated2Layout.generateSketch());

        return temp;
    }

    /**
     * 提交前的检查
     * @return
     */
    public String checkAllFields() {
        String currentField = "";

        // 综合检查二
        if(Common.getEnvironment() != Common.INTERNAL_100_6_VERSION)
            currentField = integrated2Layout.checkAllFields();

        if(currentField.contains("leftFront")) {
            Toast.makeText(rootView.getContext(), "未拍摄左前轮照片！", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(3);
            integrated2Layout.locateTirePart();

            return currentField;
        } else if(currentField.contains("rightFront")) {
            Toast.makeText(rootView.getContext(), "未拍摄右前轮照片！", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(3);
            integrated2Layout.locateTirePart();

            return currentField;
        } else if(currentField.contains("leftRear")) {
            Toast.makeText(rootView.getContext(), "未拍摄左后轮照片！", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(3);
            integrated2Layout.locateTirePart();

            return currentField;
        } else if(currentField.contains("rightRear")) {
            Toast.makeText(rootView.getContext(), "未拍摄右后轮照片！", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(3);
            integrated2Layout.locateTirePart();

            return currentField;
        } else if(currentField.contains("spare")) {
            Toast.makeText(rootView.getContext(), "未拍摄备胎照片！", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(3);
            integrated2Layout.locateTirePart();

            return currentField;
        } else if(currentField.contains("edits")) {
            Toast.makeText(rootView.getContext(), "轮胎标号有误！", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(3);
            integrated2Layout.locateTirePart();

            return "edits";
        }

        // 综合检查三
        currentField = integrated3Layout.checkAllFields();

        if(currentField.equals("coop")) {
            Toast.makeText(rootView.getContext(), "未选择从检人员！", Toast.LENGTH_SHORT).show();
            viewPager.setCurrentItem(4);

            return currentField;
        }

        return currentField;
    }

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

    private void selectTab(int currIndex) {
        exteriorTab.setTextColor(currIndex == 0 ? Common.selectedColor : Common.unselectedColor);
        interiorTab.setTextColor(currIndex == 1 ? Common.selectedColor : Common.unselectedColor);
        itTab1.setTextColor(currIndex == 2 ? Common.selectedColor : Common.unselectedColor);
        itTab2.setTextColor(currIndex == 3 ? Common.selectedColor : Common.unselectedColor);
        itTab3.setTextColor(currIndex == 4 ? Common.selectedColor : Common.unselectedColor);
    }

    /**
     * 生成综合检查的JSONObject
     * @return
     */
    public JSONObject generateJSONObject() {
        JSONObject conditions = new JSONObject();

        try {
            JSONObject exterior = exteriorLayout.generateJSONObject();
            JSONObject interior = interiorLayout.generateJSONObject();
            JSONObject engine = integrated1Layout.generateEngineJSONObject();
            JSONObject gearbox = integrated1Layout.generateGearboxJSONObject();
            JSONObject fluid = integrated1Layout.generateFluidJSONObject();
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

            // 综合检查 - 液位检查
            conditions.put("fluid", fluid);

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

            String comment = "";

            if(!comment1.equals(""))
                comment += comment1 + ";";

            if(!comment2.equals(""))
                comment += comment2 + ";";

            if(!comment3.equals(""))
                comment += comment3;

            conditions.put("comment", comment);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return conditions;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * @param jsonObject 大json串
     */
    public void fillInData(JSONObject jsonObject) {
        try {
            JSONObject conditions = jsonObject.getJSONObject("conditions");

            JSONObject exterior = conditions.getJSONObject("exterior");
            JSONObject interior = conditions.getJSONObject("interior");
            JSONObject engine = conditions.getJSONObject("engine");
            JSONObject gearbox = conditions.getJSONObject("gearbox");
            JSONObject fluid = conditions.getJSONObject("fluid");
            JSONObject function = conditions.getJSONObject("function");
            JSONObject flooded = conditions.getJSONObject("flooded");
            JSONObject tires = conditions.getJSONObject("tires");

            // 综合检查 - 备注
            String comment1 = conditions.get("comment1") == JSONObject.NULL ? "" : conditions.getString("comment1");
            String comment2 = conditions.get("comment2") == JSONObject.NULL ? "" : conditions.getString("comment2");
            String comment3 = conditions.get("comment3") == JSONObject.NULL ? "" : conditions.getString("comment3");


            if(CarCheckActivity.isModify()) {
                exteriorLayout.fillInData(exterior, jsonObject.getJSONObject("photos"));
                interiorLayout.fillInData(interior, jsonObject.getJSONObject("photos"));
            } else {
                exteriorLayout.fillInData(exterior);
                interiorLayout.fillInData(interior);
            }

            integrated1Layout.fillInData(engine, gearbox, fluid, function, comment1);

            if(CarCheckActivity.isModify()) {
                integrated2Layout.fillInData(flooded, tires, jsonObject.getJSONObject("photos"), comment2);
            } else {
                integrated2Layout.fillInData(flooded, tires, comment2);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void fillInData(String checkCooperatorName) {
        integrated3Layout.fillInData(checkCooperatorName);
    }

    public void clearCache() {
        exteriorLayout.clearCache();
        interiorLayout.clearCache();
        Integrated1Layout.clearCache();
        Integrated2Layout.photoEntityMap.clear();
    }

    /**
     * 获取标准化备注文字
     */
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

                jsonObject.put("UserId", UserInfo.getInstance().getId());
                jsonObject.put("Key", UserInfo.getInstance().getKey());

                soapService = new SoapService();

                // 设置soap的配置
                soapService.setUtils(Common.getPICTURE_ADDRESS() + Common.CAR_CHECK_SERVICE, Common.GET_STANDARD_REMARKS);

                success = soapService.communicateWithServer(jsonObject.toString());
            } catch (JSONException e) {
                Log.d("DFCarChecker", "Json解析错误: " + e.getMessage());
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.d(AppCommon.TAG, soapService.getResultMessage());
        }
    }
}
