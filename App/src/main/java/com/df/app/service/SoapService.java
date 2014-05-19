package com.df.app.service;

import android.graphics.Bitmap;
import android.util.Log;

import com.df.app.util.Common;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.ByteArrayOutputStream;

/**
 * Created by 岩 on 13-12-28.
 *
 * SoapService
 */
public class SoapService implements ISoapService {
    private String errorMessage;
    private String resultMessage;

    private String url;
    private String soapAction;
    private String methodName;

    public SoapService() {}

    /**
     * 设置url, methodName
     * @param url
     * @param methodName
     */
    public void setUtils(String url, String methodName) {
        this.url = url;
        this.soapAction = Common.SOAP_ACTION + methodName;
        this.methodName = methodName;
    }

    // 设置错误信息
    public void setErrorMessage(String msg) {errorMessage = msg;}

    // 获取错误信息
    public String getErrorMessage() { return errorMessage; }

    // 获取结果信息
    public void setResultMessage(String resultMessage) { this.resultMessage = resultMessage; }
    public String getResultMessage() { return resultMessage; }

    /**
     * 通讯，不传递数据，比如检查更新
     * @return 是否成功
     */
    public boolean communicateWithServer() {
        errorMessage = "";
        resultMessage = "";

        // 各种配置
        SoapObject request = new SoapObject(Common.NAMESPACE, this.methodName);

        SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
        new MarshalBase64().register(envelope);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE trans = new HttpTransportSE(this.url);

        try {
            trans.call(this.soapAction, envelope);
        } catch (Exception e) {
            if(e.getMessage() != null)
                Log.d(Common.TAG, "无法连接到服务器：" + e.getMessage());
            else
                Log.d(Common.TAG, "无法连接到服务器！" );

            errorMessage = "无法连接到服务器！";
            resultMessage = "";

            return false;
        }

        // 收到的结果
        SoapObject soapObject = (SoapObject) envelope.bodyIn;

        // 成功失败标志位
        String result = soapObject.getProperty(0).toString();

        // 成功
        if(!result.equals("")) {
            resultMessage = result;
            return true;
        }
        // 失败
        else {
            Log.d(Common.TAG, "获取版本失败！");
            return false;
        }
    }

    /**
     * 通讯，如登录、提交信息等
     * @param jsonString 要通讯的数据
     * @return 是否成功
     */
    public boolean communicateWithServer(String jsonString) {
        errorMessage = "";
        resultMessage = "";

        SoapObject request = new SoapObject(Common.NAMESPACE, this.methodName);
        request.addProperty("inputStringJson", jsonString);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); // ??
        envelope.bodyOut = request;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE trans = new HttpTransportSE(this.url);

        try {
            trans.call(this.soapAction, envelope);
        } catch (Exception e) {
            if(e.getMessage() != null)
                Log.d(Common.TAG, "无法连接到服务器：" + e.getMessage());
            else
                Log.d(Common.TAG, "无法连接到服务器！");

            errorMessage = "无法连接到服务器！";
            resultMessage = "";
            e.printStackTrace();

            return false;
        }

        // 收到的结果
        SoapObject soapObject = (SoapObject) envelope.bodyIn;

        // 成功失败标志位
        String flag = soapObject.getProperty(0).toString();

        // JSON格式数据
        resultMessage = soapObject.getProperty(1).toString();

