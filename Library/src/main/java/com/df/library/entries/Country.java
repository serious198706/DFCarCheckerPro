package com.df.library.entries;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by 岩 on 13-10-16.
 *
 * 国家
 */

public class Country {
    public String name;
    public String id;
    public List<Brand> brands;

    public Country() {
        brands = new ArrayList<Brand>();
    }

    /**
     * 获取此国家下的所有品牌名称
     * @return
     */
    public List<String> getBrandNames() {
        List<String> brandNames = new ArrayList<String>();
        brandNames.add("");
        for(int i = 0; i < brands.size(); i++) {
            brandNames.add(brands.get(i).name);
        }

        return brandNames;
    }

    /**
     * 根据id查找品牌
     * @param id
     * @return
     */
    public Brand getBrandById(String id) {
        Brand brand = null;

        for(int i = 0; i < brands.size(); i++) {
            if(brands.get(i).id.equals(id)) {
                brand = brands.get(i);
            }
        }

        return brand;
    }
}
