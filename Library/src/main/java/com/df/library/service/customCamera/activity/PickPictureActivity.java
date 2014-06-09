package com.df.library.service.customCamera.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;

import com.df.library.service.customCamera.PhotoProcessManager;
import com.df.library.service.customCamera.PhotoTask;


/**
 * 提取照片
 * @author 谭军华
 * 创建于2014年4月17日 下午6:32:51
 */
public class PickPictureActivity extends AbsPickPictureActivity{

	PhotoTask currTask;
	PhotoProcessManager photoProcessManager= PhotoProcessManager.getInstance();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		currTask=photoProcessManager.getCurrentPhotographTask();
		pickPicture();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		photoProcessManager.noticeFinish();
	}
	
	
	@Override
	protected void onObtainPicture(Bitmap bmp) {
		savePhoto(bmp);
	}

	@Override
	protected void onObtainPictureError() {
		super.onObtainPictureError();
//		pickPicture();
		photoProcessManager.noticeFinish();
		finish();
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
//			Intent intent=new Intent(this,MyPhotographActivity.class);
//			startActivity(intent);
			finish();
			return;
		}
		
		//2.改变照片任务的状态
		Intent intent=new Intent(this,PhotoEditActivity.class);
		intent.putExtra(PhotoEditActivity.EXTRA_FROM, PhotoEditActivity.FROM_PHOTOLIB);
		startActivity(intent);
		finish();
		
	}
}
