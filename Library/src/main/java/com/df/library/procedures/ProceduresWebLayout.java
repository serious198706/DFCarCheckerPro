package com.df.library.procedures;

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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.df.library.R;
import com.df.library.entries.UserInfo;
import com.df.library.util.Common;
import com.df.library.util.Helper;

/**
 * Created by 岩 on 14-1-13.
 *
 * 手续录入（网页）
 */
public class ProceduresWebLayout extends LinearLayout {
    private View rootView;
    private WebView proceduresWeb;
    private ProgressBar pb;
    private String url;
    private String address = null;

    public ProceduresWebLayout(Context context) {
        super(context);
        init(context);
    }

    public ProceduresWebLayout(Context context, String address) {
        super(context);
        this.address = address;
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
        rootView = LayoutInflater.from(context).inflate(R.layout.procedures_web_layout, this);
        pb = (ProgressBar)findViewById(R.id.progressBar);
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
        Double val = (double) width / (double) 700;
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

    public void showContent(boolean show) {
        pb.setVisibility(show ? GONE : VISIBLE);
        proceduresWeb.setVisibility(show ? VISIBLE : GONE);
    }

    /**
     * 确定车辆基本信息后，将以下数据作为参数提交到网页
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
        url = Common.getPROCEDURES_ADDRESS() + "procedure/cardetection/index?";

        url += "userId=" + UserInfo.getInstance().getId();
        url += "&";
        url += "userName=" + UserInfo.getInstance().getName();
        url += "&";
        url += "orid=" + UserInfo.getInstance().getOrid();
        url += "&";
        url += "plateType=" + UserInfo.getInstance().getPlateType();
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
        proceduresWeb.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                showContent(true);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!Helper.checkInternetConnection(rootView.getContext())) {
                    Toast.makeText(rootView.getContext(), "No Internet!", Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    view.loadUrl(url);
                    return true;
                }
            }
        });
        proceduresWeb.setWebChromeClient(new WebChromeClient());
        proceduresWeb.getSettings().setJavaScriptEnabled(true);
        proceduresWeb.addJavascriptInterface(this, "android");
        proceduresWeb.setInitialScale(getScale());
        proceduresWeb.clearCache(true);
    }

    public void updateUi(String carId) {
        String url = Common.getPROCEDURES_ADDRESS() + "procedure/cardetection/index?";

        url += "userId=" + UserInfo.getInstance().getId();
        url += "&";
        url += "userName=" + UserInfo.getInstance().getName();
        url += "&";
        url += "carId=" + carId;
        url += "&";
        url += "plateType=" + UserInfo.getInstance().getPlateType();
        url += "&";
        url += "orid=" + UserInfo.getInstance().getOrid();

        proceduresWeb = (WebView)findViewById(R.id.proceduresWeb);
        proceduresWeb.loadUrl(url);
        proceduresWeb.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {showContent(true);
            }
        });
        proceduresWeb.setWebChromeClient(new WebChromeClient());
        proceduresWeb.getSettings().setJavaScriptEnabled(true);
        proceduresWeb.addJavascriptInterface(this, "android");
        proceduresWeb.setInitialScale(getScale());
        proceduresWeb.clearCache(true);
    }
}
