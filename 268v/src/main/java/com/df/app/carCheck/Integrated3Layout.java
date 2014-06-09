package com.df.app.carCheck;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.df.app.R;
import com.df.app.service.util.AppCommon;
import com.df.library.entries.Cooperator;
import com.df.library.asyncTask.GetCooperatorTask;
import com.df.library.service.views.SelectCooperatorDialog;
import com.df.library.util.Common;
import com.df.library.entries.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.df.library.util.Helper.getEditViewText;
import static com.df.library.util.Helper.setEditViewText;
import static com.df.library.util.Helper.setTextView;

/**
 * Created by 岩 on 13-12-25.
 *
 * 综合三，暂时只有从检人员的选择
 */
public class Integrated3Layout extends LinearLayout {
    private View rootView;
    private List<Cooperator> cooperators;
    private List<String> cooperatorNames;
    private String storedCooperatorName;

    private Cooperator selectedCooperator;

    public Integrated3Layout(Context context) {
        super(context);
        init(context);
    }

    public Integrated3Layout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Integrated3Layout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.integrated3_layout, this);

        cooperators = new ArrayList<Cooperator>();
        cooperatorNames = new ArrayList<String>();
        cooperatorNames.add("");

        storedCooperatorName = "";

        setTextView(rootView, R.id.userName, UserInfo.getInstance().getName());

        getCooperatorNames();

        final EditText cooperatorEdit = (EditText)findViewById(R.id.cooperatorName_edit);
        cooperatorEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectCooperatorDialog dialog = new SelectCooperatorDialog(rootView.getContext(), cooperators,
                        new SelectCooperatorDialog.OnSelected() {
                            @Override
                            public void onSelected(Cooperator cooperator) {
                                selectedCooperator = cooperator;
                                cooperatorEdit.setText(cooperator.getName());
                            }
                        });
                dialog.show();
            }
        });
    }

    /**
     * 获取从检人员的名字，并填入spinner
     */
    private void getCooperatorNames() {
        GetCooperatorTask getCooperatorTask = new GetCooperatorTask(rootView.getContext(), new GetCooperatorTask.OnGetListFinish() {
            @Override
            public void onFinish(String result) {
                try {
                    JSONArray jsonArray = new JSONArray(result);

                    int length = jsonArray.length();

                    for(int i = 0; i < length; i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        Cooperator cooperator = new Cooperator(jsonObject.getInt("CheckCooperatorId"),
                                jsonObject.getString("CheckCooperatorName"));

                        cooperators.add(cooperator);
                        cooperatorNames.add(jsonObject.getString("CheckCooperatorName"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(String result) {
                Toast.makeText(rootView.getContext(), "获取从检人列表失败！" + result, Toast.LENGTH_LONG).show();
                Log.d(AppCommon.TAG, "获取从检人列表失败！" + result);
            }
        });

        getCooperatorTask.execute();
    }

    /**
     * 生成综合三的备注
     * @return
     */
    public String generateCommentString() {
        return "";
    }

    /**
     * 获取从检人员的id
     * @return
     */
    public int getCooperatorId() {
        if(selectedCooperator != null)
            return selectedCooperator.getId();
        else
            return -1;
    }

    /**
     * 获取从检人员的名字
     * @return
     */
    public String getCooperatorName() {
        if(cooperatorNames.size() == 1)
            return "";
        else
            return getEditViewText(rootView, R.id.cooperatorName_edit);
    }

    /**
     * 检查所有域
     * @return
     */
    public String checkAllFields() {
        return getCooperatorName().equals("") ? "coop" : "";
    }

    public void fillInData(String checkCooperatorName) {
        this.storedCooperatorName = checkCooperatorName;

        setEditViewText(rootView, R.id.cooperatorName_edit, this.storedCooperatorName);
    }
}
