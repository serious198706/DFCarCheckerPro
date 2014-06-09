package com.df.kia.service.Adapter;

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

import com.df.kia.R;
import com.df.kia.carCheck.PhotoFaultLayout;
import com.df.library.entries.Action;
import com.df.kia.service.AddPhotoCommentActivity;
import com.df.kia.carCheck.IssueLayout;
import com.df.kia.carCheck.PhotoLayout;
import com.df.library.entries.Issue;
import com.df.library.entries.ListedPhoto;
import com.df.library.entries.PhotoEntity;
import com.df.library.asyncTask.DownloadImageTask;
import com.df.library.service.customCamera.BitmapUtil;
import com.df.library.service.customCamera.IPhotoProcessListener;
import com.df.library.service.customCamera.PhotoProcessManager;
import com.df.library.service.customCamera.PhotoTask;
import com.df.library.service.customCamera.activity.PhotoEditActivity;
import com.df.library.service.customCamera.activity.PhotographActivity;
import com.df.kia.service.util.AppCommon;
import com.df.library.util.Common;
import com.df.library.util.Helper;
import com.df.library.util.lazyLoadHelper.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.df.library.util.Helper.drawTextToBitmap;
import static com.df.library.util.Helper.setTextView;

/**
 * Created by 岩 on 14-3-5.
 *
 * 弹出的绘制窗口中的列表adapter
 */
public class IssuePhotoListAdapter extends BaseAdapter implements IPhotoProcessListener {
    private ImageLoader imageLoader;

    public interface OnDeleteItem {
        public void onDeleteItem(int position);
    }

    private Context context;
    private List<ListedPhoto> items;
    private Issue issue;
    private OnDeleteItem mCallback;
    private boolean delete;

    public IssuePhotoListAdapter(Context context, List<ListedPhoto> items, Issue issue, boolean delete, OnDeleteItem listener) {
        this.context = context;
        this.items = items;
        this.issue = issue;
        this.delete = delete;
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

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(ListedPhoto listedPhoto) {
        this.items.add(listedPhoto);
    }

    public void setItems(List<ListedPhoto> items) {
        this.items = items;
    }

    public void remove(int position) {
        this.items.remove(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.issue_photo_list_item, null);
        }

        final ListedPhoto listedPhoto = items.get(position);

        if(listedPhoto != null) {
            ImageView indexImage = (ImageView)view.findViewById(R.id.indexImage);
            indexImage.setImageBitmap(drawTextToBitmap(context, R.drawable.damage, position + 1));

            final PhotoEntity photoEntity = listedPhoto.getPhotoEntity();

            // 照片
            ImageView photo = (ImageView)view.findViewById(R.id.issuePhoto);

            if(photoEntity.getFileName() == null || photoEntity.getFileName().equals("")) {
                final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera_list);
                photo.setImageBitmap(bitmap);
            } else {
                // 如果图片名称中有http，则表示是来自网络的图片
                if(photoEntity.getThumbFileName().contains("http")) {
                    imageLoader.DisplayImage(photoEntity.getThumbFileName(), photo);
                } else {
                    Bitmap bitmap = BitmapFactory.decodeFile(AppCommon.photoDirectory + photoEntity
                            .getThumbFileName());
                    photo.setImageBitmap(bitmap);
                }
            }

            // 备注
            TextView comment = (TextView)view.findViewById(R.id.issueComment);
            comment.setText(photoEntity.getComment());

            // 删除按钮
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

            // 如果是删除模式，则禁用所有点击事件
            if(!delete || !listedPhoto.isDelete()) {
                photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 将要修改的photoEntity提取出来
                        PhotoLayout.reTakePhotoEntity = issue.getPhotoEntities().get(position);
                        PhotoLayout.listedPhoto = listedPhoto;
                        IssueLayout.photoListAdapter = IssuePhotoListAdapter.this;
                        showPhoto(photoEntity.getName(), photoEntity.getFileName());
                    }
                });

                comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        IssueLayout.photoEntityModify = issue.getPhotoEntities().get(position);
                        IssueLayout.listedPhoto = listedPhoto;
                        IssueLayout.photoListAdapter = IssuePhotoListAdapter.this;

                        Intent intent = new Intent(context, AddPhotoCommentActivity.class);
                        intent.putExtra("fileName", photoEntity.getFileName());
                        intent.putExtra("comment", ((TextView)view).getText().toString());
                        ((Activity)context).startActivityForResult(intent, Common.MODIFY_COMMENT);
                    }
                });
            }

            // 如果为删除模式，则不可点击
            if(delete) {
                photo.setOnClickListener(null);
                comment.setOnClickListener(null);
                deleteButton.setOnClickListener(null);
            }

            deleteButton.setVisibility(delete ? View.INVISIBLE : View.VISIBLE);

            // 如果在之前的操作里选择了删除
            if(listedPhoto.isDelete()) {
                view.setAlpha(0.3f);
            } else {
                view.setAlpha(1.0f);
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

                    BitmapUtil.saveBitmap(bitmap, 100, AppCommon.photoDirectory + currentTimeMillis + ".jpg");

                    Intent intent = new Intent(context, PhotoEditActivity.class);

                    intent.putExtra(PhotoEditActivity.EXTRA_DSTPATH, AppCommon.photoDirectory);
                    intent.putExtra(PhotoEditActivity.EXTRA_PHOTOTASK, new PhotoTask(0, name, currentTimeMillis, AppCommon.photoDirectory, 0));
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
                Helper.startCamera(context, AppCommon.photoDirectory, name, file);
            } else {
                intent.setClass(context, PhotoEditActivity.class);

                file = Long.parseLong(fileName.substring(0, fileName.length() - 4));
                intent.putExtra(PhotoEditActivity.EXTRA_DSTPATH, AppCommon.photoDirectory);
                intent.putExtra(PhotoEditActivity.EXTRA_PHOTOTASK, new PhotoTask(0, name, file, AppCommon.photoDirectory, 0));
                context.startActivity(intent);
            }
        }
    }

    @Override
    public void onPhotoProcessFinish(List<PhotoTask> list) {
        PhotoTask photoTask = list.get(0);

        // 处理完成
        if(photoTask.getState() == PhotoTask.STATE_COMPLETE) {
            Helper.handlePhoto(AppCommon.photoDirectory, photoTask.getFileName() + ".jpg");

            PhotoEntity tempPhotoEntity = PhotoFaultLayout.photoListAdapter.getItem(PhotoLayout.reTakePhotoEntity);

            Bitmap bitmap = BitmapFactory.decodeFile(AppCommon.photoDirectory + photoTask.getFileName() + ".jpg");

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
