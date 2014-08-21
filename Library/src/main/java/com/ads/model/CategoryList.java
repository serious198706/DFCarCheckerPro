package com.ads.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.SAXException;

import android.util.Log;

import com.df.library.util.MyApplication;


@SuppressWarnings("deprecation")
public class CategoryList extends MenuItem
{
	private static final String tag = CategoryList.class.getSimpleName();
	private static String mLanguage;
	static SAXHandler mHandler = null;
	static CategoryList mInstance = null;

	private CategoryList()
	{
		mHandler = new SAXHandler();
	}

	public static synchronized CategoryList create(String language)
			throws ParserConfigurationException, SAXException, IOException
	{
		if (mInstance != null && mLanguage.equals(language))
		{
			return mInstance;
		}

		mLanguage = language;
		mInstance = new CategoryList(); // Create the instance first, we need
										// the SAX parser handler.

		String categoryXML = null;
		if (language.equals("zh"))
		{
			categoryXML = MyApplication.CN_CATEGORY_XML;
		} else
		{
			categoryXML = MyApplication.EN_CATEGORY_XML;
		}
		InputStream is = MyApplication.getAssetFileInputSream(categoryXML);
		SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		saxParser.parse(is, mHandler);
		mInstance.level = -2;
		return mInstance;
	}

	class SAXHandler extends HandlerBase
	{
		private CategoryMenu mCurrentProcessingCategory = null;

		/**
		 * EXAMPLE XML file 1 <?xml version="1.0" encoding="utf-8"?> 2 <Root> 3
		 * <Child name="bob" age="12"> 4 <Phone>12345678</Phone> 5 </Child> 6
		 * </Root>
		 */

		/**
		 * Callback when parse texts of an elements, like line 4, the text is
		 * "12345678"
		 */
		@Override
		public void characters(char[] ch, int start, int length)
		{
		}

		/**
		 * Callback when the parsing of whole document is finished
		 */
		@Override
		public void endDocument()
		{
			Log.d(tag, String.format("End document"));
			CategoryList.this.trim();
		}

		/**
		 * Callback when the parsing of an element is finished like line4,5,6
		 */
		@Override
		public void endElement(String name)
		{
			if ("category".equalsIgnoreCase(name))
			{ // end of a category
				CategoryList.this.add(mCurrentProcessingCategory);
			}
		}

		/**
		 * Callback when the parsing of a document is started
		 */
		@Override
		public void startDocument()
		{
			Log.d(tag, String.format("Start document"));

		}

		/**
		 * Callback when the parsing of an element is start like line2,3,4
		 */
		@Override
		public void startElement(String name, AttributeList atts)
		{
			if ("categorylist".equalsIgnoreCase(name))
			{ // root element
				CategoryList.this.mItems = new ArrayList<MenuItem>();
			} else if ("category".equalsIgnoreCase(name))
			{ // iterate every category information
				String title = null;
				String pic = null;
				String menufile = null;

				for (int i = 0; i < atts.getLength(); i++)
				{
					String attname = atts.getName(i);
					String value = atts.getValue(i);
					if ("name".equalsIgnoreCase(attname))
					{
						title = value;
						Log.d("---", "Start category:" + value);
					} else if ("pic".equalsIgnoreCase(attname))
					{
						pic = value;
					} else if ("menu".equalsIgnoreCase(attname))
					{
						menufile = value;
					}

				}

				try
				{
					Log.d(tag, "Create menu from file: " + menufile);
					mCurrentProcessingCategory = CategoryMenu
							.createFromFile(MyApplication
									.getAssetFileInputSream(menufile));
					mCurrentProcessingCategory.title = title;
					mCurrentProcessingCategory.pic = pic;
					mCurrentProcessingCategory.parent = CategoryList.this;
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

	}
}
