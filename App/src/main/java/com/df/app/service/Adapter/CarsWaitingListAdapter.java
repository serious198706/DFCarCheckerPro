package com.df.app.service.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.df.app.R;
import com.df.app.entries.CarsWaitingItem;

import java.util.ArrayList;

import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 14-1-7.
 *
 * 待检车辆adapter
 */
public class CarsWaitingListAdapter extends BaseAdapter {
    public interface OnModifyProcedure {
        public void onModifyProcedure(CarsWaitingItem item);
    }

    public interface OnEditPressed {
        public void onEditPressed(int position);
    }

    private Context context;
    private ArrayList<CarsWaitingItem> items;
    private OnModifyProcedure mCallback;
    private OnEditPressed mEditCallback;

    public CarsWaitingListAdapter(Context context, ArrayList<CarsWaitingItem> objects, OnModifyProcedure listener, OnEditPressed listener1) {
        this.context = context;
        this.items = objects;
        this.mCallback = listener;
        this.mEditCallback = listener1;
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
                mEditCallback.onEditPressed(position);
            }
        });

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
