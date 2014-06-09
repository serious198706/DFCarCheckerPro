package com.df.kia.carCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.kia.MainActivity;
import com.df.kia.R;
import com.df.library.entries.Brand;
import com.df.library.entries.CarSettings;
import com.df.library.entries.Country;
import com.df.library.entries.Manufacturer;
import com.df.library.entries.Model;
import com.df.library.entries.Series;
import com.df.library.entries.VehicleModel;
import com.df.library.asyncTask.GetCarSettingsTask;
import com.df.library.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import static com.df.library.util.Helper.getEditViewText;
import static com.df.library.util.Helper.setEditViewText;
import static com.df.library.util.Helper.setTextView;

/**
 * Created by 岩 on 14-1-8.
 *
 * 车辆信息，包括基本信息和配置信息
 */
public class VehicleInfoLayout extends LinearLayout {
    private static View rootView;

    // 车辆配置
    private CarSettings mCarSettings;

    // 存储车型信息（来自xml）
    private VehicleModel vehicleModel;

    // 选择车型的五个spinner
    private EditText countryEdit;
    private EditText brandEdit;
    private EditText manufacturerEdit;
    private EditText seriesEdit;
    private EditText modelEdit;


    // 记录五个spinner最后选择的位置
    private int lastCountryIndex = 0;
    private int lastBrandIndex = 0;
    private int lastManufacturerIndex = 0;
    private int lastSeriesIndex = 0;
    private int lastModelIndex = 0;

    // 获取车辆配置信息后，更新页面的回调
    private OnGetCarSettings mCallback;

    // 获取车辆详细信息后，填入数据的回调
    private OnUiUpdated mUiUpdatedCallback;

    public VehicleInfoLayout(Context context, OnGetCarSettings listener) {
        super(context);
        this.mCallback = listener;
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.vehicle_info_layout, this);

        vehicleModel = MainActivity.vehicleModel;
        mCarSettings = BasicInfoLayout.mCarSettings;

