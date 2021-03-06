package com.df.app.carsChecked;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.df.app.R;
import com.df.library.service.views.MyViewPagerAdapter;
import com.df.library.util.Common;
import com.df.library.util.MyOnClick;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.df.library.util.Helper.setTextView;

public class CarReportActivity extends Activity implements ViewPager.OnPageChangeListener{
    private ViewPager viewPager;
    private TextView basicTab;
    private TextView optionsTab;
    private TextView accidentTab;
    private TextView integratedTab;
    private TextView photoTab;
    private TextView otherTab;

    private BasicInfoLayout basicInfoLayout;
    private OptionsLayout optionsLayout;
    private AccidentLayout accidentResultLayout;
    private IntegratedLayout integratedLayout;
    private OtherLayout otherLayout;
    private PhotoLayout photoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_report);

        Bundle bundle = getIntent().getExtras();

        if(!bundle.containsKey("jsonString")) {
            finish();
        }

        String jsonString = bundle.getString("jsonString");

        InitViewPager(jsonString);
        InitTextView();
    }

    /**
     * 初始化viewPager，用来承载各个模块，可以通过滑动切换
     */
    private void InitViewPager(String jsonString) {
        viewPager = (ViewPager)findViewById(R.id.vPager);
        List<View> views = new ArrayList<View>();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONObject features = jsonObject.getJSONObject("features");
            JSONObject accident = jsonObject.getJSONObject("accident");
            JSONObject conditions = jsonObject.getJSONObject("conditions");
            JSONObject photos = jsonObject.getJSONObject("photos");

            JSONObject procedures = features.getJSONObject("procedures");
            JSONObject options = features.getJSONObject("options");

            basicInfoLayout = new BasicInfoLayout(this, procedures, Integer.toString(options.getInt("seriesId")),
                    Integer.toString(options.getInt("modelId")));
            optionsLayout = new OptionsLayout(this, options);
            accidentResultLayout = new AccidentLayout(this, accident, photos);
            integratedLayout = new IntegratedLayout(this, conditions, options, photos);
            photoLayout = new PhotoLayout(this, photos);
            otherLayout = new OtherLayout(this, procedures);

            views.add(basicInfoLayout);
            views.add(optionsLayout);
            views.add(accidentResultLayout);
            views.add(integratedLayout);
            views.add(photoLayout);
            views.add(otherLayout);

            viewPager.setAdapter(new MyViewPagerAdapter(views));
            viewPager.setCurrentItem(0);
            viewPager.setOnPageChangeListener(this);

            setTextView(getWindow().getDecorView(), R.id.currentItem, features.getJSONObject("procedures").getString("license"));

            Button backButton = (Button)findViewById(R.id.buttonBack);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化标签，用来标识当前模块，可以点击
     */
    private void InitTextView() {
        basicTab= (TextView) findViewById(R.id.basicTab);
        optionsTab = (TextView) findViewById(R.id.optionsTab);
        accidentTab = (TextView) findViewById(R.id.accidentTab);
        integratedTab = (TextView) findViewById(R.id.integratedTab);
        photoTab = (TextView) findViewById(R.id.photoTab);
        otherTab = (TextView) findViewById(R.id.otherTab);

        basicTab.setOnClickListener(new MyOnClick(viewPager, 0));
        optionsTab.setOnClickListener(new MyOnClick(viewPager, 1));
        accidentTab.setOnClickListener(new MyOnClick(viewPager, 2));
        integratedTab.setOnClickListener(new MyOnClick(viewPager, 3));
        photoTab.setOnClickListener(new MyOnClick(viewPager, 4));
        otherTab.setOnClickListener(new MyOnClick(viewPager, 5));

        selectTab(0);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        selectTab(i);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private void selectTab(int currIndex) {
        basicTab.setTextColor(currIndex == 0 ? Common.selectedColor : Common.unselectedColor);
        optionsTab.setTextColor(currIndex == 1 ? Common.selectedColor : Common.unselectedColor);
        accidentTab.setTextColor(currIndex == 2 ? Common.selectedColor : Common.unselectedColor);
        integratedTab.setTextColor(currIndex == 3 ? Common.selectedColor : Common.unselectedColor);
        photoTab.setTextColor(currIndex == 4 ? Common.selectedColor : Common.unselectedColor);
        otherTab.setTextColor(currIndex == 5 ? Common.selectedColor : Common.unselectedColor);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, CarsCheckedListActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(integratedLayout != null)
            integratedLayout.destroyTask();

        if(accidentResultLayout != null)
            accidentResultLayout.destroyTask();
    }
}
