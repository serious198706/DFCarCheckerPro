package com.df.app.entries;

/**
 * Created by 岩 on 14-1-13.
 */
public class Cooperator {
    private int id;
    private String name;

    public Cooperator(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
