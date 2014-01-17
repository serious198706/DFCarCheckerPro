package com.df.app.service.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.df.app.CarCheck.AccidentResultLayout;
import com.df.app.R;
import com.df.app.entries.Issue;
import com.df.app.entries.PhotoEntity;
import com.df.app.util.Common;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-26.
 */
public class PhotoListAdapter extends ArrayAdapter<PhotoEntity> {
    private List<PhotoEntity> items;
    private Context context;
    private Dialog mPictureDialog;

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
            final Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + photoEntity
                    .getThumbFileName());
            photo.setImageBitmap(bitmap);

//            if(bitmap.getWidth() == 400) {
//                photo.setLayoutParams(new LinearLayout.LayoutParams(160, 120));
//            }

            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPhoto(Common.photoDirectory + photoEntity.getFileName());
                }
            });


            TextView photoName = (TextView) view.findViewById(R.id.photo_name);
            photoName.setText(photoEntity.getName());

            EditText photoComment = (EditText) view.findViewById(R.id.photo_comment);
            photoComment.setText(photoEntity.getComment());
        }
        return view;
    }

    private void showPhoto(String fileName) {
        View view = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.picture_popup,
                (ViewGroup)  ((Activity)getContext()).findViewById(R.id.layout_root));

        ImageView image = (ImageView) view.findViewById(R.id.fullimage);

        Bitmap bitmap = BitmapFactory.decodeFile(fileName);
        image.setImageBitmap(bitmap);

        mPictureDialog = new Dialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        mPictureDialog.setContentView(view);
        mPictureDialog.setCancelable(true);
        mPictureDialog.show();
    }
}
