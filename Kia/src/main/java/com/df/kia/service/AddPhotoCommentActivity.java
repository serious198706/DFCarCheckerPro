package com.df.kia.service;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.df.kia.R;
import com.df.kia.service.util.AppCommon;

import static com.df.library.util.Helper.getEditViewText;
import static com.df.library.util.Helper.setEditViewText;
import static com.df.library.util.Helper.setTextView;

/**
 * Created by 岩 on 14-01-13.
 *
 * 添加照片备注页面
 */
public class AddPhotoCommentActivity extends Activity {
    private String fileName;
    private String lastComment = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo_comment);

        Bundle bundle = getIntent().getExtras();

        if(bundle.containsKey("fileName")) {
            fileName = bundle.getString("fileName");
        }

        Bitmap bitmap = BitmapFactory.decodeFile(AppCommon.photoDirectory + fileName);

        ImageView imageView = (ImageView)findViewById(R.id.image);
        imageView.setImageBitmap(bitmap);

        Button okButton = (Button)findViewById(R.id.ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveResult();
            }
        });

        TextView textView = (TextView)findViewById(R.id.currentItem);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert();
            }
        });

        if(bundle.containsKey("comment")) {
            lastComment = bundle.getString("comment");
            setEditViewText(getWindow().getDecorView(), R.id.comment, lastComment);
        }
    }

    @Override
    public void onBackPressed() {
        alert();
    }

    private void alert() {
        View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(R.string.discardComment);
        content.setTextSize(20f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, getResources().getString(R.string.alert));

        AlertDialog dialog = new AlertDialog.Builder(AddPhotoCommentActivity.this)
                .setView(view1)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (lastComment.equals(getEditViewText(getWindow().getDecorView(), R.id.comment).trim())) {
                            lastComment = getEditViewText(getWindow().getDecorView(), R.id.comment).trim();
                            Intent intent = new Intent();
                            intent.putExtra("COMMENT", lastComment);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra("COMMENT", lastComment);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }

                        EditText editText = (EditText)findViewById(R.id.comment);

                        InputMethodManager imm = (InputMethodManager)getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show(); 
    }

    /**
     * 保存备注信息，并返回主activity
     */
    private void saveResult() {
        EditText editText = (EditText)findViewById(R.id.comment);

        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        String commentString = editText.getText().toString().trim();

        Intent intent = new Intent();
        intent.putExtra("COMMENT", commentString);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
