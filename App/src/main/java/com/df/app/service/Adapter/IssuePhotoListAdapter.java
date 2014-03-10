package com.df.app.service.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
import com.df.app.carCheck.PhotoFaultLayout;
import com.df.app.entries.Issue;
import com.df.app.entries.IssuePhoto;
import com.df.app.entries.PhotoEntity;
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
    private List<IssuePhoto> items;
    private Issue issue;
    private OnDeleteItem mCallback;

    public IssuePhotoListAdapter(Context context, List<IssuePhoto> items, Issue issue, OnDeleteItem listener) {
        this.context = context;
        this.items = items;
        this.issue = issue;
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

    public void addItem(IssuePhoto issuePhoto) {
        this.items.add(issuePhoto);
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

        final IssuePhoto issuePhoto = items.get(position);

        if(issuePhoto != null) {
            ImageView indexImage = (ImageView)view.findViewById(R.id.indexImage);
            indexImage.setImageBitmap(drawTextToBitmap(context, R.drawable.damage, position + 1));

            ImageView photo = (ImageView)view.findViewById(R.id.issuePhoto);

            if(issuePhoto.getFileName() == null || issuePhoto.getFileName().equals("")) {
                final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.camera);
                photo.setImageBitmap(bitmap);
            } else {
                final Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + issuePhoto.getFileName());
                photo.setImageBitmap(bitmap);
            }

            //TODO 点击图片之后要做什么？
            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            TextView comment = (TextView)view.findViewById(R.id.issueComment);
            comment.setText(issuePhoto.getDesc());
            comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    IssueLayout.photoEntityModify = issue.getPhotoEntities().get(position);
                    IssueLayout.issuePhoto = issuePhoto;
                    IssueLayout.photoListAdapter = IssuePhotoListAdapter.this;

                    Intent intent = new Intent(context, AddPhotoCommentActivity.class);
                    intent.putExtra("fileName", issuePhoto.getFileName());
                    intent.putExtra("comment", ((TextView)view).getText().toString());
                    ((Activity)context).startActivityForResult(intent, Common.MODIFY_COMMENT);
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
