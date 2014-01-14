package com.df.app.CarCheck;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.entries.Cooperator;
import com.df.app.service.AsyncTask.GetCooperatorTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.df.app.util.Helper.getSpinnerSelectedText;
import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 13-12-25.
 */
public class Integrated3Layout extends LinearLayout {
    private View rootView;
    private List<Cooperator> cooperators;
    private List<String> cooperatorNames;

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

        setTextView(rootView, R.id.userName, MainActivity.userInfo.getName());

        getCooperatorNames();
    }

    private void getCooperatorNames() {
        GetCooperatorTask getCooperatorTask = new GetCooperatorTask(rootView.getContext(), new GetCooperatorTask.OnGetListFinish() {
            @Override
            public void onFinish(String result) {
                try {
                    JSONArray jsonArray = new JSONArray(result);

                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        Cooperator cooperator = new Cooperator(jsonObject.getInt("CheckCooperatorId"),
                                jsonObject.getString("CheckCooperatorName"));

                        cooperators.add(cooperator);
                        cooperatorNames.add(jsonObject.getString("CheckCooperatorName"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Spinner cooperatorSpinner = (Spinner)findViewById(R.id.cooperatorName_spinner);
                cooperatorSpinner.setAdapter(new ArrayAdapter(rootView.getContext(),
                        android.R.layout.simple_spinner_dropdown_item, cooperatorNames));
            }

            @Override
            public void onFailed() {

            }
        });

        getCooperatorTask.execute();
    }

    public String generateCommentString() {
        return "";
    }

    public int getCooperatorId() {
        return 0;
    }

    public String getCooperatorName() {
        return getSpinnerSelectedText(rootView, R.id.cooperatorName_spinner);
    }
}
