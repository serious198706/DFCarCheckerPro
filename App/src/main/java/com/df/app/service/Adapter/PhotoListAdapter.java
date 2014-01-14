package com.df.app.service.Adapter;

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
            Bitmap bitmap = BitmapFactory.decodeFile(Common.photoDirectory + photoEntity
                    .getFileName());
            photo.setImageBitmap(bitmap);

            TextView photoName = (TextView) view.findViewById(R.id.photo_name);
            photoName.setText(photoEntity.getName());

            EditText photoComment = (EditText) view.findViewById(R.id.photo_comment);
            photoComment.setText(photoEntity.getComment());
        }
        return view;
    }
}
