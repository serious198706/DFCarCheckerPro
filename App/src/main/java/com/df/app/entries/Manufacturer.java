package com.df.app.entries;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-10-16.
 *
 * 厂商
 */

public class Manufacturer {
    public String name;
    public String id;
    public List<Series> serieses;

    public Manufacturer() {
        serieses = new ArrayList<Series>();
    }

    /**
     * 获取此品牌下的所有厂商名称
     * @return
     */
    public List<String> getSerialNames() {
        List<String> serialNames = new ArrayList<String>();
        serialNames.add("");
        for(int i = 0; i < serieses.size(); i++) {
            serialNames.add(serieses.get(i).name);
        }

        return serialNames;
    }

    /**
     * 根据id查找厂商
     * @param id
     * @return
     */
    public Series getSeriesById(String id) {
        Series series = null;

        for(int i = 0; i < serieses.size(); i++) {
            if(serieses.get(i).id.equals(id)) {
                series = serieses.get(i);
            }
        }

        return series;
    }
}
