package com.df.library.service.customCamera.edit;

import android.annotation.SuppressLint;
import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;

/**
 * 层
 * @author 谭军华
 * 创建于2014年4月18日 上午10:26:50
 */
public class Layer extends Shap{


	List<Sprite> spriteList=new ArrayList<Sprite>();
	
	public Layer(){
	
	}
	
	
	@SuppressLint("WrongCall")
	@Override
	public void onDraw(Canvas canvas){
		synchronized (this) {
			for(Sprite sprite:spriteList){
				sprite.onDraw(canvas);
			}
		}
	}
}
