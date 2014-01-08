package com.df.app.service;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.df.app.R;
import com.df.app.entries.CarsCheckedItem;

import java.util.ArrayList;

import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 14-1-7.
 */
public class CarsCheckedListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<CarsCheckedItem> items;

    public CarsCheckedListAdapter(Context context, ArrayList<CarsCheckedItem> objects) {
        this.context = context;
        this.items = objects;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.cars_checked_list_row, null);
        }

        final CarsCheckedItem carsCheckedItem = items.get(position);

        if (carsCheckedItem != null) {
            setTextView(view, R.id.car_number, carsCheckedItem.getPlateNumber());
            setTextView(view, R.id.car_type, carsCheckedItem.getCarType());
            setTextView(view, R.id.car_color, carsCheckedItem.getExteriorColor());
            setTextView(view, R.id.car_level, carsCheckedItem.getLevel());
            setTextView(view, R.id.car_status, carsCheckedItem.getStatus());
            setTextView(view, R.id.car_date, carsCheckedItem.getDate());
        }

        return view;
    }
}
