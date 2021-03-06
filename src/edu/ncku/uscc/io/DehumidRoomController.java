package edu.ncku.uscc.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.DataStoreManager.Device;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;
import edu.ncku.uscc.util.panelTimerThread;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.util.ArrayList;
import java.util.List;

public class DehumidRoomController extends Thread implements
		SerialPortEventListener {

	private static final String LCK_REMOVE_CMD = "sudo rm -f /var/lock/LCK..ttyUSB";

	private static final int PANEL_CMD_ONOFF = 0x80;
	private static final int PANEL_CMD_MODE = 0x81;
//	private static final int PANEL_CMD_SET = 0x82;
	private static final int PANEL_CMD_HUMID_SET = 0x83;
	private static final int PANEL_CMD_TIMER_SET = 0x84;
	private static final int PANEL_CMD_START = 0x85;
	private static final int PANEL_CMD_SHUTDOWM = 0x86;
	private static final int PANEL_CMD_TEMP_ABNORMAL = 0x87;
	private static final int PANEL_CMD_DEFROST_TEMP_ABNORMAL = 0x88;
	private static final int PANEL_CMD_MINUS_TIMER = 0x89;
	private static final int PANEL_CMD_DEHUMID_MODE = 0x8A;
	private static final int PANEL_CMD_DRYCLOTHES_MODE = 0x8B;
	private static final int PANEL_CMD_MULTIPLE_ABNORMAL = 0x8C;
	private static final int PANEL_CMD_HUMID_ABNORMAL = 0x8D;
	private static final int PANEL_CMD_FAN_ABNORMAL = 0x8E;
	private static final int PANEL_CMD_COMPRESSOR_ABNORMAL = 0x8F;
	private static final int PANEL_CMD_SETTING_HUMID = 0xCE;
	private static final int PANEL_CMD_SETTING_TIMER = 0xCF;
	private static final int PANEL_CMD_HUMID = 0x68;

	private static final int PANEL_REP_ON = 0x30;
	private static final int PANEL_REP_OFF = 0x31;
	private static final int PANEL_REP_DEHUMID = 0x32;
	private static final int PANEL_REP_DRY_CLOTHES = 0x33;
//	private static final int PANEL_REP_NO_SET = 0x34;
//	private static final int PANEL_REP_HUMID_SET = 0x35;
//	private static final int PANEL_REP_TIMER_SET = 0x36;
	private static final int PANEL_REP_OK = 0x55;

	private static final int DEHUMID_CMD_ON = 0x30;
	private static final int DEHUMID_CMD_OFF = 0x31;
	private static final int DEHUMID_CMD_DEHUMID_MODE = 0x32;
	private static final int DEHUMID_CMD_DRY_CLOTHES_MODE = 0x33;
	private static final int DEHUMID_CMD_DEHUMIDITY_SET = 0x34;
//	private static final int DEHUMID_CMD_TIMER_SET = 0x35;
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
	private static final int TIME_OUT = 400;
	private static final int ERR = 3;

	private static final int INITIAL_RATE = 100;
	private static final int RATE_CONSTANT = 3;
	private static final double DROP_RATIO = 0.77;

	private static final int MIN_HUMIDITY = 40;
	private static final int MAX_HUMIDITY = 90;

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

	/** The panel timer */
	private panelTimerThread panelTimerThread = new panelTimerThread();

	/** The index of the room after room index scan */
	private int roomIndex;
	/** Initial flag */
	private boolean init = true;

	/** These integers are used to count check rate */
	private int[] checkRates = new int[DEHUMIDIFIERS_A_ROOM];

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
		int checkRate;

		try {
			roomIndex = scanRoomIndex();
			if (roomIndex < ROOM_ID_MIN) {
				throw new Exception("Could not scan any room index for "
						+ serialPort.getName());
			}
			while (output != null) {

				synPanel(roomIndex);

				for (int did = 0; did < DEHUMIDIFIERS_A_ROOM; did++) {
					checkRate = (int) (Math.random() * 100);
					if (checkRate <= checkRates[did]) {
						synDehumidifier(roomIndex, did);
					}
				}

				if (init)
					init = false;
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
	 * To drop the check rate for one of the dehumidifiers.
	 * 
	 * @param originCheckRate
	 * @return The value after drop action
	 */
	private int drop(int originCheckRate) {
		return (int) (originCheckRate * DROP_RATIO + RATE_CONSTANT);
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
		byte err = ERR + 1;
		int offsetRoomIndex = roomIndex - ROOM_ID_MIN;
		Device panel = dataStoreManager.getPanel(offsetRoomIndex);

		while (true) {
			if (!init && dataStoreManager.isPanelONOFFChange(offsetRoomIndex)) {
				txBuf[0] = (panel.isOn()) ? (byte) PANEL_CMD_START
						: (byte) PANEL_CMD_SHUTDOWM;
			} else {
				// ask panel it is on or off
				txBuf[0] = (byte) PANEL_CMD_ONOFF;
			}
			rxBuf = -1;
			if (output == null)
				return;
			output.write(txBuf);
			synchronized (lock) {
				lock.wait(TIME_OUT);
			}

			if (rxBuf == PANEL_REP_ON) {
				panel.setOn(true);
				panel.setLive(true);
				Log.info(String.format("Panel %d is ON.", offsetRoomIndex));
				break;
			} else if (rxBuf == PANEL_REP_OFF) {
				panel.setOn(false);
				panel.setLive(true);
				Log.info(String.format("Panel %d is OFF.", offsetRoomIndex));
				break;
			} else {
				if (--err <= 0) {
					panel.setLive(false);
					Log.warn(String.format("Panel %d is not live.",
							offsetRoomIndex));
					return;
				}
			}
		}

		if (!panel.isOn())
			return;

		// ask panel its mode
		while (panel.isOn()) {
			if (dataStoreManager.isPanelModeChange(offsetRoomIndex)) {
				if (panel.isModeDehumid()) {
					txBuf[0] = (byte) PANEL_CMD_DEHUMID_MODE;
				} else if (panel.isModeDry()) {
					txBuf[0] = (byte) PANEL_CMD_DRYCLOTHES_MODE;
				}
			} else {
				txBuf[0] = (byte) PANEL_CMD_MODE;
			}
			rxBuf = -1;
			if (output == null)
				return;
			output.write(txBuf);
			synchronized (lock) {
				lock.wait(TIME_OUT);
			}

			if (rxBuf == PANEL_REP_DEHUMID) {
				panel.setModeDehumid(true);
				panel.setModeDry(false);
				Log.info(String.format("Panel %d is dehumid mode.",
						offsetRoomIndex));
				break;
			} else if (rxBuf == PANEL_REP_DRY_CLOTHES) {
				panel.setModeDehumid(false);
				panel.setModeDry(true);
				Log.info(String.format("Panel %d is dry clothes mode.",
						offsetRoomIndex));
				break;
			} else if (rxBuf == PANEL_REP_OK) {
				panel.setModeDehumid((txBuf[0] == (byte) PANEL_CMD_DEHUMID_MODE));
				panel.setModeDry((txBuf[0] == (byte) PANEL_CMD_DRYCLOTHES_MODE));
				Log.info(String.format("Panel %d changes mode.",
						offsetRoomIndex));
				break;
			} else {
				if (--err <= 0) {
					return;
				}
			}
		}

		// ask panel its humidity set
		while (panel.isOn()) {
			if (dataStoreManager.isPanelDehumiditySetChange(offsetRoomIndex)) {
				txBuf[0] = (byte) PANEL_CMD_SETTING_HUMID;
			} else {
				txBuf[0] = (byte) PANEL_CMD_HUMID_SET;
			}

			rxBuf = -1;
			if (output == null)
				return;
			output.write(txBuf);
			synchronized (lock) {
				lock.wait(TIME_OUT);
			}

			if (rxBuf == PANEL_REP_OK) {
				Log.info(String.format(
						"Start to change set of humidity of Panel %d",
						offsetRoomIndex, (int) rxBuf));
				int humidSet = panel.getHumidSet();
				txBuf[0] = (byte) humidSet;
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}

				if (rxBuf == PANEL_REP_OK) {
					panel.setHumidSet(true);
					panel.setHumidSetValue(humidSet);
					Log.info(String.format(
							"Change set of humidity of Panel %d success",
							offsetRoomIndex, (int) rxBuf));
					break;
				}
			} else if (rxBuf >= 0) {
				if(panel.getHumidSet() != rxBuf){
					panel.setHumidSet(true);
					panel.setHumidSetValue((int) rxBuf);
					Log.info(String.format("The humidity set of Panel %d is %d.",
							offsetRoomIndex, (int) rxBuf));
				}else{
					panel.setHumidSet(false);
				}
				break;
			} else {
				if (--err <= 0) {
					return;
				}
			}
		}

		// ask panel its timer
		while (panel.isOn()) {
			if (dataStoreManager.isPanelTimerSetChange(offsetRoomIndex)) {
				txBuf[0] = (byte) PANEL_CMD_SETTING_TIMER;
			} else {
				txBuf[0] = (byte) PANEL_CMD_TIMER_SET;
			}

			rxBuf = -1;
			if (output == null)
				return;
			output.write(txBuf);
			synchronized (lock) {
				lock.wait(TIME_OUT);
			}

			if (rxBuf == PANEL_REP_OK) {
				Log.info(String.format(
						"Start to change set of timer of Panel %d",
						offsetRoomIndex, (int) rxBuf));
				int timeSet = panel.getTimerSet();
				txBuf[0] = (byte) timeSet;
				rxBuf = -1;
				if (output == null)
					return;
				output.write(txBuf);
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}

				if (rxBuf == PANEL_REP_OK) {
					panel.setTimerSetValue(timeSet);
					Log.info(String.format(
							"Change set of timer of Panel %d success",
							offsetRoomIndex, (int) rxBuf));
					break;
				}
			} else if (rxBuf >= 0) {
				panel.setTimerSetValue(rxBuf);
				Log.info(String.format("The timer set of Panel %d is %d.",
						offsetRoomIndex, rxBuf));
				break;
			} else {
				if (--err <= 0) {
					return;
				}
			}
		}

		// set panel timer
		if (panel.getTimerSet() > 0) {
			if (panelTimerThread.getBackupTimerSet() != panel.getTimerSet()) {
				panelTimerThread.newScheduleThread(panel.getTimerSet());
			} else if (panelTimerThread.getTimerMinusOneFlag()) {
				while (panel.isOn()) {
					txBuf[0] = (byte) PANEL_CMD_MINUS_TIMER;
					rxBuf = -1;
					if (output == null)
						return;
					output.write(txBuf);
					synchronized (lock) {
						lock.wait(TIME_OUT);
					}

					if (rxBuf == PANEL_REP_OK) {
						panelTimerThread.backpuTimerMinusOne();
						panel.setTimerSetValue(panelTimerThread
								.getBackupTimerSet());
						panelTimerThread.setTimerMinusOneFlag(false);
						Log.info(String
								.format("The timer set of Panel %d minus one hour. : %d",
										offsetRoomIndex, panel.getTimerSet()));
						break;
					} else {
						if (--err <= 0) {
							return;
						}
					}
				}

				// if timer countdown finishing close the panel
				if (panelTimerThread.getTimerCountdownFinishingFlag()) {
					while (panel.isOn()) {
						txBuf[0] = (byte) PANEL_CMD_SHUTDOWM;
						rxBuf = -1;
						if (output == null)
							return;
						output.write(txBuf);
						synchronized (lock) {
							lock.wait(TIME_OUT);
						}

						if (rxBuf == DEHUMID_CMD_OFF) {
							panelTimerThread
									.setTimerCountdownFinishingFlag(false);
							Log.info(String.format("Panel %d if OFF.",
									offsetRoomIndex));
							break;
						} else {							
							if (--err <= 0) {
								return;
							}
						}
					}
				}
			}
		}

		IReferenceable dehumidifier;

		// synchronize errors
		while (panel.isOn()) {

			byte countAbnormal = 0;
			for (int did = 0; did < DEHUMIDIFIERS_A_ROOM; did++) {
				dehumidifier = dataStoreManager.getDehumidifier(
						offsetRoomIndex, did);
				if (dehumidifier.isHighTempWarning()) {
					txBuf[0] = (byte) PANEL_CMD_TEMP_ABNORMAL;
					countAbnormal++;
				} else if (dehumidifier.isDeforstTempWarning()) {
					txBuf[0] = (byte) PANEL_CMD_DEFROST_TEMP_ABNORMAL;
					countAbnormal++;
				} else if (dehumidifier.isHumidWarning()) {
					txBuf[0] = (byte) PANEL_CMD_HUMID_ABNORMAL;
					countAbnormal++;
				} else if (dehumidifier.isFanWarning()) {
					txBuf[0] = (byte) PANEL_CMD_FAN_ABNORMAL;
					countAbnormal++;
				} else if (dehumidifier.isCompressorWarning()) {
					txBuf[0] = (byte) PANEL_CMD_COMPRESSOR_ABNORMAL;
					countAbnormal++;
				}
			}

			if (countAbnormal == 0)
				break;
			else if (countAbnormal > 1)
				txBuf[0] = (byte) PANEL_CMD_MULTIPLE_ABNORMAL;
			
			Log.debug("Abnormal!");

			rxBuf = -1;
			if (output == null)
				return;
			output.write(txBuf);
			synchronized (lock) {
				lock.wait(TIME_OUT);
			}

			if (rxBuf == PANEL_REP_OK) {
				Log.debug(String.format("Panel %d is abnormal.",
						offsetRoomIndex));
				break;
			} else {
				if (--err <= 0) {
					return;
				}
			}
		}

		// synchronize humidity in the room
		int humid = 0, avgHumid = 0;

		/* count the sum and the avg of humidity. */
		if (panel.isOn()) {
			
			for (int did = 0; did < DEHUMIDIFIERS_A_ROOM; did++) {
				dehumidifier = dataStoreManager.getDehumidifier(
						offsetRoomIndex, did);
				if (dehumidifier.isLive()) {					
					humid += dehumidifier.getHumid();
					avgHumid++;
				}
			}
			
			if (avgHumid > 0) {
				avgHumid = humid / avgHumid;
			} 
			
			if (avgHumid < MIN_HUMIDITY || avgHumid > MAX_HUMIDITY) {
				Log.debug("Humidity for panel is not in range : " + avgHumid);
				int sum = 0, num = 0;
				for (int did = 0; did < DEHUMIDIFIERS_A_ROOM; did++) {
					dehumidifier = dataStoreManager.getDehumidifier(
							offsetRoomIndex, did);
					if (dehumidifier.isLive()) {
						Log.warn(String
								.format("The humidity for dehumidifier %d in room %d is %d.",
										did, roomIndex, dehumidifier.getHumid()));
						sum += dehumidifier.getHumid();
						num++;
					}
				}
				Log.debug("The humidity sum of these dehumidifiers : " + sum);
				Log.debug("The num of these dehumidifiers : " + num);
				return;
			}
		} else {
			return;
		}
		
		txBuf[0] = (byte) (PANEL_CMD_HUMID + avgHumid);
		while (panel.isOn()) {
			rxBuf = -1;
			if (output == null)
				return;
			output.write(txBuf);
			synchronized (lock) {
				lock.wait(TIME_OUT);
			}

			if (rxBuf == PANEL_REP_OK) {
				Log.info(String.format("The humidity of Panel %d is %d.",
						offsetRoomIndex, avgHumid));
				panel.setHumid(avgHumid);
				break;
			} else {
				if (--err <= 0) {
					return;
				}
			}
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
		int offsetRoomIndex = roomIndex - ROOM_ID_MIN;
		txBuf[0] = (byte) ((roomIndex << 3) + did);
		IReferenceable dehumidifier = dataStoreManager.getDehumidifier(
				roomIndex - ROOM_ID_MIN, did);

		rxBuf = -1;
		if (output == null)
			return false;
		output.write(txBuf);
		synchronized (lock) {
			lock.wait(TIME_OUT);
		}

		switch (rxBuf) {
		case DEHUMID_REP_OK:
			dehumidifier.setHighTempWarn(false);
			dehumidifier.setDeforstTempWarn(false);
			dehumidifier.setHumidWarn(false);
			dehumidifier.setFanWarn(false);
			dehumidifier.setCompressorWarn(false);
			dehumidifier.setLive(true);
			checkRates[did] = INITIAL_RATE;
			return true;
		case DEHUMID_REP_HIGH_TEMP_ABNORMAL:
			dehumidifier.setHighTempWarn(true);
			dehumidifier.setLive(true);
			checkRates[did] = INITIAL_RATE;
			Log.warn(String.format(
					"The dehumidifier %d in room %d acks high temp abnormal.",
					did, offsetRoomIndex));
			return true;
		case DEHUMID_REP_DEFROST_TEMP_ABNORMAL:
			dehumidifier.setDeforstTempWarn(true);
			dehumidifier.setLive(true);
			checkRates[did] = INITIAL_RATE;
			Log.warn(String
					.format("The dehumidifier %d in room %d acks defrost temp abnormal.",
							did, offsetRoomIndex));
			return true;
		case DEHUMID_REP_DEHUMID_ABNORMAL:
			dehumidifier.setHumidWarn(true);
			dehumidifier.setLive(true);
			checkRates[did] = INITIAL_RATE;
			Log.warn(String.format(
					"The dehumidifier %d in room %d acks dehumid abnormal.",
					did, offsetRoomIndex));
			return true;
		case DEHUMID_REP_FAN_ABNORMAL:
			dehumidifier.setFanWarn(true);
			dehumidifier.setLive(true);
			checkRates[did] = INITIAL_RATE;
			Log.warn(String.format(
					"The dehumidifier %d in room %d acks fan abnormal.", did,
					offsetRoomIndex));
			return true;
		case DEHUMID_REP_COMPRESSOR_ABNORMAL:
			dehumidifier.setCompressorWarn(true);
			dehumidifier.setLive(true);
			checkRates[did] = INITIAL_RATE;
			Log.warn(String.format(
					"The dehumidifier %d in room %d acks compressor abnormal.",
					did, offsetRoomIndex));
			return true;
		default:
			checkRates[did] = drop(checkRates[did]);
			dehumidifier.setLive(false);
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
		int err = ERR;
		int offsetRoomIndex = roomIndex - ROOM_ID_MIN;
		Device dehumidifier = dataStoreManager.getDehumidifier(
				offsetRoomIndex, did);
		Device panel = dataStoreManager.getPanel(offsetRoomIndex);

		// synchronize onoff status between panel and dehumidifier
		while (true) {

			if (!notifyDeviceID(roomIndex, did)) {
				if (--err <= 0) {
					return;
				}
				continue;
			}

			rxBuf = -1;
			boolean on = panel.isOn();
			txBuf[0] = (on) ? (byte) DEHUMID_CMD_ON : (byte) DEHUMID_CMD_OFF;
			if (output == null)
				return;
			output.write(txBuf);
			synchronized (lock) {
				lock.wait(TIME_OUT);
			}

			if (dehumidifierEcho(rxBuf)) {
				dehumidifier.setOn(on);
				checkRates[did] = INITIAL_RATE;
				break;
			} else {
				checkRates[did] = drop(checkRates[did]);
				if (--err <= 0) {
					return;
				}
			}
		}

		// synchronize dehumid mode between panel and dehumidifier
		while (dehumidifier.isOn()) {

			if (!notifyDeviceID(roomIndex, did)) {
				if (--err <= 0) {
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
			if (output == null)
				return;
			output.write(txBuf);
			synchronized (lock) {
				lock.wait(TIME_OUT);
			}

			if (dehumidifierEcho(rxBuf)) {
				dehumidifier.setModeDehumid(true);
				dehumidifier.setModeDry(false);
				checkRates[did] = INITIAL_RATE;
				break;
			} else {
				checkRates[did] = drop(checkRates[did]);
				if (--err <= 0) {
					return;
				}
			}
		}

		// synchronize dry clothes mode between panel and dehumidifier
		while (dehumidifier.isOn()) {

			if (!notifyDeviceID(roomIndex, did)) {
				if (--err <= 0) {
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
			if (output == null)
				return;
			output.write(txBuf);
			synchronized (lock) {
				lock.wait(TIME_OUT);
			}

			if (dehumidifierEcho(rxBuf)) {
				dehumidifier.setModeDry(true);
				dehumidifier.setModeDehumid(false);
				checkRates[did] = INITIAL_RATE;
				break;
			} else {
				checkRates[did] = drop(checkRates[did]);
				if (--err <= 0) {
					return;
				}
			}
		}

		// synchronize humidity setting between panel and dehumidifier
		while (dehumidifier.isOn() && panel.isHumidSet()) {

			if (!notifyDeviceID(roomIndex, did)) {
				if (--err <= 0) {
					return;
				}
				continue;
			}

			rxBuf = -1;
			txBuf[0] = (byte) DEHUMID_CMD_DEHUMIDITY_SET;
			if (output == null)
				return;
			output.write(txBuf);
			synchronized (lock) {
				lock.wait(TIME_OUT);
			}

			if (dehumidifierEcho(rxBuf)) {
				checkRates[did] = INITIAL_RATE;
				rxBuf = -1;
				txBuf[0] = (byte) panel.getHumidSet();
				if (output == null)
					return;
				output.write(txBuf);
				synchronized (lock) {
					lock.wait(TIME_OUT);
				}

				if (dehumidifierEcho(rxBuf)) {
					dehumidifier.setHumidSetValue(panel.getHumidSet());
					break;
				} else {
					checkRates[did] = drop(checkRates[did]);
					if (--err <= 0) {
						return;
					}
				}
			} else {
				checkRates[did] = drop(checkRates[did]);
				if (--err <= 0) {
					return;
				}
			}
		}

		// ask the real humidity of the dehumidifier
		while (true) {

			if (!notifyDeviceID(roomIndex, did)) {
				if (--err <= 0) {					
					return;
				}
				continue;
			}

			rxBuf = -1;
			txBuf[0] = (byte) DEHUMID_CMD_DEHUMIDITY_DIGIT_ONES;
			int humidity = 0;
			if (output == null)
				return;
			output.write(txBuf);
			synchronized (lock) {
				lock.wait(TIME_OUT);
			}

			if (rxBuf >= 0 && rxBuf <= 9) {
				humidity += rxBuf;
				checkRates[did] = INITIAL_RATE;
			} else {
				checkRates[did] = drop(checkRates[did]);
				if (--err <= 0) {
					return;
				}
				continue;
			}

			if (!notifyDeviceID(roomIndex, did)) {
				if (--err <= 0) {
					return;
				}
				continue;
			}

			rxBuf = -1;
			txBuf[0] = (byte) DEHUMID_CMD_DEHUMIDITY_DIGIT_TENS;
			output.write(txBuf);
			synchronized (lock) {
				lock.wait(TIME_OUT);
			}

			if (rxBuf >= 0 && rxBuf <= 9) {
				humidity += rxBuf * 10;
				dehumidifier.setHumid(humidity);
				checkRates[did] = INITIAL_RATE;
				break;
			} else {
				checkRates[did] = drop(checkRates[did]);
				if (--err <= 0) {
					return;
				}
			}
		}
	}
	
	private boolean dehumidifierEcho(byte rxBuf){
		if (rxBuf == DEHUMID_REP_OK
				|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL
				|| rxBuf == DEHUMID_REP_FAN_ABNORMAL
				|| rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL)
			return true;
		else{
			return false;
		}
	}
}
