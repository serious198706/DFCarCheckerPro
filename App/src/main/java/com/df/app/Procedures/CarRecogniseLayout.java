package com.df.app.Procedures;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
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

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.Brand;
import com.df.app.entries.CarSettings;
import com.df.app.entries.Country;
import com.df.app.entries.Manufacturer;
import com.df.app.entries.Model;
import com.df.app.entries.Series;
import com.df.app.entries.VehicleModel;
import com.df.app.service.SoapService;
import com.df.app.util.Common;
import com.df.app.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.df.app.util.Helper.enableView;
import static com.df.app.util.Helper.getEditViewText;
import static com.df.app.util.Helper.isVin;
import static com.df.app.util.Helper.setEditViewText;
import static com.df.app.util.Helper.showView;

/**
 * Created by 岩 on 13-12-20.
 */
public class CarRecogniseLayout extends LinearLayout {
    OnShowContentListener mShowContentCallback;
    OnUpdatePreviewListener mUpdatePreviewCallback;
    View rootView;

    // 此页面的6个信息控件
    public static EditText plateNumberEdit;
    public static EditText licenceModelEdit;
    public static EditText vehicleTypeEdit;
    public static EditText useCharacterEdit;
    public static EditText engineSerailEdit;
    public static EditText brandEdit;

    // soapservice
    private SoapService soapService;

    // 车辆信息
    private VehicleModel vehicleModel;

    // 获取车辆配置信息的线程
    private GetCarSettingsTask mGetCarSettingsTask;

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

    // 是否为进口车
    public static boolean isPorted;

    // 车辆配置信息
    private CarSettings mCarSettings;

    public CarRecogniseLayout(Context context, OnShowContentListener listener) {
        super(context);
        mShowContentCallback = listener;
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.car_recognise_layout, this);

        mCarSettings = InputProceduresLayout.mCarSettings;

