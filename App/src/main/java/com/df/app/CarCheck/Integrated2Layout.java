package com.df.app.CarCheck;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.df.app.R;
import com.df.app.service.MyScrollView;
import com.df.app.util.Common;

/**
 * Created by 岩 on 13-12-25.
 */
public class Integrated2Layout extends LinearLayout {
    private static View rootView;

    private int[] spinnerIds = {
            R.id.cigarLighter_spinner,
            R.id.seatBelts_spinner,
            R.id.ashtray_spinner,
            R.id.rearSeats_spinner,
            R.id.spareTireGroove_spinner,
            R.id.trunkCorner_spinner,
            R.id.audio_spinner,
            R.id.seatSlide_spinner,
            R.id.ecu_spinner,
            R.id.it_water_back_corner_spinner,
            R.id.backCorner_spinner,
            R.id.discBox_spinner,
            R.id.storageCorner_spinner,
            R.id.newFuse_spinner,
            R.id.fuse_spinner,
            R.id.engineRoom_spinner};

    public Integrated2Layout(Context context) {
        super(context);
        init(context);
    }

    public Integrated2Layout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Integrated2Layout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.integrated2_layout, this);

        ImageView tireImage = (ImageView)findViewById(R.id.tire_image);

        Bitmap bitmap = BitmapFactory.decodeFile(Common.utilDirectory + "r3d4");
        tireImage.setImageBitmap(bitmap);

        MyScrollView scrollView = (MyScrollView)findViewById(R.id.root);
        scrollView.setListener(new MyScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(MyScrollView scrollView, int x, int y, int oldx, int oldy) {
                if (scrollView.getScrollY() > 5) {
                    showShadow(true);
                } else {
                    showShadow(false);
                }
            }
        });

        for(int i = 0; i < spinnerIds.length; i++) {
            setSpinnerColor(spinnerIds[i], Color.RED);
        }
    }

    private static void setSpinnerColor(int spinnerId, int color) {
        Spinner spinner = (Spinner) rootView.findViewById(spinnerId);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i >= 1)
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.RED);
                else
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);

                // 当选择项为“无”时，还应为黑色字体
                if(adapterView.getSelectedItem().toString().equals("无")) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    private void showShadow(boolean show) {
        findViewById(R.id.shadow).setVisibility(show ? VISIBLE : INVISIBLE);
    }
}
