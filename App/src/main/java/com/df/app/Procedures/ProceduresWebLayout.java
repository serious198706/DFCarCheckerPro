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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.df.app.R;
import com.df.app.service.WebAppInterface;
import com.df.app.util.Common;

/**
 * Created by 岩 on 14-1-13.
 */
public class ProceduresWebLayout extends LinearLayout {
    private Context context;
    private View rootView;

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

        WebView proceduresWeb = (WebView)findViewById(R.id.proceduresWeb);
        proceduresWeb.loadUrl("http://192.168.8.200:9901/Function/CarDetection2/Default.aspx");
        proceduresWeb.setWebViewClient(new WebViewClient());
        proceduresWeb.getSettings().setJavaScriptEnabled(true);
        proceduresWeb.addJavascriptInterface(this, "android");
        //proceduresWeb.setInitialScale(getScale());
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

    private int getScale(){
        Display display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        Double val = new Double(width)/new Double(1200);
        val = val * 100d;
        return val.intValue();
    }
}