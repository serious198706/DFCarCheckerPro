package com.df.app.util;

import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by 岩 on 13-12-18.
 */
public class Common {

    public static String utilDirectory = Environment.getExternalStorageDirectory().getPath() +
            "/.cheyipai_pro/";
    public static String photoDirectory = Environment.getExternalStorageDirectory().getPath() +
            "/Pictures/DFCarCheckerPro/";

    public static String TAG = "DFCarCheckerPro";

    public static String NAMESPACE = "http://cheyipai";
    public static final String SERVER_ADDRESS = "http://192.168.100.6:50/";
    public static final String PICTURE_ADDRESS = "http://192.168.100.6:8006/";

    // 调用地址
    public static final String USER_MANAGE_SERVICE = "UserManageService.svc";
    public static final String REPORT_SERVICE = "ReportService.svc";

    public static void setTextViewText(View view, int textId, String text) {
        TextView textView = (TextView)view.findViewById(textId);
        textView.setText(text);
    }

    public static void setEditViewText(View view, int textId, String text) {
        EditText editText = (EditText)view.findViewById(textId);
        editText.setText(text);
    }

    public static String getEditViewText(View view, int textId) {
        EditText editText = (EditText)view.findViewById(textId);
        return editText.getText().toString();
    }

    public static void showView(View view, int viewId, boolean show) {
        view.findViewById(viewId).setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
