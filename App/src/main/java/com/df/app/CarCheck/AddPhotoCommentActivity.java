package com.df.app.carCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.df.app.R;
import com.df.app.util.Common;

import static com.df.app.util.Helper.getEditViewText;
import static com.df.app.util.Helper.setEditViewText;
import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 14-01-13.
 *
 * 添加照片备注页面
 */
public class AddPhotoCommentActivity extends Activity {
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo_comment);

        Bundle bundle = getIntent().getExtras();

        if(bundle.containsKey("fileName")) {
            fileName = bundle.getString("fileName");
        }

        Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + fileName);

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
            setEditViewText(getWindow().getDecorView(), R.id.comment, bundle.getString("comment"));
        }
    }

    @Override
    public void onBackPressed() {
        alert();
    }

    private void alert() {
        // 如果备注框不为空
        if(!getEditViewText(getWindow().getDecorView(), R.id.comment).trim().equals("")) {
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
                            Intent intent = new Intent();
                            intent.putExtra("COMMENT", "");
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create();

            dialog.show();
        } else {
            saveResult();
        }
    }

    /**
     * 保存备注信息，并返回主activity
     */
    private void saveResult() {
        String commentString = getEditViewText(getWindow().getDecorView(), R.id.comment).trim();

        Intent intent = new Intent();
        intent.putExtra("COMMENT", commentString);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
