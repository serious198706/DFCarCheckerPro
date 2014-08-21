package com.df.library.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.net.smtp.AuthenticatingSMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.apache.commons.net.smtp.SimpleSMTPHeader;
import org.apache.commons.net.util.Base64;
import org.apache.james.mime4j.codec.EncoderUtil;
import org.apache.james.mime4j.codec.EncoderUtil.Usage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * 日志helper
 * 
 * @author 谭军华 创建于2014年7月3日 下午4:30:40
 */
@SuppressLint("SimpleDateFormat")
public class LogHelper {

	public static final String EMAIL_SMTPHOSTNAME = "smtp.163.com";
	public static final String EMAIL_TO = "liudapeng@cheyipai.com";
//	public static final String EMAIL_TO = "tanjunhua@cheyipai.com";
	public static final String EMAIL_FROM = "log_1987@163.com";
	public static final String EMAIL_ACCOUNT = "log_1987";
	public static final String EMAIL_PASSWORD = "NTcxMjgwNA==";
	// public static final String EMAIL_SUBJECT="123456";

	Context mContext;
	String currUserName, errorInfo, extra, log;
	SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // 格式化当前系统日期

	public LogHelper(Context context, String userName, String errorInfo) {
		this(context, userName, "", errorInfo);
	}

	public LogHelper(Context context, String userName, String extra,
			String errorInfo) {
		this.mContext = context;
		this.currUserName = userName;
		this.extra = extra;
		this.errorInfo = errorInfo;

		// 生成日志
		log = createLog();
	}

	/**
	 * 
	 应用 版本 帐号 包名 MAC地址 其它备注 错误信息
	 */

	private String createLog() {
		// 1.基本信息
		String appName = getApplicationName(mContext);
		String versionName = getAppVersionName(mContext);
		String pkgName = mContext.getPackageName();
		String mac = getMac(mContext);
		String time = dateFm.format(new Date());

		// 2.拼装内容
		StringBuffer sb = new StringBuffer();
		sb.append("应用：").append(appName).append("\n");
		sb.append("版本：").append(versionName).append("\n");
		sb.append("包名：").append(pkgName).append("\n");
		sb.append("---------------------------------------------------------").append("\n");
		sb.append("当前用户账号：").append(currUserName).append("\n");
		sb.append("机器MAC地址：").append(mac).append("\n");
		sb.append("创建日志时间：").append(time).append("\n");
		if (extra != null && extra.trim().length() > 0) {
			sb.append("其它备注信息：").append(time).append("\n");
		}
		sb.append("---------------------------------------------------------")
				.append("\n");
		sb.append(errorInfo);

		return sb.toString();
	}

	// 获取手机的mac地址
	public String getMac(Context context) {
		 String macSerial = null;
         String str = "";
         try {
                 Process pp = Runtime.getRuntime().exec(
                                 "cat /sys/class/net/wlan0/address ");
                 InputStreamReader ir = new InputStreamReader(pp.getInputStream());
                 LineNumberReader input = new LineNumberReader(ir);


                 for (; null != str;) {
                         str = input.readLine();
                         if (str != null) {
                                 macSerial = str.trim();// 去空格
                                 break;
                         }
                 }
         } catch (IOException ex) {
                 // 赋予默认值
                 ex.printStackTrace();
         }
         return macSerial;
	}

	/**
	 * 返回当前程序版本名
	 */
	public String getAppVersionName(Context context) {
		String versionName = "";
		try {
			// ---get the package info---
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
			if (versionName == null || versionName.length() <= 0) {
				return "";
			}
		} catch (Exception e) {
			Log.e("VersionInfo", "Exception", e);
		}
		return versionName;
	}

	/**
	 * 得到应用名字
	 * 
	 * @return
	 */
	private String getApplicationName(Context context) {
		PackageManager packageManager = null;
		ApplicationInfo applicationInfo = null;
		try {
			packageManager = context.getPackageManager();
			applicationInfo = packageManager.getApplicationInfo(
					context.getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			applicationInfo = null;
		}
		String applicationName = (String) packageManager
				.getApplicationLabel(applicationInfo);
		return applicationName;
	}


	/**
	 * 发送日志
	 */
	public void sendLogByEmail() {
		String sendMsg = EncoderUtil.encodeB(log.getBytes()); //编码
		String appName = getApplicationName(mContext);
		String subject="[" + appName + "]-错误日志";
		subject = EncoderUtil.encodeIfNecessary(subject, Usage.TEXT_TOKEN, 0); //使用apache mime4j中的EncoderUtil來編碼郵件主題

//		byte b[]=DatatypeConverter.parseBase64Binary(s);
		String pwd=new String(Base64.decodeBase64(EMAIL_PASSWORD)); //解密
		/**
		 * Base64.decodeBase64(pwd)
		 */
		
		try {
			AuthenticatingSMTPClient client = new AuthenticatingSMTPClient();
			client.connect("smtp.163.com");
			int reply = client.getReplyCode();
			if (!SMTPReply.isPositiveCompletion(reply)) {
				client.disconnect();
				return;
			}
			client.elogin();
			client.auth(AuthenticatingSMTPClient.AUTH_METHOD.LOGIN, EMAIL_FROM,pwd);
			client.setSender(EMAIL_FROM);
			client.addRecipient(EMAIL_TO);
			
			SimpleSMTPHeader header = new SimpleSMTPHeader(EMAIL_FROM, EMAIL_TO,subject);
			header.addHeaderField("Content-Type", "text/plain; charset=UTF-8");
			header.addHeaderField("Content-Transfer-Encoding", "base64");
			Writer writer = client.sendMessageData();
			if (writer != null) {
				writer.write(header.toString());
				writer.write(sendMsg);
				writer.close();
				client.completePendingCommand();
			}

			client.logout();
			client.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
