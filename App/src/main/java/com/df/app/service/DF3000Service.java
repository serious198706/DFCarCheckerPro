package com.df.app.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.df.app.entries.Measurement;
import com.df.app.entries.SerialNumber;
import com.df.app.filter.GaugeInfoFilter;
import com.df.app.filter.ProbeSerialNumberFilter;
import com.df.app.filter.TransmitValueFilter;
import com.xinque.android.serial.driver.UsbSerialDriver;

/**
 * Created by zsg on 14-1-6.
 *
 * df3000
 */

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
		if (DF3000Service == null || com.df.app.service.DF3000Service.sDriver == null) {
			DF3000Service = new DF3000Service(sDriver);
		}
		return DF3000Service;
	}

	private DF3000Service(UsbSerialDriver sDriver) {
		com.df.app.service.DF3000Service.sDriver = sDriver;
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

				// 初始化设置，死的
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

		byte bufReceive[];
		int size;
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
	 * @return 是否成功
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

    /**
     * 检查序列号是否正确
     * @return
     */
	public boolean CheckQuaNixSN() {
		boolean bRet;
		Command.QNC_GETPROBESN cmd = new Command.QNC_GETPROBESN(m_SerialNum.getNumber());
		ProbeSerialNumberFilter probeSerialNumberFilter = new ProbeSerialNumberFilter(cmd);
		
		byte[] cmddata = cmd.getData();
		
		if (!WriteData(cmddata, cmddata.length)) {
			return false;
		}
		
		// 需多次获取
		for (int i = 0; i < 5; i++) {
			byte[] buf = new byte[MAX_DATABUF_LEN];
			int len;
			try {
				len = ReceiveData(buf);

				if (len == 0)
					continue;

				byte[] data = new byte[len];
				System.arraycopy(buf, 0, data, 0, len);
				probeSerialNumberFilter.receive(data);
			} catch (Exception e) {
				close();
				e.printStackTrace();
			}
		}

		bRet = probeSerialNumberFilter.doFilter();

		return bRet;
	}

    /**
     * 开始采集数据
     * @param onReceiveData
     * @return
     */
	public List<Measurement> startCollect(OnReceiveData onReceiveData){
		List<Measurement> measurements = new ArrayList<Measurement>();

		if (sDriver == null) {
			return measurements;
		}
		
		try {
			byte[] buf = new byte[MAX_DATABUF_LEN];
			int len;

			List<Command.QNC_TRANSMIT_VALUES> cmds = getAllMeasurementCommand();

			for(int i = 0; i < cmds.size(); i++){
				Command.QNC_TRANSMIT_VALUES cmd = cmds.get(i);
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
				}
				
				int[] bRet = transmitValueFilter.doFilter();

                Measurement measurement = new Measurement(i + 1);
                measurement.setMaterial("");
                measurement.setValue(bRet);

                measurements.add(measurement);

                // 收到数据，向界面发送
                onReceiveData.onReceiveData(new Measurement(i+1).setValue(bRet));
			}
		} catch (Exception e) {
			close();
		} 
		
		return measurements;
	}

    /**
     * 获取所有测量数据
     * @return
     */
	private List<Command.QNC_TRANSMIT_VALUES> getAllMeasurementCommand(){
		List<Command.QNC_TRANSMIT_VALUES> ALL_QNC_TRANSMIT_VALUES = new ArrayList<Command.QNC_TRANSMIT_VALUES>();

        // 添加所有的传输指令，29个区
        for(int i = 1; i <= 29; i++) {
            ALL_QNC_TRANSMIT_VALUES.add(new Command.QNC_TRANSMIT_VALUES(m_SerialNum.getNumber(),
                    new byte[]{0x00, (byte) i, 0, 0}));
        }
		return ALL_QNC_TRANSMIT_VALUES;
	}

	/**
	 * 写数据
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
	 * 读数据
	 * 
	 * @param data 接收数组
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

    /**
     * 获取所有序列号
     * @return
     */
	public List<SerialNumber> getSerialNumbers() {
		return serialNumbers;
	}

    /**
     * 获取指定序列号
     * @param position
     */
	public void selectSerial(int position) {
		m_SerialNum = serialNumbers.get(position);
	}

    /**
     * 关闭服务
     */
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

    /**
     * CollectDataLayout实现此接口，当接收到数据时调用此接口
     */
    public interface OnReceiveData {
        public void onReceiveData(Measurement measurement);
    }
}
