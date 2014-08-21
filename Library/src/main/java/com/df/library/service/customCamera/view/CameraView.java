package com.df.library.service.customCamera.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.df.library.R;
import com.df.library.service.customCamera.BitmapUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 摄像头View
 * 
 * @author 谭军华 创建于2014年4月3日 下午6:36:09
 */
@SuppressLint({ "InlinedApi", "NewApi", "ResourceAsColor" })
public class CameraView extends RelativeLayout implements
		SurfaceHolder.Callback {

	// public static final int EXPOSURE_COMPENSATION_2=2; //ExposureCompensation
	// public static final int EXPOSURE_COMPENSATION_1=1; //ExposureCompensation
	// public static final int EXPOSURE_COMPENSATION_0=0; //ExposureCompensation
	// public static final int EXPOSURE_COMPENSATION_MINUS1=1;
	// //ExposureCompensation minus -1
	// public static final int EXPOSURE_COMPENSATION_MINUS2=2;
	// //ExposureCompensation -2

	/*
	 * 1.对焦时抖动 2.对焦失败 3.对焦成功 4.拍摄中抖动 5.拍摄成功
	 */
	public static final int MSG_SET_FOUCS_AREA = 0x01; // ExposureCompensation
														// -2
	public static final int MSG_TAKE_PICTURE = 0x02; // ExposureCompensation -2
	public static final int MSG_TACK_PICTURE_RESULT_FOUCSSHAKE = 0x03; //
	public static final int MSG_TACK_PICTURE_RESULT_FOUCSFAIL = 0x04; //
	public static final int MSG_TACK_PICTURE_RESULT_FOUCSSUCCESS = 0x05; //
	public static final int MSG_TACK_PICTURE_RESULT_PHOTOGRAPHSHAKE = 0x06; // Photograph
	public static final int MSG_TACK_PICTURE_RESULT_PHOTOGRAPHSUCCESS = 0x07; // Photograph
	public static final int MSG_TACK_PICTURE_RESULT_PHOTOGRAPHFAIL = 0x08; // Photograph
	public static final int MSG_SET_EXPOSURE_RATE = 0x09; // ExposureCompensation
															// 1,0.5,0,-0.5,1
	public static final int MSG_SWITCH_CAMERA = 0x0A; //
	public static final int MSG_SWTICH_CAMERA_RESULT_SCUCCESS = 0x0B; //
	public static final int MSG_SWTICH_CAMERA_RESULT_ERROR = 0x0C; //
	public static final int MSG_VIEW_SIZE_CHANGE = 0x0D; //
	public static final int MSG_CAMERA_STARTPREVIEW = 0x0E; //startpreview
	public static final int MSG_CAMERA_STOPPREVIEW = 0x0F; //stopreview

	/**
	 * The facing of the camera is opposite to that of the screen.
	 */
	public static final int CAMERA_FACING_BACK = 0;

	/**
	 * The facing of the camera is the same as that of the screen.
	 */
	public static final int CAMERA_FACING_FRONT = 1;
	
	public static final float PICTURE_WIDTH=800;
	public static final float PICTURE_HEIGHT=600;
	public static final float PICTURE_SIZE_RATE=PICTURE_WIDTH/PICTURE_HEIGHT;

	private ImageView imgFocus;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;

	private Sensor sensor;
	private Camera mCamera;
	private SensorManager sm;
	private SoundPool soundPool;
	private CHandler mCHandler;
	// private FocusHelper mFocusHelper;
	public CameraParamUtil cameraParamUtil;
	private MySensorEventListener mySensorEventListener;

	private boolean mPreviewRunning;
	private int targetWidth = 1024;
	private static int cameraId = CAMERA_FACING_BACK;

	public CameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public CameraView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub

		init();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mCamera != null) {
			mCamera.release();
		}
	}
	
	/**
	 * 恢复到前置摄像头
	 */
	public static void resetCameraId(){
		cameraId=CAMERA_FACING_BACK;
	}

	private void init() {
		// 控件
		initViews();

		cameraParamUtil = new CameraParamUtil(this.getContext());
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		// mFocusHelper = new FocusHelper(this,imgFocus);

		// sound
		soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
		soundPool.load(this.getContext(), R.raw.camera_shutter1, 1);

		// focus
		// mHandler.postDelayed(new Runnable(){
		//
		// @Override
		// public void run() {
		// mFocusHelper.focusingInCenter();
		// }
		//
		// }, 1500);
	}

	private void initViews() {
		mSurfaceView = new SurfaceView(getContext());
		this.addView(mSurfaceView, new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT));
		imgFocus = new ImageView(getContext());

		int s = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                150, getResources().getDisplayMetrics());
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(s, s);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		imgFocus.setBackgroundColor(android.R.color.background_light);
		this.addView(imgFocus, lp);
		// imgFocus.setVisibility(View.GONE);

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}
		try {

			// mCamera.setPreviewCallback(mPreviewCallback);
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 设置默认参数
		setDefaultCameraParams();
		mCamera.startPreview();
		mPreviewRunning = true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open(cameraId);

//		mCamera.setPreviewCallback(new PreviewCallback() {
//
//			@Override
//			public void onPreviewFrame(byte[] data, Camera camera) {
//				if(takePicture){
//					Size size=camera1.getParameters().getPreviewSize();
//					int rgb[]=new int[size.width*size.height];
//					decodeYUV(rgb, data, size.width, size.height); //改编码。。。
//					Bitmap bitmap= Bitmap.createBitmap(rgb, size.width, size.height, Bitmap.Config.ARGB_8888);
//					mCHandler.obtainMessage(
//							MSG_TACK_PICTURE_RESULT_PHOTOGRAPHSUCCESS, bitmap)
//							.sendToTarget();
//
//
//				}
//				takePicture=false;
//			}
//		});

		mySensorEventListener = new MySensorEventListener();
		registerSensorListener();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mPreviewRunning = false;
		mCamera.setPreviewCallback(null);
		mCamera.release();
		mCamera = null;
		sm.unregisterListener(mySensorEventListener, sensor); //
	}

	/**
	 * 设置默认参数
	 */
	private void setDefaultCameraParams() {
		if (mCamera == null) {
			return;
		}

		Parameters params = mCamera.getParameters();
		boolean supported = params.getSupportedFocusModes().contains(
				Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		if (supported) {
			params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		}
		params.setPictureFormat(ImageFormat.JPEG);// 设置图片格式

		List<Size> list = params.getSupportedPictureSizes();

		int degree=cameraParamUtil.setCameraDisplayOrientation((Activity)this.getContext(), cameraId, mCamera);
		params.setRotation(degree);
		degree=degree%360;

//		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
//		android.hardware.Camera.getCameraInfo(cameraId, info);
		params.setPreviewSize(800, 600);
		mCamera.setParameters(params);

		float pw=params.getPreviewSize().width;
		float ph=params.getPreviewSize().height;
		int w=getMeasuredWidth(),h=getMeasuredHeight();


		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		Point size=new Point();
		wm.getDefaultDisplay().getSize(size);
		int width = size.x;//屏幕宽度
		int height =size.y;


		w=width;
		h=height;
		float rate=pw/ph;
		if(degree==0 || degree==180){
//			w=(int) (pw/ph*h);
			w=(int) (rate*h);
//			if(w>width){
//				rate=width/(1.0f*w);
//				w=(int) (w*rate);
//				h=(int) (h*rate);
//			}

		}else{
//			h=(int) (pw/ph*w);
			h=(int) (rate*w);
//			if(h>height){
//				rate=height/(1.0f*h);
//				w=(int) (w*rate);
//				h=(int) (h*rate);
//			}

		}

		RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(w, h);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		setLayoutParams(lp);

		mCHandler.obtainMessage(MSG_VIEW_SIZE_CHANGE, lp).sendToTarget();

		mSurfaceHolder.setSizeFromLayout();

//		mSurfaceView.getHolder().setFixedSize(w, h);

	}



	public int getTargetWidth() {
		return targetWidth;
	}

	public void setTargetWidth(int targetWidth) {
		this.targetWidth = targetWidth;
		cameraParamUtil.setPreviewAndPictureSize(targetWidth);
	}

	/**
	 * 注册重力加速度监听器
	 */
	private void registerSensorListener() {
		sm = (SensorManager) this.getContext().getSystemService(
				Context.SENSOR_SERVICE);
		// 选取加速度感应器
		mySensorEventListener = new MySensorEventListener();
		sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sm.registerListener(mySensorEventListener, sensor,
				SensorManager.SENSOR_DELAY_UI);

	}

	/**
	 * 切换摄像头
	 *
	 * @param cameraId
	 */
	private boolean switchCamera(int cameraId) {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mPreviewRunning = false;
			mCamera.release();
			mCamera = null;
		}

		mCamera = Camera.open(cameraId);
		CameraView.cameraId = cameraId;
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
			setDefaultCameraParams();
			mCamera.startPreview();
			mPreviewRunning = true;
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private void startPreview(){
		if(mCamera!=null){
			mCamera.startPreview();
		}
	}

	private void stopPreview(){
		if(mCamera!=null){
			mCamera.stopPreview();
		}
	}

	/**
	 * 拍照
	 */
	private void tackPicture() {
		time = System.currentTimeMillis();
		try {
			// focusHelper.focusingInCenter();
			mySensorEventListener.startCheckShake();
			mCamera.autoFocus(mAutoFocusCallBack);
		} catch (Exception e) {
			mySensorEventListener.stopCheckShake();
		}

	}

	/**
	 * 1.对焦时抖动 2.对焦失败 3.对焦成功 4.拍摄中抖动 5.拍摄成功
	 */
	long time = 0;
	boolean takePicture=false;
	private AutoFocusCallback mAutoFocusCallBack = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {

			boolean isShake = mySensorEventListener.checkIsShake();

			mySensorEventListener.stopCheckShake(); // 停止检测
			if (isShake) {
				Log.d("", "对焦时，抖动了，拍照失败！");
				mCHandler.sendEmptyMessage(MSG_TACK_PICTURE_RESULT_FOUCSSHAKE);
				return;
			}

			mCamera.enableShutterSound(false);
			mySensorEventListener.startCheckShake();

//			mSurfaceView.setDrawingCacheEnabled(true);
//			mSurfaceView.buildDrawingCache();
//			Bitmap bitmap=mSurfaceView.getDrawingCache();

//			mCHandler.obtainMessage(
//					MSG_TACK_PICTURE_RESULT_PHOTOGRAPHSUCCESS, bitmap)
//					.sendToTarget();

//			takePicture=true;
			mCamera.takePicture(null, null, mPictureCallback);

			// if (success) {
			// Log.v("AutoFocusCallback", "AutoFocusCallback" + success);
			// // focusHelper.focusSuccess();
			// mCamera.enableShutterSound(false);
			// mySensorEventListener.startCheckShake();
			// mCamera.takePicture(null, null, mPictureCallback);
			// Log.d("", "对焦成功");
			// mCHandler.sendEmptyMessage(MSG_TACK_PICTURE_RESULT_FOUCSSUCCESS);
			// } else {
			// Log.d("", "对焦失败");
			// mCHandler.sendEmptyMessage(MSG_TACK_PICTURE_RESULT_FOUCSFAIL);
			// // focusHelper.hiddenFocus();
			//
			// }
			// // focusHelper.focusingInCenter();

		}
	};

	/**
	 * 拍照的回调接口
	 */
	PictureCallback mPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			boolean isShake = mySensorEventListener.checkIsShake();
			mySensorEventListener.stopCheckShake(); // 停止检测

			if (isShake) {
				Log.d("Focus", "拍摄中抖动了，拍摄照片失败。。。");
				Toast.makeText(CameraView.this.getContext(),
                        "拍摄中抖动了，拍摄照片失败。。。", Toast.LENGTH_SHORT).show();
				mCamera.startPreview();

				mCHandler
						.sendEmptyMessage(MSG_TACK_PICTURE_RESULT_PHOTOGRAPHSHAKE);
				return;
			}
			soundPool.play(1, 1, 1, 0, 0, 1); // 播放声音

			Log.d("PictureCallback",
                    "…onPictureTaken…" + (System.currentTimeMillis() - time)
                            + "ms"
            );

			if (data != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
                        data.length, options);

				bitmap = BitmapUtil.scaleAndCrop(bitmap, PICTURE_WIDTH, PICTURE_HEIGHT);

