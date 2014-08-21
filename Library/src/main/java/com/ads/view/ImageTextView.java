package com.ads.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ads.utils.DeviceUtil;

public class ImageTextView extends LinearLayout
{
	Context mContext;
	LinearLayout imageWrapper;
	ImageView image;
	LinearLayout textWrapper;
	TextView text;

	public ImageTextView(Context context)
	{
		super(context);
		mContext = context;

		image = new ImageView(mContext);
		image.setPadding(8, 8, 8, 8);
		image.setAdjustViewBounds(true);
		image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

		imageWrapper = new LinearLayout(mContext);
		imageWrapper.setGravity(Gravity.CENTER);
		imageWrapper.addView(image);

		text = new TextView(mContext);
		text.setPadding(8, 8, 8, 8);

		textWrapper = new LinearLayout(mContext);
		textWrapper.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		textWrapper.setGravity(Gravity.CENTER);
		textWrapper.addView(text);

		this.addView(imageWrapper);
		this.addView(textWrapper);

		// default
		setHorizontal();
		setSmallSize();
	}

	public void setHorizontal()
	{
		this.setOrientation(LinearLayout.HORIZONTAL);
		imageWrapper.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
	}

	public void setVertical()
	{
		this.setOrientation(LinearLayout.VERTICAL);
		imageWrapper.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
	}

	public void setImageAndText(int imageResource, String textString)
	{
		image.setImageResource(imageResource);
		text.setText(textString);
	}

	public void setImageAndText(Drawable d, String textString)
	{
		image.setImageDrawable(d);
		text.setText(textString);
	}

	public void setTextGravity(int gravity)
	{
		textWrapper.setGravity(gravity);
	}

	public void setImageSize(int w, int h)
	{
		image.setMaxWidth(w);
		image.setMinimumWidth(w);
		image.setMaxHeight(h);
		image.setMinimumHeight(h);
	}

	public void setTextSize(int size)
	{
		text.setTextSize(size);
	}

	public void setSmallSize()
	{
		if (DeviceUtil.getScreenHeight(mContext) > 1000)
		{
			image.setMaxHeight(128);
			image.setMaxWidth(128);
			text.setTextSize(24);
		} else
		{
			image.setMaxHeight(96);
			image.setMaxWidth(96);
			text.setTextSize(24);
		}
	}

	public void setLargeSize()
	{
		if (DeviceUtil.getScreenHeight(mContext) > 1000)
		{
			image.setMaxHeight(196);
			image.setMaxWidth(196);
			text.setTextSize(36);
		} else
		{
			image.setMaxHeight(168);
			image.setMaxWidth(168);
			text.setTextSize(30);
		}
	}
}
