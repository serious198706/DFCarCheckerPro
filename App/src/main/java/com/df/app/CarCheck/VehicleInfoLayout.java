package com.df.app.carCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.Brand;
import com.df.app.entries.CarSettings;
import com.df.app.entries.Country;
import com.df.app.entries.Manufacturer;
import com.df.app.entries.Model;
import com.df.app.entries.Series;
import com.df.app.entries.VehicleModel;
import com.df.app.service.AsyncTask.GetCarSettingsTask;
import com.df.app.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import static com.df.app.util.Helper.getEditViewText;
import static com.df.app.util.Helper.setEditViewText;

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
    private Spinner countrySpinner;
    private Spinner brandSpinner;
    private Spinner manufacturerSpinner;
    private Spinner seriesSpinner;
    private Spinner modelSpinner;

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
                ((Activity) getContext()).finish();
            }
        });
        mGetCarSettingsTask.execute();
    }

    /**
     * 手动选择车型
     */
    private void selectCarManually() {
        View view = LayoutInflater.from(rootView.getContext()).inflate(R.layout
                .dialog_vehiclemodel_select, null);

        TextView title = (TextView)view.findViewById(R.id.title);
        title.setText(R.string.select_model);

        countrySpinner = (Spinner) view.findViewById(R.id.country_spinner);
        brandSpinner = (Spinner) view.findViewById(R.id.brand_spinner);
        manufacturerSpinner = (Spinner) view.findViewById(R.id.manufacturer_spinner);
        seriesSpinner = (Spinner) view.findViewById(R.id.series_spinner);
        modelSpinner = (Spinner) view.findViewById(R.id.model_spinner);

        AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                .setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 记录用户选择的位置
                        lastCountryIndex = countrySpinner.getSelectedItemPosition();
                        lastBrandIndex = brandSpinner.getSelectedItemPosition();
                        lastManufacturerIndex = manufacturerSpinner.getSelectedItemPosition();
                        lastSeriesIndex = seriesSpinner.getSelectedItemPosition();
                        lastModelIndex = modelSpinner.getSelectedItemPosition();

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
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 取消
                        lastCountryIndex = countrySpinner.getSelectedItemPosition();
                        lastBrandIndex = brandSpinner.getSelectedItemPosition();
                        lastManufacturerIndex = manufacturerSpinner.getSelectedItemPosition();
                        lastSeriesIndex = seriesSpinner.getSelectedItemPosition();
                        lastModelIndex = modelSpinner.getSelectedItemPosition();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        // 记录用户选择的位置
                        lastCountryIndex = countrySpinner.getSelectedItemPosition();
                        lastBrandIndex = brandSpinner.getSelectedItemPosition();
                        lastManufacturerIndex = manufacturerSpinner.getSelectedItemPosition();
                        lastSeriesIndex = seriesSpinner.getSelectedItemPosition();
                        lastModelIndex = modelSpinner.getSelectedItemPosition();
                    }
                })
                .create();

        setCountrySpinner(vehicleModel);

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

        // 设置变速器形式Spinner
        if(modelString.contains("A/MT")) {
            mCarSettings.setTransmission("A/MT");
        } else if(modelString.contains("MT")) {
            mCarSettings.setTransmission("MT");
        } else if(modelString.contains("CVT") || modelString.contains("DSG")) {
            mCarSettings.setTransmission("CVT");
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

    // <editor-fold defaultstate="collapsed" desc="设置各种Spinner">

    /**
     * 设置国家Spinner
     * @param vehicleModel
     */
    private void setCountrySpinner(final VehicleModel vehicleModel) {
        ArrayAdapter<String> adapter;

        if(vehicleModel == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, vehicleModel.getCountryNames());
        }

        countrySpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 选择国别时，更改品牌的Spinner Adapter
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0) {
                    setBrandSpinner(null);
                } else if(i >= 1) {
                    setBrandSpinner(vehicleModel.countries.get(i - 1));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        countrySpinner.setSelection(lastCountryIndex);
        lastCountryIndex = 0;
    }

    /**
     * 设置品牌Spinner
     * @param country
     */
    private void setBrandSpinner(final Country country) {
        ArrayAdapter<String> adapter;
        if(country == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, country.getBrandNames());
        }

        brandSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 选择品牌时，更改厂商的Spinner Adapter
        brandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(country == null || i == 0) {
                    setManufacturerSpinner(null);
                } else if(i >= 1) {
                    setManufacturerSpinner(country.brands.get(i - 1));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 如果该项只有一个条目，则默认选中，否则选中上次记录的值
        if(country != null && country.getBrandNames().size() == 2) {
            brandSpinner.setSelection(1);
        } else {
            brandSpinner.setSelection(lastBrandIndex);
        }

        lastBrandIndex = 0;
    }

    /**
     * 设置厂商Spinner
     * @param brand
     */
    private void setManufacturerSpinner(final Brand brand) {
        ArrayAdapter<String> adapter;

        if(brand == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, brand.getManufacturerNames());
        }

        manufacturerSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 选择厂商时，更改车系的Spinner Adapter
        manufacturerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (brand == null || i == 0) {
                    setSeriesSpinner(null);
                } else if (i >= 1) {
                    setSeriesSpinner(brand.manufacturers.get(i - 1));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 如果该项只有一个条目，则默认选中，否则选中上次记录的值
        if(brand != null && brand.getManufacturerNames().size() == 2) {
            manufacturerSpinner.setSelection(1);
        } else {
            manufacturerSpinner.setSelection(lastManufacturerIndex);
        }

        lastManufacturerIndex = 0;
    }

    /**
     * 设置车系Spinner
     * @param manufacturer
     */
    private void setSeriesSpinner(final Manufacturer manufacturer) {
        ArrayAdapter<String> adapter;

        if(manufacturer == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, manufacturer.getSeriesNames());
        }

        seriesSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 选择车系时，更改型号的Spinner Adapter
        seriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (manufacturer == null || i == 0) {
                    setModelSpinner(null);
                } else if (i >= 1) {
                    setModelSpinner(manufacturer.serieses.get(i - 1));
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 如果该项只有一个条目，则默认选中，否则选中上次记录的值
        if(manufacturer != null && manufacturer.getSeriesNames().size() == 2) {
            seriesSpinner.setSelection(1);
        } else {
            seriesSpinner.setSelection(lastSeriesIndex);
        }

        lastSeriesIndex = 0;
    }

    /**
     * 设置车型Spinner
     * @param series
     */
    private void setModelSpinner(final Series series) {
        ArrayAdapter<String> adapter;

        if(series == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, series.getModelNames());
        }

        modelSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 如果该项只有一个条目，则默认选中，否则选中上次记录的值
        if(series != null && series.getModelNames().size() == 2) {
            modelSpinner.setSelection(1);
        } else {
            modelSpinner.setSelection(lastModelIndex);
        }

        lastModelIndex = 0;
    }
    // </editor-fold>

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
