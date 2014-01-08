package com.df.app.Procedures;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableRow;

import com.df.app.Procedures.CarRecogniseLayout;
import com.df.app.R;
import com.df.app.service.MyScrollView;
import com.df.app.util.Common;
import com.df.app.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import static com.df.app.util.Helper.SetSpinnerData;
import static com.df.app.util.Helper.getEditViewText;
import static com.df.app.util.Helper.setEditViewText;
import static com.df.app.util.Helper.setSpinnerSelectionWithIndex;
import static com.df.app.util.Helper.setSpinnerSelectionWithString;
import static com.df.app.util.Helper.setTextView;
import static com.df.app.util.Helper.showView;

/**
 * Created by 岩 on 13-12-20.
 */
public class ProceduresLayout extends LinearLayout implements MyScrollView.ScrollViewListener{
    private View rootView;

    // 界面里的各种view
    private EditText plateNumberEdit;
    private EditText licenceModelEdit;
    private Spinner vehicleTypeSpinner;
    private Spinner useCharacterSpinner;
    private EditText mileageEdit;
    private Spinner firstLogYearSpinner;
    private Spinner manufactureYearSpinner;
    private Spinner ticketSpinner;
    private Spinner lastTransferCountSpinner;
    private Spinner compulsoryInsuranceSpinner;
    private Spinner businessInsuranceSpinner;
    private EditText licencePhotoMatchEdit;
    private TableRow importProceduresRow;

    private JSONObject procedures;

    // 照片是否相符
    private boolean match;

    // 是否为修改模式
    private boolean modifyMode;

    // 车牌号码
    private String plateNumber;

    public ProceduresLayout(Context context) {
        super(context);
        init(context);
    }