        // 点击识别按钮
        Button recogniseButton = (Button) rootView.findViewById(R.id.recognise_button);
        recogniseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 询问是否要重新识别
                if(!getEditViewText(rootView, R.id.plateNumber_edit).equals("")) {
                    AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                            .setTitle(R.string.alert)
                            .setMessage("确定要重新识别行驶证信息？")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) { fillInDummyData(); }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            }).create();

                    dialog.show();
                } else {
                    fillInDummyData();
                }
            }
        });

        // 点击vin确定按钮
        Button vinConfirmButton = (Button) rootView.findViewById(R.id.vinConfirm_button);
        vinConfirmButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 如果VIN码为空，则不显示内容
                if(getEditViewText(rootView, R.id.vin_edit).equals("")) {
                    Toast.makeText(rootView.getContext(), "请填写VIN码", Toast.LENGTH_SHORT).show();
                } else {
                    checkVinAndGetCarSettings();
                }
            }
        });


        // 点击品牌选择按钮
        Button brandSelectButton = (Button) rootView.findViewById(R.id.brand_select_button);
        brandSelectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCarManually();
            }
        });

        plateNumberEdit = (EditText) rootView.findViewById(R.id.plateNumber_edit);
        licenceModelEdit = (EditText) rootView.findViewById(R.id.licenseModel_edit);
        vehicleTypeEdit = (EditText) rootView.findViewById(R.id.vehicleType_edit);
        useCharacterEdit = (EditText) rootView.findViewById(R.id.useCharacter_edit);
        engineSerailEdit = (EditText) rootView.findViewById(R.id.engineSerial_edit);
        brandEdit = (EditText) rootView.findViewById(R.id.brand_edit);

        vehicleModel = MainActivity.vehicleModel;
    }

    // 检查VIN并获取车辆配置
    private void checkVinAndGetCarSettings() {
        final String vinString = getEditViewText(rootView, R.id.vin_edit);

        // 是否为空
        if(vinString.equals("")) {
            Toast.makeText(rootView.getContext(), "请输入VIN码", Toast.LENGTH_SHORT).show();
            findViewById(R.id.vin_edit).requestFocus();
            return;
        }

        // 检查VIN码
        if(!isVin(vinString)) {
            AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                    .setTitle(R.string.alert)
                    .setMessage("您输入的VIN码为: " + vinString + "\n" + "系统检测到VIN码可能有误，是否确认继续提交？\n" )
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setEditViewText(rootView, R.id.brand_edit, "");
                            findViewById(R.id.brand_select_button).setEnabled(false);

                            // 传参数为空，表示提交的为VIN
                            getCarSettingsFromServer("");
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            findViewById(R.id.vin_edit).requestFocus();
                        }
                    }).create();

            dialog.show();
            return;
        }

        setEditViewText(rootView, R.id.brand_edit, "");
        findViewById(R.id.brand_select_button).setEnabled(false);

        // 传参数为空，表示提交的为VIN
        getCarSettingsFromServer("");
    }

    // 从服务器获取车辆配置
    private void getCarSettingsFromServer(String seriesId) {
        mGetCarSettingsTask = new GetCarSettingsTask(rootView.getContext());
        mGetCarSettingsTask.execute(seriesId);
    }

    // 手动选择车型
    private void selectCarManually() {
        View view = LayoutInflater.from(rootView.getContext()).inflate(R.layout
                .dialog_vehiclemodel_select, null);

        TextView title = (TextView)view.findViewById(R.id.title);
        title.setText(R.string.select_model);

        countrySpinner = (Spinner) view.findViewById(R.id.country_spinner);
        brandSpinner = (Spinner) view.findViewById(R.id.brand_spinner);
        manufacturerSpinner = (Spinner) view.findViewById(R.id.production_spinner);
        seriesSpinner = (Spinner) view.findViewById(R.id.serial_spinner);
        modelSpinner = (Spinner) view.findViewById(R.id.model_spinner);

        AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                .setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 确定
                        // 判断是否为进口车

                        if (countrySpinner.getSelectedItemPosition() > 1) {
                            isPorted = true;
                        } else {
                            isPorted = false;
                        }

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

    // 更新UI
    private void updateUi() {
        // 设置厂牌型号的EditText
        setEditViewText(rootView, R.id.brand_edit, mCarSettings.getBrandString());

        // 不能再修改此页面的下列信息，只允许在手续信息页修改
        enableView(rootView, R.id.plateNumber_edit, false);
        enableView(rootView, R.id.licenseModel_edit, false);
        enableView(rootView, R.id.vehicleType_edit, false);
        enableView(rootView, R.id.useCharacter_edit, false);
        enableView(rootView, R.id.engineSerial_edit, false);

        // 其他页面的显示与ui更新
        mShowContentCallback.showContent();
        mShowContentCallback.updateUi();
    }

    // 更新车辆配置信息
    private void updateCarSettings(String config, String category, String figure) {
        Country country = vehicleModel.countries.get(lastCountryIndex - 1);
        Brand brand = country.brands.get(lastBrandIndex - 1);
        Manufacturer manufacturer = brand.manufacturers.get(lastManufacturerIndex - 1);
        Series series = manufacturer.serieses.get(lastSeriesIndex - 1);
        Model model = series.models.get(lastModelIndex - 1);

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
        mCarSettings.setCountry(country);
        mCarSettings.setBrand(brand);
        mCarSettings.setManufacturer(manufacturer);
        mCarSettings.setSeries(series);
        mCarSettings.setModel(model);

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
        String categoryArray[] = getResources().getStringArray(R
                .array.category_item);

        if(Integer.parseInt(category) > 0)
            mCarSettings.setCategory(categoryArray[Integer.parseInt
                    (category) - 1]);
        else
            mCarSettings.setCategory(categoryArray[Integer.parseInt
                    (category)]);

        mCarSettings.setFigure(figure);

        enableView(rootView, R.id.brand_select_button, true);
    }

    // <editor-fold defaultstate="collapsed" desc="获取车辆配置信息的Task">

    private class GetCarSettingsTask extends AsyncTask<String, Void, Boolean> {
        Context context;
        String seriesId;
        String modelId;

        String modelName = "";
        List<String> modelNames;
        JSONObject jsonObject;
        List<JSONObject> jsonObjects;

        Country country = null;
        Brand brand = null;
        Manufacturer manufacturer = null;
        Series series = null;
        Model model = null;
        String config = null;
        String category = null;
        String figure = null;

        ProgressDialog mProgressDialog;

        private GetCarSettingsTask(Context context) {
            this.context = context;
            this.seriesId = null;
        }

        @Override
        protected void onPreExecute()
        {
            mProgressDialog = ProgressDialog.show(rootView.getContext(), null,
                    "正在获取车辆信息，请稍候。。", false, false);
            model = null;
            modelName = "";
            modelNames = null;
            jsonObject = null;
            jsonObjects = null;
        }


        @Override
        protected Boolean doInBackground(String... params) {
            boolean success = false;

            // 传输seriesId和modelId
            if(!params[0].equals("")) {
                // 从传入的参数中解析出seriesId和modelId
                String temp[] = params[0].split(",");
                seriesId = temp[0];
                modelId = temp[1];

                try {
                    JSONObject jsonObject = new JSONObject();

                    // SeriesId + userID + key
                    jsonObject.put("SeriesId", seriesId);
                    jsonObject.put("ModelId", modelId);
                    jsonObject.put("UserId", MainActivity.userInfo.getId());
                    jsonObject.put("Key", MainActivity.userInfo.getKey());

                    soapService = new SoapService();

                    // 设置soap的配置
                    soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE,
                            Common.GET_OPTIONS_BY_SERIESIDANDMODELID);

                    success = soapService.communicateWithServer(jsonObject.toString());
                } catch (JSONException e) {
                    Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                    return false;
                }
            }
            // 传输VIN
            else {
                try {
                    JSONObject jsonObject = new JSONObject();

                    // vin + userID + key
                    jsonObject.put("Vin", getEditViewText(rootView, R.id.vin_edit));
                    jsonObject.put("UserId", MainActivity.userInfo.getId());
                    jsonObject.put("Key", MainActivity.userInfo.getKey());

                    soapService = new SoapService();

                    // 设置soap的配置
                    soapService.setUtils(Common.SERVER_ADDRESS + Common.CAR_CHECK_SERVICE,
                            Common.GET_OPTIONS_BY_VIN);

                    success = soapService.communicateWithServer(jsonObject.toString());
                } catch (JSONException e) {
                    Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                    return false;
                }
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mGetCarSettingsTask = null;

            mProgressDialog.dismiss();

            String result = soapService.getResultMessage();

            // 如果成功通信
            if (success) {
                try {
                    // 开始位为[，表示传输的是全部信息
                    if(result.startsWith("[")) {
                        JSONArray jsonArray = new JSONArray(result);

                        // 用来存储车辆配置信息的JsonObject list
                        jsonObjects = new ArrayList<JSONObject>();

                        // 用来存储车辆型号的string list
                        modelNames = new ArrayList<String>();

                        for(int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            jsonObjects.add(jsonObject);

                            // 如果某个ID不存在，则默认使用第一个
                            if(jsonObject.has("countryId"))
                                country = vehicleModel.getCountryById(jsonObject.getString("countryId"));
                            else
                                country = vehicleModel.countries.get(0);

                            if(jsonObject.has("brandId"))
                                brand = country.getBrandById(jsonObject.getString("brandId"));
                            else
                                brand = country.brands.get(0);

                            if(jsonObject.has("manufacturerId"))
                                manufacturer = brand.getManufacturerById(jsonObject.getString("manufacturerId"));
                            else
                                manufacturer = brand.manufacturers.get(0);

                            if(jsonObject.has("seriesId"))
                                series = manufacturer.getSeriesById(jsonObject.getString("seriesId"));
                            else
                                series = manufacturer.serieses.get(0);

                            if(jsonObject.has("modelId"))
                                model = series.getModelById(jsonObject.getString("modelId"));
                            else
                                model = series.models.get(0);

                            // 配置信息
                            // config:powerSteering,powerWindows,powerSeats
                            config = jsonObject.getString("config");

                            // 车辆类型，不做处理，提交时直接提交
                            category = jsonObject.getString("category");

                            // 以此判断车辆图形
                            figure = jsonObject.getString("figure");

                            modelNames.add(manufacturer.name + " " + series.name + " " + model.name);
                        }

                        // 弹出一个对话框，供用户进行选择
                        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());

                        builder.setTitle(R.string.select_model);

                        String[] tempArray = new String[modelNames.size() + 1];

                        for(int i = 0; i < modelNames.size(); i++) {
                            tempArray[i] = modelNames.get(i);
                        }

                        // 加入一个“其他”，当用户点击时，直接弹出型号选择页面
                        tempArray[modelNames.size()] = "其他";

                        builder.setItems(tempArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 如果点击了“其他”（也就是传递回来的配置不符合现车）
                                if(i == modelNames.size()) {
                                    selectCarManually();
                                }
                                // 选择了某一辆车
                                else {
                                    modelName = modelNames.get(i);
                                    jsonObject = jsonObjects.get(i);

                                    try {
                                        country = vehicleModel.getCountryById(jsonObject.getString("countryId"));
                                        brand = country.getBrandById(jsonObject.getString("brandId"));
                                        manufacturer = brand.getManufacturerById(jsonObject.getString("manufacturerId"));
                                        series = manufacturer.getSeriesById(jsonObject.getString("seriesId"));
                                        model = series.getModelById(jsonObject.getString("modelId"));

                                        config = jsonObject.getString("config");
                                        category = jsonObject.getString("category");
                                        figure = jsonObject.getString("figure");

                                        // 根据用户选择的车型的id，记录车型选择spinner的位置
                                        lastCountryIndex = vehicleModel.getCountryNames().indexOf(country.name);
                                        lastBrandIndex = country.getBrandNames().indexOf(brand.name);
                                        lastManufacturerIndex = brand.getManufacturerNames().indexOf(manufacturer.name);
                                        lastSeriesIndex = manufacturer.getSerialNames().indexOf(series.name);
                                        lastModelIndex = series.getModelNames().indexOf(model.name);

                                        // 更新配置信息
                                        updateCarSettings(config, category, figure);

                                        // 更新UI
                                        updateUi();
                                    } catch (JSONException e) {
                                        Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                                    }
                                }
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();

                        showView(rootView, R.id.brand_input, true);
                    }
                    // 开始位为{，表示传输的是配置信息 config:powerWindows,powerSteer...
                    else {
                        JSONObject jsonObject = new JSONObject(result);
                        config = jsonObject.getString("config");
                        category = jsonObject.getString("category");
                        figure = jsonObject.getString("figure");

                        // 更新配置信息
                        updateCarSettings(config, category, figure);

                        // 更新UI
                        updateUi();
                    }
                } catch (JSONException e) {
                    Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                }
            }
            // 如果失败
            else {
                // 传输失败，获取错误信息并显示
                Log.d("DFCarChecker", "获取车辆配置信息失败：" + soapService.getErrorMessage());

                if(soapService.getErrorMessage().equals("没有检测到任何有关此VIN的相关配置信息")) {
                    AlertDialog dialog = new AlertDialog.Builder(this.context)
                            .setTitle(R.string.alert)
                            .setMessage("没有匹配的车型")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // 没有匹配的车型，手动选择
                                    selectCarManually();
                                    showView(rootView, R.id.brand_input, true);
                                }
                            }).create();

                    dialog.show();

                    // 选择完车型后，更新界面
                    updateUi();
                }
                else {
                    Toast.makeText(context, soapService.getErrorMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mGetCarSettingsTask = null;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置各种Spinner">
    // 设置国家Spinner
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

    // 设置品牌Spinner
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

    // 设置厂商Spinner
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

    // 设置车系Spinner
    private void setSeriesSpinner(final Manufacturer manufacturer) {
        ArrayAdapter<String> adapter;

        if(manufacturer == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, manufacturer.getSerialNames());
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
        if(manufacturer != null && manufacturer.getSerialNames().size() == 2) {
            seriesSpinner.setSelection(1);
        } else {
            seriesSpinner.setSelection(lastSeriesIndex);
        }

        lastSeriesIndex = 0;
    }

    // 设置车型Spinner
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

    private void fillInDummyData() {
        // 可以修改此页面的下列信息
        enableView(rootView, R.id.plateNumber_edit, true);
        enableView(rootView, R.id.licenseModel_edit, true);
        enableView(rootView, R.id.vehicleType_edit, true);
        enableView(rootView, R.id.useCharacter_edit, true);
        enableView(rootView, R.id.engineSerial_edit, true);

        setEditViewText(rootView, R.id.plateNumber_edit, "粤M26332");
        setEditViewText(rootView, R.id.licenseModel_edit, "奇瑞SQR7160");
        setEditViewText(rootView, R.id.vehicleType_edit, "小型轿车");
        setEditViewText(rootView, R.id.useCharacter_edit, "非营运");
        setEditViewText(rootView, R.id.engineSerial_edit, "K1H00875");
        setEditViewText(rootView, R.id.vin_edit, "LSJDA11A21D012476");
    }

    // BasicInfoLayout必须实现此方法
    // 显示手续信息与基本信息的内容
    public interface OnShowContentListener {
        public void showContent();
        public void updateUi();
    }

    // IntegratedCheckLayout必须实现此方法
    public interface OnUpdatePreviewListener {
        public void updatePreview();
    }
}