        // 成功
        if(flag.equals("0")) {
            errorMessage = "";
            return true;
        }
        // 失败
        else {
            errorMessage = resultMessage;
            Log.d(Common.TAG, resultMessage);

            return false;
        }
    }

    /**
     * 上传照片
     * @param bitmap 图片
     * @param jsonString 图片信息
     * @return 是否成功
     */
    public boolean uploadPicture(Bitmap bitmap, String jsonString) {
        errorMessage = "";
        resultMessage = "";

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] byteArray = null;
        byte[] newByteArray;

        // 将图片转换成流
        // 有可能有的缺陷没有照片
        if(bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
            byteArray = stream.toByteArray();
        }
        else {
            // 如果没有图片，那还传个毛线
            errorMessage = "图片为空！";
            return false;
        }

        // 在图片流后面加上分隔符 #:
        jsonString = "#:" + jsonString;

        // 将图片流复制到新的byte数组中
        int length = byteArray.length;

        newByteArray = new byte[length + jsonString.getBytes().length];
        System.arraycopy(byteArray, 0, newByteArray, 0, length);

        for(int i = 0; i < jsonString.getBytes().length; i++) {
            newByteArray[length + i] = jsonString.getBytes()[i];
        }

        // 各种配置
        SoapObject request = new SoapObject(Common.NAMESPACE, this.methodName);
        request.addProperty("stream", newByteArray);

        SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
        new MarshalBase64().register(envelope);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE trans = new HttpTransportSE(this.url);

        try {
            trans.call(this.soapAction, envelope);
        } catch (Exception e) {
            if(e.getMessage() != null)
                Log.d(Common.TAG, "无法连接到服务器：" + e.getMessage());
            else
                Log.d(Common.TAG, "无法连接到服务器！");

            errorMessage = "无法连接到服务器！";

            return false;
        }

        // 收到的结果
        SoapObject soapObject = (SoapObject) envelope.bodyIn;

        // 成功失败标志位
        String result = soapObject.getProperty(0).toString();

        // JSON格式数据
        resultMessage = soapObject.getPropertySafely("SaveCarPictureTagKeyResult", "").toString();

        // 成功
        if(result.equals("0")) {
            // JSON格式数据
            Log.d(Common.TAG, resultMessage);
            errorMessage = "";
            return true;
        }
        // 失败
        else {
            Log.d(Common.TAG, resultMessage);
            errorMessage = resultMessage;
            return false;
        }
    }

    /**
     * 上传空照片
     * @param jsonString 图片信息
     * @return 是否成功
     */
    public boolean uploadPicture(String jsonString) {
        errorMessage = "";
        resultMessage = "";

        byte[] byteArray = new byte[6];
        byte[] newByteArray;

        for(int i = 0; i < byteArray.length; i++) {
            byteArray[i] = 'F';
        }

        // 在图片流后面加上分隔符 #:
        jsonString = "#:" + jsonString;

        // 将图片流复制到新的byte数组中
        int length = byteArray.length;

        newByteArray = new byte[length + jsonString.getBytes().length];
        System.arraycopy(byteArray, 0, newByteArray, 0, length);

        for(int i = 0; i < jsonString.getBytes().length; i++) {
            newByteArray[length + i] = jsonString.getBytes()[i];
        }

        // 各种配置
        SoapObject request = new SoapObject(Common.NAMESPACE, this.methodName);
        request.addProperty("stream", newByteArray);

        SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
        new MarshalBase64().register(envelope);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE trans = new HttpTransportSE(this.url);

        try {
            trans.call(this.soapAction, envelope);
        } catch (Exception e) {
            if(e.getMessage() != null)
                Log.d(Common.TAG, "无法连接到服务器：" + e.getMessage());
            else
                Log.d(Common.TAG, "无法连接到服务器！");

            errorMessage = "无法连接到服务器！";

            return false;
        }

        // 收到的结果
        SoapObject soapObject = (SoapObject) envelope.bodyIn;

        // 成功失败标志位
        String result = soapObject.getProperty(0).toString();

        // JSON格式数据
        resultMessage = soapObject.getPropertySafely("SaveCarPictureTagKeyResult", "").toString();

        // 成功
        if(result.equals("0")) {
            // JSON格式数据
            errorMessage = "";
            return true;
        }
        // 失败
        else {
            Log.d(Common.TAG, resultMessage);
            errorMessage = resultMessage;
            return false;
        }
    }
}
