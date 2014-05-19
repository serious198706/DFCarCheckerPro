package com.df.app.service.customCamera.edit;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class BlurSprite extends Sprite{
	
	public BlurSprite(Bitmap bmp,int x,int y) {
		this.bmp=bmp;
		this.x=x;
		this.y=y;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

}
