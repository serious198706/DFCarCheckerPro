package com.df.app.service.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.df.app.Procedures.InputProceduresActivity;
import com.df.app.R;
import com.df.app.entries.CarsCheckedItem;
import com.df.app.entries.CarsWaitingItem;

import java.util.ArrayList;

import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 14-1-7.
 */
public class CarsWaitingListAdapter extends BaseAdapter {
    public interface OnModifyProcedure {
        public void onModifyProcedure(CarsWaitingItem item);
    }

    private Context context;
    private ArrayList<CarsWaitingItem> items;
    private OnModifyProcedure mCallback;

    public CarsWaitingListAdapter(Context context, ArrayList<CarsWaitingItem> objects, OnModifyProcedure listener) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.cars_checked_list_row, null);
        }

        final CarsWaitingItem carsWaitingItem = items.get(position);

        if (carsWaitingItem != null) {
            setTextView(view, R.id.car_number, "车牌号码：" + carsWaitingItem.getPlateNumber());
            setTextView(view, R.id.car_type, "型号：" + carsWaitingItem.getCarType());
            setTextView(view, R.id.car_color, "颜色：" + carsWaitingItem.getExteriorColor());
            setTextView(view, R.id.car_date, "创建日期：" + carsWaitingItem.getDate());
            view.findViewById(R.id.car_level).setVisibility(View.GONE);
            view.findViewById(R.id.car_status).setVisibility(View.GONE);
        }

        Button button1 = (Button)view.findViewById(R.id.example_row_b_action_1);
        button1.setText(R.string.modifyProcedures);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onModifyProcedure(carsWaitingItem);
            }
        });

        Button button2 = (Button)view.findViewById(R.id.example_row_b_action_2);
        button2.setText(R.string.check);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "检测车辆", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
