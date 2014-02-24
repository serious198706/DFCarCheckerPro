package com.df.app.entries;

/**
 * Created by 岩 on 13-12-24.
 *
 * 问题查勘
 */
public class Issue {
    // 问题id
    private int id;

    // 问题描述
    private String desc;

    // 视角
    private String view;

    // 总结
    private String summary;

    // 严重等级
    private String serious;

    // 是否选中
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
