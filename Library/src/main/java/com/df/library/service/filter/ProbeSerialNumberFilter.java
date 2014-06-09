package com.df.library.service.filter;

import com.df.library.service.ByteBuffer;
import com.df.library.service.Command;

import java.util.Arrays;

/**
 * 设备响应
 * AA 01 ED AA 03 8B 60 48 BA 8B 60 66 03 41 
 * @author zsg
 */

public class ProbeSerialNumberFilter {

	private ByteBuffer buffer = new ByteBuffer();

	private Command.QNC_GETPROBESN cmd;

	public ProbeSerialNumberFilter(Command.QNC_GETPROBESN cmd) {
		this.cmd = cmd;
	}

	public void receive(byte[] data) {
		buffer.append(data);
	}

	public byte[] getBytes() {
		return buffer.getBytes();
	}

	/**
	 * 是否响应
	 * @return
	 */
	public boolean doFilter() {
		boolean bRet = false;
		byte[] data = buffer.getBytes();
		byte[] sn = cmd.getSN();
		byte[] temp = new byte[14];
		byte[] tempSn = new byte[3];
		
		for (int index = 0; data.length - index >= 14; index++) {
			System.arraycopy(data, index, temp, 0, 14);
			if (temp[0] != (byte) 0xAA || temp[1] != cmd.getCSH()
					|| temp[2] != cmd.getCSL() || temp[3] != (byte) 0xAA
					|| temp[4] != (byte) 0x03 || temp[8] != (byte) 0xBA) {
				continue;
			}
			
			System.arraycopy(temp, 5, tempSn, 0, 3);
			if (Arrays.equals(tempSn, sn)) {
				bRet = true;
				break;
			}
		}		
		
		return bRet;
	}

}
