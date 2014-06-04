package com.df.app.service.customCamera.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;


import com.df.app.R;
import com.df.app.service.customCamera.PhotoProcessManager;
import com.df.app.service.customCamera.PhotoTask;
import com.df.app.service.customCamera.view.CameraView;

import java.util.ArrayList;

/**
 * 拍照
 * @author 谭军华
 * 创建于2014年4月15日 下午4:31:02
 */
@SuppressLint("NewApi")
public class PhotographActivity extends Activity implements OnClickListener {

	//
	public static final String EXTRA_DSTPATH="dst_path";
	public static final String EXTRA_PHOTOTASKLIST="phototask_list";
	
	//拍照参数相关------------------------------------------
	public static final int COLOR_RED= Color.RED;
	public static final int COLOR_WHITE= Color.WHITE;
						//  Transparency
	public static final int TRANSPARENCY_25=0x40;
	public static final int TRANSPARENCY_50=0x80;
	public static final int TRANSPARENCY_75=0xBF;  //Opacity Transparency
	
	public static final float EXPOSURE_RATES[]={0.8f,0.4f,0,-0.4f,-0.8f};
	public static final int COLORS[]={COLOR_WHITE,COLOR_RED};
	public static final int TRANSPARENCYS[]={TRANSPARENCY_25,TRANSPARENCY_50,TRANSPARENCY_75};	
	
	//消息相关-------------------------------------------
	public static final int MSG_SET_EXPOSURE=0x101;
	
	boolean clickable=true;
	PhotoTask currTask;
	CameraView.CHandler cHandler;
	Parameter parameter=new Parameter();
	PhotoProcessManager photoProcessManager=PhotoProcessManager.getInstance();
	
//	ListView lvExposure;
	
	SeekBar sbAlpha;
	TextView txtTitle;
	CameraView cameraView;
	LinearLayout llMenu,llExposure,llGuide;//ll_guide
	Button btnWhiteGuide,btnRedGuide;
	ImageView imgPreview,imgGuideMask;
	View imgCamera,imgGuide,imgCameraSwicher,imgExposure,imgPhotoLib,imgBack,vStandardLine;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//CameraView.resetCameraId(); //恢复id?
		setContentView(R.layout.activity_photograph);
		
		boolean isOk=initData();
		if(isOk){
			initViews();
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		cHandler.sendEmptyMessage(CameraView.MSG_CAMERA_STARTPREVIEW);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		cHandler.sendEmptyMessage(CameraView.MSG_CAMERA_STOPPREVIEW);
	}
	
	/**
	 * 初始化数据
	 */
	@SuppressWarnings("unchecked")
	private boolean initData(){
		boolean initOk=true;
		//1.数据相关
		//表示有数据
		if(getIntent().getExtras() !=null && getIntent().getExtras().containsKey(EXTRA_DSTPATH)){
			String dstPath=getIntent().getStringExtra(EXTRA_DSTPATH);
			ArrayList<PhotoTask> list=(ArrayList<PhotoTask>) getIntent().getSerializableExtra(EXTRA_PHOTOTASKLIST);
			if(dstPath==null || list==null){
				initOk=false;
			}else{				
				photoProcessManager.initPhotoData(dstPath, list);
			}
		}

		//2.设置当前数据
		currTask=photoProcessManager.getCurrentPhotographTask();
		if(currTask==null){
			initOk=false;
		}
		
		//3.错误处理
		if(!initOk){
			Toast.makeText(this, "参数不正确！", Toast.LENGTH_SHORT).show();
			finish();
		}
		
		return initOk;
		
	}
		
