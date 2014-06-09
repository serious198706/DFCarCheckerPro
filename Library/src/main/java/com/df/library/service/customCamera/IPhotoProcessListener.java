package com.df.library.service.customCamera;

import java.util.List;

/**
 * 照片处理监听器
 * @author 谭军华
 * 创建于2014年4月17日 下午2:04:42
 */
public interface IPhotoProcessListener {

	/**
	 * 当照片处理结束
	 * @param list
	 */
	public void onPhotoProcessFinish(List<PhotoTask> list);
}
