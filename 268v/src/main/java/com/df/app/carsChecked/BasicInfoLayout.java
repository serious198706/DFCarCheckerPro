package com.df.app.carsChecked;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.library.entries.Brand;
import com.df.library.entries.Country;
import com.df.library.entries.Manufacturer;
import com.df.library.entries.Model;
import com.df.library.entries.Series;
import com.df.library.util.MyScrollView;

import org.json.JSONException;
import org.json.JSONObject;

import static com.df.library.util.Helper.setTextView;

/**
 * Created by 岩 on 14-3-14.
 */
public class BasicInfoLayout extends LinearLayout {
    private ViewGroup rootView;

    public BasicInfoLayout(Context context, JSONObject procedures, String seriesId, String modelId) {
        super(context);
        init(context, procedures, seriesId, modelId);
    }

    public BasicInfoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BasicInfoLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init(Context context, JSONObject procedures, String seriesId, String modelId) {
        rootView = (ViewGroup)LayoutInflater.from(context).inflate(R.layout.car_report_basic_layout, this);

        MyScrollView scrollView = (MyScrollView) findViewById(R.id.root);
        scrollView.setListener(new MyScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy) {
                if (scrollView.getScrollY() > 5) {
                    showShadow(true);
                } else {
                    showShadow(false);
                }
            }
        });

        // 移除输入框的焦点，避免每次输入完成后界面滚动
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        scrollView.setFocusable(true);
        scrollView.setFocusableInTouchMode(true);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
            }
        });

        try {
            fillInData(procedures, seriesId, modelId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    private View findView(ViewGroup viewGroup, String tag) {
        View view = null;

        for(int i = 0; i < viewGroup.getChildCount(); i++) {

            view = viewGroup.getChildAt(i);

            if (view instanceof ViewGroup) {
                view = findView((ViewGroup)view, tag);

                if(tag.equals(view.getTag())) {
                    return view;
                }
            } else {
                if(tag.equals(view.getTag())) {
                    return view;
                }
            }
        }

        return view;
    }

    private void fillInData(JSONObject procedures, String seriesId, String modelId) throws JSONException {

//        Iterator iterator = procedures.keys();
//        try {
//            while (iterator.hasNext()) {
//                String temp = (String)iterator.next();
//
//                TextView textView = (TextView)findView(rootView, temp);
//                if(textView != null) {
//                    textView.setText(procedures.get(temp) == JSONObject.NULL ? "无" : procedures.getString(temp));
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        setTextView(rootView, R.id.vehicleType_text, procedures.get("licensetype") == JSONObject.NULL ? "无" : procedures.getString("licensetype"));
        setTextView(rootView, R.id.regDate_text, procedures.get("regdate") == JSONObject.NULL ? "无" : procedures.getString("regdate"));
        setTextView(rootView, R.id.builtDate_text, procedures.get("leavefactorydate") == JSONObject.NULL ? "无" : procedures.getString("leavefactorydate"));
        setTextView(rootView, R.id.plateNumber_text, procedures.get("license") == JSONObject.NULL ? "无" : procedures.getString("license"));
        setTextView(rootView, R.id.exteriorColor_text, procedures.get("color") == JSONObject.NULL ? "无" : procedures.getString("color"));
        setTextView(rootView, R.id.mileage_text, procedures.get("mileage") == JSONObject.NULL ? "无" : procedures.getString("mileage"));
        setTextView(rootView, R.id.vin_text, procedures.get("vin") == JSONObject.NULL ? "无" : procedures.getString("vin"));
        setTextView(rootView, R.id.engineSerial_text, procedures.get("engineno") == JSONObject.NULL ? "无" : procedures.getString("engineno"));
        setTextView(rootView, R.id.licenseModel_text, procedures.get("model2") == JSONObject.NULL ? "无" : procedures.getString("model2"));
        setTextView(rootView, R.id.ownerName_text, procedures.get("owner") == JSONObject.NULL ? "无" : procedures.getString("owner"));
        setTextView(rootView, R.id.ownerIdNumber_text, procedures.get("ownerid") == JSONObject.NULL ? "无" : procedures.getString("ownerid"));
        setTextView(rootView, R.id.ownerPhone_text, procedures.get("ownermobile") == JSONObject.NULL ? "无" : procedures.getString("ownermobile"));
        setTextView(rootView, R.id.regArea_text, procedures.get("regarea") == JSONObject.NULL ? "无" : procedures.getString("regarea"));
        setTextView(rootView, R.id.transferCount_text, procedures.get("tradetimes") == JSONObject.NULL ? "无" : procedures.getString("tradetimes"));
        setTextView(rootView, R.id.violationRecords_text, procedures.get("wzjl") == JSONObject.NULL ? "无" : procedures.getString("wzjl"));
        setTextView(rootView, R.id.useCharacter_text, procedures.get("kind") == JSONObject.NULL ? "无" : procedures.getString("kind"));
        setTextView(rootView, R.id.originUser_text, procedures.get("kind2") == JSONObject.NULL ? "无" : procedures.getString("kind2"));
        setTextView(rootView, R.id.namePlate_text, procedures.get("csmp") == JSONObject.NULL ? "无" : procedures.getString("csmp"));
        setTextView(rootView, R.id.import_text, procedures.get("sfjk") == JSONObject.NULL ? "无" : procedures.getString("sfjk"));

        setTextView(rootView, R.id.importWay_text, procedures.get("gmfs") == JSONObject.NULL ? "无" : procedures.getString("gmfs"));
        setTextView(rootView, R.id.importEntryBill_text, procedures.get("jkgd") == JSONObject.NULL ? "无" : procedures.getString("jkgd"));
        setTextView(rootView, R.id.importBusinessCheck_text, procedures.get("sjd") == JSONObject.NULL ? "无" : procedures.getString("sjd"));
        setTextView(rootView, R.id.licencePhotoMatch_text, procedures.get("sxdd") == JSONObject.NULL ? "无" : procedures.getString("sxdd"));
        setTextView(rootView, R.id.driveLicence_text, procedures.get("drivinglicense") == JSONObject.NULL ? "无" : procedures.getString("drivinglicense"));
        setTextView(rootView, R.id.regLicence_text, procedures.get("regcert") == JSONObject.NULL ? "无" : procedures.getString("regcert"));
        setTextView(rootView, R.id.invoice_text, procedures.get("invoice") == JSONObject.NULL ? "无" : procedures.getString("invoice"));
        setTextView(rootView, R.id.surtax_text, procedures.get("surtax") == JSONObject.NULL ? "无" : procedures.getString("surtax"));
        setTextView(rootView, R.id.maintenanceManual_text, procedures.get("manual") == JSONObject.NULL ? "无" : procedures.getString("manual"));
        setTextView(rootView, R.id.vehicleTax_text, procedures.get("cctax") == JSONObject.NULL ? "无" : procedures.getString("cctax"));
        setTextView(rootView, R.id.annualInspectionDate_text, procedures.get("njdate") == JSONObject.NULL ? "无" : procedures.getString("njdate"));
        setTextView(rootView, R.id.spareKey_text, procedures.get("key") == JSONObject.NULL ? "无" : procedures.getString("key"));
        setTextView(rootView, R.id.compulsoryInsurance_text, procedures.get("jqx") == JSONObject.NULL ? "无" : procedures.getString("jqx"));
        setTextView(rootView, R.id.compulsoryInsuranceDate_text, procedures.get("jqdate") == JSONObject.NULL ? "无" : procedures.getString("jqdate"));

        JSONObject sx = procedures.getJSONObject("sx");
        setTextView(rootView, R.id.insurance_text, sx.getString("Value") == JSONObject.NULL ? "无" : sx.getString("Value"));

        setTextView(rootView, R.id.insuranceRegion_text, procedures.get("sxdy") == JSONObject.NULL ? "无" : procedures.getString("sxdy"));
        setTextView(rootView, R.id.insuranceAmount_text, procedures.get("sxmoney") == JSONObject.NULL ? "无" : procedures.getString("sxmoney"));
        setTextView(rootView, R.id.insuranceExpiryDate_text, procedures.get("sxdate") == JSONObject.NULL ? "无" : procedures.getString("sxdate"));

        setTextView(rootView, R.id.insuranceCompany_text, procedures.get("insurer") == JSONObject.NULL ? "无" : procedures.getString("insurer"));

        if(procedures.get("source") == JSONObject.NULL) {
            setTextView(rootView, R.id.source_text, "无");
        } else {
            JSONObject source = procedures.getJSONObject("source");
            String sourceString = "";
            if(source.has("f1") && !source.isNull("f1")) {
                sourceString += source.getJSONObject("f1").getString("Value");
            } else {
                sourceString = "无";
            }

            // 没有f1就没有f2
            if(source.has("f2") && !source.isNull("f2")) {
                sourceString += " - ";
                sourceString += source.getJSONObject("f2").getString("Value");
            }

            // 没有f2就没有f3
            if(source.has("f3") && !source.isNull("f3")) {
                sourceString += " - ";
                sourceString += source.getJSONObject("f3").getString("Value");
            }

            setTextView(rootView, R.id.source_text, sourceString);
        }

        setTextView(rootView, R.id.seller_text, procedures.get("xsry") == JSONObject.NULL ? "无" : procedures.getString("xsry"));
        setTextView(rootView, R.id.carProperty_text, procedures.get("CarAttributeName") == JSONObject.NULL ? "无" : procedures.getString("CarAttributeName"));
        setTextView(rootView, R.id.exchangeRequirement_text, procedures.get("zhxq") == JSONObject.NULL ? "无" : procedures.getString("zhxq"));
        setTextView(rootView, R.id.exchangeCarModel_text, procedures.get("zhxh") == JSONObject.NULL ? "无" : procedures.getString("zhxh"));


        // 更新配置信息
        Country country = null;
        Brand brand = null;
        Manufacturer manufacturer = null;
        Series series = null;
        Model model = null;

        boolean found = false;

        for(Country country1 : MainActivity.vehicleModel.getCountries()) {
            for(Brand brand1 : country1.brands) {
                for(Manufacturer manufacturer1 : brand1.manufacturers) {
                    for(Series series1 : manufacturer1.serieses) {
                        if(series1.id.equals(seriesId)) {
                            manufacturer = manufacturer1;
                            brand = brand1;
                            country = country1;
                            series = series1;
                            model = series.getModelById(modelId);

                            found = true;
                            break;
                        }
                    }
                    if(found)
                        break;
                }
                if(found)
                    break;
            }
            if(found)
                break;
        }

        // 车型
        String brandString = manufacturer.name + " " + series.name + " " + model.name;

        setTextView(rootView, R.id.brandText, brandString);
    }
}
