package com.df.app.entries;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by 岩 on 14-1-8.
 */
public class CarsWaitingItem {
    int carId;
    String plateNumber;
    String carType;
    String exteriorColor;
    String status;
    String date;
    String countryId;
    String brandId;
    String manufacturerId;
    String seriesId;
    String modelId;
    JSONObject jsonObject;

//    "CarId": 2,
//            "Vin": "15844",
//            "EngineSerial": "发动机号",
//            "VehicleType": "行驶证车辆类型",
//            "Mileage": "表征里程",
//            "PlateNumber": "京A2548",
//            "LicenseModel": "行驶证品牌型号",
//            "ExteriorColor": "红色",
//            "RegDate": "2012-12-03",
//            "BuildDate": "2012-12-03",
//            "CreateDate": "2013-01-01"

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(String manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public String getExteriorColor() {
        return exteriorColor;
    }

    public void setExteriorColor(String exteriorColor) {
        this.exteriorColor = exteriorColor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
