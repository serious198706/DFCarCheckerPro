package com.df.app.filter;

import com.df.app.service.ByteBuffer;
import com.df.app.service.SerialNumber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * QNC_GETGAUGEINFO的响应
 * 设备号过滤 AA 00 B0 AA 03 (8D 08 61) B0 14 0A 0D 01 D4
 *
 * @author zsg
 *
 */
public class GaugeInfoFilter {
	private ByteBuffer buffer = new ByteBuffer();

	private List<SerialNumber> serialNumbers = new ArrayList<SerialNumber>();

	public void receive(byte[] data) {
		buffer.append(data);
	}

	public byte[] getBytes() {
		return buffer.getBytes();
	}

    public void doFilter() {
        serialNumbers.clear();
        byte[] data = buffer.getBytes();

        byte[] tag = new byte[] { (byte) 0xAA, (byte) 0x00, (byte) 0xB0 };
        byte[] temp = new byte[3];

        List<byte[]> serialData = new ArrayList<byte[]>();
        for (int index = 0; data.length - index >= 14;) {
            System.arraycopy(data, index, temp, 0, 3);

            if (Arrays.equals(tag, temp)) {
                int next = 0;
                for (int j = index + 14; data.length - j >= 14; j++) {
                    System.arraycopy(data, j, temp, 0, 3);
                    if (Arrays.equals(tag, temp)) {
                        next = j;
                        break;
                    }
                }

                if (next == 0) {
                    byte[] sdata = new byte[data.length - 3];
                    System.arraycopy(data, 3, sdata, 0, sdata.length);
                    serialData.add(sdata);
                    break;
                } else {
                    byte[] sdata = new byte[next - index];
                    System.arraycopy(data, index, sdata, 0, sdata.length);
                    serialData.add(sdata);
                    index = next;
                }

            } else {
                index++;
            }
        }

        byte[] temp2 = new byte[11];
        for (byte[] bytes : serialData) {
            for (int index = 0; bytes.length - index >= 11;) {
                System.arraycopy(bytes, index, temp2, 0, 11);
                if (temp2[0] == (byte) 0xAA && temp2[1] == (byte) 0x03
                        && temp2[5] == (byte) 0xB0) {
                    byte[] m_SerialNum = new byte[3];
                    System.arraycopy(temp2, 2, m_SerialNum, 0, 3);
                    // Log.d("temp2", Decoder.dumpHexString(m_SerialNum));

                    SerialNumber sn = new SerialNumber(m_SerialNum);
                    if (!sn.exist(serialNumbers)) {
                        serialNumbers.add(sn);
                    }
                    index += 11;
                } else {
                    index++;
                }
            }
        }
    }

	public List<SerialNumber> getSerialNumbers() {
		doFilter();
		return serialNumbers;
	}

}
