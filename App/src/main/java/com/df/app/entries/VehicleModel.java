package com.df.app.entries;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 13-10-15.
 *
 * 解析xml文件后的实体类
 */

public class VehicleModel {
    // 国家列表
    public List<Country> countries;
    public String version;

    public VehicleModel() {
        countries = new ArrayList<Country>();
    }

    public VehicleModel getVehicleModelInstance() {
        return new VehicleModel();
    }

    public List<Country> getCountries() {
        return countries;
    }

    /**
     * 获取所有国家名称
     * @return
     */
    public List<String> getCountryNames() {
        List<String> countryNames = new ArrayList<String>();

        countryNames.add("");

        for(int i = 0; i < countries.size(); i++) {
            countryNames.add(countries.get(i).name);
        }

        return countryNames;
    }

    /**
     * 根据id查找国家
     * @param id
     * @return
     */
    public Country getCountryById(String id) {
        Country country = null;

        for(int i = 0; i < countries.size(); i++) {
            if(countries.get(i).id.equals(id)) {
                country = countries.get(i);
            }
        }

        return country;
    }
}


