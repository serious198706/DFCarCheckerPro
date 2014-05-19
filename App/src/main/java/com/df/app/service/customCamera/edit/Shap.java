package com.df.app.service.customCamera.edit;

import android.graphics.Canvas;

/**
 * 形状
 * @author 谭军华
 * 创建于2014年4月18日 上午10:19:31
 */
public abstract class Shap {
	protected int x;
	protected int y;
	protected int parentX;
	protected int parentY;
//	protected int width;
//	protected int height;
	
	public abstract void onDraw(Canvas canvas);

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
}