	/**
	 * 初始化控件相关
	 */
	private void initViews(){
		llExposure=(LinearLayout) findViewById(R.id.ll_exposure);
		llGuide=(LinearLayout) findViewById(R.id.ll_guide);
		cameraView=(CameraView) findViewById(R.id.camera);
		imgCamera=findViewById(R.id.img_camera);
		imgGuide=findViewById(R.id.img_guide);
		imgCameraSwicher=findViewById(R.id.img_camera_switcher);
		imgExposure=findViewById(R.id.img_exposure);
		imgPhotoLib=findViewById(R.id.img_photo_lib);
		imgBack=findViewById(R.id.img_back);
		imgPreview=(ImageView) findViewById(R.id.img_preview);
		imgGuideMask=(ImageView) findViewById(R.id.img_guide_mask);
		btnWhiteGuide=(Button) findViewById(R.id.btn_white_guide);
		btnRedGuide=(Button) findViewById(R.id.btn_red_guide);
		sbAlpha=(SeekBar) findViewById(R.id.sb_alpha);
		txtTitle=(TextView) findViewById(R.id.txt_title);
		llMenu=(LinearLayout) findViewById(R.id.ll_memu);
		vStandardLine=findViewById(R.id.v_standard_line);
		
		imgCamera.setOnClickListener(this);
		imgGuide.setOnClickListener(this);
		imgCameraSwicher.setOnClickListener(this);
		imgExposure.setOnClickListener(this);
		imgPhotoLib.setOnClickListener(this);
		imgBack.setOnClickListener(this);
		btnWhiteGuide.setOnClickListener(this);
		btnRedGuide.setOnClickListener(this);
		
		cameraView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hiddenAllSubMenu();
			}
		});
		
		
		//alpha值变化
		sbAlpha.setMax(2);
		sbAlpha.setProgress(1);
		sbAlpha.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				parameter.setGuideTransparencyIndex(progress);
				updateUI();
				sbAlpha.setEnabled(false);
				cHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
