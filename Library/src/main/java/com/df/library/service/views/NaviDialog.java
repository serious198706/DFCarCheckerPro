package com.df.library.service.views;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;

import com.df.library.R;


/**
 * Created by 岩 on 2014/6/4.
 */
public class NaviDialog extends Dialog {
    public interface OnChoice {
        public void onChoise(int index);
    }

    private Context context;
    private OnChoice mCallback;

    public NaviDialog(Context context, OnChoice listener) {
        super(context);

        this.context = context;
        this.mCallback = listener;

        init();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.navi_dialog_layout);

        for(int i = 0; i < 5; i++) {
            int redId = context.getResources().getIdentifier("btn" + (i + 1), "id", context.getPackageName());

            final int index = i;

            findViewById(redId).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onChoise(index);
                    choose(index);
                }
            });
        }
    }

    private void choose(int index) {
        findViewById(R.id.btn1).setBackgroundResource(index == 0 ? R.drawable.basic_info_active : R.drawable.basic_info);
        findViewById(R.id.btn2).setBackgroundResource(index == 1 ? R.drawable.accident_active : R.drawable.accident);
        findViewById(R.id.btn3).setBackgroundResource(index == 2 ? R.drawable.integrated_active : R.drawable.integrated);
        findViewById(R.id.btn4).setBackgroundResource(index == 3 ? R.drawable.photo_active : R.drawable.photo);
        findViewById(R.id.btn5).setBackgroundResource(index == 4 ? R.drawable.transaction_active : R.drawable.transaction);
    }
}
