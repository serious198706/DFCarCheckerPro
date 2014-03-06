package com.df.app.service.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.df.app.carCheck.PhotoLayout;
import com.df.app.R;
import com.df.app.entries.PhotoEntity;
import com.df.app.util.Common;

import java.io.File;
import java.util.List;

/**
 * Created by 岩 on 13-12-26.
 *
 * 照片列表adapter
 */
public class PhotoListAdapter extends ArrayAdapter<PhotoEntity> {
    private List<PhotoEntity> items;
    private Context context;

    public PhotoListAdapter(Context context, int layoutResourceId, List<PhotoEntity> items) {
        super(context, layoutResourceId, items);
        this.context = context;
        this.items = items;
    }

    public void setItems(List<PhotoEntity> items) {
        this.items = items;
    }

    public void addItem(PhotoEntity item) {
        this.items.add(item);
    }

    public void removeItem(PhotoEntity item) {
        if(this.items.contains(item))
            this.items.remove(item);
    }

    public List<PhotoEntity> getItems() {
        return items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.photo_list_item, null);
        }

        final PhotoEntity photoEntity = items.get(position);

        if (photoEntity != null) {
            ImageView photo = (ImageView) view.findViewById(R.id.photo);

            if(photoEntity.getThumbFileName() == null || photoEntity.getThumbFileName().equals("")) {
                final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera);
                photo.setImageBitmap(bitmap);
            } else {
                final Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + photoEntity
                        .getThumbFileName());
                photo.setImageBitmap(bitmap);
            }

            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPhoto(Common.photoDirectory + photoEntity.getFileName());
                }
            });


            TextView photoName = (TextView) view.findViewById(R.id.photo_name);
            photoName.setText(photoEntity.getName());

            // 照片备注框
            EditText photoComment = (EditText) view.findViewById(R.id.photo_comment);
            photoComment.setText(photoEntity.getComment());

            // 备注更改时将对应的photoEntity也更改
            photoComment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    photoEntity.setComment(((EditText)view).getText().toString());
                }
            });

            Button reTakeButton = (Button)view.findViewById(R.id.reTake);
            reTakeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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

                    // 将要修改的photoEntity提取出来
                    PhotoLayout.reTakePhotoEntity = photoEntity;

                    Uri fileUri = Uri.fromFile(new File(filePath));
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // 设置拍摄的文件名
                    ((Activity)context).startActivityForResult(intent, Common.PHOTO_RETAKE);
                }
            });
        }
        return view;
    }

    /**
     * 点击缩略图时，显示对应的图片
     * @param fileName
     */
    private void showPhoto(String fileName) {
        View view = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.picture_popup,
                (ViewGroup)  ((Activity)getContext()).findViewById(R.id.layout_root));

        ImageView image = (ImageView) view.findViewById(R.id.fullimage);

        Bitmap bitmap = BitmapFactory.decodeFile(fileName);
        image.setImageBitmap(bitmap);

        Dialog mPictureDialog = new Dialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        mPictureDialog.setContentView(view);
        mPictureDialog.setCancelable(true);
        mPictureDialog.show();
    }
}
