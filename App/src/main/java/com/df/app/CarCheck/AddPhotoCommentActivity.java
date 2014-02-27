package com.df.app.carCheck;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.df.app.R;
import com.df.app.util.Common;

import static com.df.app.util.Helper.getEditViewText;

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
    }

    @Override
    public void onBackPressed() {
        saveResult();
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
