package com.df.app.filter;

import com.df.app.service.ByteBuffer;
import com.df.app.service.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 测量数据解析
 * 
 * @author zsg
 * 
 */

@SuppressWarnings("unused")
public class TransmitValueFilter {
	private ByteBuffer buffer = new ByteBuffer();

	private Command.QNC_TRANSMITVALUES cmd;

	public TransmitValueFilter(Command.QNC_TRANSMITVALUES cmd) {
		this.cmd = cmd;
	}

	public void receive(byte[] data) {
		buffer.append(data);
	}

	public byte[] getBytes() {
		return buffer.getBytes();
	}

	public int[] doFilter() {
		int[] iRet = new int[0];
		byte[] data = buffer.getBytes();

		byte[] start = new byte[] { (byte) 0xAA, cmd.getCSH(), cmd.getCSL() };
		byte[] startTemp = new byte[3];

		int startIndex = 0;
		for (int index = 0; data.length - index >= 3; index++) {
			System.arraycopy(data, index, startTemp, 0, 3);
			if (Arrays.equals(start, startTemp)) {
				startIndex = index + 3;
				break;
			}
		}

		if (startIndex >= 3) {
			byte[] sn = cmd.getSN();
			byte[] temp = new byte[24];

			List<Integer> list = new ArrayList<Integer>();

			for (int index = startIndex; data.length - index >= 24;) {
				System.arraycopy(data, index, temp, 0, 24);

				if (temp[0] != (byte) 0xAA || temp[1] != (byte) 0x10
						|| temp[2] != sn[0] || temp[3] != sn[1]
						|| temp[4] != sn[2] || temp[5] != (byte) 0xB6) {
					index++;
					continue;
				}

				// Log.d("doFilter", Decoder.dumpHexString(temp));
				byte[] v1 = new byte[3];
				byte[] v2 = new byte[3];
				v1[0] = temp[9];
				v1[1] = temp[10];
				v1[2] = temp[11];
				addValue(list, v1[0], v1[1], v1[2]);
				v2[0] = temp[16];
				v2[1] = temp[17];
				v2[2] = temp[18];
				addValue(list, v2[0], v2[1], v2[2]);
				index += 24;
			}

			iRet = new int[list.size()];
			for (int i = 0; i < list.size(); i++) {
				iRet[i] = list.get(i);
				// Log.d("doFilter value", String.valueOf(iRet[i]));

			}
		}

		return iRet;
	}

	private void addValue(List<Integer> list, byte one, byte two, byte three) {
		String hex = toHexString(one) + toHexString(two) + toHexString(three);
		int value = Integer.parseInt(hex, 16);

		if (value > 0) {
			list.add((int) Math.round(value / 100.0));
		}
	}

	private String toHexString(byte v) {
		String hex = Integer.toHexString(v & 0xFF);
		if (hex.length() == 1) {
			return "0" + hex;
		}

		return hex;
	}
}
