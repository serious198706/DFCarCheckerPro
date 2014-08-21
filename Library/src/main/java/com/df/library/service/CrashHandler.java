package com.df.library.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;

import com.df.library.entries.UserInfo;

/**
 * 崩溃处理器
 * @author 谭军华
 * 创建于2014年7月1日 上午10:27:49
 */
public class CrashHandler implements UncaughtExceptionHandler {

	private static final String ERR_PATH=android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/error.txt";
	/** 系统默认的UncaughtException处理类 */
//	private Thread.UncaughtExceptionHandler mDefaultHandler;
	/** 程序的Context对象 */
	private Context mContext;

	/** CrashHandler实例 */
	private static CrashHandler INSTANCE;

	/** 保证只有一个CrashHandler实例 */
	private CrashHandler() {
	}

	/** 获取CrashHandler实例 ,单例模式 */
	public static CrashHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CrashHandler();
		}
		return INSTANCE;
	}

	/**
	 * 初始化,注册Context对象, 获取系统默认的UncaughtException处理器, 设置该CrashHandler为程序的默认处理器
	 * 
	 * @param ctx
	 */
	public void init(Context ctx) {
		mContext = ctx;
//		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, final Throwable ex) {
		ex.printStackTrace();

		
		new Thread(){
			public void run() {
				sendError(ex);
				System.exit(0);
			};
		}.start();
	}
	
	/**
	 * 发送错误信息到邮件
	 * @param ex
	 */
	public void sendError(Throwable ex){
		Writer info = new StringWriter();   
		PrintWriter printWriter = new PrintWriter(info);   
		ex.printStackTrace(printWriter);   
		Throwable cause = ex.getCause();   
		while (cause != null) {   
			cause.printStackTrace(printWriter);   
			cause = cause.getCause();   
		}   
		String result = info.toString();   
		printWriter.close(); 

        String userName = "";

        UserInfo userInfo = UserInfo.getInstance();

        if(userInfo != null) {
            userName = userInfo.getName();
        } else {
            userName = "";
        }

		LogHelper helper=new LogHelper(mContext, userName ,result);
		helper.sendLogByEmail();
		
	}
}
