package com.df.library.service.views;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;

import com.df.library.R;
import com.df.library.entries.Cooperator;
import com.df.library.service.adapter.CooperatorListAdapter;

import java.util.List;

/**
 * Created by 岩 on 14-4-25.
 */
public class SelectCooperatorDialog extends Dialog {
    private ListView listView;
    private CooperatorListAdapter adapter;

    public interface OnSelected {
        public void onSelected(Cooperator cooperator);
    }

    private Context context;
    private OnSelected mCallback;
    private List<Cooperator> cooperators;

    public SelectCooperatorDialog(Context context, List<Cooperator> cooperators, OnSelected listener) {
        super(context);
        setContentView(R.layout.cooperator_list);

        this.context = context;
        this.cooperators = cooperators;
        this.mCallback = listener;

        init();
    }

    private void init() {
        EditText searchView = (EditText)findViewById(R.id.searchCooperator);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Filter filter = adapter.getFilter();

                filter.filter(s.toString());
            }
        }
        );

        listView = (ListView)findViewById(R.id.cooperatorList);

        adapter = new CooperatorListAdapter(context, cooperators, new CooperatorListAdapter.OnSelected() {
            @Override
            public void onSelected(Cooperator cooperator) {
                dismiss();
                mCallback.onSelected(cooperator);
            }
        });

        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(false);
    }
}
