package edu.ncku.uscc.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.util.ArrayList;
import java.util.List;

public class DehumidRoomController extends Thread implements
		SerialPortEventListener {

	private static final int PANEL_CMD_ONOFF = 0x80;
	private static final int PANEL_CMD_MODE = 0x81;
	private static final int PANEL_CMD_SET = 0x82;
	private static final int PANEL_CMD_HUMID_SET = 0x83;
	private static final int PANEL_CMD_TIMER_SET = 0x84;
	private static final int PANEL_CMD_START = 0x85;
	private static final int PANEL_CMD_SHUTDOWM = 0x86;
	// private static final int PANEL_CMD_TEMP_ABNORMAL = 0x87;
	// private static final int PANEL_CMD_DEFROST = 0x88;
	// private static final int PANEL_CMD_MINUS_TIMER = 0x89;
	private static final int PANEL_CMD_HUMID = 0x68;

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
	// private static final int DEHUMID_CMD_DEHUMIDITY_SET = 0x34;
	// private static final int DEHUMID_CMD_TIMER_SET = 0x35;
	private static final int DEHUMID_CMD_DEHUMIDITY_DIGIT_ONES = 0x38;
	private static final int DEHUMID_CMD_DEHUMIDITY_DIGIT_TENS = 0x39;

	private static final int DEHUMID_REP_OK = 0x55;
	private static final int DEHUMID_REP_HIGH_TEMP_ABNORMAL = 0x56;
	private static final int DEHUMID_REP_DEFROST_TEMP_ABNORMAL = 0x57;

	private static final int DEHUMIDIFIERS_A_ROOM = 8;	

	private static final String LCK_REMOVE_CMD = "sudo rm -f /var/lock/LCK..ttyUSB";

	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 800;
	private static final int ERR = 2;

	/** A synchronization lock */
	private final Object lock = new Object();

	/** The instance of SerialPort class */
	private SerialPort serialPort;
	/** DisconnectListeners List to trigger disconnection event */
	private List<SerialPortDisconnectListener> listeners = new ArrayList<SerialPortDisconnectListener>();
	/** A interface to manager modbus dataStore for DehumidSystem */
	private DataStoreManager dataStoreManager;
	/** Buffered input stream from the port */
	private InputStream input;
	/** The output stream to the port */
	private OutputStream output;
	/** The buff receives byte data from serial port interface */
	private Byte rxBuf;

	/** The index of the room after room index scan */
	private int roomIndex;
	/**  */
	private boolean init = true;

	/**
	 * Constructor
	 * 
	 * @param slave
	 * @param serialPort
	 */
	public DehumidRoomController(DataStoreManager dataStoreManager,
			SerialPort serialPort) {
		super();
		this.dataStoreManager = dataStoreManager;
		this.serialPort = serialPort;
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
		try {
			roomIndex = roomIndexScan();
			if (roomIndex < 0) {
				throw new Exception("Could not scan any room index for "
						+ serialPort.getName());
			}
			while (output != null) {
				synPanel(roomIndex);
				for (int did = 0; did < DEHUMIDIFIERS_A_ROOM; did++) {
					synDehumidifier(roomIndex, did);
				}
			}
		} catch (IOException e) {
			Log.error(e, e);
			for (SerialPortDisconnectListener listener : listeners) {
				listener.onDisconnectEvent(serialPort.getName());
			}
		} catch (Exception e) {
			Log.error(e, e);
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
		
		if(input != null){
			input.close();
			input = null;
		}
		
		if(output != null){
			output.close();
			output = null;
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		synchronized (lock) {
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
					lock.notify();

				} catch (IOException e) {
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
				} catch (Exception e) {
					Log.error(e, e);
				}
			}
		}
	}

	/**
	 * Try to scan the index of the room for the SerialPort
	 * 
	 * @return Index(>=0) of the room, return -1 by fail
	 * @throws IOException
	 * @throws Exception
	 */
	private int roomIndexScan() throws IOException, Exception {
		// TODO Auto-generated method stub
		byte[] txBuf = new byte[1];
		final int ROOM_MAX = 4, DEHUMIDIFIER_MAX = 8;

		for (int roomScanIndex = 0; roomScanIndex < ROOM_MAX; roomScanIndex++) {
			for (int did = 0; did < DEHUMIDIFIER_MAX; did++) {
				txBuf[0] = (byte) ((roomScanIndex << 3) + did);
				rxBuf = -1;
				synchronized (lock) {
					for(int usbIndex = 0;usbIndex < 4;Runtime.getRuntime().exec(LCK_REMOVE_CMD  + usbIndex));
					if (output == null)
						return -1;
					output.write(txBuf);
					Log.info(String.format("Scan roomIndex : %x in %s",
							((int) txBuf[0] & 0xff), serialPort.getName()));
					lock.wait(TIME_OUT);
				}

				if (rxBuf < 0) {
					continue;
				} else if (rxBuf == DEHUMID_REP_OK
						|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
						|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
					Log.info("Scan room index : " + roomScanIndex);
					return roomScanIndex;
				}
			}
		}

		return -1;
	}

	/**
	 * To synchronize data between panel status and modbus dataStore
	 * 
	 * @param roomIndex
	 *            - The panel belongs
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void synPanel(int roomIndex) throws IOException, Exception {

		byte[] txBuf = new byte[1];
		byte err = ERR;
		IReferenceable panel = dataStoreManager.getPanel(roomIndex);

		if (!init && dataStoreManager.isPanelONOFFChange(roomIndex)) {
			txBuf[0] = (panel.isOn()) ? (byte) PANEL_CMD_START
					: (byte) PANEL_CMD_SHUTDOWM;
		} else {
			// ask panel it is on or off
			txBuf[0] = (byte) PANEL_CMD_ONOFF;
		}
		while (true) {
			rxBuf = -1;
			synchronized (lock) {
				if (output == null)
					return;
				output.write(txBuf);
				// Log.info(String.format("Send to panel : %x in room %d",
				// ((int) txBuf[0] & 0xff), roomIndex));
				lock.wait(TIME_OUT);
			}

			if (rxBuf == PANEL_REP_ON) {
				panel.setOn(true);
				panel.setLive(true);
				Log.info(String.format("Panel %d is ON.", roomIndex));
				break;
			} else if (rxBuf == PANEL_REP_OFF) {
				panel.setOn(false);
				panel.setLive(true);
				Log.info(String.format("Panel %d is OFF.", roomIndex));
				break;
			} else {
				panel.setLive(false);
				Log.warn(String.format("Panel %d is not live.", roomIndex));
				if (--err <= 0) {
					Log.debug("Timeout or data is not expected.");
					return;
				}
			}
		}

		// ask panel its mode
		txBuf[0] = (byte) PANEL_CMD_MODE;
		while (panel.isOn()) {
			rxBuf = -1;
			synchronized (lock) {
				if (output == null)
					return;
				output.write(txBuf);
				// Log.info(String.format("Send to panel : %x in room %d",
				// ((int) txBuf[0] & 0xff), roomIndex));
				lock.wait(TIME_OUT);
			}

			if (rxBuf == PANEL_REP_DEHUMID) {
				panel.setModeDehumid(true);
				panel.setModeDry(false);
				panel.setLive(true);
				Log.info(String.format("Panel %d is dehumid mode.", roomIndex));
				break;
			} else if (rxBuf == PANEL_REP_DRY_CLOTHES) {
				panel.setModeDehumid(false);
				panel.setModeDry(true);
				panel.setLive(true);
				Log.info(String.format("Panel %d is dry clothes mode.",
						roomIndex));
				break;
			} else {
				panel.setLive(false);
				Log.warn(String.format("Panel %d is not live.", roomIndex));
				if (--err <= 0) {
					Log.debug("Timeout or data is not expected.");
					return;
				}
			}
		}

		// ask panel its set status
		txBuf[0] = (byte) PANEL_CMD_SET;
		while (panel.isOn()) {
			rxBuf = -1;
			synchronized (lock) {
				if (output == null)
					return;
				output.write(txBuf);
				// Log.info(String.format("Send to panel : %x in room %d",
				// ((int) txBuf[0] & 0xff), roomIndex));
				lock.wait(TIME_OUT);
			}

			if (rxBuf == PANEL_REP_NO_SET) {
				panel.setHumidSet(false);
				panel.setTimerSet(false);
				panel.setLive(true);
				Log.info(String.format("Panel %d hasn't any set.", roomIndex));
				break;
			} else if (rxBuf == PANEL_REP_HUMID_SET) {
				panel.setHumidSet(true);
				panel.setTimerSet(false);
				panel.setLive(true);
				Log.info(String.format("Panel %d has humidity set.", roomIndex));
				break;
			} else if (rxBuf == PANEL_REP_TIMER_SET) {
				panel.setHumidSet(false);
				panel.setTimerSet(true);
				panel.setLive(true);
				Log.info(String.format("Panel %d has timer set.", roomIndex));
				break;
			} else {
				panel.setLive(false);
				Log.warn(String.format("Panel %d is not live.", roomIndex));
				if (--err <= 0) {
					Log.debug("Timeout or data is not expected.");
					return;
				}
			}
		}

		// ask panel its humidity set
		txBuf[0] = (byte) PANEL_CMD_HUMID_SET;
		while (panel.isOn()) {
			rxBuf = -1;
			synchronized (lock) {
				if (output == null)
					return;
				output.write(txBuf);
				// Log.info(String.format("Send to panel : %x in room %d",
				// ((int) txBuf[0] & 0xff), roomIndex));
				lock.wait(TIME_OUT);
			}

			if (rxBuf >= 0) {
				panel.setHumidSet((int) rxBuf);
				panel.setLive(true);
				Log.info(String.format("The humidity set of Panel %d is %d.",
						roomIndex, (int) rxBuf));
				break;
			} else {
				panel.setLive(false);
				Log.warn(String.format("Panel %d is not live.", roomIndex));
				if (--err <= 0) {
					Log.debug("Timeout or data is not expected.");
					return;
				}
			}
		}

		// ask panel its timer
		txBuf[0] = (byte) PANEL_CMD_TIMER_SET;
		while (panel.isOn()) {
			rxBuf = -1;
			synchronized (lock) {
				if (output == null)
					return;
				output.write(txBuf);
				// Log.info(String.format("Send to panel : %x in room %d",
				// ((int) txBuf[0] & 0xff), roomIndex));
				lock.wait(TIME_OUT);
			}

			if (rxBuf >= 0) {
				panel.setTimerSet(rxBuf);
				panel.setLive(true);
				Log.info(String.format("The timer set of Panel %d is %d.",
						roomIndex, rxBuf));
				break;
			} else {
				panel.setLive(false);
				Log.warn(String.format("Panel %d is not live.", roomIndex));
				if (--err <= 0) {
					Log.debug("Timeout or data is not expected.");
					return;
				}
			}
		}

		// synchronize humidity in the room
		int humid = 0, avgHumid = 0;
		for (int did = 0; did < DEHUMIDIFIERS_A_ROOM; did++) {
			IReferenceable dehumidifier = dataStoreManager.getDehumidifier(
					roomIndex, did);
			if (dehumidifier.isLive()) {
				Log.debug(String.format(
						"The humidity for dehumidifier %d in room %d is %d.",
						did, roomIndex, dehumidifier.getHumid()));
				humid += dehumidifier.getHumid();
				avgHumid++;
			}
		}
		if (avgHumid > 0) {
			avgHumid = humid / avgHumid;
		} else {
			return;
		}
		if (humid >= 40 && humid <= 90) {
			txBuf[0] = (byte) (PANEL_CMD_HUMID + avgHumid);
			while (true) {
				rxBuf = -1;
				synchronized (lock) {
					if (output == null)
						return;
					output.write(txBuf);
					Log.info(String.format("Send to panel : %x in room %d",
							((int) txBuf[0] & 0xff), roomIndex));
					lock.wait(TIME_OUT);
				}

				if (rxBuf == PANEL_REP_OK) {
					Log.info(String.format("The humidity of Panel %d is %d.",
							roomIndex, avgHumid));
					panel.setHumid(avgHumid);
					break;
				} else {
					panel.setLive(false);
					Log.warn(String.format("Panel %d is not live.", roomIndex));
					if (--err <= 0) {
						Log.debug("Timeout or data is not expected.");
						return;
					}
				}
			}
		} else {
			Log.debug("Humidity for panel is not in range : " + humid);
		}

	}

	/**
	 * Before synchronizing panels, it is necessary to notify the specific
	 * dehumidifier
	 * 
	 * @param roomIndex
	 *            - The dehumidifier belongs
	 * @param did
	 *            - Device ID
	 * @return Success or not
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private boolean notifyDeviceID(int roomIndex, int did) throws IOException,
			InterruptedException {

		byte[] txBuf = new byte[1];
		txBuf[0] = (byte) ((roomIndex << 3) + did);
		IReferenceable panel = dataStoreManager.getDehumidifier(roomIndex, did);

		rxBuf = -1;
		synchronized (lock) {
			if (output == null)
				return false;
			output.write(txBuf);
			Log.info(String.format(
					"Send Device ID to dehumidifier %d in room %d : %x", did,
					roomIndex, ((int) txBuf[0] & 0xff)));
			lock.wait(TIME_OUT);
		}

		switch (rxBuf) {
		case DEHUMID_REP_OK:
			panel.setHighTempWarn(false);
			panel.setTempWarn(false);
			panel.setLive(true);
			// Log.info(String.format(
			// "The dehumidifier %d in room %d acks OK.", did, roomIndex));
			return true;
		case DEHUMID_REP_HIGH_TEMP_ABNORMAL:
			panel.setHighTempWarn(true);
			panel.setLive(true);
			Log.info(String.format(
					"The dehumidifier %d in room %d acks high temp abnormal.",
					did, roomIndex));
			return true;
		case DEHUMID_REP_DEFROST_TEMP_ABNORMAL:
			panel.setTempWarn(true);
			panel.setLive(true);
			Log.info(String
					.format("The dehumidifier %d in room %d acks defrost temp abnormal.",
							did, roomIndex));
			return true;
		default:
			panel.setLive(false);
			return false;
		}
	}

	/**
	 * To synchronize data between dehumidifier status and modbus dataStore by
	 * panel status
	 * 
	 * @param roomIndex
	 *            - The dehumidifier belongs
	 * @param did
	 *            - Device ID
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void synDehumidifier(int roomIndex, int did) throws IOException,
			InterruptedException {

		byte[] txBuf = new byte[1];
		byte err = ERR;
		IReferenceable dehumidifier = dataStoreManager.getDehumidifier(
				roomIndex, did);
		IReferenceable panel = dataStoreManager.getPanel(roomIndex);

		// synchronize onoff status between panel and dehumidifier
		while (true) {

			if (!notifyDeviceID(roomIndex, did)) {
				if (--err <= 0) {
					Log.debug("Timeout or data is not expected.");
					return;
				}
				continue;
			}

			rxBuf = -1;
			boolean on = panel.isOn();
			txBuf[0] = (on) ? (byte) DEHUMID_CMD_ON : (byte) DEHUMID_CMD_OFF;
			synchronized (lock) {
				if (output == null)
					return;
				output.write(txBuf);
				Log.info(String.format(
						"Send to dehumidifier %d in room %d : %x", did,
						roomIndex, ((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf == DEHUMID_REP_OK
					|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
					|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
				dehumidifier.setOn(on);
				Log.info(String.format(
						"Dehumidifier %d in room %d power sync.", did,
						roomIndex));
				break;
			} else {
				dehumidifier.setLive(false);
				if (--err <= 0) {
					Log.debug("Timeout or data is not expected.");
					return;
				}
			}
		}

		// synchronize dehumid mode between panel and dehumidifier
		while (dehumidifier.isOn()) {

			if (!notifyDeviceID(roomIndex, did)) {
				if (--err <= 0) {
					Log.debug("Timeout or data is not expected.");
					return;
				}
				continue;
			}

			rxBuf = -1;
			boolean dehumidMode = panel.isModeDehumid();
			if (dehumidMode) {
				txBuf[0] = (byte) DEHUMID_CMD_DEHUMID_MODE;
			} else {
				break;
			}
			synchronized (lock) {
				if (output == null)
					return;
				output.write(txBuf);
				Log.info(String.format(
						"Send to dehumidifier %d in room %d : %x", did,
						roomIndex, ((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf == DEHUMID_REP_OK
					|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
					|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
				dehumidifier.setModeDehumid(true);
				dehumidifier.setModeDry(false);
				Log.info(String.format(
						"Dehumidifier %d in room %d dehumid mode set.", did,
						roomIndex));
				break;
			} else {
				dehumidifier.setLive(false);
				if (--err <= 0) {
					Log.debug("Timeout or data is not expected.");
					return;
				}
			}
		}

		// synchronize dry clothes mode between panel and dehumidifier
		while (dehumidifier.isOn()) {

			if (!notifyDeviceID(roomIndex, did)) {
				if (--err <= 0) {
					Log.debug("Timeout or data is not expected.");
					return;
				}
				continue;
			}

			rxBuf = -1;
			boolean dryMode = panel.isModeDry();
			if (dryMode) {
				txBuf[0] = (byte) DEHUMID_CMD_DRY_CLOTHES_MODE;
			} else {
				break;
			}
			synchronized (lock) {
				if (output == null)
					return;
				output.write(txBuf);
				Log.info(String.format(
						"Send to dehumidifier %d in room %d : %x", did,
						roomIndex, ((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf == DEHUMID_REP_OK
					|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
					|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
				dehumidifier.setModeDry(true);
				dehumidifier.setModeDehumid(false);
				Log.info(String.format(
						"Dehumidifier %d in room %d dry mode set.", did,
						roomIndex));
				break;
			} else {
				dehumidifier.setLive(false);
				if (--err <= 0) {
					Log.debug("Timeout or data is not expected.");
					return;
				}
			}
		}

		// synchronize humidity set between panel and dehumidifier
		// while (true) {
		//
		// if (!notifyDeviceID(roomIndex, did)) {
		// if (--err <= 0) {
		// Log.error("Timeout or data is not expected.");
		// return;
		// }
		// continue;
		// }
		//
		// rxBuf = -1;
		// txBuf[0] = (byte) DEHUMID_CMD_DEHUMIDITY_SET;
		// synchronized (lock) {
		// if (output == null)
		// return;
		// output.write(txBuf);
		// Log.info(String.format(
		// "Send to dehumidifier %d in room %d : %x", did,
		// roomIndex, ((int) txBuf[0] & 0xff)));
		// lock.wait(TIME_OUT);
		// }
		//
		// if (rxBuf == DEHUMID_REP_OK
		// || rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
		// || rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
		// rxBuf = -1;
		// txBuf[0] = (byte) panel.getHumidSet();
		// synchronized (lock) {
//		 if (output == null)
//		 return;
		// output.write(txBuf);
		// Log.info(String.format(
		// "Send to dehumidifier %d in room %d : %x", did,
		// roomIndex, ((int) txBuf[0] & 0xff)));
		// lock.wait(TIME_OUT);
		// }
		// if (rxBuf == DEHUMID_REP_OK
		// || rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
		// || rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
		// dehumidifier.setHumidSet(txBuf[0]);
		// Log.info(String.format(
		// "Dehumidifier %d in room %d humidity set sync.",
		// did, roomIndex));
		// break;
		// } else {
		// dehumidifier.setLive(false);
		// if (--err <= 0) {
		// Log.error("Timeout or data is not expected.");
		// return;
		// }
		// }
		// } else {
		// dehumidifier.setLive(false);
		// if (--err <= 0) {
		// Log.error("Timeout or data is not expected.");
		// return;
		// }
		// }
		// }

		// synchronize timer set between panel and dehumidifier
//		while (true) {
//
//			if (!notifyDeviceID(roomIndex, did)) {
//				if (--err <= 0) {
//					Log.error("Timeout or data is not expected.");
//					return;
//				}
//				continue;
//			}
//
//			rxBuf = -1;
//			txBuf[0] = (byte) DEHUMID_CMD_TIMER_SET;
//			synchronized (lock) {
//				if (output == null)
//					 return;
//				output.write(txBuf);
//				Log.info(String.format(
//						"Send to dehumidifier %d in room %d : %x", did,
//						roomIndex, ((int) txBuf[0] & 0xff)));
//				lock.wait(TIME_OUT);
//			}
//
//			if (rxBuf == DEHUMID_REP_OK
//					|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
//					|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
//				rxBuf = -1;
//				txBuf[0] = (byte) panel.getTimerSet();
//				synchronized (lock) {
//					if (output == null)
//						 return;
//					output.write(txBuf);
//					Log.info(String.format(
//							"Send to dehumidifier %d in room %d : %x", did,
//							roomIndex, ((int) txBuf[0] & 0xff)));
//					lock.wait(TIME_OUT);
//				}
//
//				if (rxBuf == DEHUMID_REP_OK
//						|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
//						|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
//					dehumidifier.setTimerSet(txBuf[0]);
//					Log.info(String.format(
//							"Dehumidifier %d in room %d timer set sync.", did,
//							roomIndex));
//					break;
//				} else {
//					dehumidifier.setLive(false);
//					if (--err <= 0) {
//						Log.error("Timeout or data is not expected.");
//						return;
//					}
//				}
//			} else {
//				dehumidifier.setLive(false);
//				if (--err <= 0) {
//					Log.error("Timeout or data is not expected.");
//					return;
//				}
//			}
//		}

		// ask the real humidity of the dehumidifier
		while (true) {

			if (!notifyDeviceID(roomIndex, did)) {
				if (--err <= 0) {
					Log.debug("Timeout or data is not expected.");
					return;
				}
				continue;
			}

			rxBuf = -1;
			txBuf[0] = (byte) DEHUMID_CMD_DEHUMIDITY_DIGIT_ONES;
			int humidity = 0;
			synchronized (lock) {
				if (output == null)
					return;
				output.write(txBuf);
				Log.info(String.format(
						"Send to dehumidifier %d in room %d : %x", did,
						roomIndex, ((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf >= 0) {
				humidity += rxBuf;
				Log.debug(String
						.format("The humidity in ones for dehumidifier %d in room %d is %d.",
								did, roomIndex, rxBuf));
			} else {
				dehumidifier.setLive(false);
				if (--err <= 0) {
					Log.debug("Timeout or data is not expected.");
					return;
				}
				continue;
			}

			if (!notifyDeviceID(roomIndex, did)) {
				if (--err <= 0) {
					Log.debug("Timeout or data is not expected.");
					return;
				}
				continue;
			}

			rxBuf = -1;
			txBuf[0] = (byte) DEHUMID_CMD_DEHUMIDITY_DIGIT_TENS;
			synchronized (lock) {
				output.write(txBuf);
				Log.info(String.format(
						"Send to dehumidifier %d in room %d : %x", did,
						roomIndex, ((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf >= 0) {
				Log.debug(String
						.format("The humidity in tens for dehumidifier %d in room %d is %d.",
								did, roomIndex, rxBuf));
				humidity += rxBuf * 10;
				dehumidifier.setHumid(humidity);
				Log.info(String.format(
						"Dehumidifier %d in room %d humidity : %d", did,
						roomIndex, humidity));
				break;
			} else {
				dehumidifier.setLive(false);
				if (--err <= 0) {
					Log.debug("Timeout or data is not expected.");
					return;
				}
			}
		}
	}
}
