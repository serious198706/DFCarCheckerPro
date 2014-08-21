package com.df.library.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;


import com.df.library.R;

import static com.df.library.util.Helper.setTextView;

/**
 * 自定义提示框
 * Created by 岩 on 14-3-27.
 */
public class MyAlertDialog {
    public static final int BUTTON_STYLE_OK_CANCEL = 1;
    public static final int BUTTON_STYLE_OK = 2;
    public static final int BUTTON_STYLE_CANCEL = 3;
    public static final int NO_BUTTON = 4;
    public static final int POSITIVE_PRESSED = 5;
    public static final int NEGATIVE_PRESSED = 6;

    private static AlertDialog dialog;

    private AlertDialog _dialog;

    public static void dismiss() {
        if(dialog != null)
            dialog.dismiss();
    }

    private static void setTextView(View view, int textId, String string) {
        TextView textView = (TextView)view.findViewById(textId);

        if(string == null)
            textView.setText("无");
        else
            textView.setText(string);
    }

    public void _showAlert(Context context, String msg, String title, int style, final Handler handler) {
        if(_dialog != null)
            if(_dialog.isShowing())
                _dialog.dismiss();

        View view1 = ((Activity)context).getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(msg);
        content.setTextSize(20f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, title);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view1);

        switch (style) {
            case BUTTON_STYLE_OK_CANCEL:
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        handler.sendEmptyMessage(POSITIVE_PRESSED);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        handler.sendEmptyMessage(NEGATIVE_PRESSED);
                    }
                });
                break;
            case BUTTON_STYLE_OK:
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        handler.sendEmptyMessage(POSITIVE_PRESSED);
                    }
                });
                break;
            case BUTTON_STYLE_CANCEL:
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        handler.sendEmptyMessage(NEGATIVE_PRESSED);
                    }
                });
                break;
            case NO_BUTTON:
                break;
        }

        _dialog = builder.setCancelable(false).create();
        _dialog.show();
    }

    public void _dismiss() {
        if(_dialog != null)
            _dialog.dismiss();
    }


    public static void showAlert(Context context, String msg, String title, int style, final Handler handler) {
        View view1 = ((Activity)context).getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(msg);
        content.setTextSize(20f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, title);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view1);

        switch (style) {
            case BUTTON_STYLE_OK_CANCEL:
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        handler.sendEmptyMessage(POSITIVE_PRESSED);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        handler.sendEmptyMessage(NEGATIVE_PRESSED);
                    }
                });
                break;
            case BUTTON_STYLE_OK:
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        handler.sendEmptyMessage(POSITIVE_PRESSED);
                    }
                });
                break;
            case BUTTON_STYLE_CANCEL:
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        handler.sendEmptyMessage(NEGATIVE_PRESSED);
                    }
                });
                break;
            case NO_BUTTON:
                break;
        }

        dialog = builder.setCancelable(false).create();
        dialog.show();
    }

    public static void showAlert(Context context, int msg, String title, int style, final Handler handler) {
        String msgstr = context.getResources().getString(msg);
        showAlert(context, msgstr, title, style, handler);
    }

    public static void showAlert(Context context, int msg, int title, int style, final Handler handler) {
        String msgstr = context.getResources().getString(msg);
        showAlert(context, msgstr, title, style, handler);
    }

    public static void showAlert(Context context, String msg, int title, int style, final Handler handler) {
        String titlestr = context.getResources().getString(title);
        showAlert(context, msg, titlestr, style, handler);
    }
}
