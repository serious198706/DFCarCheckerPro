package com.df.app.service.AsyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.df.app.MainActivity;
import com.df.app.service.SoapService;
import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by 岩 on 14-1-13.
 *
 * 获取车辆详细信息线程
 */

public class GetCarDetailTask extends AsyncTask<Void, Void, Boolean> {
    public interface OnGetDetailFinished {
        public void onFinish(String result);
        public void onFailed(String result);
    }

    private Context context;
    private SoapService soapService;
    private ProgressDialog progressDialog;
    private OnGetDetailFinished mCallback;
    private int carId;
    private JSONObject resultObject;

    public GetCarDetailTask(Context context, int carId, OnGetDetailFinished listener) {
        this.context = context;
        this.carId = carId;
        this.mCallback = listener;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(context, null, "正在获取车辆信息，请稍候...", false, false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = false;

        // 如果本地存在这辆车的数据
        File file = new File(Common.savedDirectory + Integer.toString(carId));

        if(file.exists()) {
            try {
                String result = "";
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
                BufferedReader reader=new BufferedReader(read);
                String line;

                while ((line = reader.readLine()) != null) {
                    result += line;
                }

                read.close();

                // 获取一次详细信息，以更新手续信息（防止手续信息修改后本地未更新）
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("CarId", carId);
                jsonObject.put("UserId", MainActivity.userInfo.getId());
                jsonObject.put("Key", MainActivity.userInfo.getKey());

                soapService = new SoapService();
                soapService.setUtils(Common.getSERVER_ADDRESS() + Common.CAR_CHECK_SERVICE, Common.GET_CAR_DETAIL);

                success = soapService.communicateWithServer(jsonObject.toString());

                // 如果成功，更新手续信息
                if(success) {
                    JSONObject temp = new JSONObject(soapService.getResultMessage());
                    JSONObject features = temp.getJSONObject("features");
                    JSONObject procedures = features.getJSONObject("procedures");

                    resultObject = new JSONObject(result);
                    JSONObject newFeatures = resultObject.getJSONObject("features");
                    newFeatures.put("procedures", procedures);

                    resultObject.put("features", newFeatures);
                    soapService.setResultMessage(resultObject.toString());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 如果不存在
        else {
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("CarId", carId);
                jsonObject.put("UserId", MainActivity.userInfo.getId());
                jsonObject.put("Key", MainActivity.userInfo.getKey());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            soapService = new SoapService();
            soapService.setUtils(Common.getSERVER_ADDRESS() + Common.CAR_CHECK_SERVICE, Common.GET_CAR_DETAIL);

            success = soapService.communicateWithServer(jsonObject.toString());
        }

        return success;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        progressDialog.dismiss();

        if(success) {
            mCallback.onFinish(soapService.getResultMessage());
        } else {
            mCallback.onFailed(soapService.getErrorMessage());
        }
    }
}
