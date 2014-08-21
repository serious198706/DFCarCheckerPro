package com.ads.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import android.os.Environment;

public class FileUtil
{
	/**
	 * 在SDCard中创建path的目录
	 * @param path			相对于SDCard的路径,如/ABC/XXX/
	 * @return 实际路径 	创建成功
	 * @return null		创建失败或没有sdcard
	 */
	public static String createSDCardDir(String path)
	{
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{	
			String sdPath = Environment.getExternalStorageDirectory().toString()+path;
			File dir=new File(sdPath);
			if(!dir.exists())
			{	
				if(dir.mkdirs())
				{
					return sdPath;
				}
			}else
			{
				return sdPath;
			}
		}
		return null;
	}
	
	/**
	 * 遍历path目录找到最新修改的文件
	 * @param path
	 * @return
	 */
	public static String lastModifiedFile(String path)
	{
		String ret = null;
		File dir = new File(path);
		File[] allFiles = dir.listFiles();
		if(allFiles.length>0)
		{
			long mtime=0;
			int index=0;
			for(int i=0;i<allFiles.length;i++)
			{
				File file = allFiles[i];
				if(mtime==0)
				{
					mtime = file.lastModified();
					index = 0;
				}else
				{
					if(file.lastModified()>mtime)
					{
						mtime = file.lastModified();
						index = i;
					}
				}
			}
			ret = allFiles[index].getName();
		}
		return ret;
	}
	
	/**
	 * 保存Content到filepath
	 * @param filepath
	 * @param content
	 */
	public static boolean saveSDCardFile(String filepath, String content)
	{
		try
		{
			FileOutputStream fout = new FileOutputStream(filepath);
			fout.write(content.getBytes());
			fout.close();
			return true;
		}catch(Exception e)
		{	e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 读取filepath的内容
	 * @param filepath
	 */
	public static String loadSDCardFile(String filepath)
	{
		try
		{
			FileInputStream fin = new FileInputStream(filepath);
			int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            fin.close();
			return new String(buffer);
		}catch(Exception e)
		{	e.printStackTrace();
			return null;
		}
	}
}
