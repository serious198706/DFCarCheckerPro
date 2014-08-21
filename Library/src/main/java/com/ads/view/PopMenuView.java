package com.ads.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.df.library.R;

public class PopMenuView extends FrameLayout {
    private TextView mTitle;
    private ListView mList;
    private Button mBtnStart;
    private Button mBtnExit;
    private ArrayAdapter<String> mAdapter;
    
    private static OnTouchListener mDummyListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    };

    public PopMenuView(Context context) {
        super(context);
    }

    public PopMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        inflater.inflate(R.layout.pop_menu, this); 
        
        setOnTouchListener(mDummyListener);
        
        mTitle = (TextView) findViewById(R.id.ads_pop_menu_title);
        mList = (ListView) findViewById(R.id.ads_pop_menu_list);
        mBtnStart = (Button) findViewById(R.id.ads_pop_menu_btn_start);
        mBtnExit = (Button) findViewById(R.id.ads_pop_menu_btn_exit);
        
        mList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mList.setItemsCanFocus(false);
        
        mAdapter = new ArrayAdapter<String>(context, R.layout.radio_text_resource, R.id.radioTxt);
        mList.setAdapter(mAdapter);
    }
    
    public void setTitle(String title) {
        mTitle.setText(title);
    }
    
    public void invalidateListView() {
        mList.invalidateViews();
    }
    
    public void addItem(String item) {
        mAdapter.add(item);
        mList.setItemChecked(0, true);
    }
    
    public void clearItems() {
        mAdapter.clear();
    }
    
    public void setButtonsOnClickListener(OnClickListener listener) {
        mBtnStart.setOnClickListener(listener);
        mBtnExit.setOnClickListener(listener);
    }
    
    public int getSelectedItemId() {
        return mList.getCheckedItemPosition();
    }
}
