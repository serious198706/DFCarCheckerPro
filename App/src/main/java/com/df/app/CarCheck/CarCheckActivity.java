package com.df.app.CarCheck;

import android.app.ActionBar;
import android.app.Activity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.df.app.R;

public class CarCheckActivity extends Activity {
    int tabIds[] = {R.id.basicInfo, R.id.accidentCheck, R.id.integratedCheck, R.id.photo};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_check);

        final ActionBar actionBar = getActionBar();
        actionBar.hide();

        Button basicInfoButton = (Button)findViewById(R.id.buttonBasicInfo);
        basicInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {selectTab(R.id.basicInfo);
            }
        });

        Button accidentCheckButton = (Button)findViewById(R.id.buttonAccidentCheck);
        accidentCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {selectTab(R.id.accidentCheck);
            }
        });

        Button integratedButton = (Button)findViewById(R.id.buttonIntegratedCheck);
        integratedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {selectTab(R.id.integratedCheck);
            }
        });

        Button photoButton = (Button)findViewById(R.id.buttonPhoto);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTab(R.id.photo);
            }
        });
    }

    private void selectTab(int layoutId) {
        for(int id : tabIds) {
            if(id != layoutId) {
                findViewById(id).setVisibility(View.GONE);
            } else {
                findViewById(id).setVisibility(View.VISIBLE);
            }
        }
    }
}
