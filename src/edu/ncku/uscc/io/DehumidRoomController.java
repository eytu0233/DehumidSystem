package edu.ncku.uscc.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.ncku.uscc.util.Log;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DehumidRoomController extends Thread implements
		SerialPortEventListener {

	private static final String LCK_REMOVE_CMD = "sudo rm -f /var/lock/LCK..ttyUSB";

	private static final int PANEL_CMD_ONOFF = 0x80;
	private static final int PANEL_CMD_MODE = 0x81;
	private static final int PANEL_CMD_SET = 0x82;
	private static final int PANEL_CMD_HUMID_SET = 0x83;
	private static final int PANEL_CMD_TIMER_SET = 0x84;
	private static final int PANEL_CMD_START = 0x85;
	private static final int PANEL_CMD_SHUTDOWM = 0x86;
	private static final int PANEL_CMD_TEMP_ABNORMAL = 0x87;
	private static final int PANEL_CMD_DEFROST = 0x88;
	private static final int PANEL_CMD_MINUS_TIMER = 0x89;
//	private static final int PANEL_CMD_HUMID = 0x68;
	private static final int PANEL_CMD_ENFORCE_DEHUMID = 0x8A;
	private static final int PANEL_CMD_ENFORCE_DRY_CLOTHES = 0x8B;
	private static final int PANEL_CMD_HUMID_ABNORMAL = 0x8D;
	private static final int PANEL_CMD_FAN_ABNORMAL = 0x8E;
	private static final int PANEL_CMD_COMPRESSOR_ABNORMAL = 0x8F;
	private static final int PANEL_CMD_SET_HUMID = 0xCE;
	private static final int PANEL_CMD_SET_TIMER = 0xCF;

	private static final int PANEL_REP_ON = 0x30;
	private static final int PANEL_REP_OFF = 0x31;
	private static final int PANEL_REP_DEHUMID = 0x32;
	private static final int PANEL_REP_DRY_CLOTHES = 0x33;
	private static final int PANEL_REP_NO_SET = 0x34;
	private static final int PANEL_REP_HUMID_SET = 0x35;
	private static final int PANEL_REP_TIMER_SET = 0x36;
	private static final int PANEL_REP_OK = 0x55;

	private static final int DEHUMID_CMD_ON = 0x30;
	private static final int DEHUMID_CMD_OFF = 0x31;
	private static final int DEHUMID_CMD_DEHUMID_MODE = 0x32;
	private static final int DEHUMID_CMD_DRY_CLOTHES_MODE = 0x33;
	private static final int DEHUMID_CMD_DEHUMIDITY_SET = 0x34;
	private static final int DEHUMID_CMD_TIMER_SET = 0x35;
	private static final int DEHUMID_CMD_DEHUMIDITY_DIGIT_ONES = 0x38;
	private static final int DEHUMID_CMD_DEHUMIDITY_DIGIT_TENS = 0x39;

	private static final int DEHUMID_REP_OK = 0x55;
	private static final int DEHUMID_REP_HIGH_TEMP_ABNORMAL = 0x56;
	private static final int DEHUMID_REP_DEFROST_TEMP_ABNORMAL = 0x57;
	private static final int DEHUMID_REP_DEHUMID_ABNORMAL = 0x58;
	private static final int DEHUMID_REP_FAN_ABNORMAL = 0x59;
	private static final int DEHUMID_REP_COMPRESSOR_ABNORMAL = 0x5A;

	private static final int DEHUMIDIFIERS_A_ROOM = 8;

	private static final int ROOM_ID_MIN = 2;
	private static final int ROOM_ID_MAX = 5;

	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 300;
	private static final int ERR = 2;

	private static final int INITIAL_RATE = 100;

	/** A synchronization lock */
	private final Object lock = new Object();

	/** The instance of SerialPort class */
	private SerialPort serialPort;
	/** DisconnectListeners List to trigger disconnection event */
	private List<SerialPortDisconnectListener> listeners = new ArrayList<SerialPortDisconnectListener>();
	/** Buffered input stream from the port */
	private InputStream input;
	/** The output stream to the port */
	private OutputStream output;
	/** The buff receives byte data from serial port interface */
	private Byte rxBuf;
	

	private int targetIndex;
	/** The index of the room after room index scan */
	private int roomIndex;
	/** Initial flag */
	
	private Scanner userInput = new Scanner(System.in);

	/** These integers are used to count check rate */
	private int[] checkRates = new int[DEHUMIDIFIERS_A_ROOM];

	/**
	 * Constructor
	 * 
	 * @param slave
	 * @param serialPort
	 */
	public DehumidRoomController(SerialPort serialPort) {
		super();
		this.serialPort = serialPort;
	}

	public DehumidRoomController(SerialPort serialPort, int roomIndex) {
		// TODO Auto-generated constructor stub
		super();
		this.serialPort = serialPort;
		this.targetIndex = roomIndex;
	}

	/**
	 * To initailize this object and start a thread
	 * 
	 * @throws Exception
	 */
	public void initialize() throws Exception {
		// open the streams
		input = serialPort.getInputStream();
		output = serialPort.getOutputStream();

		// add event listeners
		serialPort.addEventListener(this);
		serialPort.notifyOnDataAvailable(true);

		for (int did = 0; did < DEHUMIDIFIERS_A_ROOM; checkRates[did++] = INITIAL_RATE)
			;

		setDaemon(true);
		start();
	}

	/**
	 * Add disconnect listener
	 * 
	 * @param listener
	 * @throws Exception
	 */
	public void addDisconnectListener(SerialPortDisconnectListener listener)
			throws Exception {
		listeners.add(listener);
	}

	/**
	 * Remove disconnect listener
	 * 
	 * @param listener
	 * @throws Exception
	 */
	public void removeDisconnectListener(SerialPortDisconnectListener listener)
			throws Exception {
		listeners.remove(listener);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		int comm;

		try {
			roomIndex = scanRoomIndex();
			while (roomIndex == -1)
				Thread.sleep(1000000);
//			while (roomIndex != targetIndex) {
//				Log.info("sleep");
//				Thread.sleep(1000000);
//			}
			while (output != null) {

				System.out.println("\n\n\n面板命令(HEX)			控制板命令(HEX)\n"
						+ "(1)問面板ON/OFF 0x80		(19)控制板ON 0x30\n"
						+ "(2)問面板狀態 0x81		(20)控制板OFF 0x31\n"
						+ "(3)回傳面板狀態 0x82		(21)控制板除濕模式 0x32\n"
						+ "(4)回傳濕度設定 0x83		(22)控制板乾衣模式 0x33\n"
						+ "(5)回傳定時設定 0x84		(23)控制板設定濕度 0x34\n"
						+ "(6)外部啟動命令 0x85		(24)控制板設定時間 0x35\n"
						+ "(7)外部關機命令 0x86		(25)要求回傳現在濕度個位數 0x38\n"
						+ "(8)高吐溫度異常通知 0x87	(26)要求回傳現在濕度十位數 0x39\n"
						+ "(9)除霜溫度異常通知 0x88\n"
						+ "(10)定時減1通知 0x89\n"
						+ "(11)設定濕度資料\n"
						+ "(12)強制除濕模式 0x8A\n"
						+ "(13)強制乾衣模式 0x8B\n"
						+ "(14)濕度異常通知 0x8D\n"
						+ "(15)風扇異常通知 0x8E\n"
						+ "(16)壓縮機異常通知 0x8F\n"
						+ "(17)外部濕度設定 0xCE\n"
						+ "(18)外部定時設定 0xCF\n\n"
						+ "What is your command?"
						);
				comm = userInput.nextInt();
				
				test(comm, roomIndex);
			}
		} catch (Exception e) {
			Log.error(e, e);
			for (SerialPortDisconnectListener listener : listeners) {
				listener.onDisconnectEvent(serialPort.getName());
			}
		} finally {
			try {
				close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.error(e, e);
			}
		}
	}

	/**
	 * This should be called when you stop using the port. This will prevent
	 * port locking on platforms like Linux.
	 * 
	 * @throws IOException
	 */
	public synchronized void close() throws IOException {

		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}

		if (input != null) {
			input.close();
			input = null;
		}

		if (output != null) {
			output.close();
			output = null;
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {

		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {

			try {
				int available = input.available();
				byte chunk[] = new byte[available];

				if (input == null)
					return;
				input.read(chunk, 0, available);

				for (byte b : chunk) {
					rxBuf = b;
				}

				// System.out.println(String.format("Recv from panel : %x",
				// ((int) rxBuf & 0xff)));

				synchronized (lock) {
					lock.notifyAll();
				}

			} catch (IOException e) {
				synchronized (lock) {
					Log.error(serialPort.getName() + " disconnected!", e);
					for (SerialPortDisconnectListener listener : listeners) {
						listener.onDisconnectEvent(serialPort.getName());
					}
					try {
						close();
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						Log.error(ex, ex);
					}
				}
			} catch (Exception e) {
				Log.error(e, e);
			}

		}
	}

	/**
	 * Try to scan the index of the room for the SerialPort
	 * 
	 * @return Index(>=2) of the room, return -1 by fail
	 * @throws IOException
	 * @throws Exception
	 */
	private int scanRoomIndex() throws IOException, Exception {
		// TODO Auto-generated method stub
		byte[] txBuf = new byte[1];

		for (int roomScanIndex = ROOM_ID_MIN; roomScanIndex <= ROOM_ID_MAX; roomScanIndex++) {
			for (int did = 0; did < DEHUMIDIFIERS_A_ROOM; did++) {
				txBuf[0] = (byte) ((roomScanIndex << 3) + did);
				for (int usbIndex = 0; usbIndex < 4; Runtime.getRuntime().exec(
						LCK_REMOVE_CMD + usbIndex++))
					;
				rxBuf = -1;
				if (output == null)
					return -1;
				output.write(txBuf);
				Log.info(String.format("Scan roomIndex : %x in %s",
						((int) txBuf[0] & 0xff), serialPort.getName()));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}

				if (rxBuf < 0) {
					continue;
				} else if (rxBuf == DEHUMID_REP_OK
						|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
						|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL
						|| rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL
						|| rxBuf == DEHUMID_REP_FAN_ABNORMAL
						|| rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL) {
					Log.info("Scan room index : " + roomScanIndex + " " +rxBuf);
					return roomScanIndex;
				}
			}
		}

		return -1;
	}

	
	private void test(int comm, int roomIndex) throws IOException, Exception {
		byte[] txBuf = new byte[1];
		byte err = ERR + 1;
		int set;
		int did;
		
		switch (comm) {
		case 1:
			txBuf[0] = (byte) PANEL_CMD_ONOFF;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf == PANEL_REP_ON) {
					Log.info(String.format("Panel is ON. 0x%x", rxBuf));
					break;
				} else if (rxBuf == PANEL_REP_OFF) {
					Log.info(String.format("Panel is OFF. 0x%x", rxBuf));
					break;
				} else {
					Log.debug(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			break;
			
		case 2:
			txBuf[0] = (byte) PANEL_CMD_MODE;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf == PANEL_REP_DEHUMID) {
					Log.info(String.format("Panel is dehumid mode. 0x%x", rxBuf));
					break;
				} else if (rxBuf == PANEL_REP_DRY_CLOTHES) {
					Log.info(String.format("Panel is dry clothes mode. 0x%x", rxBuf));
					break;
				} else {
					Log.warn(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			break;
			
		case 3:
			txBuf[0] = (byte) PANEL_CMD_SET;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf == PANEL_REP_NO_SET) {
					Log.info(String.format("Panel hasn't any set. 0x%x", rxBuf));
					break;
				} else if (rxBuf == PANEL_REP_HUMID_SET) {
					Log.info(String.format("Panel has humidity set. 0x%x", rxBuf));
					break;
				} else if (rxBuf == PANEL_REP_TIMER_SET) {
					Log.info(String.format("Panel has timer set. 0x%x", rxBuf));
					break;
				} else {
					Log.warn(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			break;
			
		case 4:
			txBuf[0] = (byte) PANEL_CMD_HUMID_SET;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf >= 1 && rxBuf <= 9) {
					Log.info(String.format("The humidity set of Panel is %d.", 45 + ((int) rxBuf) * 5));
					break;
				} else {
					Log.warn(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			break;
			
		case 5:
			txBuf[0] = (byte) PANEL_CMD_TIMER_SET;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf >= 0) {
					Log.debug(String.format("The timer set of Panel is %d.", rxBuf));
					break;
				} else {
					Log.warn(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			break;
			
		case 6:
			txBuf[0] = (byte) PANEL_CMD_START;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf == PANEL_REP_ON) {
					Log.info(String.format("Panel is ON. 0x%x", rxBuf));
					break;
				} else {
					Log.debug(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			break;
			
		case 7:
			txBuf[0] = (byte) PANEL_CMD_SHUTDOWM;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf == PANEL_REP_OFF) {
					Log.info(String.format("Panel is OFF. 0x%x", rxBuf));
					break;
				} else {
					Log.debug(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			break;
			
		case 8:
			txBuf[0] = (byte) PANEL_CMD_TEMP_ABNORMAL;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf == PANEL_REP_OK) {
					Log.info(String.format("Panel is in temp abnormal mode. 0x%x", rxBuf));
					break;
				} else {
					Log.debug(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			break;
			
		case 9:
			txBuf[0] = (byte) PANEL_CMD_DEFROST;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf == PANEL_REP_OK) {
					Log.info(String.format("Panel is in defrost mode. 0x%x", rxBuf));
					break;
				} else {
					Log.debug(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			break;
			
		case 10:
			txBuf[0] = (byte) PANEL_CMD_MINUS_TIMER;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf == PANEL_REP_OK) {
					Log.info(String.format("Panel timer minus one. 0x%x", rxBuf));
					break;
				} else {
					Log.debug(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			break;
			
		case 11:
//			userInput = new Scanner(System.in);
			byte dehumid;
			System.out.println("輸入設定濕度資料(40~90):");
			dehumid = userInput.nextByte();
			txBuf[0] = (byte) (dehumid + 104);
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf == PANEL_REP_OK) {
					Log.info(String.format("Panel humid set OK. 0x%x", rxBuf));
					break;
				} else {
					Log.debug(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			break;
			
		case 12:
			txBuf[0] = (byte) PANEL_CMD_ENFORCE_DEHUMID;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf == PANEL_REP_OK) {
					Log.info(String.format("Enforce panel dehumid OK. 0x%x", rxBuf));
					break;
				} else {
					Log.debug(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			break;
			
		case 13:
			txBuf[0] = (byte) PANEL_CMD_ENFORCE_DRY_CLOTHES;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf == PANEL_REP_OK) {
					Log.info(String.format("Enforce panel dry clothes OK. 0x%x", rxBuf));
					break;
				} else {
					Log.debug(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			break;
			
		case 14:
			txBuf[0] = (byte) PANEL_CMD_HUMID_ABNORMAL;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf == PANEL_REP_OK) {
					Log.info(String.format("Panel humid abnormal rep OK. 0x%x", rxBuf));
					break;
				} else {
					Log.debug(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			break;
			
		case 15:
			txBuf[0] = (byte) PANEL_CMD_FAN_ABNORMAL;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf == PANEL_REP_OK) {
					Log.info(String.format("Panel fan abnormal rep OK. 0x%x", rxBuf));
					break;
				} else {
					Log.debug(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			break;
			
		case 16:
			txBuf[0] = (byte) PANEL_CMD_COMPRESSOR_ABNORMAL;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf == PANEL_REP_OK) {
					Log.info(String.format("Panel compressor abnormal rep OK. 0x%x", rxBuf));
					break;
				} else {
					Log.debug(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			break;
			
		case 17:
			txBuf[0] = (byte) PANEL_CMD_SET_HUMID;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf == PANEL_REP_OK) {
					Log.info(String.format("Panel rep OK. 0x%x", rxBuf));
					break;
				} else {
					Log.debug(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			
			System.out.println("Please enter humid set(1~9):");
			did = userInput.nextInt();
			err = ERR + 1;
			if (did >= 1 && did <= 9){
				txBuf[0] = (byte) did;
				while (true) {
					rxBuf = -1;
					if (output == null)
						return;
					output.write(txBuf);
					// Log.info(String.format("Send to dehumidifier %d in room
					// %d :
					// %x",
					// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
					synchronized (lock) {
						lock.wait(TIME_OUT);
					}

					if (rxBuf == PANEL_REP_OK) {
						Log.info(String.format("Panel humid set OK. 0x%x", did, rxBuf));
						break;
					} else {
						Log.debug(String.format("Panel is not live."));
						if (--err <= 0) {
							Log.info("Timeout or data is not expected.");
							return;
						}
					}
				}
			}  else {
				System.out.println("Number of did is amount 1 to 9. Press any key to try again.");
				System.in.read();
				return;
			}
			break;
			
		case 18:
			txBuf[0] = (byte) PANEL_CMD_SET_TIMER;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
//				Log.info(String.format("Send to panel : %x in room %d",
//						((int) txBuf[0] & 0xff), roomIndex));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}
	
				if (rxBuf == PANEL_REP_OK) {
					Log.info(String.format("Panel rep OK. 0x%x", rxBuf));
					break;
				} else {
					Log.debug(String.format("Panel is not live."));
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}
			}
			
			System.out.println("Please enter humid set(0~12):");
			did = userInput.nextInt();
			err = ERR + 1;
			if (did >= 0 && did <= 12){
				txBuf[0] = (byte) did;
				while (true) {
					rxBuf = -1;
					if (output == null)
						return;
					output.write(txBuf);
					// Log.info(String.format("Send to dehumidifier %d in room
					// %d :
					// %x",
					// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
					synchronized (lock) {
						lock.wait(TIME_OUT);
					}

					if (rxBuf == PANEL_REP_OK) {
						Log.info(String.format("Panel timer set OK. 0x%x", did, rxBuf));
						break;
					} else {
						Log.debug(String.format("Panel is not live."));
						if (--err <= 0) {
							Log.info("Timeout or data is not expected.");
							return;
						}
					}
				}
			}  else {
				System.out.println("Number of did is amount 0 to 12. Press any key to try again.");
				System.in.read();
				return;
			}
			break;
			
		case 19:
			System.out.println("Please enter dehumid ID(0~7):");
			did = userInput.nextInt();
			if (did >= 0 && did <= 7) {
				txBuf[0] = (byte) ((byte) (roomIndex << 3) + did);
				Log.info(String.format("txBuf %x", (int)txBuf[0] & 0xff));
				while (true) {
					rxBuf = -1;
					if (output == null)
						return;
					output.write(txBuf);
					// Log.info(String.format("Send to dehumidifier %d in room
					// %d :
					// %x",
					// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
					synchronized (lock) {
						lock.wait(TIME_OUT);
					}

					if (rxBuf == DEHUMID_REP_OK) {
						Log.info(String.format("The dehumidifier %d acks OK. 0x%x", did, rxBuf));
						break;
					} else {
						if (--err <= 0) {
							Log.info("Timeout or data is not expected.");
							return;
						}
					}
				}
			} else {
				System.out.println("Number of did is amount 0 to 7. Press any key to try again.");
				System.in.read();
				return;
			}
			
			txBuf[0] = (byte) DEHUMID_CMD_ON;
			err = ERR + 1;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
				// Log.info(String.format("Send to dehumidifier %d in room %d :
				// %x",
				// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}

				if (rxBuf == DEHUMID_REP_OK) {
					Log.info(String.format("The dehumidifier ON acks OK. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks high temp abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks defrost temp abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks dehumid abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_FAN_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks fan abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks compressor abnormal. 0x%x", rxBuf));
					break;
				} else {
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}

			}
			break;

		case 20:
			System.out.println("Please enter dehumid ID(0~7):");
			did = userInput.nextInt();
			if (did >= 0 && did <= 7) {
				txBuf[0] = (byte) ((byte) (roomIndex << 3) + did);
				while (true) {
					rxBuf = -1;
					if (output == null)
						return;
					output.write(txBuf);
					// Log.info(String.format("Send to dehumidifier %d in room
					// %d :
					// %x",
					// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
					synchronized (lock) {
						lock.wait(TIME_OUT);
					}

					if (rxBuf == DEHUMID_REP_OK) {
						Log.info(String.format("The dehumidifier %d acks OK. 0x%x", did, rxBuf));
						break;
					} else {
						if (--err <= 0) {
							Log.info("Timeout or data is not expected.");
							return;
						}
					}
				}
			} else {
				System.out.println("Number of did is amount 0 to 7. Press any key to try again.");
				System.in.read();
				return;
			}
			
			txBuf[0] = (byte) DEHUMID_CMD_OFF;
			err = ERR + 1;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
				// Log.info(String.format("Send to dehumidifier %d in room %d :
				// %x",
				// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}

				if (rxBuf == DEHUMID_REP_OK) {
					Log.info(String.format("The dehumidifier OFF acks OK. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks high temp abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks defrost temp abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks dehumid abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_FAN_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks fan abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks compressor abnormal. 0x%x", rxBuf));
					break;
				} else {
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}

			}
			break;
			
		case 21:
			System.out.println("Please enter dehumid ID(0~7):");
			did = userInput.nextInt();
			if (did >= 0 && did <= 7) {
				txBuf[0] = (byte) ((byte) (roomIndex << 3) + did);
				while (true) {
					rxBuf = -1;
					if (output == null)
						return;
					output.write(txBuf);
					// Log.info(String.format("Send to dehumidifier %d in room
					// %d :
					// %x",
					// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
					synchronized (lock) {
						lock.wait(TIME_OUT);
					}

					if (rxBuf == DEHUMID_REP_OK) {
						Log.info(String.format("The dehumidifier %d acks OK. 0x%x", did, rxBuf));
						break;
					} else {
						if (--err <= 0) {
							Log.info("Timeout or data is not expected.");
							return;
						}
					}
				}
			} else {
				System.out.println("Number of did is amount 0 to 7. Press any key to try again.");
				System.in.read();
				return;
			}
			
			txBuf[0] = (byte) DEHUMID_CMD_DEHUMID_MODE;
			err = ERR + 1;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
				// Log.info(String.format("Send to dehumidifier %d in room %d :
				// %x",
				// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}

				if (rxBuf == DEHUMID_REP_OK) {
					Log.info(String.format("The dehumidifier dehumid mode acks OK.0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks high temp abnormal.0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks defrost temp abnormal.0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks dehumid abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_FAN_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks fan abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks compressor abnormal. 0x%x", rxBuf));
					break;
				} else {
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}

			}
			break;
			
		case 22:
			System.out.println("Please enter dehumid ID(0~7):");
			did = userInput.nextInt();
			if (did >= 0 && did <= 7) {
				txBuf[0] = (byte) ((byte) (roomIndex << 3) + did);
				while (true) {
					rxBuf = -1;
					if (output == null)
						return;
					output.write(txBuf);
					// Log.info(String.format("Send to dehumidifier %d in room
					// %d :
					// %x",
					// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
					synchronized (lock) {
						lock.wait(TIME_OUT);
					}

					if (rxBuf == DEHUMID_REP_OK) {
						Log.info(String.format("The dehumidifier %d acks OK. 0x%x", did, rxBuf));
						break;
					} else {
						if (--err <= 0) {
							Log.info("Timeout or data is not expected.");
							return;
						}
					}
				}
			} else {
				System.out.println("Number of did is amount 0 to 7. Press any key to try again.");
				System.in.read();
				return;
			}
			
			txBuf[0] = (byte) DEHUMID_CMD_DRY_CLOTHES_MODE;
			err = ERR + 1;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
				// Log.info(String.format("Send to dehumidifier %d in room %d :
				// %x",
				// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}

				if (rxBuf == DEHUMID_REP_OK) {
					Log.info(String.format("The dehumidifier dry clothes mode acks OK.0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks high temp abnormal.0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks defrost temp abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks dehumid abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_FAN_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks fan abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks compressor abnormal. 0x%x", rxBuf));
					break;
				} else {
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}

			}
			break;
			
		case 23:
			System.out.println("Please enter dehumid ID(0~7):");
			did = userInput.nextInt();
			if (did >= 0 && did <= 7) {
				txBuf[0] = (byte) ((byte) (roomIndex << 3) + did);
				while (true) {
					rxBuf = -1;
					if (output == null)
						return;
					output.write(txBuf);
					// Log.info(String.format("Send to dehumidifier %d in room
					// %d :
					// %x",
					// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
					synchronized (lock) {
						lock.wait(TIME_OUT);
					}

					if (rxBuf == DEHUMID_REP_OK) {
						Log.info(String.format("The dehumidifier %d acks OK. 0x%x", did, rxBuf));
						break;
					} else {
						if (--err <= 0) {
							Log.info("Timeout or data is not expected.");
							return;
						}
					}
				}
			} else {
				System.out.println("Number of did is amount 0 to 7. Press any key to try again.");
				System.in.read();
				return;
			}
			
			txBuf[0] = (byte) DEHUMID_CMD_DEHUMIDITY_SET;
			err = ERR + 1;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
				// Log.info(String.format("Send to dehumidifier %d in room %d :
				// %x",
				// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}

				if (rxBuf == DEHUMID_REP_OK) {
					Log.info(String.format("The dehumidifier set dehumidity acks OK.0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks high temp abnormal.0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks defrost temp abnormal.0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks dehumid abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_FAN_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks fan abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks compressor abnormal. 0x%x", rxBuf));
					break;
				} else {
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}

			}
			
			System.out.println("Enter dehumidity set(0~10):");
			set = userInput.nextInt();
			err = ERR + 1;
			if (set >= 0 && set <=10) {
				txBuf[0] = (byte) set;
				while (true) {
					rxBuf = -1;
					if (output == null)
						return;
					output.write(txBuf);
					// Log.info(String.format("Send to dehumidifier %d in room %d :
					// %x",
					// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
					synchronized (lock) {
						lock.wait(TIME_OUT);
					}

					if (rxBuf == DEHUMID_REP_OK) {
						Log.info(String.format("The dehumidifier set dehumidity acks OK.0x%x", rxBuf));
						break;
					} else if (rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL) {
						Log.warn(String.format("The dehumidifier acks high temp abnormal.0x%x", rxBuf));
						break;
					} else if (rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
						Log.warn(String.format("The dehumidifier acks defrost temp abnormal.0x%x", rxBuf));
						break;
					} else if (rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL) {
						Log.warn(String.format("The dehumidifier acks dehumid abnormal. 0x%x", rxBuf));
						break;
					} else if (rxBuf == DEHUMID_REP_FAN_ABNORMAL) {
						Log.warn(String.format("The dehumidifier acks fan abnormal. 0x%x", rxBuf));
						break;
					} else if (rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL) {
						Log.warn(String.format("The dehumidifier acks compressor abnormal. 0x%x", rxBuf));
						break;
					} else {
						if (--err <= 0) {
							Log.info("Timeout or data is not expected.");
							return;
						}
					}

				}
			} else {
				System.out.println("Error! Number amount 0~12. Please Press any key to Restart.");
				System.in.read();
				return;
			}
			
			
			break;
			
		case 24:
			System.out.println("Please enter dehumid ID(0~7):");
			did = userInput.nextInt();
			if (did >= 0 && did <= 7) {
				txBuf[0] = (byte) ((byte) (roomIndex << 3) + did);
				while (true) {
					rxBuf = -1;
					if (output == null)
						return;
					output.write(txBuf);
					// Log.info(String.format("Send to dehumidifier %d in room
					// %d :
					// %x",
					// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
					synchronized (lock) {
						lock.wait(TIME_OUT);
					}

					if (rxBuf == DEHUMID_REP_OK) {
						Log.info(String.format("The dehumidifier %d acks OK. 0x%x", did, rxBuf));
						break;
					} else {
						if (--err <= 0) {
							Log.info("Timeout or data is not expected.");
							return;
						}
					}
				}
			} else {
				System.out.println("Number of did is amount 0 to 7. Press any key to try again.");
				System.in.read();
				return;
			}
			
			txBuf[0] = (byte) DEHUMID_CMD_TIMER_SET;
			err = ERR + 1;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
				// Log.info(String.format("Send to dehumidifier %d in room %d :
				// %x",
				// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}

				if (rxBuf == DEHUMID_REP_OK) {
					Log.info(String.format("The dehumidifier set timer acks OK.0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks high temp abnormal.0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks defrost temp abnormal.0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks dehumid abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_FAN_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks fan abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks compressor abnormal. 0x%x", rxBuf));
					break;
				} else {
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}

			}
			
			System.out.println("Enter timer set(0~12):");
			set = userInput.nextInt();
			err = ERR + 1;
			if (set >= 0 && set <=12) {
				txBuf[0] = (byte) set;
				while (true) {
					rxBuf = -1;
					if (output == null)
						return;
					output.write(txBuf);
					// Log.info(String.format("Send to dehumidifier %d in room %d :
					// %x",
					// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
					synchronized (lock) {
						lock.wait(TIME_OUT);
					}

					if (rxBuf == DEHUMID_REP_OK) {
						Log.info(String.format("The dehumidifier set timer acks OK.0x%x", rxBuf));
						break;
					} else if (rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL) {
						Log.warn(String.format("The dehumidifier acks high temp abnormal.0x%x", rxBuf));
						break;
					} else if (rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
						Log.warn(String.format("The dehumidifier acks defrost temp abnormal.0x%x", rxBuf));
						break;
					} else if (rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL) {
						Log.warn(String.format("The dehumidifier acks dehumid abnormal. 0x%x", rxBuf));
						break;
					} else if (rxBuf == DEHUMID_REP_FAN_ABNORMAL) {
						Log.warn(String.format("The dehumidifier acks fan abnormal. 0x%x", rxBuf));
						break;
					} else if (rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL) {
						Log.warn(String.format("The dehumidifier acks compressor abnormal. 0x%x", rxBuf));
						break;
					} else {
						if (--err <= 0) {
							Log.info("Timeout or data is not expected.");
							return;
						}
					}

				}
			} else {
				System.out.println("Error! Number amount 0~12. Please Press any key to Restart.");
				System.in.read();
				return;
			}
			break;
			
		case 25:
			System.out.println("Please enter dehumid ID(0~7):");
			did = userInput.nextInt();
			if (did >= 0 && did <= 7) {
				txBuf[0] = (byte) ((byte) (roomIndex << 3) + did);
				while (true) {
					rxBuf = -1;
					if (output == null)
						return;
					output.write(txBuf);
					// Log.info(String.format("Send to dehumidifier %d in room
					// %d :
					// %x",
					// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
					synchronized (lock) {
						lock.wait(TIME_OUT);
					}

					if (rxBuf == DEHUMID_REP_OK) {
						Log.info(String.format("The dehumidifier %d acks OK. 0x%x", did, rxBuf));
						break;
					} else {
						if (--err <= 0) {
							Log.info("Timeout or data is not expected.");
							return;
						}
					}
				}
			} else {
				System.out.println("Number of did is amount 0 to 7. Press any key to try again.");
				System.in.read();
				return;
			}
			
			txBuf[0] = (byte) DEHUMID_CMD_DEHUMIDITY_DIGIT_ONES;
			err = ERR + 1;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
				// Log.info(String.format("Send to dehumidifier %d in room %d :
				// %x",
				// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}

				if (rxBuf >= 0 && rxBuf <=10) {
					Log.info(String.format("Digit ones of the dehumitity is %d", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks high temp abnormal.0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks defrost temp abnormal.0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks dehumid abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_FAN_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks fan abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks compressor abnormal. 0x%x", rxBuf));
					break;
				} else {
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}

			}
			break;
			
		case 26:
			System.out.println("Please enter dehumid ID(0~7):");
			did = userInput.nextInt();
			if (did >= 0 && did <= 7) {
				txBuf[0] = (byte) ((byte) (roomIndex << 3) + did);
				while (true) {
					rxBuf = -1;
					if (output == null)
						return;
					output.write(txBuf);
					// Log.info(String.format("Send to dehumidifier %d in room
					// %d :
					// %x",
					// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
					synchronized (lock) {
						lock.wait(TIME_OUT);
					}

					if (rxBuf == DEHUMID_REP_OK) {
						Log.info(String.format("The dehumidifier %d acks OK. 0x%x", did, rxBuf));
						break;
					} else {
						if (--err <= 0) {
							Log.info("Timeout or data is not expected.");
							return;
						}
					}
				}
			} else {
				System.out.println("Number of did is amount 0 to 7. Press any key to try again.");
				System.in.read();
				return;
			}
			
			txBuf[0] = (byte) DEHUMID_CMD_DEHUMIDITY_DIGIT_TENS;
			err = ERR + 1;
			while (true) {
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
				// Log.info(String.format("Send to dehumidifier %d in room %d :
				// %x",
				// did, roomIndex - ROOM_ID_MIN, ((int) txBuf[0] & 0xff)));
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}

				if (rxBuf >= 0 && rxBuf <=10) {
					Log.info(String.format("Digit tens of the dehumitity is %d", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks high temp abnormal.0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks defrost temp abnormal.0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks dehumid abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_FAN_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks fan abnormal. 0x%x", rxBuf));
					break;
				} else if (rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL) {
					Log.warn(String.format("The dehumidifier acks compressor abnormal. 0x%x", rxBuf));
					break;
				} else {
					if (--err <= 0) {
						Log.info("Timeout or data is not expected.");
						return;
					}
				}

			}
			break;
			
		}
		return ;
	}
}
