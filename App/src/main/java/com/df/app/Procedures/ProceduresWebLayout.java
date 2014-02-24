package com.df.app.Procedures;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.df.app.MainActivity;
import com.df.app.R;

/**
 * Created by 岩 on 14-1-13.
 *
 * 手续录入（网页）
 */
public class ProceduresWebLayout extends LinearLayout {
    private Context context;
    private View rootView;
    private WebView proceduresWeb;

    public ProceduresWebLayout(Context context) {
        super(context);
        init(context);
    }

    public ProceduresWebLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProceduresWebLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    private void init(Context context) {
        this.context = context;

        rootView = LayoutInflater.from(context).inflate(R.layout.procedures_web_layout, this);
    }

    @JavascriptInterface
    public void commit() {
        Toast.makeText(rootView.getContext(), "提交成功！", Toast.LENGTH_SHORT).show();
        ((Activity)getContext()).finish();
    }

    @JavascriptInterface
    public void commit(final String result) {
        Toast.makeText(rootView.getContext(), result, Toast.LENGTH_SHORT).show();
    }

    /**
     * 适配屏幕
     * @return
     */
    private int getScale(){
        Display display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        Double val = new Double(width)/new Double(700);
        val = val * 100d;
        return val.intValue();
    }


    public void onBackPressed() {

    }

    public boolean canGoBack() {
        return false;
    }

    public void goBack() {
        proceduresWeb.goBack();
    }

    /**
     * 确定车辆基本信息后，将以下信息作为参数提交到网页
     * @param vin vin
     * @param plateNumber 车牌号码
     * @param licenseModel 行驶证车辆类型
     * @param vehicleType 车辆类型
     * @param useCharacter 使用性质
     * @param engineSerial 发动机号
     * @param seriesId 车系id
     * @param modelId 车型id
     */
    public void updateUi(String vin, String plateNumber, String licenseModel, String vehicleType, String useCharacter, String engineSerial, String seriesId, String modelId) {
        String url = "http://192.168.18.88:8001/Function/CarDetection2/Default.aspx?";

        url += "userId=" + MainActivity.userInfo.getId();
        url += "&";
        url += "userName=" + MainActivity.userInfo.getName();
        url += "&";
        url += "orid=" + MainActivity.userInfo.getOrid();
        url += "&";
        url += "plateNumber=" + plateNumber;
        url += "&";
        url += "licenseModel=" + licenseModel;
        url += "&";
        url += "vehicleType=" + vehicleType;
        url += "&";
        url += "useCharacter=" + useCharacter;
        url += "&";
        url += "engineSerial=" + engineSerial;
        url += "&";
        url += "vin=" + vin;
        url += "&";
        url += "seriesId=" + seriesId;
        url += "&";
        url += "modelId=" + modelId;

        proceduresWeb = (WebView)findViewById(R.id.proceduresWeb);
        proceduresWeb.loadUrl(url);
        proceduresWeb.setWebViewClient(new WebViewClient());
        proceduresWeb.setWebChromeClient(new WebChromeClient());
        proceduresWeb.getSettings().setJavaScriptEnabled(true);
        proceduresWeb.addJavascriptInterface(this, "android");
        proceduresWeb.setInitialScale(getScale());
        proceduresWeb.clearCache(true);
    }
}
