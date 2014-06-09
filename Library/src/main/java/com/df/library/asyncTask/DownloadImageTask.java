package com.df.library.asyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;

/**
 * Created by 岩 on 14-3-15.
 */
public class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {


    public interface OnDownloadFinished {
        public void onFinish(Bitmap bitmap);
        public void onFailed();
    }

    private Context context;
    private String url;
    private OnDownloadFinished mCallback;
    private ProgressDialog progressDialog;

    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(context, null, "请稍候...", false, false);
    }


    public DownloadImageTask(Context context, String url, OnDownloadFinished listener) {
        this.context = context;
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
        progressDialog.dismiss();

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
