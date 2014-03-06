package com.df.app.entries;

/**
 * Created by 岩 on 14-3-5.
 */
public class IssuePhoto {
    private int index;
    private String fileName;
    private String desc;

    public IssuePhoto(int index, String fileName, String desc) {
        this.index = index;
        this.fileName = fileName;
        this.desc = desc;
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
}
