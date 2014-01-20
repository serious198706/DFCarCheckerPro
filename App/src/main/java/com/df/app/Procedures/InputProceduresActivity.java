package com.df.app.Procedures;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import com.df.app.CarCheck.BasicInfoLayout;
import com.df.app.CarCheck.OptionsLayout;
import com.df.app.R;
import com.df.app.entries.CarSettings;

import java.util.List;

import static com.df.app.util.Helper.setTextView;

public class InputProceduresActivity extends Activity {
    private InputProceduresLayout inputProceduresLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procedures);

        Button homeButton = (Button) findViewById(R.id.buttonHome);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quitConfirm();
            }
        });

        inputProceduresLayout = (InputProceduresLayout)findViewById(R.id.inputProcedures);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            inputProceduresLayout.fillInData(bundle.getString("jsonString"));
        }
    }

    @Override
    public void onBackPressed() {
        if(inputProceduresLayout.canGoBack()) {
            inputProceduresLayout.goBack();
        } else {
            quitConfirm();
        }
    }

    private void quitConfirm() {
        View view1 = getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout)view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(R.string.quitInputProcedures);
        content.setTextSize(20f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, getResources().getString(R.string.alert));

        AlertDialog dialog = new AlertDialog.Builder(InputProceduresActivity.this)
                .setView(view1)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();
    }
}
