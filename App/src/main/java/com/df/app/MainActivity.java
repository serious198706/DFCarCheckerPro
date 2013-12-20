package com.df.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.df.app.CarCheck.CarCheckActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button carCheckButton = (Button)findViewById(R.id.buttonCarCheck);
        carCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterCarCheck();
            }
        });

        Button carsWaitingButton = (Button)findViewById(R.id.buttonCarsWaiting);
        carsWaitingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterCarsWaiting();
            }
        });

        Button carsCheckedButton = (Button)findViewById(R.id.buttonCarsChecked);
        carsCheckedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterCarsChecked();
            }
        });

        Button quitButton = (Button)findViewById(R.id.buttonQuit);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quit();
            }
        });
    }

    private void enterCarCheck() {
        Intent intent = new Intent(this, CarCheckActivity.class);
        startActivity(intent);
    }

    private void enterCarsWaiting() {

    }

    private void enterCarsChecked() {

    }

    private void quit() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.alert)
                .setMessage(R.string.quitMsg)
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
