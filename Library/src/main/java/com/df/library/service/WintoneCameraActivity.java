package com.df.library.service;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.library.util.Common;
import com.df.library.R;


public class WintoneCameraActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "CameraActivity";

    //预览尺寸     默认设置
    public int WIDTH = 320;//640;//1024; 
    public int HEIGHT = 240;//480;//768;
    //拍摄尺寸      默认设置
    public int srcwidth = 2048;//1600;//2048;final
    public int srcheight = 1536;//1200;//1536;final
    //证件类型
    int nMainID = 6;
    private ImageButton backbtn, confirmbtn, resetbtn, takepicbtn, lighton,lightoff,cuton,cutoff;
    private TextView back_reset_text,take_recog_text,light_text,cut_text;
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ToneGenerator tone;
    private ImageView imageView ;
    private Bitmap bitmap;
    private RelativeLayout rlyaout;
    private Boolean cut = true;
    private List<String> focusModes;
    public long fastClick = 0;
    public int recogType = -1;//自动识别、划线识别
    private int width,height;
    private ImageView top_left,top_right,bottom_left,bottom_right,left_cut,right_cut;
    private String path;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        DisplayMetrics dm = new DisplayMetrics();  
        getWindowManager().getDefaultDisplay().getMetrics(dm); 
        width = dm.widthPixels;
        height = dm.heightPixels;
        setContentView(R.layout.wintone_camera);

        // 设置拍摄尺寸
        Intent intentget = this.getIntent();
        srcwidth = intentget.getIntExtra("srcwidth", 2048);
        srcheight = intentget.getIntExtra("srcheight", 1536);
        WIDTH = intentget.getIntExtra("WIDTH", 640);
        HEIGHT = intentget.getIntExtra("HEIGHT", 480);
        recogType = intentget.getIntExtra("recogType", 1);
        nMainID = intentget.getIntExtra("nMainID", 1100);
        path = intentget.getStringExtra("path");
        findview();
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(WintoneCameraActivity.this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void findview() {
    	back_reset_text = (TextView) findViewById(R.id.back_and_reset_text);
    	back_reset_text.setTextColor(Color.BLACK);
    	take_recog_text = (TextView) findViewById(R.id.take_and_confirm_text);
    	take_recog_text.setTextColor(Color.BLACK);
    	light_text = (TextView) findViewById(R.id.light_on_off_text);
    	light_text.setTextColor(Color.BLACK);
    	cut_text = (TextView) findViewById(R.id.cut_on_off_text);
    	cut_text.setTextColor(Color.BLACK);
    	
    	int button_width = (int) (height*0.125);
    	int button_distance = (int) (height*0.1);
    	
        RelativeLayout.LayoutParams lParams= new RelativeLayout.LayoutParams(button_width,button_width);
        lParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        lParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        lParams.topMargin = button_distance;
        backbtn = (ImageButton) findViewById(R.id.backbtn);
        backbtn.setLayoutParams(lParams);
        backbtn.setOnClickListener(new mClickListener());
        resetbtn = (ImageButton) findViewById(R.id.reset_btn);
        resetbtn.setLayoutParams(lParams);
        resetbtn.setOnClickListener(new mClickListener());
        
        lParams= new RelativeLayout.LayoutParams(button_width,button_width);
        lParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        lParams.addRule(RelativeLayout.BELOW, R.id.backbtn);
        lParams.topMargin = button_distance;
        takepicbtn = (ImageButton) findViewById(R.id.takepic_btn);
        takepicbtn.setLayoutParams(lParams);
        takepicbtn.setOnClickListener(new mClickListener());
        confirmbtn = (ImageButton) findViewById(R.id.confirm_btn);
        confirmbtn.setLayoutParams(lParams);
        confirmbtn.setOnClickListener(new mClickListener());
        
        lParams= new RelativeLayout.LayoutParams(button_width,button_width);
        lParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        lParams.addRule(RelativeLayout.BELOW, R.id.confirm_btn);
        lParams.topMargin = button_distance;
        lighton = (ImageButton) findViewById(R.id.lighton);
        lighton.setLayoutParams(lParams);
        lighton.setOnClickListener(new mClickListener());
        lightoff = (ImageButton) findViewById(R.id.lightoff);
        lightoff.setLayoutParams(lParams);
        lightoff.setOnClickListener(new mClickListener());
        
        lParams= new RelativeLayout.LayoutParams(button_width,button_width);
        lParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        lParams.addRule(RelativeLayout.BELOW, R.id.lighton);
        lParams.topMargin = button_distance;
        cuton = (ImageButton) findViewById(R.id.cuton);
        cuton.setLayoutParams(lParams);
        cuton.setOnClickListener(new mClickListener());
        cutoff = (ImageButton) findViewById(R.id.cutoff);
        cutoff.setLayoutParams(lParams);
        cutoff.setOnClickListener(new mClickListener());
        
        top_left = (ImageView) findViewById(R.id.topleft);
        top_right = (ImageView) findViewById(R.id.topright);
    	bottom_left = (ImageView) findViewById(R.id.bottomleft);
    	bottom_right = (ImageView) findViewById(R.id.bottomright);
    	
    	RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int)(height*0.18), (int)(height*0.18));
    	layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
    	layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
    	top_left.setLayoutParams(layoutParams);
    	
    	layoutParams = new RelativeLayout.LayoutParams((int)(height*0.18), (int)(height*0.18));
    	layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
    	layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.idcard_rightlyaout);
    	top_right.setLayoutParams(layoutParams); 
    	
    	layoutParams = new RelativeLayout.LayoutParams((int)(height*0.18), (int)(height*0.18));
    	layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
    	layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
    	bottom_left.setLayoutParams(layoutParams);
    	
    	layoutParams = new RelativeLayout.LayoutParams((int)(height*0.18),(int)(height*0.18));
    	layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
    	layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.idcard_rightlyaout);
    	bottom_right.setLayoutParams(layoutParams);
    	
    	int margin = 0;
    	int cutImageLayoutHeight = 0;
    	if (srcwidth==1280||srcwidth==960) {
    		margin = (int)((height*1.333)*0.165);
        	cutImageLayoutHeight = (int)(height*0.135);
		}
    	if (srcwidth==1600||srcwidth==1200) {
    		margin = (int)((height*1.333)*0.19);
        	cutImageLayoutHeight = (int)(height*0.108);
		}
    	if (srcwidth==2048||srcwidth==1536) {
    		margin = (int)((height*1.333)*0.22);
        	cutImageLayoutHeight = (int)(height*0.13);
		}
    	left_cut = (ImageView) findViewById(R.id.leftcut);
    	right_cut = (ImageView) findViewById(R.id.rightcut);
    	layoutParams = new RelativeLayout.LayoutParams((int)(cutImageLayoutHeight*0.6), cutImageLayoutHeight);
    	layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
    	layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);    			
    	layoutParams.leftMargin = margin;
    	left_cut.setLayoutParams(layoutParams);
    	
    	layoutParams = new RelativeLayout.LayoutParams((int)(cutImageLayoutHeight*0.6), cutImageLayoutHeight);
    	layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
    	layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.idcard_rightlyaout);    			
    	layoutParams.rightMargin = margin;
    	right_cut.setLayoutParams(layoutParams);


    	imageView = (ImageView) findViewById(R.id.backimageView);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceViwe);
        rlyaout = (RelativeLayout) findViewById(R.id.idcard_rightlyaout);
        int layout_width = (int)(width-((height*4)/3));
        RelativeLayout.LayoutParams  lP= new RelativeLayout.LayoutParams(layout_width,height);
        lP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        lP.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlyaout.setLayoutParams(lP);

        top_left.setBackgroundResource(R.drawable.top_left);
        bottom_left.setBackgroundResource(R.drawable.bottom_left);
        top_right.setBackgroundResource(R.drawable.top_right);
        bottom_right.setBackgroundResource(R.drawable.bottom_right);
        showFourImageView();
    }

    private void hideFourImageView(){
    	top_left.setVisibility(View.INVISIBLE);
    	top_right.setVisibility(View.INVISIBLE);
    	bottom_left.setVisibility(View.INVISIBLE);
    	bottom_right.setVisibility(View.INVISIBLE);
    }
    
    private void showFourImageView(){
    	left_cut.setVisibility(View.INVISIBLE);
    	right_cut.setVisibility(View.INVISIBLE);    	
    	top_left.setVisibility(View.VISIBLE);
    	top_right.setVisibility(View.VISIBLE);
    	bottom_left.setVisibility(View.VISIBLE);
    	bottom_right.setVisibility(View.VISIBLE);
    	
    }
    
	private class mClickListener implements OnClickListener {
		public void onClick(View v) {
            int id = v.getId();

            if(id == R.id.backbtn) {
                finish();
            } else if(id == R.id.takepic_btn) {
                takepicbtn.setEnabled(false);
                takePicture();
            } else if(id == R.id.reset_btn) {
                showFourImageView();
                takepicbtn.setVisibility(View.VISIBLE);
                take_recog_text.setText("拍照");
                backbtn.setVisibility(View.VISIBLE);
                back_reset_text.setText("返回");
                imageView.setImageDrawable(null);
                resetbtn.setVisibility(View.INVISIBLE);
                confirmbtn.setVisibility(View.INVISIBLE);
                takepicbtn.setEnabled(true);
                if (null != bitmap) {
                    bitmap.recycle();
                    bitmap = null;
                }
                camera.startPreview();
            } else if(id == R.id.confirm_btn) {
                if (isEffectClick()) {
                    confirmbtn.setEnabled(false);
                    hideFourImageView();
                    takepicbtn.setVisibility(View.VISIBLE);
                    backbtn.setVisibility(View.VISIBLE);
                    resetbtn.setVisibility(View.INVISIBLE);
                    confirmbtn.setVisibility(View.INVISIBLE);
                    imageView.setImageDrawable(null);
                    savephoto();
                } else {
                    Log.i(TAG, "confirmbtn click invalid");
                }
            } else if(id == R.id.lighton) {
                lightoff.setVisibility(View.VISIBLE);
                lighton.setVisibility(View.INVISIBLE);
                light_text.setText("关闪光灯");
                // 开启闪光灯
                Camera.Parameters parameters = camera.getParameters();
                parameters.set("flash-mode", "on");
                camera.setParameters(parameters);
            } else if(id == R.id.lightoff) {
                lighton.setVisibility(View.VISIBLE);
                lightoff.setVisibility(View.INVISIBLE);
                light_text.setText("开闪光灯");
                // 关闭闪光灯
                Camera.Parameters parameters2 = camera.getParameters();
                parameters2.set("flash-mode", "off");
                camera.setParameters(parameters2);
            } else if(id == R.id.cuton) {
                cuton.setVisibility(View.INVISIBLE);
                cutoff.setVisibility(View.VISIBLE);
                cut_text.setText("关闭裁切");
                cut = true;
            } else if(id == R.id.cutoff) {
                cuton.setVisibility(View.VISIBLE);
                cutoff.setVisibility(View.INVISIBLE);
                cut_text.setText("打开裁切");
                cut = false;
            }
		}

	}

    protected int readPreferences(String perferencesName, String key) {
    	SharedPreferences preferences = getSharedPreferences(perferencesName, MODE_PRIVATE);
	    int result = preferences.getInt(key, 0);
	    return result;
    }

    /**
     * 读取配置文件
     * @return
     * @throws IOException
     */
    public String readtxt() throws IOException {
         File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
        }
         String paths = sdDir.toString();
         if (paths.equals("") || paths == null) {
             return "";
         }
         String path = paths + "/AndroidWT/idcard.cfg";
         File file = new File(path);
         if(!file.exists()) return "";
         FileReader fileReader = new FileReader(path);
         BufferedReader br = new BufferedReader(fileReader);
         String str = "";
         String r = br.readLine();
         while (r != null) {
             str += r;
             r = br.readLine();
         }
         br.close();
         fileReader.close();
         return str;
     }

    /**
     * 防止快速点击
     * @return
     */
    public boolean isEffectClick() {
         long lastClick = System.currentTimeMillis();
         long diffTime = lastClick - fastClick;
         if(diffTime > 5000) {
             fastClick = lastClick;
             return true;
         }
         return false;
     }

    /* 拍照对焦 */
    /* 拍照 */
    public void takePicture() {
        if (camera != null) {
            try {
                if(focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    camera.autoFocus(new AutoFocusCallback() {
                        public void onAutoFocus(boolean success, Camera camera) {
                            if (success) {
                                camera.takePicture(shutterCallback, null, PictureCallback);
                            } else {
                                camera.takePicture(shutterCallback, null, PictureCallback);
                            }
                        }
                    });
                } else {
                    camera.takePicture(shutterCallback, null, PictureCallback);
                    Toast.makeText(getBaseContext(), "不支持自动对焦", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                camera.stopPreview();
                camera.startPreview();
                takepicbtn.setEnabled(true);
                Toast.makeText(this, R.string.toast_autofocus_failure, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "exception:" + e.getMessage());
            }
        }
    }


    // 快门按下的时候onShutter()被回调拍照声音
    private ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            if (tone == null)
                // 发出提示用户的声音
                tone = new ToneGenerator(
                        1,//AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                        ToneGenerator.MIN_VOLUME);
            tone.startTone(ToneGenerator.TONE_PROP_BEEP);
        }
    };

    /* 拍照后回显 */
    private PictureCallback PictureCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(TAG, "onPictureTaken");
            BitmapFactory.Options opts = new BitmapFactory.Options(); 
            // 设置成了true,不占用内存，只获取bitmap宽高
            opts.inJustDecodeBounds = true;
            // 根据内存大小设置采样率 
            // 需要测试！
            int SampleSize = computeSampleSize(opts, -1, 2048*1536);
            opts.inSampleSize = SampleSize;
            opts.inJustDecodeBounds = false;
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            //opts.inNativeAlloc = true; //属性设置为true，可以不把使用的内存算到VM里。SDK默认不可设置这个变量，只能用反射设置。
			try {
				Field field = BitmapFactory.Options.class.getDeclaredField("inNativeAlloc");
				field.set(opts, true);
			} catch (Exception e) {
				Log.i(TAG, "Exception inNativeAlloc");
			}
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            if (srcwidth == 2048 && srcheight == 1536) {
       		 	Matrix matrix = new Matrix(); 
    			matrix.postScale(0.625f,0.625f);
    			bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
			}
            if (srcwidth == 1600 && srcheight == 1200) {
       		 	Matrix matrix = new Matrix(); 
    			matrix.postScale(0.8f,0.8f);
    			bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
			}

            File myCaptureFile = new File(path);
            try {
                BufferedOutputStream bos = new BufferedOutputStream(
                        new FileOutputStream(myCaptureFile));
                /* 采用压缩转档方法 */
                bitmap.compress(CompressFormat.JPEG, 100, bos);
                /* 调用flush()方法，更新BufferStream */
                bos.flush();
                /* 结束OutputStream */
                bos.close();
                //隐藏焦点图片和行驶证外框
                hideFourImageView();

                /* 将拍照下来且保存完毕的图文件，显示出来 */
                imageView.setImageBitmap(bitmap);
                takepicbtn.setVisibility(View.INVISIBLE);
                backbtn.setVisibility(View.INVISIBLE);
                resetbtn.setVisibility(View.VISIBLE);
                back_reset_text.setText("重拍");
                confirmbtn.setVisibility(View.VISIBLE);
                take_recog_text.setText("确认");
                confirmbtn.setEnabled(true);
                resetCamera();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };
    
    /* 保存图片并送识别 */
    private void savephoto() {
        if (bitmap!=null) {
        	bitmap.recycle();
        	bitmap = null;
        }

        Intent intent = new Intent();
        intent.putExtra("cut", cut);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
        if (camera != null) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPictureFormat(PixelFormat.JPEG);
                parameters.setPreviewSize(WIDTH, HEIGHT);
                parameters.setPictureSize(srcwidth, srcheight);
                camera.setParameters(parameters);
                camera.setPreviewDisplay(holder);
                camera.startPreview();
                focusModes = parameters.getSupportedFocusModes();
            } catch (IOException e) {
                camera.release();
                camera = null;
                e.printStackTrace();
            }
        }
    }

    // 在surface创建时激发
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        // 获得Camera对象
        takepicbtn.setEnabled(true);
        if(null == camera) {
            camera = Camera.open();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
		}
        finish();
    }

    /* 相机重置 */
    private void resetCamera() {
        if (camera != null) {
            camera.stopPreview();

        }
    }
   
    public static int computeSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
       int initialSize = computeInitialSampleSize(options, minSideLength,
             maxNumOfPixels);
       int roundedSize;
       if (initialSize <= 8) {
          roundedSize = 1;
          while (roundedSize < initialSize) {
               roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
       double w = options.outWidth;
       double h = options.outHeight;
       int lowerBound = (maxNumOfPixels == -1) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
       int upperBound = (minSideLength == -1) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                Math.floor(h / minSideLength));
       if (upperBound < lowerBound) {
            return lowerBound;
       }
       if ((maxNumOfPixels == -1) &&
                (minSideLength == -1)) {
            return 1;
       } else if (minSideLength == -1) {
            return lowerBound;
       } else {
            return upperBound;
       }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}