        // 点击品牌选择按钮
        Button brandSelectButton = (Button) rootView.findViewById(R.id.brand_select_button);
        brandSelectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCarManually();
            }
        });
    }

    /**
     * 从服务器获取车辆配置
     */
    private void getCarSettingsFromServer(String seriesId) {
        GetCarSettingsTask mGetCarSettingsTask = new GetCarSettingsTask(rootView.getContext(), seriesId, new GetCarSettingsTask.OnGetCarSettingsFinished() {
            @Override
            public void onFinished(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);

                    updateCarSettings(jsonObject.getString("config"),
                            jsonObject.getString("category"), jsonObject.getString("figure"));

                    // 更新UI
                    updateUi();
                } catch (JSONException e) {
                    Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                }
            }

            @Override
            public void onFailed(String result) {
                // 传输失败，获取错误信息并显示
                Log.d("DFCarChecker", "获取车辆配置信息失败：" + result);
                Toast.makeText(rootView.getContext(), result, Toast.LENGTH_LONG).show();
            }
        });
        mGetCarSettingsTask.execute();
    }

    /**
     * 手动选择车型
     */
    private void selectCarManually() {
        View view = LayoutInflater.from(rootView.getContext()).inflate(R.layout
                .vehicle_model_select, null);

        TextView title = (TextView)view.findViewById(R.id.title);
        title.setText(R.string.select_model);

        initModelSelectEdits(view);

        AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                .setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 如果用户点击确定，则必须要求所有的Spinner为选中状态
                        if (lastCountryIndex == 0 ||
                                lastBrandIndex == 0 ||
                                lastManufacturerIndex == 0 ||
                                lastSeriesIndex == 0 ||
                                lastModelIndex == 0) {
                            Toast.makeText(rootView.getContext(), "请选择所有项目", Toast.LENGTH_SHORT).show();

                            return;
                        }

                        Country country = vehicleModel.countries.get(lastCountryIndex - 1);
                        Brand brand = country.brands.get(lastBrandIndex - 1);
                        Manufacturer manufacturer = brand.manufacturers.get(lastManufacturerIndex - 1);
                        Series series = manufacturer.serieses.get(lastSeriesIndex - 1);
                        Model model = series.models.get(lastModelIndex - 1);

                        mCarSettings.setCountry(country);
                        mCarSettings.setBrand(brand);
                        mCarSettings.setManufacturer(manufacturer);
                        mCarSettings.setSeries(series);
                        mCarSettings.setModel(model);

                        // 根据seriesId和modelId从服务器获取车辆配置信息  config:powerWindows,powerSeats...
                        getCarSettingsFromServer(series.id + "," + model.id);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();
    }

    /**
     * 更新UI
     */
    private void updateUi() {
        // 设置厂牌型号的EditText
        setEditViewText(rootView, R.id.brand_edit, mCarSettings.getBrandString());

        // 更新配置界面、外观、内饰界面
        mCallback.onGetCarSettings();

        if(mUiUpdatedCallback != null)
            mUiUpdatedCallback.onUiUpdated();
    }

    /**
     * 更新车辆配置信息
     * @param config
     * @param category
     * @param figure
     */
    private void updateCarSettings(String config, String category, String figure) {
        Manufacturer manufacturer = mCarSettings.getManufacturer();
        Series series = mCarSettings.getSeries();
        Model model = mCarSettings.getModel();

        // 车型
        String brandString = manufacturer.name + " " + series.name + " " + model.name;

        // 排量
        String displacementString = model.name;
        if (displacementString.length() > 3) {
            displacementString = displacementString.substring(0, 3);
        }

        // 更新配置信息类
        mCarSettings.setBrandString(brandString);
        mCarSettings.setDisplacement(displacementString);

        String modelString = model.getName();

        // 将排量框设置文字
        if(modelString.length() >= 3)
        {
            mCarSettings.setDisplacement(modelString.substring(0, 3));
        }

        // 设置驱动方式Spinner
        if(modelString.contains("四驱")) {
            mCarSettings.setDriveType("四驱");
        } else {
            mCarSettings.setDriveType("两驱");
        }

//        // 设置变速器形式Spinner
//        if(modelString.contains("AMT")) {
//            mCarSettings.setTransmission("AMT");
//        } else if(modelString.contains("A/MT")) {
//            mCarSettings.setTransmission("A/MT");
//        } else if(modelString.contains("MT")) {
//            mCarSettings.setTransmission("MT");
//        } else if(modelString.contains("CVT") || modelString.contains("DSG")) {
//            mCarSettings.setTransmission("CVT");
//        } else {
//            mCarSettings.setTransmission("AT");
//        }

        // TODO!!!!
        // 设置变速器形式Spinner
        if(modelString.contains("AMT")) {
            mCarSettings.setTransmission("AMT");
        } else if(modelString.contains("A/MT")) {
            mCarSettings.setTransmission("A/MT");
        } else if(modelString.contains("MT")) {
            mCarSettings.setTransmission("MT");
        } else if(modelString.contains("CVT")) {
            mCarSettings.setTransmission("CVT");
        } else if(modelString.contains("DSG")) {
            mCarSettings.setTransmission("DSG");
        } else {
            mCarSettings.setTransmission("AT");
        }

        // 设置配置信息
        mCarSettings.setConfig(config);

        // 设置车型分类，以用于图片类型判断
        String categoryArray[] = getResources().getStringArray(R.array.category_item);

        if(Integer.parseInt(category) > 0) {
            mCarSettings.setCategory(categoryArray[Integer.parseInt(category) - 1]);
        } else {
            mCarSettings.setCategory(categoryArray[Integer.parseInt(category)]);
        }

        mCarSettings.setFigure(figure);
    }

    private void initModelSelectEdits(View view) {
        TextView title = (TextView)view.findViewById(R.id.title);
        title.setText(R.string.select_model);

        countryEdit = (EditText)view.findViewById(R.id.country_edit);
        brandEdit = (EditText)view.findViewById(R.id.brand_edit);
        manufacturerEdit = (EditText)view.findViewById(R.id.manufacturer_edit);
        seriesEdit = (EditText)view.findViewById(R.id.series_edit);
        modelEdit = (EditText)view.findViewById(R.id.model_edit);

        setCountryEdit();

        if(mCarSettings.getCountry() != null) {
            countryEdit.setText(mCarSettings.getCountry().name);
            brandEdit.setText(mCarSettings.getBrand().name);
            manufacturerEdit.setText(mCarSettings.getManufacturer().name);
            seriesEdit.setText(mCarSettings.getSeries().name);
            modelEdit.setText(mCarSettings.getModel().name);
        }
    }

    /**
     * 设置国家edit
     */
    private void setCountryEdit() {
        final ArrayAdapter<String> adapter;

        if(vehicleModel == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, vehicleModel.getCountryNames());
        }

        countryEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showListDialog(R.string.chooseCountry, adapter, new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message message) {
                        if(message.what == 0) {
                            countryEdit.setText("");
                            brandEdit.setText("");
                            manufacturerEdit.setText("");
                            seriesEdit.setText("");
                            modelEdit.setText("");
                        } else {
                            Country country = vehicleModel.getCountries().get(message.what - 1);
                            countryEdit.setText(country.name);

                            // 如果此次选择与上次不同，则清空下方edit
                            if(lastCountryIndex != message.what) {
                                brandEdit.setText("");
                                manufacturerEdit.setText("");
                                seriesEdit.setText("");
                                modelEdit.setText("");
                            }

                            setBrandEdit(country);
                        }

                        lastCountryIndex = message.what;

                        return true;
                    }
                }));
            }
        });
    }

    /**
     * 设置品牌edit
     */
    private void setBrandEdit(final Country country) {
        // 设置adapter，内容为country的brand名称列表
        final ArrayAdapter<String> adapter;

        if(country == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, country.getBrandNames());
        }

        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if(message.what == 0) {
                    brandEdit.setText("");
                    manufacturerEdit.setText("");
                    seriesEdit.setText("");
                    modelEdit.setText("");
                } else {
                    Brand brand = country.brands.get(message.what - 1);
                    brandEdit.setText(brand.name);

                    // 如果此次选择与上次不同，则清空下方edit
                    if(lastBrandIndex != message.what) {
                        manufacturerEdit.setText("");
                        seriesEdit.setText("");
                        modelEdit.setText("");
                    }

                    setManufacturerEdit(brand);
                }

                lastBrandIndex = message.what;

                return true;
            }
        });

        // 如果有且只有两条内容（一条为空），则直接填充内容，并且尝试弹出manufacturer的选择框
        if(country.getBrandNames().size() == 2) {
            lastBrandIndex = 1;
            brandEdit.setText(country.getBrandNames().get(1));
            setManufacturerEdit(country.brands.get(0));
            return;
        }
        // 如果有且多于两条内容，则弹出brand的选择框
        else {
            showListDialog(R.string.chooseBrand, adapter, handler);
        }

        // 点击brandEdit的事件
        brandEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showListDialog(R.string.chooseBrand, adapter, handler);
            }
        });
    }

    /**
     * 设置厂商edit
     */
    private void setManufacturerEdit(final Brand brand) {
        // 设置adapter，内容为brand的manufacturer名称列表
        final ArrayAdapter<String> adapter;

        if(brand == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, brand.getManufacturerNames());
        }

        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if(message.what == 0) {
                    manufacturerEdit.setText("");
                    seriesEdit.setText("");
                    modelEdit.setText("");
                } else {
                    Manufacturer manufacturer = brand.manufacturers.get(message.what - 1);
                    manufacturerEdit.setText(manufacturer.name);

                    // 如果此次选择与上次不同，则清空下方edit
                    if(lastManufacturerIndex != message.what) {
                        seriesEdit.setText("");
                        modelEdit.setText("");
                    }

                    setSeriesEdit(manufacturer);
                }

                lastManufacturerIndex = message.what;

                return true;
            }
        });

        // 如果有且只有两条内容（一条为空），则直接填充内容，并且尝试弹出series的选择框
        if(brand.getManufacturerNames().size() == 2) {
            lastManufacturerIndex = 1;
            manufacturerEdit.setText(brand.getManufacturerNames().get(1));
            setSeriesEdit(brand.manufacturers.get(0));
            return;
        }
        // 如果有且多于两条内容，则弹出manufactuer的选择框
        else {
            showListDialog(R.string.chooseManufacturer, adapter, handler);
        }

        // 点击manufacturerEdit的事件
        manufacturerEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showListDialog(R.string.chooseManufacturer, adapter, handler);
            }
        });
    }

    /**
     * 设置车系edit
     */
    private void setSeriesEdit(final Manufacturer manufacturer) {
        // 设置adapter，内容为manufacturer的series名称列表
        final ArrayAdapter<String> adapter;

        if(manufacturer == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, manufacturer.getSeriesNames());
        }

        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if(message.what == 0) {
                    seriesEdit.setText("");
                    modelEdit.setText("");
                } else {
                    Series series = manufacturer.serieses.get(message.what - 1);
                    seriesEdit.setText(series.name);

                    // 如果此次选择与上次不同，则清空下方edit
                    if(lastSeriesIndex != message.what) {
                        modelEdit.setText("");
                    }

                    setModelEdit(series);
                }

                lastSeriesIndex = message.what;

                return true;
            }
        });

        // 如果有且只有两条内容（一条为空），则直接填充内容，并且尝试弹出series的选择框
        if(manufacturer.getSeriesNames().size() == 2) {
            lastSeriesIndex = 1;
            seriesEdit.setText(manufacturer.getSeriesNames().get(1));
            setModelEdit(manufacturer.serieses.get(0));
            return;
        }
        // 如果有且多于两条内容，则弹出series的选择框
        else {
            showListDialog(R.string.chooseSeries, adapter, handler);
        }

        // 点击manufacturerEdit的事件
        seriesEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showListDialog(R.string.chooseSeries, adapter, handler);
            }
        });
    }

    /**
     * 设置车型edit
     */
    private void setModelEdit(final Series series) {
        // 设置adapter，内容为series的model名称列表
        final ArrayAdapter<String> adapter;

        if(series == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_list_item_1, series.getModelNames());
        }

        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                lastModelIndex = message.what;

                if(message.what == 0) {
                    modelEdit.setText("");
                } else {
                    Model model = series.models.get(message.what - 1);
                    modelEdit.setText(model.name);
                }

                return true;
            }
        });

        // 如果有且只有两条内容（一条为空），则直接填充内容
        if(series.getModelNames().size() == 2) {
            lastModelIndex = 1;
            modelEdit.setText(series.getModelNames().get(1));
            return;
        }
        // 如果有且多于两条内容，则弹出model的选择框
        else {
            showListDialog(R.string.chooseModel, adapter, handler);
        }

        // 点击manufacturerEdit的事件
        modelEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showListDialog(R.string.chooseModel, adapter, handler);
            }
        });
    }

    /**
     * 设置国家edit
     */
    private void showListDialog(int titleId, ArrayAdapter<String> adapter, final Handler handler) {
        View view1 = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.popup_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view1)
                .create();

        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        final ListView listView = new ListView(view1.getContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                handler.sendEmptyMessage(i);
                dialog.dismiss();
            }
        });
        contentArea.addView(listView);

        setTextView(view1, R.id.title, getResources().getString(titleId));

        dialog.show();
    }

    /**
     * 生成基本信息JSONObject
     * @return
     */
    public JSONObject generateJSONObject() {
        JSONObject procedures = new JSONObject();

        try {
            procedures.put("vin", getEditViewText(rootView, R.id.vin_edit));
            procedures.put("engineSerial", getEditViewText(rootView, R.id.engineSerial_edit));
            procedures.put("plateNumber", getEditViewText(rootView, R.id.plateNumber_edit));
            procedures.put("licenseModel", getEditViewText(rootView, R.id.licenseModel_edit));
            procedures.put("vehicleType", getEditViewText(rootView, R.id.vehicleType_edit));
            procedures.put("mileage", getEditViewText(rootView, R.id.mileage_edit));
            procedures.put("exteriorColor", getEditViewText(rootView, R.id.exteriorColor_edit));
            procedures.put("regDate", getEditViewText(rootView, R.id.regDate_edit));
            procedures.put("builtDate", getEditViewText(rootView, R.id.builtDate_edit));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return procedures;
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * 更新页面，顺带更新车辆信息
     * @param procedures
     * @param seriesId
     * @param modelId
     */
    public void fillInData(JSONObject procedures, String seriesId, String modelId, OnUiUpdated listener) {
        try {
            mUiUpdatedCallback = listener;

//            setEditViewText(rootView, R.id.vin_edit, procedures.getString("vin"));
//            setEditViewText(rootView, R.id.engineSerial_edit, procedures.getString("engineSerial"));
//            setEditViewText(rootView, R.id.plateNumber_edit, procedures.getString("plateNumber"));
//            setEditViewText(rootView, R.id.licenseModel_edit, procedures.getString("licenseModel"));
//            setEditViewText(rootView, R.id.vehicleType_edit, procedures.getString("vehicleType"));
//            setEditViewText(rootView, R.id.mileage_edit, procedures.getString("mileage"));
//            setEditViewText(rootView, R.id.exteriorColor_edit, procedures.getString("exteriorColor"));
//            setEditViewText(rootView, R.id.regDate_edit, procedures.getString("regDate"));
//            setEditViewText(rootView, R.id.builtDate_edit, procedures.getString("builtDate"));

            setEditViewText(rootView, R.id.vin_edit, procedures.getString("vin"));

            if(procedures.has("engineSerial"))
                setEditViewText(rootView, R.id.engineSerial_edit, procedures.getString("engineSerial"));
            else
                setEditViewText(rootView, R.id.engineSerial_edit, procedures.getString("engineno"));
            if(procedures.has("plateNumber"))
                setEditViewText(rootView, R.id.plateNumber_edit, procedures.getString("plateNumber"));
            else
                setEditViewText(rootView, R.id.plateNumber_edit, procedures.getString("license"));
            if(procedures.has("licenseModel"))
                setEditViewText(rootView, R.id.licenseModel_edit, procedures.getString("licenseModel"));
            else
                setEditViewText(rootView, R.id.licenseModel_edit, procedures.getString("model2"));
            if(procedures.has("vehicleType"))
                setEditViewText(rootView, R.id.vehicleType_edit, procedures.getString("vehicleType"));
            else
                setEditViewText(rootView, R.id.vehicleType_edit, procedures.getString("licensetype"));
            if(procedures.has("mileage"))
                setEditViewText(rootView, R.id.mileage_edit, procedures.getString("mileage"));
            else
                setEditViewText(rootView, R.id.mileage_edit, procedures.getString("mileage"));
            if(procedures.has("exteriorColor"))
                setEditViewText(rootView, R.id.exteriorColor_edit, procedures.getString("exteriorColor"));
            else
                setEditViewText(rootView, R.id.exteriorColor_edit, procedures.getString("color"));
            if(procedures.has("regDate"))
                setEditViewText(rootView, R.id.regDate_edit, procedures.getString("regDate"));
            else
                setEditViewText(rootView, R.id.regDate_edit, procedures.getString("regdate"));
            if(procedures.has("builtDate"))
                setEditViewText(rootView, R.id.builtDate_edit, procedures.getString("builtDate"));
            else
                setEditViewText(rootView, R.id.builtDate_edit, procedures.getString("leavefactorydate"));

            // 更新配置信息
            Country country = null;
            Brand brand = null;
            Manufacturer manufacturer = null;
            Series series = null;
            Model model = null;

            boolean found = false;

            for(Country country1 : vehicleModel.getCountries()) {
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

            mCarSettings.setCountry(country);
            mCarSettings.setBrand(brand);
            mCarSettings.setManufacturer(manufacturer);
            mCarSettings.setSeries(series);
            mCarSettings.setModel(model);

            getCarSettingsFromServer(seriesId + "," + modelId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改或者半路检测时，填上已经保存的内容
     * 只更新页面
     * @param procedures
     */
    public void fillInData(JSONObject procedures) {
        try {
            setEditViewText(rootView, R.id.vin_edit, procedures.getString("vin"));
            setEditViewText(rootView, R.id.engineSerial_edit, procedures.getString("engineSerial"));
            setEditViewText(rootView, R.id.plateNumber_edit, procedures.getString("plateNumber"));
            setEditViewText(rootView, R.id.licenseModel_edit, procedures.getString("licenseModel"));
            setEditViewText(rootView, R.id.vehicleType_edit, procedures.getString("vehicleType"));
            setEditViewText(rootView, R.id.mileage_edit, procedures.getString("mileage"));
            setEditViewText(rootView, R.id.exteriorColor_edit, procedures.getString("exteriorColor"));
            setEditViewText(rootView, R.id.regDate_edit, procedures.getString("regDate"));
            setEditViewText(rootView, R.id.builtDate_edit, procedures.getString("builtDate"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取vin
     */
    public static String getVin() {
        return getEditViewText(rootView, R.id.vin_edit);
    }

    /**
     * 获取出厂日期
     */
    public static String getBuiltDate() {
        return getEditViewText(rootView, R.id.builtDate_edit);
    }

    /**
     * 获取车身颜色
     */
    public static String getExteriorColor() {
        return getEditViewText(rootView, R.id.exteriorColor_edit);
    }

    /**
     * CarCheckActivity实现此接口，获取到车辆配置信息时进行一些操作
     */
    public interface OnGetCarSettings {
        public void onGetCarSettings();
    }

    public interface OnUiUpdated {
        public void onUiUpdated();
    }
}
