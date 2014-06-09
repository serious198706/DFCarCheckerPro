package com.df.library.entries;


import com.df.library.service.Command;
import com.df.library.service.Decoder;

import java.util.List;

/**
 * Created by zsg on 14-1-6.
 *
 * 设备序列号
 */

public class SerialNumber {
	byte[] number;

	public SerialNumber(byte[] number) {
		if (number == null || number.length != Command.QNC_SERIALNUM_LEN) {
			assert (false);
		}
		this.number = number;
	}

	public byte[] getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return Decoder.dumpHexString(number);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (!(o instanceof SerialNumber)) {
			return false;
		}

		byte[] bArray = ((SerialNumber) o).getNumber();

		if (bArray.length != Command.QNC_SERIALNUM_LEN) {
			return false;
		}

		if (bArray[0] == number[0] && bArray[1] == number[1] && bArray[2] == number[2]) {
			return true;
		}

		return false;
	}

	public boolean exist(List<SerialNumber> list) {
		if (list != null) {
			for (SerialNumber sn : list) {
				if (equals(sn)) {
					return true;
				}
			}
		}
		return false;
	}

    /**
     * 获取序列号的可读形式 (provided by Docs)
     * @return
     */
    public long getSerialNumber() {
        return getSerialNumber(number[0], number[1], number[2]);
    }

    private long getSerialNumber(byte snu, byte snh, byte snl)
    {
        // translate from the internal representation of the serial number
        if ( (snu & 0x80) != 0)
        {
            // The topmost bit is set
            byte yy, kw;
            long nr;
            // sequential number
            nr = ((long)snl)+(((long)snh & 0x03) << 8);
            // week of year
            kw = (byte)((snh & 0xFC) >> 2);
            // year
            yy = (byte)(snu & 0x7F);
            return (((long)yy) * 100000
                    + kw  *   1000
                    + nr);
        }
        else
        {
            // more complicated bit shifting
            long sn1, sn2;
            sn1  = snu;
            sn2  = (sn1 & 0x0F) * 100;
            sn1  = (sn1 >> 4) * 100;
            // sn1 contains the lower three digits of the serial number
            sn1 += snl;
            // sn2 contains the upper three digits of the serial number
            sn2 += snh;
            return ((sn2) * 1000
                    + sn1);
        }
    }
}
