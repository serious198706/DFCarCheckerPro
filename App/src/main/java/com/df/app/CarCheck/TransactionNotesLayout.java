package com.df.app.carCheck;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
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

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.util.Common;

/**
 * Created by 岩 on 14-3-17.
 */
public class TransactionNotesLayout extends LinearLayout {
    private View rootView;
    private ProgressBar pb;
    private WebView transactionNotesWeb;
    private Handler handler;

    public TransactionNotesLayout(Context context) {
        super(context);
        init(context);
    }

    public TransactionNotesLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TransactionNotesLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void commitTransactionNotes(Handler handler) {
        commit(handler);
    }

    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.transaction_notes_layout, this);
        pb = (ProgressBar)findViewById(R.id.progressBar);
    }

    @JavascriptInterface
    public void commit(Handler handler) {
        this.handler = handler;
        transactionNotesWeb.loadUrl("javascript:SaveNode()");
    }

    /**
     * 提交交易备注
     * @param result
     */
    @JavascriptInterface
    public void commit(final String result) {
        Toast.makeText(rootView.getContext(), result, Toast.LENGTH_SHORT).show();
    }

    /**
     * 交易备注提交成功后
     * @param val
     */
    @JavascriptInterface
    public void commitResult(boolean val) {
        if(val) {
            Log.d(Common.TAG, "交易备注提交成功！");
            handler.sendEmptyMessage(0);
        } else {
            Log.d(Common.TAG, "交易备注提交失败！");
            handler.sendEmptyMessage(1);
        }
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

    public void showContent(boolean show) {
        pb.setVisibility(show ? GONE : VISIBLE);
        transactionNotesWeb.setVisibility(show ? VISIBLE : GONE);
    }

    /**
     * 确定车辆基本信息后，将以下信息作为参数提交到网页
     * @param carId carId
     * @param modify
     */
    public void updateUi(int carId, boolean modify) {
        String url = Common.getPROCEDURES_ADDRESS() + "Function/CarDetection2/TransactionNotes.aspx?";

        url += "id=" + Integer.toString(carId);

        if(modify) {
            url += "&edit=1";
        }

        transactionNotesWeb = (WebView)findViewById(R.id.transactionNotesWeb);
        transactionNotesWeb.loadUrl(url);
        transactionNotesWeb.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                showContent(true);
            }
        });
        transactionNotesWeb.setWebChromeClient(new WebChromeClient());
        transactionNotesWeb.getSettings().setJavaScriptEnabled(true);
        transactionNotesWeb.addJavascriptInterface(this, "android");
        transactionNotesWeb.setInitialScale(getScale());
        transactionNotesWeb.clearCache(true);
    }
}
