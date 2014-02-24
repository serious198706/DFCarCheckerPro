package com.df.app.entries;

/**
 * Created by 岩 on 13-12-25.
 *
 * 照片实体类，拍摄完成的照片都以此种方式保存
 */

public class PhotoEntity {
    private String fileName;
    private String thumbFileName;
    private String jsonString;
    private String comment;
    private String name;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getThumbFileName() {
        return thumbFileName;
    }

    public void setThumbFileName(String thumbFileName) {
        this.thumbFileName = thumbFileName;
    }

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}