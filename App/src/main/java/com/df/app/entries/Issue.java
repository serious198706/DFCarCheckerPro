package com.df.app.entries;

/**
 * Created by 岩 on 13-12-24.
 */
public class Issue {
    private int id;
    private String desc;
    private String view;
    private String summary;
    private String serious;
    private String select;

    public Issue() {
        super();
    }

    public Issue(int id, String desc, String view, String summary, String serious, String select) {
        super();
        this.id = id;
        this.desc = desc;
        this.view = view;
        this.summary = summary;
        this.serious = serious;
        this.select = select;
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

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSerious() {
        return serious;
    }

    public void setSerious(String serious) {
        this.serious = serious;
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }
}
