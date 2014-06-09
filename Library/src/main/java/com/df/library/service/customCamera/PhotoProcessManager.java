package com.df.library.service.customCamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 照片管理
 * @author 谭军华
 * 创建于2014年4月16日 上午10:26:34
 */
public class PhotoProcessManager {

	public static final String TEMP_PATH= Environment.getExternalStorageDirectory().getPath()+"/.df/tmp/";
	public static final String TEMP_CROP_FILE_PATH=TEMP_PATH+"crop.tmp";
	
	public static final int MODE_EDIT_ONLY = 0;
	public static final int MODE_PHOTOGRAPH_EDIT = 1;
	
	private String dstPath;
	private List<PhotoTask> photoTaskList=new ArrayList<PhotoTask>();
	private WeakReference<IPhotoProcessListener> wrPhotoProcessListener;
	private static PhotoProcessManager photoProcessManager=new PhotoProcessManager();	
	
	
	static{
		File dir=new File(TEMP_PATH);
		if(!dir.exists()){
			dir.mkdirs();
		}
	}
	
	private PhotoProcessManager(){
		
	}
	
	/**
	 * singleton
	 * @return
	 */
	public static PhotoProcessManager getInstance(){
		return photoProcessManager;
	}
	
	/**
	 * 设置接收通知
	 * @param l
	 */
	public void registPhotoProcessListener(IPhotoProcessListener l){
		wrPhotoProcessListener=new WeakReference<IPhotoProcessListener>(l);
	}

	/**
	 * 得到需要拍照
	 * @return
	 */
	public PhotoTask getCurrentPhotographTask(){
		for(PhotoTask task:photoTaskList){
			if(task.getState()==PhotoTask.STATE_WAIT){
				return task;
			}
		}
		return null;
	}
	
	/**
	 * 得到当前需要处理的照片
	 * @return
	 */
	public PhotoTask getCurrentModifyTask(){
		for(PhotoTask task:photoTaskList){
			if(task.getState()==PhotoTask.STATE_OBTAINPHOTO){
				return task;
			}
		}
		return null;
	}
	
	/**
	 * 检测所有任务是否完成
	 * @return
	 */
	public boolean checkIsComplete(){
		for(PhotoTask task:photoTaskList){
			if(task.getState()!=PhotoTask.STATE_COMPLETE){
				return false;
			}
		}
		return true;
	}
	

	public void initPhotoData(String dstPath,List<PhotoTask> tasks){
		if(dstPath==null || tasks==null){
			return;
		}
		photoTaskList.clear();
		photoTaskList.addAll(tasks);
		this.dstPath=dstPath;
		if(!dstPath.endsWith("/")){
			dstPath=dstPath+"/";
		}
		File dir=new File(dstPath);
		if(!dir.exists()){
			dir.mkdirs();
		}
		deleteTempFiles(); //清除临时文件
	}
	
	public void initModifyPhotoData(String dstPath, List<PhotoTask> tasks){
		for(PhotoTask task:tasks){
			task.setState(PhotoTask.STATE_OBTAINPHOTO);
		}
		photoTaskList.clear();
		photoTaskList.addAll(tasks);
		
		deleteTempFiles(); //清除临时文件
	}
	
	
	/**
	 * 删除临时图片
	 */
	public void deleteTempFiles(){
		File dir = new File(TEMP_PATH);
		File files[] = dir.listFiles();
		for(File file:files){
			file.delete();
		}
	}
	
	/**
	 * 提交拍照结果
	 * @param task
	 * @param bmp
	 * @return
	 */
	public boolean submitPhotograph(PhotoTask task,Bitmap bmp){
		String fileName = Long.toString(task.getFileName()) + ".jpg";
		String path = TEMP_PATH + fileName;
		boolean isOk = BitmapUtil.saveBitmap(bmp, path);
		
		if(isOk){
			//task.setFileName(fileName);
			//task.setPath(path);
			task.setState(PhotoTask.STATE_OBTAINPHOTO);
		}
		
		return isOk;
	}
	
	public void submitPickPicture(PhotoTask task){
		task.setState(PhotoTask.STATE_OBTAINPHOTO);
	}
	
	/**
	 * 提交修改
	 * @param task
	 * @param bmp
	 * @return
	 */
	public boolean submitModify(PhotoTask task,Bitmap bmp,int mode){
		String path;
		String fileName;

		if(mode==MODE_EDIT_ONLY){
			fileName = task.getFileName() + ".jpg";
			path = task.getPath() + fileName;
		}else{
			fileName = task.getFileName() + ".jpg";
			path = task.getPath() + fileName;
		}
		
		boolean isOk=BitmapUtil.saveBitmap(bmp, path);
		if(isOk){
			task.setState(PhotoTask.STATE_COMPLETE);
		}
		return isOk;
	}
	
	/**
	 * 打回原型
	 * @param task
	 */
	public void resetTask(PhotoTask task){
		task.setState(PhotoTask.STATE_WAIT);
	}
	
	public void noticeFinish(){
		if(wrPhotoProcessListener!=null){
			wrPhotoProcessListener.get().onPhotoProcessFinish(photoTaskList); //发送通知
		}
	}
	
	public List<PhotoTask> getPhotoTaskList(){
		return photoTaskList;
	}
	
	
	public List<PhotoTask> getCompletePhotoTaskList(){
		List<PhotoTask> list=new ArrayList<PhotoTask>();
		for(PhotoTask task:photoTaskList){
			if(task.getState()==PhotoTask.STATE_COMPLETE){
				list.add(task);
			}
		}
		return list;
	}

	public String getDstPath() {
		return dstPath;
	}

	public static String getTempPath() {
		return TEMP_PATH;
	}
	
	/**
	 * 加载临时的图片
	 * @param task
	 * @return
	 */
	public Bitmap loadTempBitmap(PhotoTask task){
		String path = TEMP_PATH + task.getFileName() + ".jpg";
		return BitmapFactory.decodeFile(path);
	}
	
	/**
	 * 加载图片
	 * @param task
	 * @return
	 */
	public Bitmap loadBitmap(PhotoTask task){
		String path = task.getPath() + task.getFileName() + ".jpg";
		return BitmapFactory.decodeFile(path);
	}
}
