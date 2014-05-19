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
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.R;
import com.df.app.carCheck.IssueLayout;
import com.df.app.carCheck.PhotoFaultLayout;
import com.df.app.carCheck.PhotoLayout;
import com.df.app.entries.Action;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.AsyncTask.DownloadImageTask;
import com.df.app.util.Common;
import com.df.app.util.Helper;
import com.df.app.util.lazyLoadHelper.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import uk.co.senab.photoview.PhotoViewAttacher;

import static com.df.app.util.Helper.setTextView;

public class PhotoOperationActivity extends Activity {
    private String fileName;
    private ImageView imageView;
    private PhotoViewAttacher attacher;
    private float currentZoomScale = 1.0f;

    // 用来还原之前的photoEntity状态
    private PhotoEntity tempPhotoEntity = null;

    private Bitmap downloadedBitmap;

    private ImageLoader imageLoader = new ImageLoader(this);
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_operation);

        Bundle bundle = getIntent().getExtras();

        if(bundle.containsKey("fileName")) {
            fileName = bundle.getString("fileName");
        }

        imageView = (ImageView)findViewById(R.id.image);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        attacher = new PhotoViewAttacher(imageView);
        attacher.setMaximumScale(2.0f);
        attacher.setMinimumScale(0.4f);

        if(fileName.contains("http")) {
            DownloadImageTask downloadImageTask = new DownloadImageTask(this, fileName, new DownloadImageTask.OnDownloadFinished() {
                @Override
                public void onFinish(Bitmap bitmap) {
                    downloadedBitmap = bitmap;
                    imageView.setImageBitmap(bitmap);
                    attacher.update();
                    progressBar.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onFailed() {
                    Toast.makeText(PhotoOperationActivity.this, "下载图片失败！", Toast.LENGTH_SHORT).show();
                    imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.no_image));
                    attacher.update();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });

            downloadImageTask.execute();
        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + fileName);
            imageView.setImageBitmap(bitmap);
            progressBar.setVisibility(View.INVISIBLE);
        }

        ImageButton editButton = (ImageButton)findViewById(R.id.edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fileName.equals("")) {
                    Toast.makeText(PhotoOperationActivity.this, "没有图片", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(progressBar.getVisibility() != View.INVISIBLE) {
                    return;
                }

                tempPhotoEntity = new PhotoEntity();
                tempPhotoEntity.setFileName(fileName);
                tempPhotoEntity.setThumbFileName(fileName);
                tempPhotoEntity.setIndex(PhotoLayout.reTakePhotoEntity.getIndex());
                tempPhotoEntity.setModifyAction(Action.MODIFY);

                try {
                    JSONObject jsonObject = new JSONObject(PhotoLayout.reTakePhotoEntity.getJsonString());
                    jsonObject.put("Action", tempPhotoEntity.getModifyAction());

                    tempPhotoEntity.setJsonString(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(PhotoOperationActivity.this, MaskPhotoActivity.class);

                if(downloadedBitmap != null) {
                    try {
                        fileName = Long.toString(System.currentTimeMillis()) + ".jpg";
                        FileOutputStream fileOutputStream = new FileOutputStream(Common.photoDirectory + fileName);
                        downloadedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                        fileOutputStream.close();

                        intent.putExtra("fileName", fileName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    intent.putExtra("fileName", fileName);
                }

                startActivityForResult(intent, Common.MASK_PHOTO);
            }
        });

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
                tempPhotoEntity.setIndex(PhotoLayout.reTakePhotoEntity.getIndex());
                tempPhotoEntity.setModifyAction(Action.MODIFY);

                try {
                    JSONObject jsonObject = new JSONObject(PhotoLayout.reTakePhotoEntity.getJsonString());
                    jsonObject.put("Action", tempPhotoEntity.getModifyAction());

                    tempPhotoEntity.setJsonString(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Uri fileUri = Uri.fromFile(new File(Common.photoDirectory + tempPhotoEntity.getFileName()));
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // 设置拍摄的文件名
                startActivityForResult(intent, Common.PHOTO_RETAKE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        switch (requestCode) {
            case Common.MASK_PHOTO:
                if(resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    fileName = bundle.getString("fileName");

                    Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + fileName);
                    imageView.setImageBitmap(bitmap);
                    attacher.update();

                    tempPhotoEntity.setFileName(fileName);
                    tempPhotoEntity.setThumbFileName(fileName.substring(0, fileName.length() - 4) + "_t.jpg");
                    tempPhotoEntity.setModifyAction(Action.MODIFY);

                    try {
                        JSONObject jsonObject = new JSONObject(tempPhotoEntity.getJsonString());
                        jsonObject.put("Action", Action.MODIFY);
                        tempPhotoEntity.setJsonString(jsonObject.toString());

                        JSONObject photoData = jsonObject.getJSONObject("PhotoData");

                        photoData.put("width", bitmap.getWidth());
                        photoData.put("height", bitmap.getHeight());

                        jsonObject.put("PhotoData", photoData);

                        tempPhotoEntity.setJsonString(jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Common.PHOTO_RETAKE:
                if(resultCode == Activity.RESULT_OK) {
                    fileName = tempPhotoEntity.getFileName();

                    Helper.setPhotoSize(tempPhotoEntity.getFileName(), Common.PHOTO_WIDTH);
                    Helper.generatePhotoThumbnail(tempPhotoEntity.getFileName(), Common.THUMBNAIL_WIDTH);

                    Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + tempPhotoEntity.getFileName());
                    imageView.setImageBitmap(bitmap);
                    attacher.update();

                    // 新照片的宽高也要更新一下
                    try {
                        JSONObject jsonObject = new JSONObject(tempPhotoEntity.getJsonString());
                        JSONObject photoData = jsonObject.getJSONObject("PhotoData");

                        photoData.put("width", bitmap.getWidth());
                        photoData.put("height", bitmap.getHeight());

                        jsonObject.put("PhotoData", photoData);

                        tempPhotoEntity.setJsonString(jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    tempPhotoEntity = null;
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
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();
    }

    private void save() {
        if(tempPhotoEntity != null) {
            int index = PhotoFaultLayout.photoListAdapter.getItems().indexOf(PhotoLayout.reTakePhotoEntity);

            PhotoEntity photoEntity;

            if(index < 0) {
                if(PhotoLayout.listedPhoto != null) {
                    photoEntity = PhotoLayout.listedPhoto.getPhotoEntity();
                } else if(PhotoLayout.photoListAdapter != null) {
                    photoEntity = PhotoLayout.photoListAdapter.getItem(PhotoLayout.reTakePhotoEntity);
                } else {
                    photoEntity = null;
                }
            } else {
                photoEntity = PhotoFaultLayout.photoListAdapter.getItem(index);
            }

            photoEntity.setFileName(tempPhotoEntity.getFileName());
            photoEntity.setThumbFileName(tempPhotoEntity.getThumbFileName());
            photoEntity.setJsonString(tempPhotoEntity.getJsonString());
            photoEntity.setModifyAction(tempPhotoEntity.getModifyAction());
        }

        PhotoLayout.notifyDataSetChanged();

        PhotoLayout.listedPhoto = null;
        PhotoLayout.photoListAdapter = null;

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
