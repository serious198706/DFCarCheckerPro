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

import com.df.app.R;
import com.df.app.carCheck.AddPhotoCommentActivity;
import com.df.app.carCheck.IssueLayout;
import com.df.app.carCheck.PhotoLayout;
import com.df.app.entries.Issue;
import com.df.app.entries.ListedPhoto;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.PhotoOperationActivity;
import com.df.app.util.Common;

import java.util.List;

import static com.df.app.util.Helper.drawTextToBitmap;
import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 14-3-5.
 *
 * 弹出的绘制窗口中的列表adapter
 */
public class IssuePhotoListAdapter extends BaseAdapter {
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
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(ListedPhoto listedPhoto) {
        this.items.add(listedPhoto);
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
                final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera);
                photo.setImageBitmap(bitmap);
            } else {
                final Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + photoEntity.getThumbFileName());
                photo.setImageBitmap(bitmap);
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
            if(!delete) {
                photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 将要修改的photoEntity提取出来
                        PhotoLayout.reTakePhotoEntity = issue.getPhotoEntities().get(position);
                        PhotoLayout.listedPhoto = listedPhoto;
                        IssueLayout.photoListAdapter = IssuePhotoListAdapter.this;
                        showPhoto(photoEntity.getFileName());
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

            deleteButton.setVisibility(delete ? View.INVISIBLE : View.VISIBLE);
        }

        return view;
    }


    /**
     * 点击缩略图时，显示对应的图片
     * @param fileName
     */
    private void showPhoto(String fileName) {
        Intent intent = new Intent(context, PhotoOperationActivity.class);
        intent.putExtra("fileName", Common.photoDirectory + fileName);
        context.startActivity(intent);
    }
}
