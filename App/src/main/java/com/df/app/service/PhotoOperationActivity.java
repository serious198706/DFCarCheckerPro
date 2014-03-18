package com.df.app.service;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.df.app.R;
import com.df.app.carCheck.IssueLayout;
import com.df.app.carCheck.PhotoFaultLayout;
import com.df.app.carCheck.PhotoLayout;
import com.df.app.entries.PhotoEntity;
import com.df.app.util.Common;
import com.df.app.util.Helper;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;

import static com.df.app.util.Helper.setTextView;

public class PhotoOperationActivity extends Activity {
    private String fileName;
    private ImageView imageView;
    private PhotoViewAttacher attacher;
    private float currentZoomScale = 1.0f;

    // 用来还原之前的photoEntity状态
    private PhotoEntity tempPhotoEntity;

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
                alertUser();
            }
        });

        Button saveButton = (Button)findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
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
                // 创建一个临时的photoEntity，如果拍照后确定替换，则替换之，如果不替换，删之
                Long fileName = System.currentTimeMillis();
                tempPhotoEntity = new PhotoEntity();
                tempPhotoEntity.setFileName(Long.toString(fileName) + ".jpg");
                tempPhotoEntity.setThumbFileName(Long.toString(fileName) + "_t.jpg");

                Uri fileUri = Uri.fromFile(new File(Common.photoDirectory + tempPhotoEntity.getFileName()));
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // 设置拍摄的文件名
                startActivityForResult(intent, Common.PHOTO_RETAKE);


//                PhotoEntity photoEntity = PhotoLayout.reTakePhotoEntity;
//
//                String filePath = Common.photoDirectory;
//                String fileName;
//
//                // 如果当前要重拍的照片没有内容，则给其命名
//                if(photoEntity.getFileName().equals("")) {
//                    fileName = tempPhotoEntity.getFileName();
//                    filePath += fileName;
//                    photoEntity.setFileName(fileName);
//                    photoEntity.setThumbFileName(fileName.substring(0, fileName.length() - 4) + "_t.jpg");
//                } else {
//                    filePath += photoEntity.getFileName();
//                }
//
//                Uri fileUri = Uri.fromFile(new File(filePath));
//                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // 设置拍摄的文件名
//                startActivityForResult(intent, Common.PHOTO_RETAKE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        switch (requestCode) {
            case Common.PHOTO_RETAKE:
                if(resultCode == Activity.RESULT_OK) {
                    Helper.setPhotoSize(tempPhotoEntity.getFileName(), 800);
                    Helper.generatePhotoThumbnail(tempPhotoEntity.getFileName(), 400);

                    Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + tempPhotoEntity.getFileName());
                    imageView.setImageBitmap(bitmap);
                    attacher.update();
                }
                break;
        }
    }

    private void alertUser() {
        View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(R.string.cancel_confirm);
        content.setTextSize(20f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, getResources().getString(R.string.alert));

        AlertDialog dialog = new AlertDialog.Builder(PhotoOperationActivity.this)
                .setView(view1)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(tempPhotoEntity != null) {
                            // 删除新产生的文件
                            File file = new File(Common.photoDirectory + tempPhotoEntity.getFileName());
                            file.delete();
                            file = new File(Common.photoDirectory + tempPhotoEntity.getThumbFileName());
                            file.delete();
                        }

                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();
    }

    private void save() {
        File file = new File(Common.photoDirectory + PhotoLayout.reTakePhotoEntity.getFileName());
        file.delete();
        file = new File(Common.photoDirectory + PhotoLayout.reTakePhotoEntity.getThumbFileName());
        file.delete();

        int index = PhotoFaultLayout.photoListAdapter.getItems().indexOf(PhotoLayout.reTakePhotoEntity);
        PhotoEntity photoEntity = PhotoFaultLayout.photoListAdapter.getItem(index);
        photoEntity.setFileName(tempPhotoEntity.getFileName());
        photoEntity.setThumbFileName(tempPhotoEntity.getThumbFileName());

        PhotoLayout.listedPhoto.setPhotoEntity(tempPhotoEntity);
        finish();
    }

    @Override
     protected void onDestroy() {
        super.onDestroy();

        // 无论是修改哪张图片，都更新一下吧
        PhotoLayout.notifyDataSetChanged();

        if(PhotoLayout.paintPhotoListAdapter != null)
            PhotoLayout.paintPhotoListAdapter.notifyDataSetChanged();
        if(IssueLayout.photoListAdapter != null)
            IssueLayout.photoListAdapter.notifyDataSetChanged();
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
