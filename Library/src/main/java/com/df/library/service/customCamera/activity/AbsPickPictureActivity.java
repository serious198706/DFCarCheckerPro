package com.df.library.service.customCamera.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.df.library.service.customCamera.PhotoProcessManager;

import java.io.File;

/**
 * 提取图片activity
 * 
 * @author 谭军华 创建于2014年4月17日 下午3:13:45
 */
public abstract class AbsPickPictureActivity extends Activity {

	public static int REQUEST_CODE_PICK = 1001; // pick
	public static int REQUEST_CODE_CROP = 1002; // crop

	Uri cropImageUri = Uri.fromFile(new File(
            PhotoProcessManager.TEMP_CROP_FILE_PATH));

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 1.选取照片
		if (requestCode == REQUEST_CODE_PICK) {

			if (resultCode == Activity.RESULT_OK) {
				Uri uri = data.getData();

				cropImage(uri, 800, 600, REQUEST_CODE_CROP);

			} else if (resultCode == Activity.RESULT_CANCELED) {
                PhotoProcessManager.getInstance().noticeFinish();
				finish();
			} else {
				onObtainPictureError();
			}
		}

		// 2.处理截图
		if (requestCode == REQUEST_CODE_CROP) {

			Bitmap photo = null;
			if (resultCode == Activity.RESULT_CANCELED) {
                PhotoProcessManager.getInstance().noticeFinish();
				finish();
				return;
			}

			if (resultCode == Activity.RESULT_OK) {
				// Uri photoUri = data.getData();
				// if (photoUri != null) {
				photo = BitmapFactory.decodeFile(cropImageUri.getPath());
				// }
			}

			// if (photo == null && data!=null) {
			// Bundle extra = data.getExtras();
			// if (extra != null) {
			// photo = (Bitmap) extra.get("data");
			// ByteArrayOutputStream stream = new ByteArrayOutputStream();
			// photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			// }
			// }
			if (photo == null) {
				onObtainPictureError();
			} else {
				onObtainPicture(photo);
			}

		}

	}

	/**
	 * 当获取了照片后
	 * 
	 * @param bmp
	 */
	protected abstract void onObtainPicture(Bitmap bmp);

	/**
	 * 当获取照片时错误
	 */
	protected void onObtainPictureError() {
		Toast.makeText(this, "选取照片错误！", Toast.LENGTH_SHORT).show();
	}

	/**
	 * 获取照片
	 */
	protected void pickPicture() {
		Intent openAlbumIntent = new Intent(Intent.ACTION_PICK);
		openAlbumIntent.setDataAndType(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

		startActivityForResult(openAlbumIntent, REQUEST_CODE_PICK);

	}

	// 截取图片
	private void cropImage(Uri uri, int outputX, int outputY, int requestCode) {

		Intent intent = new Intent("com.android.camera1.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 4);
		intent.putExtra("aspectY", 3);
		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);
		intent.putExtra("scale", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);
		intent.putExtra("return-data", false);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
		startActivityForResult(intent, requestCode);
	}
}
