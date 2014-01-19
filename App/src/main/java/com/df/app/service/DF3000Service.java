package com.df.app.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

import com.df.app.entries.Measurement;
import com.df.app.entries.SerialNumber;
import com.df.app.filter.GaugeInfoFilter;
import com.df.app.filter.ProbeSerialNumberFilter;
import com.df.app.filter.TransmitValueFilter;
import com.df.app.service.Command.QNC_TRANSMITVALUES;
import com.xinque.android.serial.driver.UsbSerialDriver;

public class DF3000Service {
	protected int MAX_DATABUF_LEN = 1024;
	// 指令byte[] 数组的下标
	protected final Object mReadBufferLock = new Object();
	protected final Object mWriteBufferLock = new Object();

	private static UsbSerialDriver sDriver = null;

	private SerialNumber m_SerialNum;// 当前设备码
	private List<SerialNumber> serialNumbers;// 所有设备代码
	private boolean isClosed = true;

	private static DF3000Service DF3000Service;

	public static DF3000Service instance(UsbSerialDriver sDriver) {
		if (DF3000Service == null || DF3000Service.sDriver == null) {
			DF3000Service = new DF3000Service(sDriver);
		}
		return DF3000Service;
	}

	private DF3000Service(UsbSerialDriver sDriver) {
		DF3000Service.sDriver = sDriver;
		serialNumbers = new ArrayList<SerialNumber>();
	}

    public String getSerialNumber() {
        return Long.toString(m_SerialNum.getSerialNumber());
    }

