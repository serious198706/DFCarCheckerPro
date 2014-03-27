package com.df.app.service.AsyncTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.df.app.entries.PosEntity;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by 岩 on 14-3-15.
 */
public class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {
    public interface OnDownloadFinished {
        public void onFinish(Bitmap bitmap);
        public void onFailed();
    }

    private String url;
    private OnDownloadFinished mCallback;

    public DownloadImageTask(String url, OnDownloadFinished listener) {
        this.url = url;
        this.mCallback = listener;
    }

    protected Bitmap doInBackground(Void... params) {
        Bitmap tempBitmap = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            tempBitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return tempBitmap;
    }

    protected void onPostExecute(Bitmap result) {
        if(result == null) {
            mCallback.onFailed();
        } else {
            mCallback.onFinish(result);
        }
    }

    @Override
    protected void onCancelled() {
        mCallback.onFailed();
    }
}
