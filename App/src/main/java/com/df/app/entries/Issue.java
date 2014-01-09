package com.df.app.entries;

/**
 * Created by 岩 on 13-12-24.
 */
public class Issue {
    private int id;
    private String desc;
    private String popup;
    private String view;

    public Issue() {
        super();
    }

    public Issue(int id, String desc, String popup, String view) {
        super();
        this.id = id;
        this.desc = desc;
        this.popup = popup;
        this.view = view;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPopup() {
        return popup;
    }

    public void setPopup(String popup) {
        this.popup = popup;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }
}
