package com.df.library.service.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.df.library.R;
import com.df.library.entries.Cooperator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 14-4-28.
 */
public class CooperatorListAdapter extends BaseAdapter implements Filterable {
    private final OnSelected mCallback;

    private Context context;
    private List<Cooperator> items;
    private List<Cooperator> filteredItems;
    ItemFilter mFilter = new ItemFilter();

    public interface OnSelected {
        public void onSelected(Cooperator cooperator);
    }

    private class ViewHolder {
        public TextView name;
    }

    public CooperatorListAdapter(Context context, List<Cooperator> items, OnSelected listener) {
        this.context = context;
        this.items = items;
        this.mCallback = listener;
        this.filteredItems = items;
    }

    @Override
    public int getCount() {
        return filteredItems.size();
    }

    @Override
    public Cooperator getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();

        if(convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.cooperator_list_item, null);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.name.setText(filteredItems.get(position).getName());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onSelected(filteredItems.get(position));
            }
        });

        return convertView;
    }


    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<Cooperator> list = items;

            int count = list.size();
            final ArrayList<Cooperator> nlist = new ArrayList<Cooperator>(count);

            String filterableString ;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i).getName();
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(list.get(i));
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredItems = (List<Cooperator>) results.values;
            notifyDataSetChanged();
        }

    }
}
