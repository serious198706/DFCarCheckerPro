package com.df.app.entries;

/**
 * Created by 岩 on 14-3-5.
 */
public class ListedPhoto {
    private int index;
    private PhotoEntity photoEntity;
    private int type;

    public ListedPhoto(int index, PhotoEntity photoEntity) {
        this.index = index;
        this.photoEntity = photoEntity;
    }

    public ListedPhoto(int index, PhotoEntity photoEntity, int type) {
        this.index = index;
        this.photoEntity = photoEntity;
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public PhotoEntity getPhotoEntity() {
        return photoEntity;
    }

    public void setPhotoEntity(PhotoEntity photoEntity) {
        this.photoEntity = photoEntity;
    }
}
