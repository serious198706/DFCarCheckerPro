package com.df.library.service.customCamera.edit;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public abstract class Sprite extends Shap {

	protected int width;
	protected int height;
//	protected int data[];
	protected Bitmap bmp;

//	public Sprite(int data[], int width, int height) {
//		this.data = data;
//		bmp = Bitmap.createBitmap(data, 0, 0, width, height,
//				Bitmap.Config.ARGB_8888);
//	}
	
	@Override
	public void onDraw(Canvas canvas) {
		if(bmp!=null && !bmp.isRecycled()){			
			canvas.drawBitmap(bmp, x+parentX, y+parentY, null);
		}
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

//	public int[] getData() {
//		return data;
//	}
//
//	public void setData(int[] data) {
//		this.data = data;
//		bmp = Bitmap.createBitmap(data, 0, 0, width, height,
//		Bitmap.Config.ARGB_8888);
//	}

	public Bitmap getBmp() {
		return bmp;
	}

	public void setBmp(Bitmap bmp) {
		this.bmp = bmp;
	}

}
