package com.df.app.procedures;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
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
import com.df.app.service.AsyncTask.GetCarSettingsByVinTask;
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
import static com.df.app.util.Helper.setTextView;
import static com.df.app.util.Helper.showView;

/**
 * Created by 岩 on 13-12-20.
 *
 * 车辆识别
 */
public class CarRecogniseLayout extends LinearLayout {
    OnShowContent mShowContentCallback;
    OnHideContent mHideContentCallback;

    View rootView;

    // 此页面的6个信息控件
    public static EditText vehicleTypeEdit;
    public static EditText useCharacterEdit;

    // 车辆信息
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

    // 车辆配置信息
    private CarSettings mCarSettings;

    // 是否是修改手续
    private boolean modify;
    private int carId;

    /**
     * 传入回调函数指针
     * @param context
     * @param sListener
     */
    public CarRecogniseLayout(Context context, OnShowContent sListener, OnHideContent hListener) {
        super(context);
        mShowContentCallback = sListener;
        mHideContentCallback = hListener;
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
                if(!mCarSettings.getBrandString().equals("")) {
                    reRecognise(R.string.reRecognise1);
                } else if(!getEditViewText(rootView, R.id.plateNumber_edit).equals("")) {
                    reRecognise(R.string.reRecognise);
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

        vehicleTypeEdit = (EditText) rootView.findViewById(R.id.vehicleType_edit);
        vehicleTypeEdit.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP:
                            choose(R.array.vehicleType_items, R.id.vehicleType_edit);
                            break;
                    }
                return false;
            }
        });

        useCharacterEdit = (EditText) rootView.findViewById(R.id.useCharacter_edit);
        useCharacterEdit.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        choose(R.array.useCharacter_items, R.id.useCharacter_edit);
                        break;
                }
                return false;
            }
        });

        // vin输入框中只允许输入大写字母与数字
        InputFilter alphaNumericFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence arg0, int arg1, int arg2, Spanned arg3, int arg4, int arg5)
            {
                for (int k = arg1; k < arg2; k++) {
                    if (!Character.isLetterOrDigit(arg0.charAt(k))) {
                        return "";
                    }
                }
                return null;
            }
        };
        EditText vin_edit = (EditText)findViewById(R.id.vin_edit);
        vin_edit.setFilters(new InputFilter[]{ alphaNumericFilter, new InputFilter.AllCaps(), new InputFilter.LengthFilter(17)});

        EditText plateNumberEdit = (EditText)findViewById(R.id.plateNumber_edit);
        plateNumberEdit.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(10)});

        EditText licenseModelEdit = (EditText)findViewById(R.id.licenseModel_edit);
        licenseModelEdit.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(22)});

        EditText engineSerialEdit = (EditText)findViewById(R.id.engineSerial_edit);
        engineSerialEdit.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(17)});

        vehicleModel = MainActivity.vehicleModel;
    }

    /**
     * 重新识别提示框
     */
    private void reRecognise(final int text) {
        View view1 = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(text);
        content.setTextSize(20f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, getResources().getString(R.string.alert));

        AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                .setView(view1)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        fillInDummyData();
                        if(text == R.string.reRecognise1)
                            mHideContentCallback.hideContent();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).create();

        dialog.show();
    }

    /**
     * 弹出选择框
     * @param arrayId
     * @param editViewId
     */
    private void choose(final int arrayId, final int editViewId) {
        View view1 = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.popup_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view1)
                .create();

        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        final ListView listView = new ListView(view1.getContext());
        listView.setAdapter(new ArrayAdapter<String>(view1.getContext(), android.R.layout.simple_list_item_1,
                view1.getResources().getStringArray(arrayId)));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialog.dismiss();
                String temp = (String) listView.getItemAtPosition(i);
                setEditViewText(rootView, editViewId, temp);
            }
        });
        contentArea.addView(listView);

        setTextView(view1, R.id.title, getResources().getString(R.string.alert));

        dialog.show();
    }


    /**
     * 检查VIN并获取车辆配置
     */
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
            View view1 = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.popup_layout, null);
            TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
            TextView content = new TextView(view1.getContext());
            content.setText("您输入的VIN码为: " + vinString + "\n" + "系统检测到VIN码可能有误，是否确认继续提交？\n" );
            content.setTextSize(20f);
            contentArea.addView(content);

            setTextView(view1, R.id.title, getResources().getString(R.string.alert));

            AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                    .setView(view1)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setEditViewText(rootView, R.id.brand_edit, "");
                            findViewById(R.id.brand_select_button).setEnabled(false);

                            // 无参数，表示提交的为VIN
                            getCarSettingsFromServer();
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
        getCarSettingsFromServer();
    }

    /**
     * 从服务器获取车辆配置(seriesId + modelId)
     * @param seriesId seriesId + modelId
     */
    private void getCarSettingsFromServer(String seriesId) {
        com.df.app.service.AsyncTask.GetCarSettingsTask getCarSettingsTask = new com.df.app.service.AsyncTask.GetCarSettingsTask(getContext(), seriesId,
                new com.df.app.service.AsyncTask.GetCarSettingsTask.OnGetCarSettingsFinished() {
                    @Override
                    public void onFinished(String result) {
                        // 更新车辆配置
                        updateCarSettings();

                        // 更新UI
                        updateUi();
                    }

                    @Override
                    public void onFailed(String result) {
                        // 传输失败，获取错误信息并显示
                        Log.d("DFCarChecker", "获取车辆配置信息失败：" + result);
                        Toast.makeText(rootView.getContext(), result, Toast.LENGTH_LONG).show();
                    }
                });
        getCarSettingsTask.execute();
    }

    /**
     * 从服务器获取车辆配置 (vin)
     */
    private void getCarSettingsFromServer() {
        String vin = getEditViewText(rootView, R.id.vin_edit);

        GetCarSettingsByVinTask getCarSettingsByVinTask = new GetCarSettingsByVinTask(getContext(), vin,
                new GetCarSettingsByVinTask.OnGetCarSettingsFinished() {
                    @Override
                    public void onFinished(String result) {
                        final List<String> modelNames;
                        final List<JSONObject> jsonObjects;

                        try {
                            JSONArray jsonArray = new JSONArray(result);
                            jsonObjects = new ArrayList<JSONObject>();

                            // 用来存储车辆型号的string list
                            modelNames = new ArrayList<String>();

                            for(int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                Country country;
                                Brand brand;
                                Manufacturer manufacturer;
                                Series series;
                                Model model;

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

                                // 将传输回来的信息存储到list中（因为可能会有多个型号）
                                jsonObjects.add(jsonObject);
                                modelNames.add(manufacturer.name + " " + series.name + " " + model.name);
                            }

                            String[] tempArray = new String[modelNames.size() + 1];

                            for(int i = 0; i < modelNames.size(); i++) {
                                tempArray[i] = modelNames.get(i);
                            }

                            showModelChooseDialog(tempArray, new OnChooseModelFinished() {
                                @Override
                                public void onFinished(int index) throws JSONException {
                                    // 如果点击了“其他”（也就是传递回来的配置不符合现车）
                                    if (index == modelNames.size()) {
                                        selectCarManually();
                                    }
                                    // 选择了某一辆车
                                    else {
                                        JSONObject jsonObject = jsonObjects.get(index);

                                        Country country = vehicleModel.getCountryById(jsonObject.getString("countryId"));
                                        Brand brand = country.getBrandById(jsonObject.getString("brandId"));
                                        Manufacturer manufacturer = brand.getManufacturerById(jsonObject.getString("manufacturerId"));
                                        Series series = manufacturer.getSeriesById(jsonObject.getString("seriesId"));
                                        Model model = series.getModelById(jsonObject.getString("modelId"));

                                        // 根据用户选择的车型的id，记录车型选择spinner的位置
                                        lastCountryIndex = vehicleModel.getCountryNames().indexOf(country.name);
                                        lastBrandIndex = country.getBrandNames().indexOf(brand.name);
                                        lastManufacturerIndex = brand.getManufacturerNames().indexOf(manufacturer.name);
                                        lastSeriesIndex = manufacturer.getSerialNames().indexOf(series.name);
                                        lastModelIndex = series.getModelNames().indexOf(model.name);

                                        // 更新配置信息
                                        updateCarSettings();

                                        // 更新UI
                                        updateUi();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailed(String result) {
                        // 传输失败，获取错误信息并显示
                        Log.d("DFCarChecker", "获取车辆配置信息失败：" + result);

                        if(result.equals("没有检测到任何有关此VIN的相关配置信息")) {
                            View view1 = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.popup_layout, null);
                            TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
                            TextView content = new TextView(view1.getContext());
                            content.setText("没有匹配的车型");
                            content.setTextSize(20f);
                            contentArea.addView(content);

                            setTextView(view1, R.id.title, getResources().getString(R.string.alert));

                            AlertDialog dialog = new AlertDialog.Builder(getContext())
                                    .setView(view1)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            // 没有匹配的车型，手动选择
                                            selectCarManually();
                                            showView(rootView, R.id.brand_input, true);
                                        }
                                    })
                                    .create();

                            dialog.show();

                            // 选择完车型后，更新界面
                            updateUi();
                        }
                        else {
                            // 传输失败，获取错误信息并显示
                            Log.d("DFCarChecker", "获取车辆配置信息失败：" + result);
                            Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
                        }
                    }
                });
        getCarSettingsByVinTask.execute();
    }

    /**
     * 如果回传多个车辆，显示车辆型号选择
     * @param array
     * @param mCallback
     * @throws JSONException
     */
    private void showModelChooseDialog(String[] array, final OnChooseModelFinished mCallback) throws JSONException{
        array[array.length - 1] = "其他";

        View view1 = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.popup_layout, null);

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view1)
                .create();

        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        final ListView listView = new ListView(view1.getContext());
        listView.setAdapter(new ArrayAdapter<String>(view1.getContext(), android.R.layout.simple_list_item_1, array));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    mCallback.onFinished(i);
                    dialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        contentArea.addView(listView);

        setTextView(view1, R.id.title, getResources().getString(R.string.select_model));

        dialog.show();
    }

    /**
     * 手动选择车型
     */
    private void selectCarManually() {
        if(!mCarSettings.getBrandString().equals("")) {
            View view1 = ((Activity)rootView.getContext()).getLayoutInflater().inflate(R.layout.popup_layout, null);
            TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
            TextView content = new TextView(view1.getContext());
            content.setText(R.string.reMatch);
            content.setTextSize(20f);
            contentArea.addView(content);

            setTextView(view1, R.id.title, getResources().getString(R.string.alert));

            AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                    .setView(view1)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mHideContentCallback.hideContent();
                            showSelectCarDialog();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create();

            dialog.show();
        } else {
            showSelectCarDialog();
        }
    }

    private void showSelectCarDialog() {
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
        showView(rootView, R.id.brand_input, true);
        showView(rootView, R.id.recognise_button, false);

        // 设置厂牌型号的EditText
        setEditViewText(rootView, R.id.brand_edit, mCarSettings.getBrandString());

        // 不能再修改此页面的下列信息，只允许在手续信息页修改
        enableView(rootView, R.id.plateNumber_edit, false);
        enableView(rootView, R.id.licenseModel_edit, false);
        enableView(rootView, R.id.vehicleType_edit, false);
        enableView(rootView, R.id.useCharacter_edit, false);
        enableView(rootView, R.id.engineSerial_edit, false);

        if(mCarSettings.getSeries() != null)
            // 正常录入
            if(!modify) {
                // 其他页面的显示与ui更新
                mShowContentCallback.showContent(getEditViewText(rootView, R.id.vin_edit),
                        getEditViewText(rootView, R.id.plateNumber_edit),
                        getEditViewText(rootView, R.id.licenseModel_edit),
                        getEditViewText(rootView, R.id.vehicleType_edit),
                        getEditViewText(rootView, R.id.useCharacter_edit),
                        getEditViewText(rootView, R.id.engineSerial_edit),
                        mCarSettings.getSeries().id,
                        mCarSettings.getModel().id);
            }
            // 修改
            else {
                mShowContentCallback.modify(Integer.toString(carId));
            }

    }

    /**
     * 更新车辆配置信息
     */
    private void updateCarSettings() {
        Country country = vehicleModel.countries.get(lastCountryIndex - 1);
        Brand brand = country.brands.get(lastBrandIndex - 1);
        Manufacturer manufacturer = brand.manufacturers.get(lastManufacturerIndex - 1);
        Series series = manufacturer.serieses.get(lastSeriesIndex - 1);
        Model model = series.models.get(lastModelIndex - 1);

        // 车型
        String brandString = manufacturer.name + " " + series.name + " " + model.name;

        // 更新配置信息类
        mCarSettings.setBrandString(brandString);
        mCarSettings.setCountry(country);
        mCarSettings.setBrand(brand);
        mCarSettings.setManufacturer(manufacturer);
        mCarSettings.setSeries(series);
        mCarSettings.setModel(model);

        enableView(rootView, R.id.brand_select_button, true);
    }

    private void modifyCarSettings(String seriesId, String modelId) {
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

        // 车型
        String brandString = manufacturer.name + " " + series.name + " " + model.name;

        // 更新配置信息类
        mCarSettings.setBrandString(brandString);

        updateUi();
    }

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
                if (country == null || i == 0) {
                    setManufacturerSpinner(null);
                } else if (i >= 1) {
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

    /**
     * 填入测试数据
     */
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

    /**
     * 修改手续信息时，填入数据
     * @param jsonString
     */
    public void fillInData(String jsonString) {
        modify = true;

        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            setEditViewText(rootView, R.id.plateNumber_edit, jsonObject.getString("plateNumber"));
            setEditViewText(rootView, R.id.licenseModel_edit, jsonObject.getString("licenseModel"));
            setEditViewText(rootView, R.id.vehicleType_edit, jsonObject.getString("vehicleType"));
            setEditViewText(rootView, R.id.useCharacter_edit, jsonObject.getString("useCharacter"));
            setEditViewText(rootView, R.id.engineSerial_edit, jsonObject.getString("engineSerial"));
            setEditViewText(rootView, R.id.vin_edit, jsonObject.getString("vin"));

            modifyCarSettings(jsonObject.getString("seriesId"), jsonObject.getString("modelId"));

            carId = jsonObject.getInt("carId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * InputProceduresLayout必须实现此接口
     * 显示手续信息的标签及内容
     */
    public interface OnShowContent {
        public void showContent(String vin, String plateNumber, String licenseModel, String vehicleType, String useCharacter, String engineSerial,
            String seriesId, String modelId);
        public void modify(String carId);
    }

    /**
     * InputProceduresLayout必须实现此接口
     * 隐藏手续信息的标签及内容
     */
    public interface OnHideContent {
        public void hideContent();
    }

    /**
     * getCarSettingsFromServer()中实现此接口
     */
    public interface OnChooseModelFinished {
        public void onFinished(int index) throws JSONException;
    }
}
