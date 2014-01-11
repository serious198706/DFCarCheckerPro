package com.df.app.CarCheck;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.df.app.R;
import com.df.app.util.Common;

import static com.df.app.util.Helper.getEditViewText;

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

    private void saveResult() {
        if(getEditViewText(getWindow().getDecorView(), R.id.comment).trim().equals("")) {
            Toast.makeText(this, "备注内容不能为空！", Toast.LENGTH_LONG).show();
        } else {
            String commentString = getEditViewText(getWindow().getDecorView(), R.id.comment).trim();

            Intent intent = new Intent();
            intent.putExtra("COMMENT", commentString);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }
}
