package com.df.library.service.customCamera;

import com.df.library.util.Common;

/**
 * 照片任务
 * @author 谭军华
 * 创建于2014年4月16日 上午10:21:51
 */
public class PhotoTask implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int STATE_WAIT=0;
	public static int STATE_OBTAINPHOTO=1; //已经拍照或从图库中提取了一张图片
	public static int STATE_COMPLETE=2; //完成
	
	public static int TYPE_ADD=0; //新增
	public static int TYPE_EDIT=1; //编辑
	
	private int state=STATE_WAIT;
	private int type;
	private int guideResId; //辅助线图
	private String title;
	private long fileName;
	private String path;
	private int extra; //自定义数据

    /**
     * 拍照模式
     * @param guideResId
     * @param title
     * @param fileName
     * @param extra
     */
	public PhotoTask(int guideResId, String path, String title, long fileName, int extra){
		this.guideResId=guideResId;
		this.title=title;
		this.extra = extra;
        this.path = path;
        this.fileName = fileName;
		this.type=TYPE_ADD;
	}

    /**
     * 修改模式
     * @param guidResId
     * @param title
     * @param fileName
     * @param path
     * @param extra
     */
	public PhotoTask(int guidResId, String title, long fileName, String path, int extra){
		this.guideResId=guidResId;
		this.fileName=fileName;
		this.path=path;
		this.extra=extra;
		this.title=title;
		this.type=TYPE_EDIT;
	}

	public boolean hasGuide(){ return guideResId > 0;}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getGuideResId() {
		return guideResId;
	}
	public void setGuideResId(int guideResId) {
		this.guideResId = guideResId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public long getFileName() {
		return fileName;
	}
	public void setFileName(long fileName) {
		this.fileName = fileName;
	}
	public int getExtra() {
		return extra;
	}
	public void setExtra(int extra) {
		this.extra = extra;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}
