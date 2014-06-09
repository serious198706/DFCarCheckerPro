package com.df.library.entries;

/**
 * Created by 岩 on 13-10-16.
 *
 * 车型
 */

public class Model {
    public String name;
    public String id;

    public String getName() {
        return name;
    }

    /**
     * 根据id查找车型
     * @param id
     * @return
     */
    public String getNameById(String id) {
        String name = "";

        if(this.id.equals(id)) {
            name = this.name;
        }

        return name;
    }
}
