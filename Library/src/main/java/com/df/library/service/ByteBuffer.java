package com.df.library.service;

/**
 * 缓存字节数组
 * @author zsg
 * 
 */
public class ByteBuffer {

	// 缓存数组最大长度
	private final static int MAX_DATABUF_LEN = 2048;

	private byte[] buffer = new byte[MAX_DATABUF_LEN];

	private int currentIndex;

	public void append(byte[] data) {
		if (data == null || data.length == 0) {
			return;
		}

		int freeLength = buffer.length - currentIndex;
		int readLength = data.length;
		if (readLength > freeLength) {
			readLength = freeLength;
		}
		System.arraycopy(data, 0, buffer, currentIndex, readLength);
		currentIndex += readLength;
	}

	public byte[] getBytes() {
		byte[] bRet = new byte[currentIndex];
		System.arraycopy(buffer, 0, bRet, 0, currentIndex);
		return bRet;
	}

	public void reset() {
		buffer = new byte[MAX_DATABUF_LEN];
	}
}