//                int width = bitmap.getWidth();
//                int height = bitmap.getHeight();
//
//                float ratio;
//                float newWidth;
//                float newHeight;
//
//                // 如果宽度小于800, 无视
//                if(width > 800) {
//                    ratio = (float)width / (float)800;
//                    newWidth = 800;
//                    newHeight = height / ratio;
//                } else if(height > 800) {
//                    ratio = (float)height / (float)800;
//                    newWidth = width / ratio;
//                    newHeight = 800;
//                } else {
//                    newWidth = width;
//                    newHeight = height;
//                }
//
//                bitmap = Bitmap.createScaledBitmap(bitmap, (int)newWidth, (int)newHeight, true);

				mCHandler.obtainMessage(
						MSG_TACK_PICTURE_RESULT_PHOTOGRAPHSUCCESS, bitmap)
						.sendToTarget();
				Log.d("Focus", "拍摄照片成功。。。");
			} else {
				mCHandler
						.sendEmptyMessage(MSG_TACK_PICTURE_RESULT_PHOTOGRAPHFAIL);
			}

			// mCamera.startPreview();

		}
	};

	/**
	 * 对焦帮助
	 *
	 * @author 谭军华 创建于2014年4月2日 下午5:06:06
	 */
	public class FocusHelper {

		private ImageView imgFocus;
		private RelativeLayout rlParent;
		private Handler handler = new Handler();
		private DisappearCallback disappearCallback = new DisappearCallback();

		Animation scaleAnim;

		public FocusHelper(RelativeLayout parent, ImageView img) {
			this.rlParent = parent;
			this.imgFocus = img;
			scaleAnim = AnimationUtils.loadAnimation(parent.getContext(),
                    R.anim.focus_scale);
		}

		public void focusingInCenter() {
			int w = rlParent.getWidth();
			int h = rlParent.getHeight();
			float x = w / 2;
			float y = h / 2;
			focusing(x, y);

		}

		public void focusing(float x, float y) {

			Toast.makeText(rlParent.getContext(), "x:" + x + ",y:" + y,
                    Toast.LENGTH_SHORT).show();
			imgFocus.clearAnimation();
			handler.removeCallbacks(disappearCallback); // 停止消失操作

			imgFocus.setVisibility(View.VISIBLE);
			imgFocus.setImageResource(R.drawable.focus_off);

			// int l=(int) (x-imgFocus.getWidth()/2);
			// int t=(int) (y-imgFocus.getHeight()/2);

			// RelativeLayout.LayoutParams lp=(LayoutParams)
			// imgFocus.getLayoutParams();
			// lp.setMargins(l, t, 0, 0);
			// imgFocus.setLayoutParams(lp);

			// imgFocus.layout(l, t, l+imgFocus.getWidth(),
			// t+imgFocus.getHeight());
			imgFocus.invalidate();
			imgFocus.startAnimation(scaleAnim);

		}

		public void focusSuccess() {
			imgFocus.clearAnimation();
			imgFocus.setVisibility(View.VISIBLE);
			imgFocus.setImageResource(R.drawable.focus_on);
			handler.postDelayed(disappearCallback, 1500);
		}

		public void hiddenFocus() {
			imgFocus.setVisibility(View.GONE);
		}

		class DisappearCallback implements Runnable {

			@Override
			public void run() {
				hiddenFocus();
			}

		}

	}

	/**
	 * 消息处理
	 *
	 * @author 谭军华 创建于2014年4月4日 上午10:00:37
	 */
	public static class CHandler extends Handler {

		CameraView view;

		public CHandler(CameraView view) {
			this.view = view;
			this.view.mCHandler = this;
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_TAKE_PICTURE:
				view.tackPicture();
				break;
			case MSG_SET_EXPOSURE_RATE:
				// view.cameraParamUtil.showExposureCompensationPopuwindow();
				Float rate = (Float) msg.obj;
				view.cameraParamUtil.setExposureRate(rate);
				break;
			case MSG_SWITCH_CAMERA:
				boolean result = view.switchCamera(msg.arg1);
				if (result) {
					sendEmptyMessage(MSG_SWTICH_CAMERA_RESULT_SCUCCESS);
				} else {
					sendEmptyMessage(MSG_SWTICH_CAMERA_RESULT_ERROR);
				}
				break;
			case MSG_CAMERA_STARTPREVIEW:
				view.startPreview();
				break;
			case MSG_CAMERA_STOPPREVIEW:
				view.stopPreview();
				break;
			}
		}
	}

	/*
decodeYUV(argb8888, data, camSize.width, camSize.height);
Bitmap bitmap = Bitmap.createBitmap(argb8888, camSize.width,
                    camSize.height, Config.ARGB_8888);
	 */

	// decode Y, U, and V values on the YUV 420 buffer described as YCbCr_422_SP by Android
	// David Manpearl 081201
	public void decodeYUV(int[] out, byte[] fg, int width, int height)
	        throws NullPointerException, IllegalArgumentException {
	    int sz = width * height;
	    if (out == null)
	        throw new NullPointerException("buffer out is null");
	    if (out.length < sz)
	        throw new IllegalArgumentException("buffer out size " + out.length
	                + " < minimum " + sz);
	    if (fg == null)
	        throw new NullPointerException("buffer 'fg' is null");
	    if (fg.length < sz)
	        throw new IllegalArgumentException("buffer fg size " + fg.length
	                + " < minimum " + sz * 3 / 2);
	    int i, j;
	    int Y, Cr = 0, Cb = 0;
	    for (j = 0; j < height; j++) {
	        int pixPtr = j * width;
	        final int jDiv2 = j >> 1;
	        for (i = 0; i < width; i++) {
	            Y = fg[pixPtr];
	            if (Y < 0)
	                Y += 255;
	            if ((i & 0x1) != 1) {
	                final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
	                Cb = fg[cOff];
	                if (Cb < 0)
	                    Cb += 127;
	                else
	                    Cb -= 128;
	                Cr = fg[cOff + 1];
	                if (Cr < 0)
	                    Cr += 127;
	                else
	                    Cr -= 128;
	            }
	            int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
	            if (R < 0)
	                R = 0;
	            else if (R > 255)
	                R = 255;
	            int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1)
	                    + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
	            if (G < 0)
	                G = 0;
	            else if (G > 255)
	                G = 255;
	            int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
	            if (B < 0)
	                B = 0;
	            else if (B > 255)
	                B = 255;
	            out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
	        }
	    }

	}

	/**
	 * 设置参数工具
	 *
	 * @author 谭军华 创建于2014年4月4日 上午9:53:19
	 */
	@SuppressLint("NewApi")
	class CameraParamUtil {
		Context mContext;

		public CameraParamUtil(Context context) {
			this.mContext = context;
		}

		/**
		 * 设置显示方向
		 */
		 public int  setCameraDisplayOrientation (Activity activity,
		          int cameraId , android.hardware.Camera camera) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);

            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0 ;

            switch ( rotation ) {
                case Surface.ROTATION_0: degrees = 0; break ;
                case Surface.ROTATION_90 : degrees = 90; break ;
                case Surface.ROTATION_180 : degrees = 180; break ;
                case Surface.ROTATION_270 : degrees = 270; break ;
            }

            int result ;

            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360 ;
                result = (360 - result) % 360 ;   // compensate the mirror
            } else {   // back-facing
                result = (info.orientation - degrees + 360) % 360 ;
            }

            camera.setDisplayOrientation(result);

            if( info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && (degrees == 0 || degrees == 180)){
                 result = result + 180;
            }

            result = result % 360;

            return result;
        }


		/**
		 * 得到参数
		 *
		 * @return
		 */
		public Camera.Parameters getCameraParameters() {
			if (mCamera != null) {
				return mCamera.getParameters();
			}
			return null;
		}

		/**
		 * 得到曝光值 -2,-1,0,1,2相对应的值
		 *
		 * @return
		 */
		public int[] getExposureCompensationValues() {
			if (mCamera.getParameters().isAutoExposureLockSupported()) {
				int max = mCamera.getParameters().getMaxExposureCompensation();
				int middle = max / 2;
				return new int[] { max, middle, 0, -middle, -max };
			} else {
				return null;
			}
		}

		/**
		 * 得到当前的曝光下标
		 *
		 * @return
		 */
		public int getCurrExposureCompensationIndex() {
			int val = mCamera.getParameters().getExposureCompensation();
			int vals[] = getExposureCompensationValues();
			int index = -1;
			for (int i = 0; i < vals.length; i++) {
				if (i + 1 >= vals.length) {
					if (vals[i] == val) {
						index = i;
						break;
					}
				} else {
					if (val <= vals[i] && val > vals[i + 1]) {
						index = i;
						break;
					}
				}

			}
			return index;
		}

		/**
		 * 设置曝光度比率 1,0.5,0,-0.5,-1
		 *
		 * @param rate
		 */
		public void setExposureRate(float rate) {
			if (mCamera != null
					&& mCamera.getParameters().isAutoExposureLockSupported()) {
				int max = mCamera.getParameters().getMaxExposureCompensation();
				int val = (int) (max * rate);
				setExposureCompensation(val);
			}
		}

		/**
		 * 设置曝光度
		 *
		 * @param val
		 */
		public void setExposureCompensation(int val) {
			if (mCamera != null
					&& mCamera.getParameters().isAutoExposureLockSupported()) {
				Camera.Parameters params = mCamera.getParameters();
				params.setExposureCompensation(val);
				params.setAutoExposureLock(false);
				mCamera.setParameters(params);
			}
		}

		/**
		 * 设置尺寸
		 *
		 * @param targetWidth
		 */
		public void setPreviewAndPictureSize(int targetWidth) {
			if (mCamera == null) {
				return;
			}
			// 1.查找合适的尺寸
			List<Size> list = mCamera.getParameters()
					.getSupportedPictureSizes();
			List<Size> tempList = new ArrayList<Size>();
			for (Camera.Size size : list) {

				// float rate=(size.height*1.0f)/size.width;
				// if(rate!=0.75){
				// continue;
				// }
				tempList.add(size);
				if (tempList.size() > 2) {
					tempList.remove(0);
				}
				if (size.width <= targetWidth) {
					break;
				}
			}
			Camera.Size currSize = tempList.get(0);

			// 2.设置
			Camera.Parameters p = mCamera.getParameters();
			p.setPreviewSize(currSize.width, currSize.height);
			p.setPictureSize(currSize.width, currSize.height);

			mCamera.setParameters(p);
		}
	}

	/**
	 * 重力加速检测对焦与拍照的有效性
	 * 
	 * @author 谭军华 创建于2014年4月4日 上午10:27:53
	 */
	class MySensorEventListener implements SensorEventListener {
		float mLastX, mLastY, mLastZ;
		boolean beginCheckShake;

		int count;
		boolean mInitialized;
		float deltaSums[] = new float[3];
		float badCounts[] = new float[3];

		@Override
		public void onSensorChanged(SensorEvent event) {

			if (!mPreviewRunning) {
				return;
			}

			if (!beginCheckShake) {
				return;
			}

			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			if (!mInitialized) {
				mLastX = x;
				mLastY = y;
				mLastZ = z;
				mInitialized = true;
			}
			float deltaX = Math.abs(mLastX - x);
			float deltaY = Math.abs(mLastY - y);
			float deltaZ = Math.abs(mLastZ - z);

			// 开始
			count++;
			badCounts[0] += getBadCount(deltaX);
			badCounts[1] += getBadCount(deltaY);
			badCounts[2] += getBadCount(deltaZ);

			deltaSums[0] += deltaX;
			deltaSums[1] += deltaY;
			deltaSums[2] += deltaZ;

			mLastX = x;
			mLastY = y;
			mLastZ = z;
		}

		private int getBadCount(float d) {
			if (d > 0.1f && d < 1) {
				return 1;
			}
			if (d > 1) {
				return 2;
			}
			return 0;

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}

		public void stopCheckShake() {
			synchronized (this) {
				this.beginCheckShake = false;
				reCount();
			}
		}

		public void startCheckShake() {
			synchronized (this) {
				this.beginCheckShake = true;
			}
		}

		public void reCount() {
			deltaSums[0] = 0;
			deltaSums[1] = 0;
			deltaSums[2] = 0;

			badCounts[0] = 0;
			badCounts[1] = 0;
			badCounts[2] = 0;
			count = 0;
		}

		public void printAVG() {
			if (count == 0) {
				Log.d("AVG", "Count is zero");
				return;
			}
			Log.d("AVG", deltaSums[0] / count + "," + deltaSums[1] / count
                    + "," + deltaSums[2] / count + "\t count:" + count);

		}

		/**
		 * 检测是否抖动了
		 * 
		 * @return
		 */
		public boolean checkIsShake() {
			synchronized (this) {
				printAVG(); // 打印值
				if (count == 0) {
					return false;
				}
				// float xAvg=deltaSums[0]/count;
				// float yAvg=deltaSums[1]/count;
				// float zAvg=deltaSums[2]/count;
				//
				// final float val=0.3f;
				// final float zVal=0.3f;
				// if(xAvg>val || yAvg>val || zAvg>zVal){
				// return true;
				// }

				float xRate = badCounts[0] / count;
				float yRate = badCounts[1] / count;
				float zRate = badCounts[2] / count;

				reCount(); // 清零
				// if(xRate>.55f || yRate>.55f || zRate>0.65f){
				if (xRate > .9f || yRate > .9f || zRate > 0.9f) {
					return true;
				}

				// if((xRate+yRate+zRate)>=.9){
				// return true;
				// }
				//

			}
			return false;
		}
	}

}
