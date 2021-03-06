package com.df.app.procedures;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.service.util.AppCommon;
import com.df.library.entries.Brand;
import com.df.library.entries.CarSettings;
import com.df.library.entries.Country;
import com.df.library.entries.Manufacturer;
import com.df.library.entries.Model;
import com.df.library.entries.Series;
import com.df.library.entries.VehicleModel;
import com.df.library.asyncTask.GetCarSettingsByVinTask;
import com.df.library.asyncTask.GetCarSettingsTask;
import com.df.library.asyncTask.UpdateAuthorizeCodeStatusTask;
import com.df.library.service.LicenseRecognise;
import com.df.library.util.Helper;
import com.df.library.util.MyAlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wintone.idcard.android.AuthParameterMessage;
import wintone.idcard.android.AuthService;
import wintone.idcard.android.IDCardCfg;

import static com.df.library.util.Helper.enableView;
import static com.df.library.util.Helper.getEditViewText;
import static com.df.library.util.Helper.isVin;
import static com.df.library.util.Helper.setEditViewText;
import static com.df.library.util.Helper.setTextView;
import static com.df.library.util.Helper.showView;

/**
 * Created by 岩 on 13-12-20.
 *
 * 车辆识别
 */
public class CarRecogniseLayout extends LinearLayout {
    OnShowContent mShowContentCallback;
    OnHideContent mHideContentCallback;

    static View rootView;

    // 此页面的6个信息控件
    public static EditText vehicleTypeEdit;
    public static EditText useCharacterEdit;

    // 车辆信息
    private VehicleModel vehicleModel;

    // 记录五个spinner最后选择的位置
    private int lastCountryIndex = 0;
    private int lastBrandIndex = 0;
    private int lastManufacturerIndex = 0;
    private int lastSeriesIndex = -50;
    private int lastModelIndex = 0;

    // 车辆配置信息
    private CarSettings mCarSettings;

    // 是否是修改手续
    private boolean modify;
    private int carId;
    private EditText countryEdit;
    private EditText brandEdit;
    private EditText manufacturerEdit;
    private EditText seriesEdit;
    private EditText modelEdit;

    private Button recogniseButton;
    private ImageView licenseImageView;

    private boolean cut = true;

    private LicenseRecognise licenseRecognise;
    public static int nMainID = 6;

    private AuthService.authBinder authBinder;
    public static int ReturnAuthority = -1;
    private String authCode = "";

