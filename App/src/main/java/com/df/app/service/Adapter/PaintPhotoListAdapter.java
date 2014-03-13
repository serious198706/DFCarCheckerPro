package com.df.app.service.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
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
import com.df.app.entries.ListedPhoto;
import com.df.app.util.Common;

import java.util.List;

import static com.df.app.util.Helper.drawTextToBitmap;
import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 14-3-13.
 */
public class PaintPhotoListAdapter extends BaseAdapter {
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

            if(listedPhoto.getFileName() == null || listedPhoto.getFileName().equals("")) {
                final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera);
                photo.setImageBitmap(bitmap);
            } else {
                final Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + listedPhoto.getFileName());
                photo.setImageBitmap(bitmap);
            }

            //TODO 点击图片之后要做什么？ 弹出照片编辑框
            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            TextView comment = (TextView)view.findViewById(R.id.issueComment);
            comment.setText(listedPhoto.getDesc());
            comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    IssueLayout.photoEntityModify = issue.getPhotoEntities().get(position);
//                    IssueLayout.listedPhoto = listedPhoto;
//                    IssueLayout.photoListAdapter = IssuePhotoListAdapter.this;
//
//                    Intent intent = new Intent(context, AddPhotoCommentActivity.class);
//                    intent.putExtra("fileName", listedPhoto.getFileName());
//                    intent.putExtra("comment", ((TextView)view).getText().toString());
//                    ((Activity)context).startActivityForResult(intent, Common.MODIFY_COMMENT);
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
}
