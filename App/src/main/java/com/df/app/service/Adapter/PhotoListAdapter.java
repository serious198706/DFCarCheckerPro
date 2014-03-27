package com.df.app.service.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.df.app.carCheck.CarCheckActivity;
import com.df.app.carCheck.PhotoFaultLayout;
import com.df.app.carCheck.PhotoLayout;
import com.df.app.R;
import com.df.app.entries.Action;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.PhotoOperationActivity;
import com.df.app.util.Common;
import com.df.app.util.lazyLoadHelper.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 岩 on 13-12-26.
 *
 * 照片列表adapter
 */
public class PhotoListAdapter extends BaseAdapter {


    public class ViewHolder {
        public ImageView photo;
        public TextView photoName;
        public EditText photoComment;
    }

    private List<PhotoEntity> items;
    private Context context;
    private boolean editable;
    private boolean hasComment;
    private ImageLoader imageLoader;

    public PhotoListAdapter(Context context, List<PhotoEntity> items, boolean editable, boolean hasComment) {
        this.context = context;
        this.items = items;
        this.editable = editable;
        this.hasComment = hasComment;

        imageLoader=new ImageLoader(context);
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
    public int getCount() {
        return items.size();
    }

    @Override
    public PhotoEntity getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public PhotoEntity getItem(PhotoEntity photoEntity) {
        if(items.contains(photoEntity))
            return items.get(items.indexOf(photoEntity));
        else
            return null;
    }

    public void clear() {
        items.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        View view = convertView;
//        if (view == null) {
//            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context
//                    .LAYOUT_INFLATER_SERVICE);
//            view = vi.inflate(R.layout.photo_list_item, null);
//        }

        ViewHolder viewHolder = new ViewHolder();

        if(convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.photo_list_item, null);
            viewHolder.photo = (ImageView) convertView.findViewById(R.id.photo);
            viewHolder.photoName = (TextView) convertView.findViewById(R.id.photo_name);
            viewHolder.photoComment = (EditText) convertView.findViewById(R.id.photo_comment);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        final PhotoEntity photoEntity = items.get(position);

        // 设置照片
        if(photoEntity.getThumbFileName() == null || photoEntity.getThumbFileName().equals("")) {
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera);
            viewHolder.photo.setImageBitmap(bitmap);
        } else {
            if(editable) {
                // 如果图片名称中有http，则表示是来自网络的图片
                if(photoEntity.getThumbFileName().contains("http")) {
                    imageLoader.DisplayImage(photoEntity.getThumbFileName(), viewHolder.photo);
                } else {
                    Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + photoEntity
                            .getThumbFileName());
                    viewHolder.photo.setImageBitmap(bitmap);
                }
            } else {
                // 如果是浏览模式，就意味着图片来自网络
                imageLoader.DisplayImage(photoEntity.getThumbFileName(), viewHolder.photo);
            }
        }

