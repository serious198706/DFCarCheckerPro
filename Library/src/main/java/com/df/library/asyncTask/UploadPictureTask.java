package com.df.library.asyncTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.library.R;
import com.df.library.entries.Action;
import com.df.library.entries.PhotoEntity;
import com.df.library.service.SoapService;
import com.df.library.util.Common;

import java.util.List;

import static com.df.library.util.Helper.setTextView;

/**
 * Created by 岩 on 14-1-9.
 *
 * 上传图片
 */

public class UploadPictureTask extends AsyncTask<Void, Integer, Boolean> {
    public interface UploadFinished {
        public void onFinish();
        public void onCancel();
    }

    private int total;
    private Context context;
    private String path;
    private List<PhotoEntity> photoEntityList;

    private UploadFinished mCallback;

    private ProgressDialog progressDialog;

    public UploadPictureTask(Context context, String path, List<PhotoEntity> photoEntityList, UploadFinished listener) {
        this.photoEntityList = photoEntityList;
        this.context = context;
        this.mCallback = listener;
        this.path = path;

        total = photoEntityList.size();
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setMessage("正在上传照片，请稍候...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                alertUser();
            }
        });
        progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertUser();
            }
        });
        progressDialog.setMax(total);
        progressDialog.show();
    }

    private void alertUser() {
        View view1 = ((Activity) context).getLayoutInflater().inflate(R.layout.popup_layout, null);
        TableLayout contentArea = (TableLayout) view1.findViewById(R.id.contentArea);
        TextView content = new TextView(view1.getContext());
        content.setText(R.string.cancelUploading);
        content.setTextSize(20f);
        contentArea.addView(content);

        setTextView(view1, R.id.title, context.getResources().getString(R.string.alert));

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view1)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancel(true);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressDialog.show();
                    }
                })
                .create();

        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = false;

        SoapService soapService = new SoapService();
        soapService.setUtils(Common.getSERVER_ADDRESS() + Common.CAR_CHECK_SERVICE, Common.UPLOAD_PICTURE);

        for(int i = 0; i < photoEntityList.size(); i++) {
            if(isCancelled()) {
                break;
            }

            PhotoEntity photoEntity = photoEntityList.get(i);

            // 获取照片的物理路径
            Bitmap bitmap;

            String fileName = photoEntity.getFileName();

            if(photoEntity.getModifyAction().equals(Action.DELETE) || photoEntity.getModifyAction().equals(Action.COMMENT)) {
                fileName = "";
            }

            Log.d(Common.TAG, "正在上传...");
            Log.d(Common.TAG, photoEntity.getJsonString());

            // 如果照片名为空串，表示要上传空照片
            if(fileName.equals("")) {
                success = soapService.uploadPicture(photoEntity.getJsonString());
            } else {
                bitmap = BitmapFactory.decodeFile(path + fileName);

                success = soapService.uploadPicture(bitmap, photoEntity.getJsonString());
            }

            if(success) {
                // 如果成功上传，推动进度条
                Log.d(Common.TAG, "上传成功！");
                publishProgress(i + 1);
            } else {
               i--;

            }
        }

        return success;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        progressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if(success) {
            progressDialog.dismiss();
            mCallback.onFinish();
        } else {
            progressDialog.dismiss();
            Toast.makeText(context, "上传失败！！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCancelled() {
        total = 0;
        context = null;
        photoEntityList = null;

        mCallback.onCancel();
        mCallback = null;

        progressDialog.dismiss();
        progressDialog = null;
    }
}