//						hiddenAllSubMenu(); //隐藏
						sbAlpha.setEnabled(true);
					}
				}, 500);
			}
		});
		

		updateUI();
		updateGuideMask();
		
		cHandler=new CameraView.CHandler(cameraView){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what){
				case CameraView.MSG_TACK_PICTURE_RESULT_FOUCSSHAKE:
					Toast.makeText(PhotographActivity.this, "对焦失败，对焦时抖动了！", Toast.LENGTH_SHORT).show();
					clickable=true;
					break;
				case CameraView.MSG_TACK_PICTURE_RESULT_FOUCSFAIL:
					Toast.makeText(PhotographActivity.this, "对焦失败！", Toast.LENGTH_SHORT).show();
					break;
				case CameraView.MSG_TACK_PICTURE_RESULT_PHOTOGRAPHSHAKE:
					Toast.makeText(PhotographActivity.this, "拍照失败，拍照时抖动了！", Toast.LENGTH_SHORT).show();
					clickable=true;
					break;
				case CameraView.MSG_TACK_PICTURE_RESULT_PHOTOGRAPHFAIL:
					Toast.makeText(PhotographActivity.this, "拍照失败！", Toast.LENGTH_SHORT).show();
					clickable=true;
					break;
				case CameraView.MSG_TACK_PICTURE_RESULT_PHOTOGRAPHSUCCESS:
					savePhoto((Bitmap) msg.obj); //保存图片
					clickable=true;
					break;
				case CameraView.MSG_SWTICH_CAMERA_RESULT_SCUCCESS:
					parameter.switchCamera(); //切换摄像头
					clickable=true;
					break;
				case CameraView.MSG_SWTICH_CAMERA_RESULT_ERROR:
					Toast.makeText(PhotographActivity.this, "切换摄像头失败！", Toast.LENGTH_SHORT).show();
					clickable=true;
					break;
				case CameraView.MSG_VIEW_SIZE_CHANGE:
					imgGuideMask.setLayoutParams((LayoutParams) msg.obj);
					break;
				}
				
			}
		};
		
		//曝光度处理
		setCurrentExposure(3);
		int count=llExposure.getChildCount();
		for(int i=1;i<count;i++){
			final int index=i;
			TextView tv=(TextView) llExposure.getChildAt(i);
			tv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					setCurrentExposure(index);
					//设置曝光度的Rate
					cHandler.obtainMessage(CameraView.MSG_SET_EXPOSURE_RATE, parameter.getExposureRate()).sendToTarget();
					//隐藏post
					cHandler.postDelayed(new Runnable() {
						
						@Override
						public void run() {
//							hiddenAllSubMenu();
						}
					}, 500);
				}
			});
			
		}
		
		//设置范围参考线
		if(vStandardLine!=null){
			Point point=new Point();
			getWindowManager().getDefaultDisplay().getSize(point);
			int w=point.x;
			int h=(int) (w/CameraView.PICTURE_SIZE_RATE);
			RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(w, h);
			lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
			vStandardLine.setLayoutParams(lp);
		}
		
	}
	
	/**
	 * 设置当前的曝光值
	 * @param index
	 */
	private void setCurrentExposure(int index){
		parameter.setExposureIndex(index-1);
		int count=llExposure.getChildCount();
		for(int i=1;i<count;i++){
			TextView tv=(TextView) llExposure.getChildAt(i);
			if(i==index){
				tv.setBackgroundResource(R.drawable.block_blue);
			}else{
				tv.setBackgroundResource(R.drawable.block_black);
			}
		}
	}
	
	/**
	 * 显示页面数据
	 */
	private void updateUI(){
		//更新辅助
		imgGuideMask.setImageAlpha(parameter.getGuideTransparency());
		if(currTask.getGuideResId()<=0){
			imgGuide.setClickable(false);
			imgGuide.setEnabled(false);
			imgGuideMask.setVisibility(View.GONE);
			
		}else{
			imgGuideMask.setVisibility(View.VISIBLE);
		}
		
		txtTitle.setText(currTask.getTitle());
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		photoProcessManager.noticeFinish(); //通知结果
        CameraView.resetCameraId();
	}
	
	@Override
	public void onClick(View v) {
		//1.隐藏所有子菜单
		if(!v.equals(imgExposure) && !v.equals(imgGuide)){
			hiddenAllSubMenu();
		}

		if(!clickable){
			return;
		}
		
		if(v.equals(imgCamera)){
			clickable=false;
			cHandler.sendEmptyMessage(CameraView.MSG_TAKE_PICTURE); //快照
		}else if(v.equals(imgGuide)){
			llExposure.setVisibility(View.GONE);
			toggleVisible(llGuide);//.setVisibility(View.VISIBLE);
		}else if(v.equals(imgCameraSwicher)){
			clickable=false;
			cHandler.obtainMessage(CameraView.MSG_SWITCH_CAMERA, parameter.getRequestCameraId(), 0).sendToTarget();
		}else if(v.equals(imgExposure)){
			llGuide.setVisibility(View.GONE);
			toggleVisible(llExposure);//.setVisibility(View.VISIBLE);
		}else if(v.equals(imgPhotoLib)){
//			pickPicture(); //从图片库里选取照片
			startActivity(new Intent(this,PickPictureActivity.class));
			finish();
		}else if(v.equals(imgBack)){
			onBackPressed();
		}else if(v.equals(btnWhiteGuide)){
			parameter.setGuideColorIndex(0);
			updateGuideMask();
		}else if(v.equals(btnRedGuide)){
			parameter.setGuideColorIndex(1);
			updateGuideMask();
		}
		
	}  
	

	/**
	 * 显示隐藏控件
	 * @param v
	 */
	private void toggleVisible(View v){
		if(v.getVisibility()== View.GONE){
			v.setVisibility(View.VISIBLE);
		}else{
			v.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 隐藏所有的子菜单
	 */
	private void hiddenAllSubMenu(){
        // 如果菜单已经隐藏，则点击屏幕开始拍照
        if(findViewById(R.id.ll_exposure).getVisibility() == View.GONE && clickable) {
            clickable=false;
            cHandler.sendEmptyMessage(CameraView.MSG_TAKE_PICTURE); //快照
        } else {
            findViewById(R.id.ll_exposure).setVisibility(View.GONE);
            findViewById(R.id.ll_guide).setVisibility(View.GONE);
        }
	}
	
//	/**
//	 * 设置不可用
//	 * @param group
//	 * @param enabled
//	 */
//	private void setEnable(ViewGroup group,boolean enabled){
//		for(int i=0;i<group.getChildCount();i++){
//			View v=group.getChildAt(i);
//			v.setEnabled(enabled);
//			v.setClickable(enabled);
//			if(v instanceof ViewGroup){
//				setEnable((ViewGroup) v, enabled);
//			}
//		}
//	}
	
	/**
	 * 更新辅助线图片
	 */
	private void updateGuideMask(){
		if(currTask.getGuideResId()<=0){
			return;
		}
		Bitmap bmp=parameter.getGuideBitmap(currTask.getGuideResId());
		Drawable drawable = new BitmapDrawable(getResources(),bmp);
		drawable.setFilterBitmap(true);
		
		imgGuideMask.setImageDrawable(drawable);
		imgGuideMask.setImageAlpha(parameter.getGuideTransparency());
	}
	
	/**
	 * 保存图片
	 * @param bmp
	 */
	private void savePhoto(Bitmap bmp){
		//1.保存图片
		boolean isOk=photoProcessManager.submitPhotograph(currTask, bmp);
		if(!isOk){
			Toast.makeText(this, "保存照片失败,重新开始！", Toast.LENGTH_SHORT).show();
			Intent intent=new Intent(this,PhotographActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		//2.改变照片任务的状态
		Intent intent=new Intent(this,PhotoEditActivity.class);
		intent.putExtra(PhotoEditActivity.EXTRA_FROM, PhotoEditActivity.FROM_PHOTOGRAPH);
		startActivity(intent);
        CameraView.resetCameraId();
		finish();
		
	}
	
	
	
	/**
	 * 参数设置
	 * @author 谭军华
	 * 创建于2014年4月15日 下午2:17:11
	 */	
	 class Parameter{
		private float exposureRate=0;  //1,0.5,0,-0.5,-1     eg:Max=12 12,6,0,-6,-12
		
		private int currCameraId=CameraView.CAMERA_FACING_BACK;
		private int exposureIndex=2; //+2,1,0,-1,-2
		private int guideColorIndex=0; //辅助颜色下标  white,red
		private int guideOpacityIndex=1; //透明度当前下标 75% 50% 25% OPACITY
		
		/**
		 * 得到辅助线图
		 * @param resId
		 * @return
		 */
		public Bitmap getGuideBitmap(int resId){
			BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(resId);
			Bitmap bmp=bd.getBitmap();
			bmp=bmp.copy(Bitmap.Config.ARGB_8888, true);
			
			int guideColor=COLORS[guideColorIndex];
//			int alpha=TRANSPARENCYS[guideTransparencyIndex];
			int r= Color.red(guideColor);
			int g= Color.green(guideColor);
			int b= Color.blue(guideColor);
			
			int newColor= Color.rgb(r, g, b);
			for(int i=0;i<bmp.getHeight();i++){
				for(int j=0;j<bmp.getWidth();j++){
					int color = bmp.getPixel(j, i);
					if(Color.alpha(color)==0){ //透明的部分不处理
						continue;
					}
					bmp.setPixel(j, i, newColor);
				}
			}
			
			return bmp;
		}
		
		public int getExposureIndex() {
			return exposureIndex;
		}
		public void setExposureIndex(int exposureIndex) {
			this.exposureIndex = exposureIndex;
			this.exposureRate=EXPOSURE_RATES[exposureIndex];
		}
		public int getGuideColorIndex() {
			return guideColorIndex;
		}
		public void setGuideColorIndex(int guideColorIndex) {
			this.guideColorIndex = guideColorIndex;
		}

		public float getExposureRate() {
			return exposureRate;
		}

		public int getGuideTransparencyIndex() {
			return guideOpacityIndex;
		}
		public int getGuideTransparency() {
			return TRANSPARENCYS[guideOpacityIndex];
		}

		public void setGuideTransparencyIndex(int guideOpacityIndex) {
			this.guideOpacityIndex = guideOpacityIndex;
		}

		public int getCurrCameraId() {
			return currCameraId;
		}

		public void switchCamera(){
			if(currCameraId==CameraView.CAMERA_FACING_BACK){
				currCameraId=CameraView.CAMERA_FACING_FRONT;
			}else{
				currCameraId=CameraView.CAMERA_FACING_BACK;
			}
		}
		
		/**
		 * 请求的cameraId
		 * @return
		 */
		public int getRequestCameraId(){
			if(currCameraId==CameraView.CAMERA_FACING_BACK){
				return CameraView.CAMERA_FACING_FRONT;
			}else{
				return CameraView.CAMERA_FACING_BACK;
			}
		}
		
		
		
	}
	
	


}
