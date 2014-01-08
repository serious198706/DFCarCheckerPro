package com.df.app.service;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.df.app.R;
import com.df.app.entries.CarsCheckedItem;
import com.df.app.entries.CarsWaitingItem;

import java.util.ArrayList;

import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 14-1-7.
 */
public class CarsWaitingListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<CarsWaitingItem> items;

    public CarsWaitingListAdapter(Context context, ArrayList<CarsWaitingItem> objects) {
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

        final CarsWaitingItem carsWaitingItem = items.get(position);

        if (carsWaitingItem != null) {
            setTextView(view, R.id.car_number, carsWaitingItem.getPlateNumber());
            setTextView(view, R.id.car_type, carsWaitingItem.getCarType());
            setTextView(view, R.id.car_color, carsWaitingItem.getExteriorColor());
            setTextView(view, R.id.car_status, carsWaitingItem.getStatus());
            setTextView(view, R.id.car_date, carsWaitingItem.getDate());
            view.findViewById(R.id.car_level).setVisibility(View.GONE);
        }

        Button button1 = (Button)view.findViewById(R.id.example_row_b_action_1);
        button1.setText(R.string.modifyProcedures);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "修改手续信息", Toast.LENGTH_SHORT).show();
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
