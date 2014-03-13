package com.df.app.entries;

/**
 * Created by 岩 on 14-3-5.
 */
public class ListedPhoto {
    private int index;
    private String fileName;
    private String desc;
    private int type;

    public ListedPhoto(int index, String fileName, String desc) {
        this.index = index;
        this.fileName = fileName;
        this.desc = desc;
    }

    public ListedPhoto(int index, String fileName, String desc, int type) {
        this.index = index;
        this.fileName = fileName;
        this.desc = desc;
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
