package com.ads.model;

import java.util.ArrayList;

import android.util.Log;

public class MenuItem
{
	private static final String tag = MenuItem.class.getSimpleName();

	public int lastVisitPostion = 0;

	public String title;
	public String pic;
	public String link;
	public String param;

	public MenuItem parent;

	public int level;
	protected ArrayList<MenuItem> mItems = null;

	public MenuItem()
	{

	}

	public MenuItem(int l, MenuItem p, String info)
	{
		level = l;
		parent = p;

		try
		{
			String titleinfo = info;
			if (info.contains("link="))
			{
				String linkparam[] = info.split("link=|param=");
				titleinfo = linkparam[0].trim();
				link = linkparam[1].trim();
				if (info.contains("param="))
				{
					param = linkparam[2].trim();
				}
			}

			if (level == 0)
			{
				if (titleinfo.contains("(") && titleinfo.contains(""))
				{
					String tmp[] = titleinfo.split("\\(");
					title = tmp[0];
					pic = tmp[1].split("\\)")[0].toLowerCase();
				} else
				{
					title = titleinfo.split("=")[0];
				}
			} else
			{
				title = titleinfo;
			}
		} catch (Exception e)
		{
			Log.e(tag, "Error when processing menu item information: " + info);
			title = pic = "ConfigFileError";
		}
	}

	public void add(MenuItem e)
	{
		if (mItems == null)
		{
			mItems = new ArrayList<MenuItem>();
		}
		mItems.add(e);
	}

	public MenuItem getItem(int index)
	{
		if (mItems == null || mItems.size() <= index)
		{
			return null;
		}

		return mItems.get(index);
	}

	public void trim()
	{
		if (mItems != null)
		{
			mItems.trimToSize();
		}
	}

	public int size()
	{
		if (mItems == null)
		{
			return 0;
		}

		return mItems.size();
	}
}
