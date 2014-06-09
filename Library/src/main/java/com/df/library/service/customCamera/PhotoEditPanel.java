package com.df.library.service.customCamera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.df.library.service.customCamera.edit.BitmapSprite;
import com.df.library.service.customCamera.edit.BlurSprite;
import com.df.library.service.customCamera.edit.Sprite;

import java.util.ArrayList;
import java.util.List;

/**
 * 照片编辑面板
 * 
 * @author 谭军华 创建于2014年4月18日 下午4:40:44
 */
public class PhotoEditPanel extends View {

	public static final int MODE_BLUR = 0;
	public static final int MODE_ERASER = 1;
	public static final int MODE_ZOOM = 2;

	public static final int DRAG_DISTANCE = 50; // distance

	int mode = 0;
	int location[]=new int[2];
	int photoWidth, photoHeight;
	int blurSize = 50, eraserSize = 50;
	int dragX, dragY, lastDragX, lastDragY;
	float offsetX, offsetY,scaleRate = 1;
	Matrix matrix = new Matrix();

	Point centerPoint;
	Paint eraserPaint;
	Paint paint = new Paint();

	Bitmap photoBmp,paintBmp;
	Sprite blurSprite;
	Sprite photoSprite;
	List<Sprite> tempList = new ArrayList<Sprite>();
//	private Paint blurPaint;

	public PhotoEditPanel(Context context) {
		super(context);
		init();
	}

	public PhotoEditPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PhotoEditPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void setPhoto(Bitmap bmp) {
		this.photoBmp = bmp;
		this.photoWidth = bmp.getWidth();
		this.photoHeight = bmp.getHeight();

		
//		this.blurBmp=BitmapUtil.fastblur(photoBmp, 60, 0, 0, photoWidth, photoHeight);
		
		paintBmp= Bitmap.createBitmap(photoWidth, photoHeight, Bitmap.Config.ARGB_8888);
		this.getLocationInWindow(location);
		
		photoSprite = new BitmapSprite(bmp, 0, 0);
		tempList.clear();
		blurSprite=null;
		float h = getMeasuredHeight();
		float rate = h / bmp.getHeight();
		zoomTo(rate);

		paint.setColor(Color.RED);
		
		invalidate();
		

	}

	private void init() {
		eraserPaint = new Paint();
		eraserPaint.setColor(Color.TRANSPARENT);
		eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

	}

	public void zoomIn() {

	}

	public void zoomOut() {

	}

	private void zoomTo(float rate) {
		int pw = getMeasuredWidth();
		int ph = getMeasuredHeight();
		int w = photoBmp.getWidth();
		int h = photoBmp.getHeight();

//		if(centerPoint!=null){
//			if(centerPoint.x>0){
//				w=centerPoint.x;
//			}
//			if(centerPoint.y>0){
//				h=centerPoint.y;
//			}
//		}

		// 居中偏移量
		offsetX = (pw - w*rate) / 2;
		offsetY = (ph - h*rate) / 2+location[1];

		this.scaleRate = rate;
		refreshMatrix();

		invalidate();

	}
	


	private void refreshMatrix() {
		matrix.reset();
		matrix.postScale(scaleRate, scaleRate);
		matrix.postTranslate(offsetX, offsetY);
	}

	private void dragCanvas(int dragX, int dragY) {
		offsetX = offsetX + dragX;
		offsetY = offsetY + dragY;
		refreshMatrix();
		invalidate();
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	@SuppressLint("WrongCall")
	@Override
	protected void onDraw(Canvas canvas) {

		canvas.setMatrix(matrix);
		if (photoSprite != null) {
			photoSprite.onDraw(canvas);
		}
		
		
		for(Sprite sprite:tempList){
			sprite.onDraw(canvas);
		}
		
		if(blurSprite!=null){			
			blurSprite.onDraw(canvas);
		}
		
		// canvas.drawLine(0, photoHeight-5, 600, 10, paint);
		// canvas.drawLine(0, this.getMeasuredHeight()-2, 800, 10, paint);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getRawX();
		int y = (int) event.getRawY();
		// int action = event.getAction();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			handleAction(x, y, event);
			break;
		case MotionEvent.ACTION_MOVE:
			handleAction(x, y, event);
			break;
		case MotionEvent.ACTION_UP:
			handleAction(x, y, event);
			break;
		}

		return true;
	}