        if(editable) {
            viewHolder.photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 将要修改的photoEntity提取出来
                    PhotoLayout.reTakePhotoEntity = photoEntity;

                    showPhoto(photoEntity.getFileName());
                }
            });
        }

        if(!hasComment) {
            viewHolder.photoComment.setEnabled(false);
        }

        viewHolder.photoName.setText(photoEntity.getName());

        viewHolder.photoComment.setText(photoEntity.getComment());

        // 备注更改时将对应的photoEntity也更改
        viewHolder.photoComment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                String comment = ((EditText)view).getText().toString();

                if(comment.equals(photoEntity.getComment())) {
                    return;
                }

                photoEntity.setComment(comment);

                try {
                    JSONObject jsonObject = new JSONObject(photoEntity.getJsonString());
                    JSONObject photoData = jsonObject.getJSONObject("PhotoData");
                    photoData.put("comment", comment);
                    jsonObject.put("PhotoData", photoData);
                    photoEntity.setJsonString(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 修改模式
                if(CarCheckActivity.isModify()) {
                    // 如果当前为修改模式，而且图片没有修改过，则将action置成comment
                    if(photoEntity.getModifyAction().equals("")) {
                        photoEntity.setModifyAction(Action.COMMENT);

                        try {
                            JSONObject jsonObject = new JSONObject(photoEntity.getJsonString());
                            jsonObject.put("Action", Action.COMMENT);
                            photoEntity.setJsonString(jsonObject.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        Button reTakeButton = (Button)convertView.findViewById(R.id.reTake);
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

        if(photoEntity.getModifyAction() != null && photoEntity.getModifyAction().equals(Action.DELETE)) {
            convertView.setAlpha(0.3f);
            viewHolder.photo.setEnabled(false);
            viewHolder.photoComment.setEnabled(false);
            reTakeButton.setVisibility(View.INVISIBLE);
        } else {
            convertView.setAlpha(1.0f);
            viewHolder.photo.setEnabled(true);
            viewHolder.photoComment.setEnabled(true);
            reTakeButton.setVisibility(View.VISIBLE);
        }

        if(!editable) {
            viewHolder.photoComment.setEnabled(false);
            reTakeButton.setVisibility(View.INVISIBLE);
        }

//        if (photoEntity != null) {
//            // 照片
//            ImageView photo = (ImageView) view.findViewById(R.id.photo);
//
//            // 照片名称
//            TextView photoName = (TextView) view.findViewById(R.id.photo_name);
//
//            // 照片备注
//            EditText photoComment = (EditText) view.findViewById(R.id.photo_comment);
//
//            if(photoEntity.getModifyAction() != null && photoEntity.getModifyAction().equals(Action.DELETE)) {
//                view.setAlpha(0.3f);
//            } else {
//                view.setAlpha(1.0f);
//            }
//
//            if(photoEntity.getThumbFileName() == null || photoEntity.getThumbFileName().equals("")) {
//                final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera);
//                photo.setImageBitmap(bitmap);
//            } else {
//                if(editable) {
//                    // 如果图片名称中有http，则表示是来自网络的图片
//                    if(photoEntity.getThumbFileName().contains("http")) {
//                        imageLoader.DisplayImage(photoEntity.getThumbFileName(), photo);
//                    } else {
//                        Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + photoEntity
//                                .getThumbFileName());
//                        photo.setImageBitmap(bitmap);
//                    }
//                } else {
//                    // 如果是浏览模式，就意味着图片来自网络
//                    imageLoader.DisplayImage(photoEntity.getThumbFileName(), photo);
//                }
//            }
//
//            if(editable) {
//                photo.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        // 将要修改的photoEntity提取出来
//                        PhotoLayout.reTakePhotoEntity = photoEntity;
//
//                        showPhoto(photoEntity.getFileName());
//                    }
//                });
//            }
//
//            photoName.setText(photoEntity.getName());
//
//            photoComment.setText(photoEntity.getComment());
//
//            // 备注更改时将对应的photoEntity也更改
//            photoComment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View view, boolean b) {
//                    photoEntity.setComment(((EditText)view).getText().toString());
//                }
//            });
//
//            Button reTakeButton = (Button)view.findViewById(R.id.reTake);
//            reTakeButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    String filePath = Common.photoDirectory;
//                    String fileName;
//
//                    // 如果当前要重拍的照片没有内容，则给其命名
//                    if(photoEntity.getFileName().equals("")) {
//                        fileName = Long.toString(System.currentTimeMillis()) + ".jpg";
//                        filePath += fileName;
//                        photoEntity.setFileName(fileName);
//                        photoEntity.setThumbFileName(fileName.substring(0, fileName.length() - 4) + "_t.jpg");
//                    } else {
//                        filePath += photoEntity.getFileName();
//                    }
//
//                    // 将要修改的photoEntity提取出来
//                    PhotoLayout.reTakePhotoEntity = photoEntity;
//
//                    Uri fileUri = Uri.fromFile(new File(filePath));
//                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // 设置拍摄的文件名
//                    ((Activity)context).startActivityForResult(intent, Common.PHOTO_RETAKE);
//                }
//            });
//
//            if(photoEntity.getModifyAction() != null && photoEntity.getModifyAction().equals(Action.DELETE)) {
//                view.setAlpha(0.3f);
//                photoComment.setEnabled(false);
//                reTakeButton.setVisibility(View.INVISIBLE);
//            }
//
//            if(!editable) {
//                photoComment.setEnabled(false);
//                reTakeButton.setVisibility(View.INVISIBLE);
//            }
//        }
        return convertView;
    }

    /**
     * 点击缩略图时，显示对应的图片
     * @param fileName
     */
    private void showPhoto(String fileName) {
        Intent intent = new Intent(context, PhotoOperationActivity.class);
        intent.putExtra("fileName", fileName);
        context.startActivity(intent);
    }
}
