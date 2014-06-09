package com.df.library.entries;

/**
 * Created by 岩 on 13-10-22.
 *
 * 检测员
 */
public class UserInfo
{
    private static UserInfo mInstance = null;

    public static UserInfo getInstance() {
        if(mInstance == null)
            mInstance = new UserInfo();

        return mInstance;
    }

    private String id;
    private String key;
    private String name;

    private UserInfo() { }

    public String getPlateType() {
        return plateType;
    }

    public void setPlateType(String plateType) {
        this.plateType = plateType;
    }

    private String plateType;

    public String getOrid() {
        return orid;
    }

    public void setOrid(String orid) {
        this.orid = orid;
    }

    private String orid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
