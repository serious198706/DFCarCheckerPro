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
import com.df.app.carCheck.PhotoLayout;
import com.df.app.entries.Action;
import com.df.app.entries.ListedPhoto;
import com.df.app.entries.PhotoEntity;
import com.df.app.service.PhotoOperationActivity;
import com.df.app.util.Common;
import com.df.app.util.lazyLoadHelper.ImageLoader;

import java.util.List;

import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 14-3-13.
 */
public class PaintPhotoListAdapter extends BaseAdapter {
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
    public Object getItem(int position) {
        return items.get(position);
    }

    public Object getItem(ListedPhoto listedPhoto) {
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
                    showPhoto(photoEntity.getFileName());
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
        }


        return view;
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
