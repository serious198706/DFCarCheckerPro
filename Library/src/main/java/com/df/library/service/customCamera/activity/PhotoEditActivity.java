package com.df.library.service.customCamera.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.library.R;
import com.df.library.service.customCamera.PhotoEditPanel;
import com.df.library.service.customCamera.PhotoProcessManager;
import com.df.library.service.customCamera.PhotoTask;
import com.df.library.util.MyAlertDialog;

import java.util.ArrayList;


/**
 * 照片编辑
 * 
 * @author 谭军华 创建于2014年4月16日 下午3:12:45
 */
public class PhotoEditActivity extends Activity implements
        OnClickListener {

	public static final int FROM_PHOTOGRAPH=0;
	public static final int FROM_PHOTOLIB=1;
	
	public static final String EXTRA_FROM = "from";
	public static final String EXTRA_DSTPATH = "dst_path";
	public static final String EXTRA_PHOTOTASK = "phototask";
//	public static final String EXTRA_PHOTOTASKLIST = "phototask_list";



	int mode,from;
	Bitmap bmp = null;
	PhotoTask currTask;
	Handler mHandler=new Handler();
	EditHelper editHelper=new EditHelper();
	PhotoProcessManager photoProcessManager = PhotoProcessManager.getInstance();

//	ImageView imgPhoto;
	TextView txtTitle;
	PhotoEditPanel panel;
	LinearLayout llDirection;
	Button btnPencil, btnEraser, btnZoomin, btnPhotograph, btnDirection,
			btnBack, btnSave;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		boolean isOk = initData();
		if (isOk) {
			setContentView(R.layout.activity_photoedit);
			LinearLayout ll=(LinearLayout) findViewById(R.id.ll_panel);
			LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			panel=new PhotoEditPanel(this);
			ll.addView(panel, lp);
			initViews();
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
        showDiscardDialog(false);
		//photoProcessManager.noticeFinish();
	}
	
	/**
	 * 初始化数据
	 */
	private boolean initData() {

		boolean initOk = true;
		// 1.数据相关
		// 表示有数据
		mode = PhotoProcessManager.MODE_PHOTOGRAPH_EDIT;
		Intent intent = getIntent();

		if (intent.getExtras() != null
				&& intent.getExtras().containsKey(EXTRA_DSTPATH)) {
			String dstPath = getIntent().getStringExtra(EXTRA_DSTPATH);
			ArrayList<PhotoTask> list=new ArrayList<PhotoTask>();
			PhotoTask task=(PhotoTask) getIntent().getSerializableExtra(EXTRA_PHOTOTASK);
			list.add(task);
			if (dstPath == null || task == null) {
				initOk = false;
			}
			photoProcessManager.initModifyPhotoData(dstPath, list);
			mode = PhotoProcessManager.MODE_EDIT_ONLY;
		}
		
		from=intent.getIntExtra(EXTRA_FROM, -1);

		// 2.设置当前数据
		currTask = photoProcessManager.getCurrentModifyTask();
		if (currTask == null) {
			initOk = false;
		}

		// 3.错误处理
		if (!initOk) {
			Toast.makeText(this, "参数不正确！", Toast.LENGTH_SHORT).show();
			finish();
		}

		return initOk;
	}
	
	
	/**
	 * 初始化控件
	 */
	@SuppressLint("NewApi")
	private void initViews() {
		// 1.控件
		btnPencil = (Button) findViewById(R.id.btn_pencil);
		btnEraser = (Button) findViewById(R.id.btn_eraser);
		btnZoomin = (Button) findViewById(R.id.btn_zoomin);
		btnPhotograph = (Button) findViewById(R.id.btn_photograph);
		btnDirection = (Button) findViewById(R.id.btn_direction);
		btnBack = (Button) findViewById(R.id.btn_back);
		btnSave = (Button) findViewById(R.id.btn_save);
//		panel = (PhotoEditPanel) findViewById(R.id.panel);
		llDirection = (LinearLayout) findViewById(R.id.ll_direction);
		txtTitle=(TextView) findViewById(R.id.txt_title);

		llDirection.setVisibility(View.GONE);
		btnDirection.setOnClickListener(this);
		btnSave.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		btnPhotograph.setOnClickListener(this);
		btnPencil.setOnClickListener(this);
		btnEraser.setOnClickListener(this);
		btnZoomin.setOnClickListener(this);
		

		// 事件
		int count = llDirection.getChildCount();
		for (int i = 0; i < count; i++) {
			llDirection.getChildAt(i).setOnClickListener(
					new DirectionClickListener(i));
		}


		// 2.显示图片
		updateUI();

		
	}
	
	private void updateUI(){
		if (mode == PhotoProcessManager.MODE_EDIT_ONLY) {
			bmp = photoProcessManager.loadBitmap(currTask);
		} else {
			bmp = photoProcessManager.loadTempBitmap(currTask);
		}
		if (bmp == null) {
			Toast.makeText(this, "图片不存在！", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
//		imgPhoto.setImageBitmap(bmp);
		editHelper.setDirectionIndex(0);
		editHelper.setCurrTool(PhotoEditPanel.MODE_ZOOM);
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				panel.setPhoto(bmp);
			}
		}, 100);
		txtTitle.setText("编辑照片-"+currTask.getTitle());
	}

	@Override
	public void onClick(View v) {
		if (btnDirection.equals(v)) {
			if (llDirection.getVisibility() == View.GONE) {
				llDirection.setVisibility(View.VISIBLE);
			} else {
				llDirection.setVisibility(View.GONE);
			}

		} else if (btnSave.equals(v)) { // 保存
			save();
		} else if (btnBack.equals(v)) { // 返回
			showDiscardDialog(false);
		} else if (btnPhotograph.equals(v)) {
//			pickPicture();
			from=FROM_PHOTOGRAPH; //直接返回拍照
			showDiscardDialog(true);
		} else if(btnPencil.equals(v)){
			editHelper.setCurrTool(PhotoEditPanel.MODE_BLUR);
		} else if(btnEraser.equals(v)){
			editHelper.setCurrTool(PhotoEditPanel.MODE_ERASER);
		} else if(btnZoomin.equals(v)){
			editHelper.setCurrTool(PhotoEditPanel.MODE_ZOOM);
		}
	}

	/**
	 * 下一个照片任务
	 */
	private void nextPhotoTask(){
		currTask=photoProcessManager.getCurrentModifyTask();
		if(currTask==null){
			Toast.makeText(this, "没有下一个任务!", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		updateUI();
	}
	
	/**
	 * 放弃编辑，打回重来
	 */
	private void discard(boolean isRePhotograph) {
		photoProcessManager.resetTask(currTask); // 放弃
		nextStep(false,isRePhotograph);
	}

	/**
	 * 保存数据
	 */
	private void save() {
		
		//1.保存图片
		boolean isOk = photoProcessManager.submitModify(currTask, editHelper.getResultBitmap(),mode);
		if (!isOk) {
			Toast.makeText(this, "保存照片失败！", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		//2.检测流程是否完成
		boolean isComplete=photoProcessManager.checkIsComplete();
		if(isComplete){
			photoProcessManager.noticeFinish(); //通知结果
			finish();
			return ;
		}
		
		//3.继续下一个编辑照片任务还是拍照或图库
		nextStep(true);
		
		
	}
	
	private void nextStep(boolean isSave){
		nextStep(isSave,false);
	}
	
	private void nextStep(boolean isSave,boolean isRePhotograph){
		//继续下一个编辑照片任务还是拍照或图库
		if(mode==PhotoProcessManager.MODE_EDIT_ONLY){
			if(isSave){				
				nextPhotoTask(); //继续下一个任务
			}else{
				if(isRePhotograph){ //重新拍照
//					photoProcessManager.resetTask(currTask);
					Intent intent=new Intent(this, PhotographActivity.class);
					startActivity(intent);
				}else{					
					photoProcessManager.noticeFinish();
				}
				finish();
			}
		}else{ //拍照与编辑流程
			//完成了当前任务
			if(isSave){
				//是否完成了总任务
				boolean complete=photoProcessManager.checkIsComplete();
				if(complete){ //发通知
					photoProcessManager.noticeFinish();
				}else{
					Intent intent=new Intent(this, PhotographActivity.class);
					startActivity(intent);
				}
				
			}else{ //取消任务情况 各回各家
				Intent intent=new Intent();
				if(from==FROM_PHOTOGRAPH){
					intent.setClass(this, PhotographActivity.class);
				}else{
					intent.setClass(this, PickPictureActivity.class);
				}
				startActivity(intent);
			}
			finish();
		}
	}


	/**
	 * 显示是否放弃对话框
	 */
	private void showDiscardDialog(final boolean isRePhotograph) {
        MyAlertDialog.showAlert(this, isRePhotograph ? "重新拍摄这张照片？" : "放弃之前的操作？", R.string.alert, MyAlertDialog.BUTTON_STYLE_OK_CANCEL, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MyAlertDialog.POSITIVE_PRESSED:
                        discard(isRePhotograph);
                        break;
                    case MyAlertDialog.NEGATIVE_PRESSED:
                        break;
                }
                return false;
            }
        }));
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setMessage("当前编辑的照片将被删除您是否确认？");
//		// builder.setTitle("提示");
//		builder.setPositiveButton("确认删除",
//				new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//
//					}
//				});
//		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				dialog.dismiss();
//			}
//		});
//		builder.create().show();
	}

	/**
	 * 方向按钮点击事件监听器
	 * 
	 * @author 谭军华 创建于2014年4月16日 下午5:08:13
	 */
	class DirectionClickListener implements OnClickListener {
		int index;

		public DirectionClickListener(int index) {
			this.index = index;
			
		}

		@Override
		public void onClick(View v) {
			llDirection.setVisibility(View.GONE);
			editHelper.setDirectionIndex(index);
		}

	}
	
	/**
	 * 方向帮助类
	 * @author 谭军华
	 * 创建于2014年4月18日 下午7:56:52
	 */
	class EditHelper{
		
		final int directionResIds[]={
				R.drawable.direction_left,R.drawable.direction_right,R.drawable.direction_up,
				R.drawable.direction_down,R.drawable.direction_none};
		
		int directionIndex=4;
		int resId=R.drawable.direction_none;
		
		public void setDirectionIndex(int index){
			btnDirection.setBackgroundResource(directionResIds[index]);
			this.directionIndex=index;
			this.resId=directionResIds[index];
		}
		
		
		public void setCurrTool(int mode){
			panel.setMode(mode);
			LinearLayout ll=(LinearLayout) findViewById(R.id.ll_toolbox);
			for(int i=0;i<ll.getChildCount();i++){
				View v=ll.getChildAt(i);
				if(i==mode){
					v.setBackgroundColor(Color.GRAY);
				}else{
					v.setBackgroundColor(Color.TRANSPARENT);
				}
			}
			
		}
		
		/**
		 * 得到方向的图标
		 * @return
		 */
		private Bitmap getDirectionBitmap(){
			int w=bmp.getWidth();
			int size=w/20;
			BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(resId);
			Bitmap b=bd.getBitmap();
			Bitmap directionIconBmp= Bitmap.createScaledBitmap(b, size, size, true);
			return directionIconBmp;
		}
		
		/**
		 * 得到提示方向的图标
		 * @return
		 */
		private Bitmap getDirectionTipBitmap(){
			int width=bmp.getWidth();
			float h=width/25;
			
			BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.car_direction);
			Bitmap b=bd.getBitmap();
			float rate=h/b.getHeight();
			int w=(int) (rate*b.getWidth());
			
			Bitmap bmp= Bitmap.createScaledBitmap(b, w, (int) h, true);
			return bmp;
		}
		
		
		/**
		 * 得到合成后的图片
		 */
		public Bitmap getResultBitmap(){
			//1.相关参数
			Bitmap photoBmp=panel.getEditPhotoResult();

            //photoBmp = getDirectionIconBitmap(photoBmp);

			return photoBmp;
		}

        /**
         * 添加车头方向图片
         * @param src
         * @return
         */
        private Bitmap getDirectionIconBitmap(Bitmap src) {
            Bitmap directionIconBmp=getDirectionBitmap();
			int w=bmp.getWidth();
			int h=bmp.getHeight();
			int dw=directionIconBmp.getWidth();
			int dh=directionIconBmp.getHeight();
			int space=w/30;
			int left=space;


			Bitmap tipBmp=getDirectionTipBitmap();
			int tw=tipBmp.getWidth();
			int th=tipBmp.getHeight();

			int top=h-space-dh-th-space/2;
			left=left+(tw-dw)/2;


			//2.绘制
			Canvas canvas=new Canvas(src);
			canvas.drawBitmap(directionIconBmp, left, top, null);
			left=space;
			top=top+dh+space/2;
			canvas.drawBitmap(tipBmp, left, top, null);

            return src;
        }
		
		public int getFontHeight(float fontSize)  
		{  
		    Paint paint = new Paint();
		    paint.setTextSize(fontSize);  
		    FontMetrics fm = paint.getFontMetrics();
//		    return (int) fm.descent;  
		    return (int) Math.ceil(fm.descent - fm.top) + 2;
		} 
		
	}

}