	/**
	 * 搜索检测设备，可能有多个设备
	 */
	public void search() {
		serialNumbers.clear();

		if (sDriver == null) {
			return;
		}

		try {
			if (isClosed) {
				sDriver.open();
				/**
				 * 初始化设置，不需要改变
				 */
				sDriver.setParameters(19200, 8, UsbSerialDriver.STOPBITS_1, UsbSerialDriver.PARITY_SPACE);

				// 等待
				synchronized (mReadBufferLock) {
					try {
						mReadBufferLock.wait(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				isClosed = false;
			}

			GetQuaNixSN();

		} catch (Exception e) {
			close();
		}
	}

	/**
	 * 获取设备码
	 */
	private boolean GetQuaNixSN() {
		boolean bRet = false;

		byte bufReceive[] = new byte[MAX_DATABUF_LEN];
		int size = 0;
		Command cmd = new Command.QNC_GETGAUGEINFO();
		GaugeInfoFilter gaugeInfoFilter = new GaugeInfoFilter();
		
		if (!WriteData(cmd.getData(), 8)) {
			return false;
		}
		
		// 需多次获取
		for (int iTry = 0; iTry < 5; iTry++) {
			bufReceive = new byte[MAX_DATABUF_LEN];

            try {
                size = ReceiveData(bufReceive);

                // 等待
                synchronized (mReadBufferLock) {
                    try {
                        mReadBufferLock.wait(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                byte[] temp = new byte[size];
                System.arraycopy(bufReceive, 0, temp, 0, size);

                gaugeInfoFilter.receive(temp);

            } catch (Exception e) {
                e.printStackTrace();
            }
		}
		serialNumbers = gaugeInfoFilter.getSerialNumbers();

		return bRet;
	}

	/**
	 * 连接设备
	 * 
	 * @return
	 */
	public boolean connection() {
		boolean bRet = false;
		if (sDriver == null) {
			return bRet;
		}

		try {
			bRet = CheckQuaNixSN();
		} catch (Exception e) {
			close();
		}

		return bRet;
	}

	public boolean CheckQuaNixSN() {
		boolean bRet = false;
		Command.QNC_GETPROBESN cmd = new Command.QNC_GETPROBESN(m_SerialNum.getNumber());
		ProbeSerialNumberFilter probeSerialNumberFilter = new ProbeSerialNumberFilter(cmd);
		
		byte[] cmddata = cmd.getData();
		
		if (!WriteData(cmddata, cmddata.length)) {
			return false;
		}
		
		// 需多次获取
		for (int iTry = 0; iTry < 5; iTry++) {
			//Log.d(Common.TAG, "CheckQuaNixSN " + (iTry + 1) + " times");

			byte[] buf = new byte[MAX_DATABUF_LEN];
			int len;
			try {
				len = ReceiveData(buf);

				if (len == 0)
					continue;

				byte[] data = new byte[len];
				System.arraycopy(buf, 0, data, 0, len);
				probeSerialNumberFilter.receive(data);
				//Log.d("CheckQuaNixSN Read bytes:", Decoder.dumpHexString(data));

			} catch (Exception e) {
				close();
				e.printStackTrace();
			}
		}

		bRet = probeSerialNumberFilter.doFilter();

		return bRet;
	}
	
	
	public List<Measurement> startCollect(OnReceiveData onReceiveData){
		List<Measurement> measurements = new ArrayList<Measurement>();

		if (sDriver == null) {
			return measurements;
		}
		
		try {
			byte[] buf = new byte[MAX_DATABUF_LEN];
			int len;

			List<QNC_TRANSMITVALUES> cmds = getAllMeasurementCommond();

			for(int i = 0; i < cmds.size(); i++){
				QNC_TRANSMITVALUES cmd = cmds.get(i);
				TransmitValueFilter transmitValueFilter = new TransmitValueFilter(cmd);
				byte[] cmddata = cmd.getData();

				if (!WriteData(cmddata, cmddata.length)) {
					continue;
				}
				
				for (int iTry = 0; iTry < 5; iTry++) {
					len = ReceiveData(buf);
					
					byte[] temp = new byte[len];
					System.arraycopy(buf, 0, temp, 0, len);

					transmitValueFilter.receive(temp);

                    Thread.sleep(100);
					
					final String message = "read " + len + " bytes: \n" + Decoder.dumpHexString(temp);
					//Log.d("GetTransmitValue " + iTry, message);
				}
				
				int[] bRet = transmitValueFilter.doFilter();

                onReceiveData.onReceiveData(new Measurement(i+1).setValue(bRet));

				measurements.add(new Measurement(i+1).setValue(bRet));
			}
		} catch (Exception e) {
			close();
		} 
		
		return measurements;
	}
	
	private List<QNC_TRANSMITVALUES> getAllMeasurementCommond(){
		List<QNC_TRANSMITVALUES> ALL_QNC_TRANSMITVALUES = new ArrayList<QNC_TRANSMITVALUES>();

        // 添加所有的传输指令，29个区
        for(int i = 1; i <= 29; i++) {
            ALL_QNC_TRANSMITVALUES.add(new QNC_TRANSMITVALUES(m_SerialNum.getNumber(),
                    new byte[]{0x00, (byte)i, 0, 0}));
        }
		return ALL_QNC_TRANSMITVALUES;
	}

	public int[] GetTransmitValues() {
		int[] bRet = null;
		QNC_TRANSMITVALUES cmd = new QNC_TRANSMITVALUES(m_SerialNum.getNumber(),new byte[]{0x00,0x02,0x00,0x00});
		TransmitValueFilter transmitValueFilter = new TransmitValueFilter(cmd);
		byte[] cmddata = cmd.getData();
		
		if (!WriteData(cmddata, cmddata.length)) {
			return bRet;
		}

		byte[] buf;
		int len;
		try {
			// 需多次获取
			for (int iTry = 0; iTry < 5; iTry++) {
				buf = new byte[MAX_DATABUF_LEN];
				len = ReceiveData(buf);
				
				byte[] temp = new byte[len];
				System.arraycopy(buf, 0, temp, 0, len);

				transmitValueFilter.receive(temp);
				
				final String message = "read " + len + " bytes: \n" + Decoder.dumpHexString(temp);
				//Log.d("GetTransmitValue " + iTry, message);
			}
		} catch (Exception e) {
			close();
			e.printStackTrace();
		}

		bRet = transmitValueFilter.doFilter();
		
		return bRet;
	}

	/**
	 * 发送数据
	 * 
	 * @param data
	 * @param size
	 * @return
	 */
	public boolean WriteData(byte[] data, int size) {
		boolean bRet = false;
		try {
			byte[] sendData = new byte[size];
			System.arraycopy(data, 0, sendData, 0, size);
			//Log.d("WriteData", Decoder.dumpHexString(sendData));
			int len = sDriver.write(sendData, 1000);
			synchronized (mWriteBufferLock) {
				mWriteBufferLock.wait(1500);
			}
			bRet = len > 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bRet;
	}

	/**
	 * 接收数据
	 * 
	 * @param data
	 *            接收数组
	 * @return
	 */
	public int ReceiveData(byte[] data) {
		int len = 0;
		try {
			len = sDriver.read(data, 1000);
//			// 等待
//			synchronized (mReadBufferLock) {
//				try {
//					mReadBufferLock.wait(300);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return len;
	}

	public List<SerialNumber> getSerialNumbers() {
		return serialNumbers;
	}

	public void selectSerial(int position) {
		m_SerialNum = serialNumbers.get(position);
		Log.d("selectSerial", Decoder.dumpHexString(m_SerialNum.getNumber()));
	}

	public void close() {
		if (sDriver != null) {
			try {
				sDriver.close();
			} catch (IOException e) {
				// Ignore.
			}
			sDriver = null;
		}
		isClosed = true;
	}


    public interface OnReceiveData {
        public void onReceiveData(Measurement measurement);
    }
}
