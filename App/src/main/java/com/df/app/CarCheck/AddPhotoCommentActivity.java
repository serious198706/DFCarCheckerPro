package com.df.app.CarCheck;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ImageView;

import com.df.app.R;
import com.df.app.util.Common;

public class AddPhotoCommentActivity extends Activity {

    private String fileName;
    private Bitmap bitmap;


    // TODO 完善填写备注的界面
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo_comment);

        Bundle bundle = getIntent().getExtras();

        if(bundle.containsKey("fileName")) {
            fileName = bundle.getString("fileName");
        }

        bitmap = BitmapFactory.decodeFile(Common.photoDirectory + fileName);

        ImageView imageView = (ImageView)findViewById(R.id.image);
        imageView.setImageBitmap(bitmap);
    }
}
