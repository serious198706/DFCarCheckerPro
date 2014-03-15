package com.df.app.service;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.df.app.R;
import com.df.app.carCheck.PhotoLayout;
import com.df.app.entries.PhotoEntity;
import com.df.app.util.Common;
import com.df.app.util.Helper;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;

public class PhotoOperationActivity extends Activity {
    private String fileName;
    private ImageView imageView;
    private PhotoViewAttacher attacher;
    private float currentZoomScale = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_operation);

        Bundle bundle = getIntent().getExtras();

        if(bundle.containsKey("fileName")) {
            fileName = bundle.getString("fileName");
        }

        Bitmap bitmap = BitmapFactory.decodeFile(fileName);

        imageView = (ImageView)findViewById(R.id.image);
        imageView.setImageBitmap(bitmap);

        attacher = new PhotoViewAttacher(imageView);
        attacher.setMaximumScale(2.0f);
        attacher.setMinimumScale(0.4f);

        Button backButton = (Button)findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button saveButton = (Button)findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ImageButton zoomInButton = (ImageButton)findViewById(R.id.zoom_in);
        zoomInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomInImage();
            }
        });

        ImageButton zoomOutButton = (ImageButton)findViewById(R.id.zoom_out);
        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomOutImage();
            }
        });

        ImageButton reTakeButton = (ImageButton)findViewById(R.id.reTake);
        reTakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhotoEntity photoEntity = PhotoLayout.reTakePhotoEntity;
                String filePath = Common.photoDirectory;
                String fileName;

                // 如果当前要重拍的照片没有内容，则给其命名
                if(photoEntity.getFileName().equals("")) {
                    fileName = Long.toString(System.currentTimeMillis()) + ".jpg";
                    filePath += fileName;
                    photoEntity.setFileName(fileName);
                    photoEntity.setThumbFileName(fileName.substring(0, fileName.length() - 4) + "_t.jpg");
                } else {
                    filePath += photoEntity.getFileName();
                }

                Uri fileUri = Uri.fromFile(new File(filePath));
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // 设置拍摄的文件名
                startActivityForResult(intent, Common.PHOTO_RETAKE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        switch (requestCode) {
            case Common.PHOTO_RETAKE:
                Helper.setPhotoSize(PhotoLayout.reTakePhotoEntity.getFileName(), 800);
                Helper.generatePhotoThumbnail(PhotoLayout.reTakePhotoEntity.getFileName(), 400);

                Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + PhotoLayout.reTakePhotoEntity.getFileName());
                imageView.setImageBitmap(bitmap);
                attacher.update();

                PhotoLayout.notifyDataSetChanget();
                break;
        }
    }

    private void zoomInImage() {
        if(currentZoomScale <= 2.0f) {
            attacher.setScale(currentZoomScale + 0.2f, true);
            currentZoomScale += 0.2f;
        }
    }

    private void zoomOutImage() {
        if(currentZoomScale >= 0.4f) {
            attacher.setScale(currentZoomScale - 0.2f, true);
            currentZoomScale -= 0.2f;
        }
    }
}
