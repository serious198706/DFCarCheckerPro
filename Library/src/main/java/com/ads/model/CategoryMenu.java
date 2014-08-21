package com.ads.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;

public class CategoryMenu extends MenuItem
{
	private static final String tag = CategoryMenu.class.getSimpleName();

	private CategoryMenu()
	{

	}

	public static synchronized CategoryMenu createFromFile(InputStream in)
			throws Exception
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		CategoryMenu menu = new CategoryMenu();
		menu.level = -1; // This is the menu config file.

		MenuItem rootItem = menu;
		MenuItem currentContainer = rootItem;
		MenuItem lastAddedItem = null;

		int lineNumber = 0;

		while (br.ready())
		{
			String s = br.readLine();
			if(s==null || s=="")
			{	continue;
			}
			lineNumber++;

			int l = getNumberOfTabsAhead(s); // Get the tab numbers first

			s = s.trim(); // Remove the white spaces

			if (s.startsWith("//") || s.length() == 0)
			{
				continue; // Ignore the commented out lines
			}

			int n = l - currentContainer.level;
			if (n <= 2)
			{
				if (n == 1)
				{ // Moving among the level of our direct child items
					lastAddedItem = new MenuItem(l, currentContainer, s);
				} else if (n == 2)
				{ // Advance to the next level, child of the child
					currentContainer = lastAddedItem;
					lastAddedItem = new MenuItem(l, currentContainer, s);
				} else
				{ // Backtrack to some parent level
					while (n <= 0)
					{
						currentContainer.trim(); // Trim to save memory usage
						currentContainer = currentContainer.parent;
						n++;
					}
					lastAddedItem = new MenuItem(l, currentContainer, s);
				}
				currentContainer.add(lastAddedItem);
			} else
			{
				throw new Exception(String.format("Config file error, line:%d",
						lineNumber));
			}
		}
		Log.d(tag, String.format("%d lines read", lineNumber));
		return menu;
	}

	/**
	 * Get the count of tab in front of a string, white space is ignored
	 * 
	 * @param s
	 *            : input string
	 * @return the count of tab in front of the string
	 */
	public static int getNumberOfTabsAhead(String s)
	{
		byte[] bytes = s.getBytes();
		int n = 0;
		for (byte c : bytes)
		{
			if (c == '\t')
			{
				n++;
			} else if (c == ' ')
			{
				continue;
			} else
			{
				break;
			}
		}
		return n;
	}
}
