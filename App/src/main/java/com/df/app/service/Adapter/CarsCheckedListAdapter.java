package com.df.app.service.Adapter;

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

import java.util.ArrayList;

import static com.df.app.util.Helper.setTextView;

/**
 * Created by 岩 on 14-1-7.
 *
 * 已检车辆adapter
 */
public class CarsCheckedListAdapter extends BaseAdapter {
    public interface OnImport {
        public void onImport(int carId);
    }

    private Context context;
    private ArrayList<CarsCheckedItem> items;
    private OnImport mCallback;

    public CarsCheckedListAdapter(Context context, ArrayList<CarsCheckedItem> objects, OnImport listener) {
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

        final CarsCheckedItem carsCheckedItem = items.get(position);

        if (carsCheckedItem != null) {
            setTextView(view, R.id.car_number, "车牌号码：" + carsCheckedItem.getPlateNumber());
            setTextView(view, R.id.car_type, "车型：" + carsCheckedItem.getCarType());
            setTextView(view, R.id.car_color, "颜色：" + carsCheckedItem.getExteriorColor());
            setTextView(view, R.id.car_level, "分数：" + carsCheckedItem.getLevel());

            String carStatus = "";
            switch (Integer.parseInt(carsCheckedItem.getStatus())){
                case 0:
                    carStatus = "未完成";
                    break;
                case 1:
                    carStatus = "已提交";
                    break;
                case 2:
                    carStatus = "已导入";
                    break;
                case 3:
                    carStatus = "已参拍";
                    break;
                case 4:
                    carStatus = "已过期";
                    break;
                case 5:
                    carStatus = "已退回";
                    break;
            }

            setTextView(view, R.id.car_status, "状态：" + carStatus);
            setTextView(view, R.id.car_date, "创建日期：" + carsCheckedItem.getDate());

            Button button2 = (Button)view.findViewById(R.id.example_row_b_action_2);
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onImport(carsCheckedItem.getCarId());
                }
            });
        }

        return view;
    }
}
