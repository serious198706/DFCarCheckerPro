package com.df.app.carsChecked;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.df.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 14-3-14.
 */
public class OtherLayout extends LinearLayout {
    private View rootView;

    public OtherLayout(Context context, JSONObject procedures) {
        super(context);
        init(context, procedures);
    }

    public OtherLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OtherLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    private void init(Context context, JSONObject procedures) {
        rootView = LayoutInflater.from(context).inflate(R.layout.car_report_other_layout, this);

        try {
            if(procedures.has("duties")) {
                JSONObject duties = procedures.getJSONObject("duties");

                if(duties.has("buyer")) {
                    if(duties.get("buyer") != JSONObject.NULL) {
                        setTextView(rootView, R.id.duty_buyer, duties.getString("buyer").equals("") ? "无" : duties.getString("buyer"));
                    }
                }

                if(duties.has("seller")) {
                    if(duties.get("seller") != JSONObject.NULL) {
                        setTextView(rootView, R.id.duty_seller, duties.getString("seller").equals("") ? "无" : duties.getString("seller"));
                    }
                }
            }

            if(procedures.has("mfyd")) {
                JSONObject mfyd = procedures.getJSONObject("mfyd");

                if(mfyd.has("shouxu")) {
                    if(mfyd.get("shouxu") != JSONObject.NULL) {
                        setTextView(rootView, R.id.shouxu, mfyd.getString("shouxu").equals("") ? "无" : mfyd.getString("shouxu"));
                    }
                }

                if(mfyd.has("chekuang")) {
                    if(mfyd.get("chekuang") != JSONObject.NULL) {
                        setTextView(rootView, R.id.chekuang, mfyd.getString("chekuang").equals("") ? "无" : mfyd.getString("chekuang"));
                    }
                }
                if(mfyd.has("qita")) {
                    if(mfyd.get("qita") != JSONObject.NULL) {
                        setTextView(rootView, R.id.qita, mfyd.getString("qita").equals("") ? "无" : mfyd.getString("qita"));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