    public ProceduresLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProceduresLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.procedures_layout, this);

        // 移除输入框的焦点，避免每次输入完成后界面滚动
        ScrollView view = (ScrollView)rootView.findViewById(R.id.root);
        view.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
            }
        });

        // 表显里程
        mileageEdit = (EditText) rootView.findViewById(R.id.mileage_edit);

        // 公里数只允许小数点后两位，并且小数点前只能有2位
        mileageEdit.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable edt) {
                String temp = edt.toString();

                if (temp.contains(".")) {
                    int posDot = temp.indexOf(".");
                    if (posDot <= 0) return;
                    if (temp.length() - posDot - 1 > 2) {
                        edt.delete(posDot + 3, posDot + 4);
                    }
                } else {
                    if (temp.length() > 2) {
                        edt.clear();
                        edt.append(temp.substring(0, 2));
                    }
                }
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
        });

        // 行驶本与照片是否相符
        licencePhotoMatchEdit = (EditText) rootView.findViewById(R.id
                .licencePhotoMatch_edit);
        licencePhotoMatchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                licencePhotoMatchEdit.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                licencePhotoMatchEdit.setError(null);
            }
        });

        Button licencePhotoMatchButton = (Button) rootView.findViewById(R.id
                .licencePhotoMatch_button);
        licencePhotoMatchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                pictureMatch();
            }
        });

        plateNumberEdit = (EditText) rootView.findViewById(R.id.plateNumber_edit);
        plateNumberEdit.setFilters(new InputFilter[]{new InputFilter.AllCaps(),
                new InputFilter.LengthFilter(10)});
        plateNumberEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 与车型识别页的车牌产生联动
                CarRecogniseLayout.plateNumberEdit.setText(CarRecogniseLayout.plateNumberEdit
                        .getText().toString());
            }
        });

        // 行驶证品牌型号
        licenceModelEdit = (EditText) rootView.findViewById(R.id.licenseModel_edit);
        licenceModelEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 与车型识别页的行驶证品牌型号产生联动
                CarRecogniseLayout.licenceModelEdit.setText(editable.toString());
            }
        });

        // 车辆类型
        vehicleTypeSpinner = (Spinner) rootView.findViewById(R.id.vehicleType_spinner);
        vehicleTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 与车型识别页的车辆类型产生联动
                CarRecogniseLayout.vehicleTypeEdit.setText(adapterView.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 使用性质
        useCharacterSpinner = (Spinner) rootView.findViewById(R.id.useCharacter_spinner);
        useCharacterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 与车型识别页的使用性质产生联动
                CarRecogniseLayout.useCharacterEdit.setText(adapterView.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 进口车手续
        importProceduresRow = (TableRow) rootView.findViewById(R.id.importProcedures_row);

        // 初始化所有的Spinner
        setRegLocationSpinner();
        setCarColorSpinner();
        setFirstLogTimeSpinner();
        setManufactureTimeSpinner();
        setTransferCountSpinner();
        setLastTransferTimeSpinner();
        setYearlyCheckAvailableDateSpinner();
        setAvailableDateYearSpinner();
        setBusinessInsuranceAvailableDateYearSpinner();
        setOtherSpinners();

        MyScrollView scrollView = (MyScrollView)findViewById(R.id.root);
        scrollView.setListener(new MyScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy) {
                if(scrollView.getScrollY() > 5) {
                    showShadow(true);
                } else {
                    showShadow(false);
                }
            }
        });
    }

    @Override
    public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy) {

    }

    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void updateUi() {
        // 根据是否进口更改手续选项
        showView(rootView, R.id.importProcedures_table, CarRecogniseLayout.isPorted);

        // 根据识别信息填写对应的控件
        setEditViewText(rootView, R.id.plateNumber_edit, CarRecogniseLayout.plateNumberEdit
                .getText().toString());

        plateNumber = CarRecogniseLayout.plateNumberEdit.getText().toString();
        String abbrev = plateNumber.substring(0, 1);

        String[] provinceAbbreviationArray = getResources().getStringArray(R.array.province_abbreviation);
        List<String> provinceAbbreviation = Helper.StringArray2List(provinceAbbreviationArray);

        setSpinnerSelectionWithIndex(rootView, R.id.regArea_spinner, provinceAbbreviation.indexOf(abbrev));

        setEditViewText(rootView, R.id.licenseModel_edit, CarRecogniseLayout.licenceModelEdit
                .getText().toString());
        setSpinnerSelectionWithString(rootView, R.id.vehicleType_spinner,
                CarRecogniseLayout.vehicleTypeEdit.getText().toString());
        setSpinnerSelectionWithString(rootView, R.id.useCharacter_spinner,
                CarRecogniseLayout.useCharacterEdit.getText().toString());

        setTextView(rootView, R.id.brandText, "车辆型号：" +
                CarRecogniseLayout.brandEdit.getText().toString());

        littleFixAboutRegArea();
    }

    // <editor-fold defaultstate="collapsed" desc="设置各种Spinner">

    // 注册地
    private void setRegLocationSpinner()
    {
        String[] provinceArray = getResources().getStringArray(R.array.province);
        List<String> province = Helper.StringArray2List(provinceArray);
        SetSpinnerData(rootView, R.id.regArea_spinner, province);

        String[] provinceAbbreviationArray = getResources().getStringArray(R.array.province_abbreviation);
        final List<String> provinceAbbreviation = Helper.StringArray2List(provinceAbbreviationArray);

        Spinner regLocationSpinner = (Spinner) rootView.findViewById(R.id.regArea_spinner);

        regLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!modifyMode) {
                    setEditViewText(rootView, R.id.plateNumber_edit, provinceAbbreviation.get(i));
                }

                modifyMode = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    // 如果为修改模式，要手动填写一下注册地，因为spinner变化会导致注册地变化
    public void littleFixAboutRegArea() {
        if(!modifyMode) {
            setEditViewText(rootView, R.id.plateNumber_edit, CarRecogniseLayout.plateNumberEdit
                    .getText().toString());
        } else {
            try {
                setEditViewText(rootView, R.id.plateNumber_edit, procedures.getString("plateNumber"));
            } catch (JSONException e) {
                Log.d(Common.TAG, "解析JSON出错！");
            }
        }
    }

    // 车身颜色
    private void setCarColorSpinner()
    {
        String[] colorArray = getResources().getStringArray(R.array.car_color_arrays);
        List<String> colorList = Helper.StringArray2List(colorArray);

        SetSpinnerData(rootView, R.id.exteriorColor_spinner, colorList);
    }

    // 初次登记时间
    private void setFirstLogTimeSpinner()
    {
        SetSpinnerData(rootView, R.id.regYear_spinner, Helper.GetYearList(20));
        SetSpinnerData(rootView, R.id.regMonth_spinner, Helper.GetMonthList());

        firstLogYearSpinner = (Spinner) rootView.findViewById(R.id.regYear_spinner);
        firstLogYearSpinner.setSelection(17);
        firstLogYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 出厂日期不能晚于登记日期
                List<String> temp = Helper.GetYearList(20);
                SetSpinnerData(rootView, R.id.builtYear_spinner, temp.subList(0, i + 1));
                manufactureYearSpinner.setSelection(i);

                // 最后过户时间不能早于登记日期
                SetSpinnerData(rootView, R.id.transferLastYear_spinner, temp.subList(i,
                        temp.size()));

                // 年检有效期、交强险有效期、商险有效期不能早于登记日期
                int from = Integer.parseInt(temp.get(i));

                int to = Calendar.getInstance().get(Calendar.YEAR) + 2;

                SetSpinnerData(rootView, R.id.annualInspectionYear_spinner,
                        Helper.GetNumbersList(from, to));
                SetSpinnerData(rootView, R.id.compulsoryInsuranceYear_spinner,
                        Helper.GetNumbersList(from, to));
                SetSpinnerData(rootView, R.id.insuranceExpiryYear_spinner,
                        Helper.GetNumbersList(from, to));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    // 出厂日期
    private void setManufactureTimeSpinner()
    {
        SetSpinnerData(rootView, R.id.builtYear_spinner, Helper.GetYearList(20));
        SetSpinnerData(rootView, R.id.builtMonth_spinner, Helper.GetMonthList());
    }

    // 过户次数
    private void setTransferCountSpinner()
    {
        SetSpinnerData(rootView, R.id.transferCount_spinner, Helper.GetNumbersList(0, 15));
    }

    // 最后过户时间
    private void setLastTransferTimeSpinner()
    {
        SetSpinnerData(rootView, R.id.transferLastYear_spinner, Helper.GetYearList(17));
        SetSpinnerData(rootView, R.id.transferLastMonth_spinner, Helper.GetMonthList());
    }

    // 年检有效期
    private void setYearlyCheckAvailableDateSpinner() {
        SetSpinnerData(rootView, R.id.annualInspectionYear_spinner, Helper.GetYearList(2));
        SetSpinnerData(rootView, R.id.annualInspectionMonth_spinner, Helper.GetMonthList());
    }

    // 有效期至（交强险）
    private void setAvailableDateYearSpinner() {
        SetSpinnerData(rootView, R.id.compulsoryInsuranceYear_spinner, Helper.GetYearList(2));
        SetSpinnerData(rootView, R.id.compulsoryInsuranceMonth_spinner, Helper.GetMonthList());
    }

    // 商险有效期
    private void setBusinessInsuranceAvailableDateYearSpinner() {
        SetSpinnerData(rootView, R.id.insuranceExpiryYear_spinner, Helper.GetYearList(19));
        SetSpinnerData(rootView, R.id.insuranceExpiryMonth_spinner, Helper.GetMonthList());
    }

    // 设置其他spinner
    private void setOtherSpinners() {
        // 购车发票
        ((Spinner)findViewById(R.id.invoice_spinner)).setOnItemSelectedListener(new AdapterView
                .OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 有发票
                showView(rootView, R.id.invoice_edit, i <= 1);
                showView(rootView, R.id.rmb, i <= 1);
                showView(rootView, R.id.placeHolder, i > 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        // 过户次数
        ((Spinner) rootView.findViewById(R.id.transferCount_spinner)).setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        // 过户次数大于0
                        Helper.showView(rootView, R.id.transferLastDate_row, i > 0);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                }
        );

        // 商业保险
        ((Spinner) rootView.findViewById(R.id.insurance_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Helper.showView(rootView, R.id.insurance_table, i == 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 交强险
        ((Spinner) rootView.findViewById(R.id.compulsoryInsurance_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                showView(rootView, R.id.compulsoryInsuranceDate_table, i == 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 出厂日期
        manufactureYearSpinner = (Spinner) rootView.findViewById(R.id.builtYear_spinner);

        // 车船税票
        ((Spinner) findViewById(R.id.vehicleTax_spinner)).setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                findViewById(R.id.vehicleTax_edit).setVisibility(i == 0 ? VISIBLE :
                        INVISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 违章记录
        ((Spinner) findViewById(R.id.violationRecords_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                showView(rootView, R.id.violationScore_row, i == 0);
                showView(rootView, R.id.violationAmount_row, i == 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 过户要求
        ((Spinner) findViewById(R.id.transferRequirement_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                 showView(rootView, R.id.localPreSellAmount_row, i == 0 || i == 2 || i == 3);
                 showView(rootView, R.id.externalPreSellAmount_row, i == 1 || i == 3);

             }

             @Override
             public void onNothingSelected(AdapterView<?> adapterView) {

             }
         });

        // 过户时间
        ((Spinner) findViewById(R.id.transferTime_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                showView(rootView, R.id.transfer_row, i == 0);
                showView(rootView, R.id.transfer1_row, i == 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    // 实车与照片是否相符
    public void pictureMatch()
    {
        View view = LayoutInflater.from(rootView.getContext()).inflate(R.layout.picture_match_dialog, null);

        AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                .setTitle("注意")
                .setView(view)
                .setPositiveButton(R.string.attention_match, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 相符
                        setPictureMatchEdit(true);
                    }
                })
                .setNegativeButton(R.string.attention_notmatch, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 不符
                        setPictureMatchEdit(false);
                    }
                })
                .create();

        dialog.show();
    }
    private void setPictureMatchEdit(boolean match) {
        String matches = getResources().getString(R.string.attention_match);
        String notMatch = getResources().getString(R.string.attention_notmatch);
        setEditViewText(rootView, R.id.licencePhotoMatch_edit, match ? matches : notMatch);
    }
}
