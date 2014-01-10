package com.df.app.CarCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.df.app.util.Helper.getEditViewText;
import static com.df.app.util.Helper.setEditViewText;

/**
 * Created by 岩 on 14-1-8.
 */
public class VehicleInfoLayout extends LinearLayout {
    private View rootView;

    private CarSettings mCarSettings;

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

    private String countryId;
    private String brandId;
    private String manufacturerId;
    private String seriesId;
    private String modelId;

    private UpdateUi mCallback;
    private ProgressDialog progressDialog;

    public VehicleInfoLayout(Context context, UpdateUi listener) {
        super(context);
        this.mCallback = listener;
        init(context);
    }

    public VehicleInfoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VehicleInfoLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.vehicle_info_layout, this);

        brandEdit = (EditText) rootView.findViewById(R.id.brand_edit);
        vehicleModel = MainActivity.vehicleModel;

        mCarSettings = BasicInfoLayout.mCarSettings;

        countryId = CarsWaitingActivity.countryId;
        brandId = CarsWaitingActivity.brandId;
        manufacturerId = CarsWaitingActivity.manufacturerId;
        seriesId = CarsWaitingActivity.seriesId;
        modelId = CarsWaitingActivity.modelId;

        // 点击品牌选择按钮
        Button brandSelectButton = (Button) rootView.findViewById(R.id.brand_select_button);
        brandSelectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCarManually();
            }
        });

        getCarSettingsFromServer(CarsWaitingActivity.seriesId + "," + CarsWaitingActivity.modelId);
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
                        countryId = country.id;
                        Brand brand = country.brands.get(lastBrandIndex - 1);
                        brandId = brand.id;
                        Manufacturer manufacturer = brand.manufacturers.get(lastManufacturerIndex - 1);
                        manufacturerId = manufacturer.id;
                        Series series = manufacturer.serieses.get(lastSeriesIndex - 1);
                        seriesId = series.id;
                        Model model = series.models.get(lastModelIndex - 1);
                        modelId = model.id;

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

        mCallback.updateUi();
    }

    // 更新车辆配置信息
    private void updateCarSettings(String config, String category, String figure) {
        Country country = vehicleModel.getCountryById(countryId);
        Brand brand = country.getBrandById(brandId);
        Manufacturer manufacturer = brand.getManufacturerById(manufacturerId);
        Series series = manufacturer.getSeriesById(seriesId);
        Model model = series.getModelById(modelId);

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
            CarsWaitingActivity.progressDialog.dismiss();

            String result = soapService.getResultMessage();

            // 如果成功通信
            if (success) {
                try {

                    JSONObject jsonObject = new JSONObject(result);
                    config = jsonObject.getString("config");
                    category = jsonObject.getString("category");
                    figure = jsonObject.getString("figure");

                    // 更新配置信息
                    updateCarSettings(config, category, figure);

                    // 更新UI
                    updateUi();
                } catch (JSONException e) {
                    Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                }
            }
            // 如果失败
            else {
                // 传输失败，获取错误信息并显示
                Log.d("DFCarChecker", "获取车辆配置信息失败：" + soapService.getErrorMessage());

                Toast.makeText(context, soapService.getErrorMessage(), Toast.LENGTH_LONG).show();

                ((Activity)getContext()).finish();
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
    // </editor-fold>

    public interface UpdateUi {
        public void updateUi();
    }
}
