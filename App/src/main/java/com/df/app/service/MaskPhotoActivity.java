package com.df.app.service;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.df.app.R;
import com.df.app.util.Common;
import com.df.app.util.Helper;

import java.io.FileOutputStream;
import java.io.IOException;

public class MaskPhotoActivity extends Activity {
    private MaskPhoto maskPhoto;
    private String fileName;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mask_photo);

        Bundle bundle = getIntent().getExtras();

        int width, height;

        if(bundle.containsKey("fileName")) {
            fileName = bundle.getString("fileName");
            bitmap = BitmapFactory.decodeFile(Common.photoDirectory + fileName);
        } else {
            fileName = Long.toString(System.currentTimeMillis());
            bitmap = getIntent().getParcelableExtra("bitmap");
        }

        maskPhoto = new MaskPhoto(this, bitmap);

        LinearLayout root = (LinearLayout)findViewById(R.id.container);

        width = bitmap.getWidth();
        height = bitmap.getHeight();

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)root.getLayoutParams();
        params.height = height;
        params.width = width;

        root.addView(maskPhoto);

        Button saveButton = (Button)findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePhoto();
            }
        });

        Button backButton = (Button)findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discard();
            }
        });
    }

    private void discard() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private void savePhoto() {
        Bitmap bitmap = Bitmap.createBitmap(maskPhoto.getWidth(),maskPhoto.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        maskPhoto.draw(c);

        try {
            FileOutputStream out = new FileOutputStream(Common.photoDirectory + fileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Helper.generatePhotoThumbnail(fileName, 400);

        Intent intent = new Intent();
        intent.putExtra("fileName", fileName);

        // 关闭activity
        setResult(Activity.RESULT_OK, intent);

        finish();
    }
}