	/**
	 * 获取编辑后的图片
	 * 
	 * @return
	 */
	public Bitmap getEditPhotoResult() {
		Bitmap result = Bitmap.createBitmap(photoWidth, photoHeight,
                Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		canvas.drawBitmap(photoBmp, 0, 0, null);
		if (blurSprite != null) {
			canvas.drawBitmap(blurSprite.getBmp(), 0, 0, null);
		}
		return result;
	}

	public void handleAction(int rawX, int rawY, MotionEvent event) {

		int action = event.getAction();
		int x = rawX;
		int y = rawY;
		x = (int) (x - offsetX);
		y = (int) (y - offsetY);

		x = (int) (x / scaleRate);
		y = (int) (y / scaleRate);

		if (mode == MODE_BLUR) {
			blur(x, y, action);
		} else if (mode == MODE_ERASER) {
			eraser(x, y, action);
		} else if (mode == MODE_ZOOM) {
			drag(rawX, rawY, action, event);
		}
	}

	/**
	 * 检测是否在范围里
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean checkInRange(int x, int y) {
		if (x < 0 || y < 0 || x >= photoWidth || y >= photoHeight) {
			return false;
		}
		return true;
	}

	float baseValue = 0;

	public void drag(int x, int y, int action, MotionEvent event) {
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			lastDragX = x;
			lastDragY = y;
			
			centerPoint=null;
			break;
		case MotionEvent.ACTION_MOVE:

			int count = event.getPointerCount();
			if (count == 2) {
				float x0 = event.getX(0) - event.getX(1);
				float y0 = event.getY(0) - event.getY(1);
				float value = (float) Math.sqrt(x0 * x0 + y0 * y0);// 计算两点的距离
				if (baseValue == 0) {
					baseValue = value;
					return;
				} else {
					if (value - baseValue >= 5 || value - baseValue <= -5) {
						float scale = value / baseValue;// 当前两点间的距离除以手指落下时两点间的距离就是需要缩放的比例。
						scale = scale * 0.05f;
						if (value - baseValue < 0) {
							scale = 0 - scale;
						}

						float tempScale = scale + scaleRate;
						if (tempScale < 1 || tempScale > 3) {
							return;
						}
						if(centerPoint==null){
							PointF p=mid(event);
							centerPoint = new Point((int)p.x,(int)p.y);
						}
						zoomTo(tempScale);
						baseValue = value;
						// img_scale(scale); //缩放图片
					}
				}
			} else if (count == 1) {
				dragX = x - lastDragX;
				dragY = y - lastDragY;
				if (Math.abs(dragX) > 150 || Math.abs(dragY) > 150) {
					return;
				}

				dragCanvas(dragX, dragY);
				lastDragX = x;
				lastDragY = y;
			}

			invalidate(); //刷新一下
			break;
		case MotionEvent.ACTION_UP:
			baseValue=0;
			break;
		}

	}

	/** 计算两个手指间的中间点 */
	private PointF mid(MotionEvent event) {
		float midX = (event.getX(1) + event.getX(0)) / 2;
		float midY = (event.getY(1) + event.getY(0)) / 2;
		return new PointF(midX, midY);
	}

	public void eraser(int x, int y, int action) {
		if (blurSprite == null) {
			return;
		}
		
		int size=(int) (eraserSize/scaleRate);
		x=x-size/2;
		y=y-size/2;
		
		boolean isOk = checkInRange(x, y);
		if (!isOk) {
			return;
		}
		if (action == MotionEvent.ACTION_DOWN
				|| action == MotionEvent.ACTION_MOVE) {
			Canvas canvas = new Canvas(blurSprite.getBmp());
			canvas.drawRect(x, y, x + size, y + size, eraserPaint);
		}
		invalidate();
	}

	public void blur(int x, int y, int action) {
		int size=(int) (blurSize/scaleRate);
		if (action == MotionEvent.ACTION_UP) {
			if (tempList.isEmpty()) {
				return;
			}
			Bitmap bg = Bitmap.createBitmap(photoBmp.getWidth(),
                    photoBmp.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bg);
			for (Sprite sprite : tempList) {
				canvas.drawBitmap(sprite.getBmp(), sprite.getX(),
						sprite.getY(), null);
			}

			if (blurSprite != null) {
				canvas = new Canvas(blurSprite.getBmp());
				canvas.drawBitmap(bg, 0, 0, null);
			} else {
				blurSprite = new BlurSprite(bg, 0, 0);
			}
			tempList.clear();
		}

		// 检测范围
		x=x-size/2;
		y=y-size/2;
		boolean isOk = checkInRange(x, y);
		if (!isOk) {
			return;
		}

		if (action == MotionEvent.ACTION_MOVE) {
			Bitmap bmp = BitmapUtil.fastblurPart(photoBmp, 15, x, y,
					Math.min(size, photoWidth - x),
					Math.min(size, photoHeight - y));
			tempList.add(new BlurSprite(bmp, x, y));
		}

		if (action == MotionEvent.ACTION_DOWN) {
			tempList.clear();
		}

		invalidate();
	}

}
