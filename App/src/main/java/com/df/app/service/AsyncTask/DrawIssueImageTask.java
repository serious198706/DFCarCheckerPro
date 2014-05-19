package com.df.app.service.AsyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.df.app.MainActivity;
import com.df.app.R;
import com.df.app.service.SoapService;
import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by 岩 on 14-4-10.
 */
public class DrawIssueImageTask extends AsyncTask<Void, Void, Boolean> {
    private Bitmap bitmap;

    public interface OnDrawFinished {
        public void onFinished(Bitmap accident, Bitmap accidentHome);
    }

    Context context;
    String level1;
    String level2;
    // 图片层
    private ArrayList<Drawable> drawableList;
    private ProgressDialog progressDialog;
    private OnDrawFinished mCallback;

    private Bitmap accident;
    private Bitmap accidentHome;

    public DrawIssueImageTask(Context context, String level1, String level2, OnDrawFinished listener) {
        this.context = context;
        this.level1 = level1;
        this.level2 = level2;
        this.mCallback = listener;
    }

    @Override
    protected void onPreExecute()
    {
        progressDialog = ProgressDialog.show(context, null,
                "正在处理，请稍候...", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        drawBase();
        drawSketch(handelLevelNames(level1, 1));
        accident = drawSketch(handelLevelNames(level2, 2));

        drawableList.clear();
        drawBase();
        accidentHome = drawSketch(handelLevelNames(level2, 2));

        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        progressDialog.dismiss();

        mCallback.onFinished(accident, accidentHome);
    }

    /**
     * 传入"L,E,K,LC,RB"，传出"L1, E1, K1"
     *
     */
    private String handelLevelNames(String level, int n) {
        String[] partNames = level.split(",");

        String result = "";

        for(String part : partNames) {
            // 将LA,LB,LC,RA,RB,RC过滤掉
            if(part.length() != 1) {
                continue;
            }

            part += Integer.toString(n);
            result += part;
            result += ",";
        }

        return result.length() == 0 ? result : result.substring(0, result.length() - 1);
    }

    /**
     * 绘制底图
     */
    private void drawBase() {
        Bitmap baseBitmap = BitmapFactory.decodeFile(Common.utilDirectory + "base");

        // 先添加底图
        drawableList = new ArrayList<Drawable>();
        drawableList.add(new BitmapDrawable(context.getResources(), baseBitmap));
    }
    /**
     * 设置漆面预览图
     * @param level
     */
    private Bitmap drawSketch(String level) {
        Bitmap bitmap = null;

        try {
            if(!level.equals("")) {
                String[] partNames = level.split(",");

                // 根据名称添加其他图
                for(String layerName : partNames) {
                    Drawable bitmapDrawable = Drawable.createFromPath(Common.utilDirectory + layerName);
                    drawableList.add(bitmapDrawable);
                }
            }

            // 创建LayerDrawable
            LayerDrawable layerDrawable = new LayerDrawable(drawableList.toArray(new Drawable[drawableList.size()]));

            int width = layerDrawable.getIntrinsicWidth();
            int height = layerDrawable.getIntrinsicHeight();

            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            layerDrawable.setBounds(0, 0, width, height);
            layerDrawable.draw(new Canvas(bitmap));

            System.gc();


        } catch (OutOfMemoryError e) {
            Toast.makeText(context, "内存不足，请稍候重试！", Toast.LENGTH_SHORT).show();
        }

        return bitmap;
    }
}
