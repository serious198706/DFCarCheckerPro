package com.ads.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

/**
 * 设备信息工具类
 * .屏幕信息(分辨率,密度)
 */
public class DeviceUtil
{
	private static DisplayMetrics displayMetrics = null;
	
	/**
	 * 获取屏幕信息
	 */
	public static DisplayMetrics getDisplayMetrics(Context context)
	{
		Activity activity = (Activity)context;
		displayMetrics = new DisplayMetrics();   
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
	}
	/**
	 * 获取屏幕密度
	 * @return
	 */
	public static float getScreenDensity(Context context)
	{
		if(displayMetrics==null)
		{	displayMetrics = getDisplayMetrics(context);
		}
		return displayMetrics.density;
	}
	/**
	 * 获取屏幕纵向分辨率
	 */
	public static int getScreenHeight(Context context)
	{
		if(displayMetrics==null)
		{	displayMetrics = getDisplayMetrics(context);
		}
		return displayMetrics.heightPixels;
	}
	/**
	 * 获取屏幕横向分辨率
	 */
	public static int getScreenWidth(Context context)
	{
		if(displayMetrics==null)
		{	displayMetrics = getDisplayMetrics(context);
		}
		return displayMetrics.widthPixels;
	}
}
