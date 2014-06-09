package com.df.library.service;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.df.library.util.Common;
import com.df.library.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class IdcardRunner extends Activity {
    public static final String TAG = "IdcardRunner";
    public static final String PATH = Environment.getExternalStorageDirectory().toString() + "/AndroidWT";
    private Button mbutquit;
    private String selectPath;
    private int nMainID = 0;
    private Boolean cutBoolean = true;
    private String resultFileNameString = "";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏

        //调用识别
        Intent intentget = this.getIntent();
        selectPath = intentget.getStringExtra("path");
        cutBoolean =  intentget.getBooleanExtra("cut", true);
        nMainID = intentget.getIntExtra("nMainID", 0);

        if (selectPath != null && !selectPath.equals("")) {
    		int index = selectPath.lastIndexOf("/");
    		resultFileNameString = (String) selectPath.subSequence(index+1, selectPath.length());

            try {
                Intent intent = new Intent("wintone.idcard");
                Bundle bundle = new Bundle();
                int nSubID[] = null;//{0x0001};
                bundle.putString("cls", "com.df.library.service.IdcardRunner");
                bundle.putInt("nTypeInitIDCard", 0);       //保留，传0即可
                bundle.putString("lpFileName", selectPath);//指定的图像路径
                bundle.putInt("nTypeLoadImageToMemory", 0);//0不确定是哪种图像，1可见光图，2红外光图，4紫外光图
                if (nMainID == 1000) {
                    nSubID[0] = 3;
                }
                bundle.putInt("nMainID", nMainID);    //证件的主类型。6是行驶证，2是二代证，这里只可以传一种证件主类型。每种证件都有一个唯一的ID号，可取值见证件主类型说明
                bundle.putIntArray("nSubID", nSubID); //保存要识别的证件的子ID，每个证件下面包含的子类型见证件子类型说明。nSubID[0]=null，表示设置主类型为nMainID的所有证件。

                //读设置到文件里的sn
                File file = new File(PATH);
                String snString = null;
                if (file.exists()) {
                    String filePATH = PATH + "/IdCard.sn";
                    File newFile = new File(filePATH);
                    if (newFile.exists()) {
                        BufferedReader bfReader = new BufferedReader(new FileReader(newFile));
                        snString= bfReader.readLine().toUpperCase();
                        bfReader.close();
                    }else {
                        bundle.putString("sn", ""); 
                    }
                    if (snString != null && !snString.equals("")) {
                        bundle.putString("sn", snString);
                    }else {
                        bundle.putString("sn", ""); 
                    }
                }else {
                    bundle.putString("sn", ""); 
                }

                bundle.putString("authfile","");   //文件激活方式  /mnt/sdcard/AndroidWT/357816040594713_zj.txt
                bundle.putString("logo", ""); //logo路径，logo显示在识别等待页面右上角
                bundle.putBoolean("isCut", cutBoolean);   //如不设置此项默认自动裁切
                bundle.putString("returntype", "withvalue");//返回值传递方式withvalue带参数的传值方式（新传值方式）
                intent.putExtras(bundle);
                startActivityForResult(intent, 8); 
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        "没有找到应用程序" + "wintone.idcard", Toast.LENGTH_SHORT).show();
            }
            
        } else {
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         Log.i(TAG, "onActivityResult");
         setContentView(R.layout.idcardrunner);

         EditText editResult = (EditText) this.findViewById(R.id.edit_file);

         if (requestCode == 8 && resultCode == RESULT_OK){
         // 读识别返回值
             int ReturnAuthority = data.getIntExtra("ReturnAuthority", -100000);//取激活状态
             int ReturnInitIDCard = data.getIntExtra("ReturnInitIDCard", -100000);//取初始化返回值
             int ReturnLoadImageToMemory = data.getIntExtra( "ReturnLoadImageToMemory", -100000);//取读图像的返回值
             int ReturnRecogIDCard = data.getIntExtra("ReturnRecogIDCard",  -100000);//取识别的返回值

            Log.d(Common.TAG, ReturnAuthority + ", " + ReturnInitIDCard + ", " + ReturnLoadImageToMemory + ", " + ReturnRecogIDCard);

            Log.i(TAG, "ReturnLPFileName:" + data.getStringExtra("ReturnLPFileName"));
            
            if (ReturnAuthority == 0 && ReturnInitIDCard == 0
                    && ReturnLoadImageToMemory == 0 && ReturnRecogIDCard > 0) {
                String result = "";


                String str = "\n--识别结果-- \n证件类型：" + ReturnRecogIDCard + "\n"+ result;
                editResult.setText(str);

                /**
                 0	保留
                 1	号牌号码
                 2	车辆类型
                 3	所有人
                 4	住址
                 5	品牌型号
                 6	车辆识别代号
                 7	发动机号码
                 8	注册日期
                 9	发证日期
                 10	使用性质
                 */

                String[] fieldvalue = (String[]) data.getSerializableExtra("GetRecogResult");
                String fields[] = {fieldvalue[1], fieldvalue[5], fieldvalue[2], fieldvalue[10], fieldvalue[7], fieldvalue[6]};

                Message msg = new Message();
                msg.obj = fields;
                LicenseRecognise.sHandler.sendMessage(msg);
                //CarRecogniseLayout.fillInRecogData(fields);
                finish();
            } else {
                String str = "";
                if (ReturnAuthority == -100000) {
                    str = "未识别   代码： " + ReturnAuthority;
                } else if (ReturnAuthority != 0) {
                    str = "激活失败 代码：" + ReturnAuthority;
                } else if (ReturnInitIDCard != 0) {
                    str = "识别初始化失败 代码：" + ReturnInitIDCard;
                } else if (ReturnLoadImageToMemory != 0) {
                    if (ReturnLoadImageToMemory == 3) {
                        str = "识别载入图像失败，请重新识别 代码：" + ReturnLoadImageToMemory;
                    } else if(ReturnLoadImageToMemory == 1){
                        str = "识别载入图像失败，识别初始化失败,请重试 代码：" + ReturnLoadImageToMemory;
                    } else {
                        str = "识别载入图像失败 代码：" + ReturnLoadImageToMemory;
                    }
                } else if (ReturnRecogIDCard != 0) {
                    str = "识别失败 代码：" + ReturnRecogIDCard;
                }
                editResult.setText("识别结果 :" + str + "\n");
            }
         }
    }

    //跳转后结束本Activity
    @Override
    protected void onStop() {
        super.onStop();
//        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // land
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // port
        }
    }
}
