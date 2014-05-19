package com.df.app.service.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.R;
import com.df.app.carCheck.PhotoFaultLayout;
import com.df.app.service.AddPhotoCommentActivity;
import com.df.app.carCheck.PhotoLayout;
import com.df.app.entries.Action;
import com.df.app.entries.ListedPhoto;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.AsyncTask.DownloadImageTask;
import com.df.app.service.PhotoOperationActivity;
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

import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 14-3-13.
 */
public class PaintPhotoListAdapter extends BaseAdapter implements IPhotoProcessListener {
    private ImageLoader imageLoader;

    public interface OnDeleteItem {
        public void onDeleteItem(int position);
    }

    private Context context;
    private List<ListedPhoto> items;
    private OnDeleteItem mCallback;

    public PaintPhotoListAdapter(Context context, List<ListedPhoto> items, OnDeleteItem listener) {
        this.context = context;
        this.items = items;
        this.mCallback = listener;

        imageLoader = new ImageLoader(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ListedPhoto getItem(int position) {
        return items.get(position);
    }

    public List<ListedPhoto> getItems() {
        return this.items;
    }

    public ListedPhoto getItem(ListedPhoto listedPhoto) {
        return getItem(items.indexOf(listedPhoto));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(ListedPhoto item) {
        items.add(item);
    }

    public void remove(ListedPhoto item) {
        items.remove(item);
    }

    public void remove(int position) {
        items.remove(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.issue_photo_list_item, null);
        }

        final ListedPhoto listedPhoto = items.get(position);

        if(listedPhoto != null) {
            if(listedPhoto.getPhotoEntity().getModifyAction() != null && listedPhoto.getPhotoEntity().getModifyAction().equals(Action.DELETE))
                view.setAlpha(0.3f);

            ImageView indexImage = (ImageView)view.findViewById(R.id.indexImage);
            int imageId = 0;

            switch (listedPhoto.getType()) {
                case Common.COLOR_DIFF:
                    imageId = R.drawable.out_color_diff;
                    break;
                case Common.SCRATCH:
                    imageId = R.drawable.out_scratch;
                    break;
                case Common.TRANS:
                    imageId = R.drawable.out_trans;
                    break;
                case Common.SCRAPE:
                    imageId = R.drawable.out_scrape;
                    break;
                case Common.OTHER:
                    imageId = R.drawable.out_other;
                    break;
                case Common.BROKEN:
                    imageId = R.drawable.out_trans;
                    break;
                case Common.DIRTY:
                    imageId = R.drawable.out_scrape;
                    break;
            }

            Bitmap indexBitmap = BitmapFactory.decodeResource(context.getResources(), imageId);
            indexImage.setImageBitmap(indexBitmap);

            ImageView photo = (ImageView)view.findViewById(R.id.issuePhoto);

            final PhotoEntity photoEntity = listedPhoto.getPhotoEntity();

            if(photoEntity.getFileName() == null || photoEntity.getFileName().equals("")) {
                final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera);
                photo.setImageBitmap(bitmap);
            } else {
                // 如果图片名称中有http，则表示是来自网络的图片
                if(photoEntity.getThumbFileName().contains("http")) {
                    imageLoader.DisplayImage(photoEntity.getThumbFileName(), photo);
                } else {
                    Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + photoEntity
                            .getThumbFileName());
                    photo.setImageBitmap(bitmap);
                }
            }

            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 将要修改的photoEntity提取出来
                    PhotoLayout.reTakePhotoEntity = photoEntity;
                    PhotoLayout.paintPhotoListAdapter = PaintPhotoListAdapter.this;
                    PhotoLayout.listedPhoto = listedPhoto;
                    showPhoto(photoEntity.getName(), photoEntity.getFileName());
                }
            });

            TextView comment = (TextView)view.findViewById(R.id.issueComment);
            comment.setText(photoEntity.getComment());
            comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PhotoLayout.commentModEntity = photoEntity;
                    PhotoLayout.listedPhoto = listedPhoto;
                    PhotoLayout.paintPhotoListAdapter = PaintPhotoListAdapter.this;

                    Intent intent = new Intent(context, AddPhotoCommentActivity.class);
                    intent.putExtra("fileName", photoEntity.getFileName());
                    intent.putExtra("comment", ((TextView)view).getText().toString());
                    ((Activity)context).startActivityForResult(intent, Common.MODIFY_PAINT_COMMENT);
                }
            });

            Button deleteButton = (Button)view.findViewById(R.id.delete);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View view1 = ((Activity)context).getLayoutInflater().inflate(R.layout.popup_layout, null);
                    TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
                    TextView content = new TextView(view1.getContext());
                    content.setText(R.string.confirmDelete);
                    content.setTextSize(20f);
                    contentArea.addView(content);

                    setTextView(view1, R.id.title, context.getResources().getString(R.string.alert));

                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setView(view1)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // 通知界面有更新
                                    mCallback.onDeleteItem(position);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .create();

                    dialog.show();
                }
            });

            if(listedPhoto.isDelete()) {
                view.setAlpha(0.3f);
                comment.setEnabled(false);
                photo.setEnabled(false);
                deleteButton.setEnabled(false);
            } else {
                view.setAlpha(1.0f);
                comment.setEnabled(true);
                photo.setEnabled(true);
                deleteButton.setEnabled(true);
            }
        }


        return view;
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

            PhotoEntity tempPhotoEntity = PhotoLayout.listedPhoto.getPhotoEntity();

            //PhotoEntity tempPhotoEntity = PhotoFaultLayout.photoListAdapter.getItem(PhotoLayout.reTakePhotoEntity);

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
                PhotoFaultLayout.photoListAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
