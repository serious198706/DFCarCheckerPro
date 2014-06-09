package com.df.library.service.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.df.library.R;
import com.df.library.entries.CarsWaitingItem;

import java.util.ArrayList;

import static com.df.library.util.Helper.setTextView;

/**
 * Created by 岩 on 14-1-7.
 *
 * 待检车辆adapter
 */
public class CarsWaitingListAdapter extends BaseAdapter {
    public interface OnModifyProcedure {
        public void onModifyProcedure(CarsWaitingItem item);
    }

    public interface OnDeleteCar {
        public void onDeleteCar(int position);
    }

    public interface OnEditPressed {
        public void onEditPressed(int position);
    }


    public interface OnAction {
        public void onEditPressed(int position);
        public void onModifyProcedure(int positon);
        public void onDeleteCar(int position);
    }

    private Context context;
    private ArrayList<CarsWaitingItem> items;
    private OnAction mCallback;

    public CarsWaitingListAdapter(Context context, ArrayList<CarsWaitingItem> objects, OnAction listener) {
        this.context = context;
        this.items = objects;
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.cars_checked_list_row, null);
        }

        final CarsWaitingItem carsWaitingItem = items.get(position);

        if (carsWaitingItem != null) {
            setTextView(view, R.id.car_number, carsWaitingItem.getPlateNumber());
            setTextView(view, R.id.car_type, carsWaitingItem.getCarType());
            setTextView(view, R.id.car_color, carsWaitingItem.getExteriorColor());
            setTextView(view, R.id.car_date, carsWaitingItem.getDate());
            view.findViewById(R.id.car_level).setVisibility(View.GONE);
            view.findViewById(R.id.car_status).setVisibility(View.GONE);
        }

        ImageView edit = (ImageView)view.findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onEditPressed(position);
            }
        });

        Button button1 = (Button)view.findViewById(R.id.example_row_b_action_1);
        button1.setText(R.string.modifyProcedures);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onModifyProcedure(position);
            }
        });

        Button button2 = (Button)view.findViewById(R.id.example_row_b_action_2);
        button2.setText(R.string.deleteCar);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onDeleteCar(position);
            }
        });

        return view;
    }
}
