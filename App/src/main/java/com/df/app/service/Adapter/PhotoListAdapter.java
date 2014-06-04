package com.df.app.service.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.carCheck.CarCheckActivity;
import com.df.app.carCheck.PhotoLayout;
import com.df.app.R;
import com.df.app.entries.Action;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.AsyncTask.DownloadImageTask;
import com.df.app.service.customCamera.BitmapUtil;
import com.df.app.service.customCamera.IPhotoProcessListener;
import com.df.app.service.customCamera.PhotoProcessManager;
import com.df.app.service.customCamera.PhotoTask;
import com.df.app.service.customCamera.activity.PhotoEditActivity;
import com.df.app.service.customCamera.activity.PhotographActivity;
import com.df.app.util.Common;
import com.df.app.util.Helper;
import com.df.app.util.lazyLoadHelper.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by 岩 on 13-12-26.
 *
 * 照片列表adapter
 */
public class PhotoListAdapter extends BaseAdapter implements IPhotoProcessListener {
    public interface OnAction {
        public void onDelete(int position);
        public void onModifyComment(int position, String comment);
        public void onShowPhoto(int position);
    }

    private class ViewHolder {
        public ImageView photo;
        public TextView photoName;
        public EditText photoComment;
    }

    private List<PhotoEntity> items;
    private Context context;
    private boolean editable;
    private boolean hasComment;
    private boolean canDelete;
    private ImageLoader imageLoader;
    private OnAction mCallback;

    public PhotoListAdapter(Context context, List<PhotoEntity> items, boolean editable, boolean hasComment, OnAction listener) {
        this.context = context;
        this.items = items;
        this.editable = editable;
        this.hasComment = hasComment;
        this.mCallback = listener;
        this.canDelete = true;

        imageLoader=new ImageLoader(context);
    }

    public PhotoListAdapter(Context context, List<PhotoEntity> items, boolean editable, boolean hasComment, boolean canDelete, OnAction listener) {
        this.context = context;
        this.items = items;
        this.editable = editable;
        this.hasComment = hasComment;
        this.mCallback = listener;
        this.canDelete = canDelete;

        imageLoader = new ImageLoader(context);
    }

    public void addItem(PhotoEntity item) {
        this.items.add(item);
    }

    public void removeItem(PhotoEntity item) {
        if(this.items.contains(item))
            this.items.remove(item);
    }

    public void removeItem(int position) {
        items.remove(position);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
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
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera_list);
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
                    PhotoLayout.photoListAdapter = PhotoListAdapter.this;

                    showPhoto(photoEntity.getName(), photoEntity.getFileName());
                }
            });
        }

        viewHolder.photoName.setText(photoEntity.getName());
        viewHolder.photoComment.setText(photoEntity.getComment());
        viewHolder.photoComment.setEnabled(hasComment);

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

        Button deleteButton = (Button)convertView.findViewById(R.id.delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onDelete(position);
            }
        });

        try {
            JSONObject jsonObject = new JSONObject(photoEntity.getJsonString());

            if(!editable) {
                deleteButton.setVisibility(View.GONE);
            } else {
                if(jsonObject.getString("Group").equals("otherFault")) {
                    deleteButton.setVisibility(View.VISIBLE);
                } else if(canDelete) {
                    deleteButton.setVisibility(View.VISIBLE);
                } else {
                    deleteButton.setVisibility(View.GONE);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(photoEntity.getModifyAction() != null && photoEntity.getModifyAction().equals(Action.DELETE)) {
            convertView.setAlpha(0.3f);
            viewHolder.photo.setEnabled(false);
        } else {
            convertView.setAlpha(1.0f);
            viewHolder.photo.setEnabled(true);
        }

        if(!editable) {
            viewHolder.photoComment.setEnabled(false);
        }

        return convertView;
    }

    /**
     * 点击缩略图时，显示对应的图片
     * @param fileName
     */
    private void showPhoto(final String name, String fileName) {
        PhotoProcessManager.getInstance().registPhotoProcessListener(this);

        if(fileName.contains("http")) {
            DownloadImageTask downloadImageTask = new DownloadImageTask(context, fileName, new DownloadImageTask.OnDownloadFinished() {
                @Override
                public void onFinish(Bitmap bitmap) {
                    // 把图片保存成一个临时文件
                    long currentTimeMillis = System.currentTimeMillis();

                    BitmapUtil.saveBitmap(bitmap, 100, Common.photoDirectory + currentTimeMillis + ".jpg");

                    Intent intent = new Intent(context, PhotoEditActivity.class);

                    intent.putExtra(PhotoEditActivity.EXTRA_DSTPATH, Common.photoDirectory);
                    intent.putExtra(PhotoEditActivity.EXTRA_PHOTOTASK, new PhotoTask(0, name, currentTimeMillis, Common.photoDirectory, 0));
                    context.startActivity(intent);
                }

                @Override
                public void onFailed() {
                    Toast.makeText(context, "下载图片失败！", Toast.LENGTH_SHORT).show();

                }
            });

            downloadImageTask.execute();
        } else {
            Intent intent = new Intent();
            long file;

            if(fileName.equals("") || fileName == null) {
                intent.setClass(context, PhotographActivity.class);

                file = System.currentTimeMillis();
                Helper.startCamera(context, name, file);
            } else {
                intent.setClass(context, PhotoEditActivity.class);

                file = Long.parseLong(fileName.substring(0, fileName.length() - 4));
                intent.putExtra(PhotoEditActivity.EXTRA_DSTPATH, Common.photoDirectory);
                intent.putExtra(PhotoEditActivity.EXTRA_PHOTOTASK, new PhotoTask(0, name, file, Common.photoDirectory, 0));
                context.startActivity(intent);
            }
        }
    }


    @Override
    public void onPhotoProcessFinish(List<PhotoTask> list) {
        PhotoTask photoTask = list.get(0);

        // 处理完成
        if(photoTask.getState() == PhotoTask.STATE_COMPLETE) {
            Helper.handlePhoto(photoTask.getFileName() + ".jpg");

            PhotoEntity tempPhotoEntity = PhotoLayout.reTakePhotoEntity;

            Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + photoTask.getFileName() + ".jpg");

            tempPhotoEntity.setFileName(photoTask.getFileName() + ".jpg");
            tempPhotoEntity.setThumbFileName(photoTask.getFileName() + "_t.jpg");
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

                notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
