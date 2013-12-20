package com.df.app.CarCheck;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableRow;

import com.df.app.R;

import org.json.JSONObject;

/**
 * Created by 岩 on 13-12-20.
 */
public class ProceduresLayout extends LinearLayout implements View.OnClickListener {
    private View rootView;

    // 界面里的各种view
    private EditText plateNumberEdit;
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

    // 是否进口
    private boolean isPorted;

    // 照片是否相符
    private boolean match;
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

        // 实车与行驶本照片
        Button matchButton = (Button) rootView.findViewById(R.id.licencePhotoMatch_button);
        matchButton.setOnClickListener(this);

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

        // 牌照号码
        plateNumberEdit = (EditText) rootView.findViewById(R.id.plateNumber_edit);
        plateNumberEdit.setFilters(new InputFilter[]{new InputFilter.AllCaps(),
                new InputFilter.LengthFilter(10)});

        // 进口车手续
        importProceduresRow = (TableRow) rootView.findViewById(R.id.importProcedures_row);

        // 初始化所有的Spinner
//        setRegLocationSpinner();
//        setCarColorSpinner();
//        setFirstLogTimeSpinner();
//        setManufactureTimeSpinner();
//        setTransferCountSpinner();
//        setLastTransferTimeSpinner();
//        setYearlyCheckAvailableDateSpinner();
//        setAvailableDateYearSpinner();
//        setBusinessInsuranceAvailableDateYearSpinner();
//        setOtherSpinners();
    }

    @Override
    public void onClick(View view) {

    }
}
