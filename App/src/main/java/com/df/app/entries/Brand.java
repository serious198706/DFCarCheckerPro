package com.df.app.entries;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-12-23.
 *
 * 品牌
 */
public class Brand {
    public String name;
    public String id;
    public List<Manufacturer> manufacturers;
    private List<String> manufacturerNames;

    public Brand() {
        manufacturers = new ArrayList<Manufacturer>();
    }

    /**
     * 获取此品牌下所有厂商的名称
     * @return
     */
    public List<String> getManufacturerNames() {
        manufacturerNames = new ArrayList<String>();
        manufacturerNames.add("");
        for(int i = 0; i < manufacturers.size(); i++) {
            manufacturerNames.add(manufacturers.get(i).name);
        }

        return manufacturerNames;
    }

    /**
     * 根据厂商id查找厂商
     * @param id
     * @return
     */
    public Manufacturer getManufacturerById(String id) {
        Manufacturer manufacturer = null;

        for(int i = 0; i < manufacturers.size(); i++) {
            if(manufacturers.get(i).id.equals(id)) {
                manufacturer = manufacturers.get(i);
            }
        }

        return manufacturer;
    }
}