    public ServiceConnection authConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            authBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            authBinder = (AuthService.authBinder) service;
            try {
                AuthParameterMessage apm = new AuthParameterMessage();

                apm.sn = authCode;
                apm.authfile = "";

                ReturnAuthority = authBinder.getIDCardAuth(apm);

                Log.d(AppCommon.TAG, ReturnAuthority == 0 ? "授权成功！" : "授权失败");

                if(ReturnAuthority != 0) {

                } else {
                    UpdateAuthorizeCodeStatusTask updateAuthorizeCodeStatusTask = new
                            UpdateAuthorizeCodeStatusTask(getContext(), new UpdateAuthorizeCodeStatusTask.OnUpdateFinished() {
                        @Override
                        public void onFinished(String result) {
                            // TODO nothing
                        }

                        @Override
                        public void onFailed(String result) {
                            // TODO nothing

                        }
                    });updateAuthorizeCodeStatusTask.execute();

                    createSnFile(authCode);
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "授权验证失败", Toast.LENGTH_LONG).show();
            } finally{
                if (authBinder != null) {
                    getContext().unbindService(authConn);
                }
            }
        }
    };


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

    private void init(final Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.car_recognise_layout, this);

        deleteLastLicensePhoto();

        mCarSettings = CarSettings.getInstance();

        licenseRecognise = new LicenseRecognise(context, AppCommon.licensePhotoPath);

        licenseImageView = (ImageView)findViewById(R.id.licenseImage);
        licenseImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                licenseRecognise.takePhoto();
            }
        });

        // 点击识别按钮
        recogniseButton = (Button) rootView.findViewById(R.id.recognise_button);
        recogniseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 询问是否要重新识别
                if(!mCarSettings.getBrandString().equals("")) {
                    reRecognise(R.string.reRecognise1);
                } else if(!getEditViewText(rootView, R.id.plateNumber_edit).equals("")) {
                    reRecognise(R.string.reRecognise);
                } else {
                    //fillInDummyData();
                    if(licensePhotoExist())
                        recogniseLicense();
                    else
                        Toast.makeText(context, "未拍摄行驶证照片！", Toast.LENGTH_SHORT).show();
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

    private void deleteLastLicensePhoto() {
        File file = new File(AppCommon.licensePhotoPath);
        file.delete();
    }

    private boolean licensePhotoExist() {
        File file = new File(AppCommon.licensePhotoPath);
        return file.exists();
    }

    private void createSnFile(String editsString) {
        if (editsString != null && !editsString.equals("")) {
            File file = new File(AppCommon.licenseUtilPath);
            if (!file.exists()) {
                file.mkdir();
            }
            String filePATH = AppCommon.licenseUtilPath + "/idcard.sn";
            File newFile = new File(filePATH);
            try {
                newFile.delete();
                newFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(newFile);
                StringBuffer sBuffer = new StringBuffer();
                sBuffer.append(editsString);
                fos.write(sBuffer.toString().toUpperCase().getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 调用行驶证识别接口
     */
    private void recogniseLicense() {
        //调用识别
        String selectPath = AppCommon.licensePhotoPath;
        boolean cutBoolean =  cut;

        try {
            Intent intent = new Intent("wintone.idcard");
            Bundle bundle = new Bundle();
            int nSubID[] = null;
            bundle.putInt("nTypeInitIDCard", 0);       //保留，传0即可
            bundle.putString("lpFileName", selectPath);//指定的图像路径
            bundle.putInt("nTypeLoadImageToMemory", 0);//0不确定是哪种图像，1可见光图，2红外光图，4紫外光图
            bundle.putInt("nMainID", nMainID);    //证件的主类型。6是行驶证，2是二代证，这里只可以传一种证件主类型。每种证件都有一个唯一的ID号，可取值见证件主类型说明
            bundle.putIntArray("nSubID", nSubID); //保存要识别的证件的子ID，每个证件下面包含的子类型见证件子类型说明。nSubID[0]=null，表示设置主类型为nMainID的所有证件。

            //读设置到文件里的sn
            File file = new File(AppCommon.licenseUtilPath);
            String snString = null;
            if (file.exists()) {
                String filePATH = AppCommon.licenseUtilPath + "/IdCard.sn";
                File newFile = new File(filePATH);
                if (newFile.exists()) {
                    BufferedReader bfReader = new BufferedReader(new FileReader(newFile));
                    snString= bfReader.readLine().toUpperCase();
                    bfReader.close();
                }else {
                    bundle.putString("sn", "");
                }
                if (snString != null && !snString.equals("")) {
                    bundle.putString("sn", snString);
                }else {
                    bundle.putString("sn", "");
                }
            }else {
                bundle.putString("sn", "");
            }

            bundle.putString("authfile","");   //文件激活方式  /mnt/sdcard/AndroidWT/357816040594713_zj.txt
            bundle.putString("logo", ""); //logo路径，logo显示在识别等待页面右上角
            bundle.putBoolean("isCut", cutBoolean);   //如不设置此项默认自动裁切
            bundle.putString("returntype", "withvalue");//返回值传递方式withvalue带参数的传值方式（新传值方式）
            intent.putExtras(bundle);
            ((Activity)getContext()).startActivityForResult(intent, 8);
        } catch (Exception e) {
            Toast.makeText(getContext(), "没有找到应用程序" + "wintone.idcard", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 重新识别提示框
     */
    private void reRecognise(final int text) {
        MyAlertDialog.showAlert(getContext(), text, R.string.alert, MyAlertDialog.BUTTON_STYLE_OK_CANCEL,
                new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message message) {
                        switch (message.what) {
                            case MyAlertDialog.POSITIVE_PRESSED:
                                //fillInDummyData();
                                recogniseLicense();
                                if(text == R.string.reRecognise1)
                                    mHideContentCallback.hideContent();
                                break;
                            case MyAlertDialog.NEGATIVE_PRESSED:
                                break;
                        }

                        return true;
                    }
                }));
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
        InputMethodManager imm = (InputMethodManager)rootView.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.vin_edit).getWindowToken(), 0);

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
        GetCarSettingsTask getCarSettingsTask = new GetCarSettingsTask(getContext(),
                seriesId, new GetCarSettingsTask.OnGetCarSettingsFinished() {
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
                        Toast.makeText(rootView.getContext(), result, Toast.LENGTH_SHORT).show();
                        Log.d("DFCarChecker", "获取车辆配置信息失败：" + result);
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

                                // 将传输回来的数据存储到list中（因为可能会有多个型号）
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
                                        lastSeriesIndex = manufacturer.getSeriesNames().indexOf(series.name);
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
                                    .setPositiveButton(R.string.ok, null)
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialogInterface) {
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

    /**
     * 显示选择车型的对话框
     */
    private void showSelectCarDialog() {
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

    /**
     * 修改时，更新配置信息
     * @param seriesId seriesId
     * @param modelId modelId
     */
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
     * 初始化车型选择的框
     * @param view
     */
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
     * 弹出选择框
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

            carId = jsonObject.getInt("carId");

            setEditViewText(rootView, R.id.plateNumber_edit, jsonObject.getString("plateNumber"));
            setEditViewText(rootView, R.id.licenseModel_edit, jsonObject.getString("licenseModel"));
            setEditViewText(rootView, R.id.vehicleType_edit, jsonObject.getString("vehicleType"));
            setEditViewText(rootView, R.id.useCharacter_edit, jsonObject.getString("useCharacter"));
            setEditViewText(rootView, R.id.engineSerial_edit, jsonObject.getString("engineSerial"));
            setEditViewText(rootView, R.id.vin_edit, jsonObject.getString("vin"));

            modifyCarSettings(jsonObject.getString("seriesId"), jsonObject.getString("modelId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void fillInData(int carId) {
        mShowContentCallback.modify(Integer.toString(carId));
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            fillInRecogData((String[])message.obj);
            return false;
        }
    });

    public static void fillInRecogData(String[] fields) {
        setEditViewText(rootView, R.id.plateNumber_edit, fields[0]);
        setEditViewText(rootView, R.id.licenseModel_edit, fields[1]);
        setEditViewText(rootView, R.id.vehicleType_edit, fields[2]);
        setEditViewText(rootView, R.id.useCharacter_edit, fields[3]);
        setEditViewText(rootView, R.id.engineSerial_edit, fields[4]);
        setEditViewText(rootView, R.id.vin_edit, fields[5]);
    }

    public void startAuthService(String authCode) {
        this.authCode = authCode;
        Intent authIntent = new Intent(getContext(), AuthService.class);
        getContext().bindService(authIntent, authConn, Service.BIND_AUTO_CREATE);
    }

    public void updateLicensePhoto(boolean cut) {
        this.cut = cut;

        Bitmap bitmap = BitmapFactory.decodeFile(AppCommon.licensePhotoPath);

        licenseImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        licenseImageView.setImageBitmap(bitmap);

        recogniseButton.setVisibility(VISIBLE);
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
