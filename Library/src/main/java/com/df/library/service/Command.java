package com.df.library.service;

public abstract class Command {

	public final static byte BYTE_START = (byte) 0xAA; // 第一个字节
	public final static byte BYTE_GETGAUGEINFO = (byte) 0xB0; // 获取设备信息的命令字
	public final static byte BYTE_GETPROBESN = (byte) 0xBA; // 获取设备响应的命令字
	public final static byte BYTE_TRANSMITVALUES = (byte) 0xB6;// 获取测量信息的命令字

	// 指令byte[] 数组的下标
	public final static int QNC_INDEX_START = 0; // 下标0字节表示开始
	public final static int QNC_INDEX_LENGTH = 1; // 下标1字节表示参数长度
	public final static int QNC_INDEX_SERAILNUM1 = 2; // 下标2-4表示设备号
	public final static int QNC_INDEX_SERAILNUM2 = 3;
	public final static int QNC_INDEX_SERAILNUM3 = 4;
	public final static int QNC_INDEX_COMMAND = 5; // 下标5表示命令字
	public final static int QNC_INDEX_PARAMETERS = 6;// 下标6表示参数开始

	public final static int QNC_SERIALNUM_LEN = 3; // 设备码包含3个字节
	public final static byte[] SN_EMPTY = new byte[3];
	public final static byte[] PARAMETER_EMPTY = new byte[0];

	private byte[] data;

	public Command(byte cmd, byte[] sn, byte[] parameter) {
		setData(cmd, sn, parameter);
	}

	public byte[] getData() {
		return data;
	}
	
	public byte[] getSN(){
		byte[] sn = new byte[QNC_SERIALNUM_LEN];
		System.arraycopy(data, QNC_INDEX_SERAILNUM1, sn, 0, QNC_SERIALNUM_LEN);
		return sn;
	}
	
	public byte getCSH(){
		return data[data.length-2];
	}
	
	public byte getCSL(){
		return data[data.length-1];
	}

	private void setData(byte cmd, byte[] sn, byte[] parameter) {
		data = new byte[parameter.length + 8];
		data[QNC_INDEX_START] = BYTE_START;
		data[QNC_INDEX_LENGTH] = (byte) parameter.length;
		data[QNC_INDEX_SERAILNUM1] = sn[0];
		data[QNC_INDEX_SERAILNUM2] = sn[1];
		data[QNC_INDEX_SERAILNUM3] = sn[2];
		data[QNC_INDEX_COMMAND] = cmd;
		if (parameter.length > 0) {
			System.arraycopy(parameter, 0, data, QNC_INDEX_PARAMETERS, parameter.length);
		}
		checkSum(data);
	}

	/**
	 * 求和
	 * 
	 * @param data
	 * @return
	 */
	private int getCheckSum(byte[] data) {
		int wSum = 0;
		for (int i = 1; i < data.length - 2; i++) {
			wSum += data[i] & 0xFF;
		}
		return wSum;
	}

	/**
	 * 给最后2位检验单元赋值
	 * 
	 * @param data
	 */
	private void checkSum(byte[] data) {
		int wSum = getCheckSum(data);
		int len = data.length;
		data[len - 2] = (byte) ((wSum >> 8) & 0xFF);
		data[len - 1] = (byte) (wSum & 0xFF);
	}

	public static class QNC_GETGAUGEINFO extends Command {
		public QNC_GETGAUGEINFO() {
			super(BYTE_GETGAUGEINFO, SN_EMPTY, PARAMETER_EMPTY);
		}
	}

	public static class QNC_GETPROBESN extends Command {
		public QNC_GETPROBESN(byte[] sn) {
			super(BYTE_GETPROBESN, sn, PARAMETER_EMPTY);
		}
	}

	public static class QNC_TRANSMIT_VALUES extends Command {
		public QNC_TRANSMIT_VALUES(byte[] sn, byte[] parameter) {
			super(BYTE_TRANSMITVALUES, sn, parameter);
		}
	}
	
}
