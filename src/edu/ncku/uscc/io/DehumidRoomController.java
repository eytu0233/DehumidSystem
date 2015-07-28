package edu.ncku.uscc.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.util.Enumeration;

import org.apache.log4j.Logger;

public class DehumidRoomController extends Thread implements
		SerialPortEventListener {

	private final int PANEL_CMD_ONOFF = 0x80;
	private final int PANEL_CMD_MODE = 0x81;
	private final int PANEL_CMD_SET = 0x82;
	private final int PANEL_CMD_HUMID_SET = 0x83;
	private final int PANEL_CMD_TIMER_SET = 0x84;
	private final int PANEL_CMD_START = 0x85;
	private final int PANEL_CMD_SHUTDOWM = 0x86;
	private final int PANEL_CMD_TEMP_ABNORMAL = 0x87;
	private final int PANEL_CMD_DEFROST = 0x88;
	private final int PANEL_CMD_MINUS_TIMER = 0x89;

	private final int PANEL_REP_ON = 0x30;
	private final int PANEL_REP_OFF = 0x31;
	private final int PANEL_REP_DEHUMID = 0x32;
	private final int PANEL_REP_DRY_CLOTHES = 0x33;
	private final int PANEL_REP_NO_SET = 0x34;
	private final int PANEL_REP_HUMID_SET = 0x35;
	private final int PANEL_REP_TIMER_SET = 0x36;
	private final int PANEL_REP_OK = 0x55;

	private final int DEHUMID_CMD_ON = 0x30;
	private final int DEHUMID_CMD_OFF = 0x31;
	private final int DEHUMID_CMD_DEHUMID_MODE = 0x32;
	private final int DEHUMID_CMD_DRY_CLOTHES_MODE = 0x33;
	private final int DEHUMID_CMD_DEHUMIDITY_SET = 0x34;
	private final int DEHUMID_CMD_TIMER_SET = 0x35;
	private final int DEHUMID_CMD_DEHUMIDITY_DIGIT_ONES = 0x38;
	private final int DEHUMID_CMD_DEHUMIDITY_DIGIT_TENS = 0x39;

	private final int DEHUMID_REP_OK = 0x55;
	private final int DEHUMID_REP_HIGH_TEMP_ABNORMAL = 0x56;
	private final int DEHUMID_REP_DEFROST_TEMP_ABNORMAL = 0x57;

	private final int DEHUMIDIFIERS_A_ROOM = 8;

	/** A synchronization lock */
	private final Object lock = new Object();
	/** A logger to log messages */
	private final Logger logger = Log.getLogger();

	/** The port we're normally going to use. */
	private static final String PORT_NAMES[] = { "/dev/ttyACM0", // Linux
			"COM8", // Windows
	};

	private String portName;

	private SerialPort serialPort;

	/** A interface to manager modbus dataStore for DehumidSystem */
	private DataStoreManager dataStoreManager;
	/** Buffered input stream from the port */
	private InputStream input;
	/** The output stream to the port */
	private OutputStream output;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;
	/** The buff receives byte data from serial port interface */
	private Byte rxBuf;

	private int roomIndex;

	private boolean init = true;

	/**
	 * Constructor
	 * 
	 * @param slave
	 *            The instance of modbus slave
	 * @param numRooms
	 *            The index of the room
	 */
	public DehumidRoomController(ModbusTCPSlave slave,
			String portName) {
		super();
		this.dataStoreManager = new DataStoreManager(slave, roomIndex);
		this.portName = portName;
	}

	public void initialize() throws Exception {
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		// iterate through, looking for the port
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum
					.nextElement();
			if (currPortId.getName().equals(portName)) {
				System.out.println(portName);
				logger.info(portName);
				portId = currPortId;
				break;
			}
		}

		if (portId == null) {
			System.out.println("Could not find COM port.");
			logger.warn("Could not find COM port.");
		}

		// open serial port, and use class name for the appName.
		serialPort = (SerialPort) portId.open(this.getClass().getName(),
				TIME_OUT);

		// set port parameters
		serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

		// open the streams
		input = serialPort.getInputStream();
		output = serialPort.getOutputStream();

		// add event listeners
		serialPort.addEventListener(this);
		serialPort.notifyOnDataAvailable(true);

		this.setDaemon(true);
		this.start();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while (true) {
				synPanel(roomIndex);
				for (int did = 0; did < DEHUMIDIFIERS_A_ROOM; did++) {
					synDehumidifier(roomIndex, did);
				}
			}
		} catch (Exception e) {
			logger.error(e, e);
		}
	}

	/**
	 * This should be called when you stop using the port. This will prevent
	 * port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
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
				input.read(chunk, 0, available);

				for (byte b : chunk) {
					rxBuf = b;
				}
				System.out.println(String.format("Recv from panel : %x",
						((int) rxBuf & 0xff)));
				synchronized (lock) {
					lock.notify();
				}
			} catch (Exception e) {
				logger.error(e, e);
			}
		}
		// Ignore all the other eventTypes, but you should consider the other
		// ones.
	}

	/**
	 * To synchronize data between panel status and modbus dataStore
	 * 
	 * @param room
	 *            The panel belongs
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void synPanel(int room) throws Exception {

		byte[] txBuf = new byte[1];
		byte err = 3;
		IReferenceable panel = dataStoreManager.getPanel(room);

		if (!init && dataStoreManager.isPanelONOFFChange(room)) {
			txBuf[0] = (panel.isOn()) ? (byte) PANEL_CMD_START
					: (byte) PANEL_CMD_SHUTDOWM;
		} else {
			// ask panel it is on or off
			txBuf[0] = (byte) PANEL_CMD_ONOFF;
		}
		while (true) {
			rxBuf = -1;
			synchronized (lock) {
				output.write(txBuf);
				logger.info(String.format("Send to panel : %x",
						((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf == PANEL_REP_ON) {
				panel.setOn(true);
				panel.setLive(true);
				logger.info(String.format("Panel %d is ON.", room));
				break;
			} else if (rxBuf == PANEL_REP_OFF) {
				panel.setOn(false);
				panel.setLive(true);
				logger.info(String.format("Panel %d is OFF.", room));
				break;
			} else {
				panel.setLive(false);
				logger.warn(String.format("Panel %d is not live.", room));
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
			}
		}

		// ask panel its mode
		txBuf[0] = (byte) PANEL_CMD_MODE;
		while (true) {
			rxBuf = -1;
			synchronized (lock) {
				output.write(txBuf);
				logger.info(String.format("Send to panel : %x",
						((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf == PANEL_REP_DEHUMID) {
				panel.setModeDehumid(true);
				panel.setModeDry(false);
				panel.setLive(true);
				logger.info(String.format("Panel %d is dehumid mode.", room));
				break;
			} else if (rxBuf == PANEL_REP_DRY_CLOTHES) {
				panel.setModeDehumid(false);
				panel.setModeDry(true);
				panel.setLive(true);
				logger.info(String
						.format("Panel %d is dry clothes mode.", room));
				break;
			} else {
				panel.setLive(false);
				logger.warn(String.format("Panel %d is not live.", room));
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
			}
		}

		// ask panel its set status
		txBuf[0] = (byte) PANEL_CMD_SET;
		while (true) {
			rxBuf = -1;
			synchronized (lock) {
				output.write(txBuf);
				logger.info(String.format("Send to panel : %x",
						((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf == PANEL_REP_NO_SET) {
				panel.setHumidSet(false);
				panel.setTimerSet(false);
				panel.setLive(true);
				logger.info(String.format("Panel %d hasn't any set.", room));
				break;
			} else if (rxBuf == PANEL_REP_HUMID_SET) {
				panel.setHumidSet(true);
				panel.setTimerSet(false);
				panel.setLive(true);
				logger.info(String.format("Panel %d has humidity set.", room));
				break;
			} else if (rxBuf == PANEL_REP_TIMER_SET) {
				panel.setHumidSet(false);
				panel.setTimerSet(true);
				panel.setLive(true);
				logger.info(String.format("Panel %d has timer set.", room));
				break;
			} else {
				panel.setLive(false);
				logger.warn(String.format("Panel %d is not live.", room));
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
			}
		}

		// ask panel its humidity set
		txBuf[0] = (byte) PANEL_CMD_HUMID_SET;
		while (true) {
			rxBuf = -1;
			synchronized (lock) {
				output.write(txBuf);
				logger.info(String.format("Send to panel : %x",
						((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf >= 0) {
				panel.setHumidSet(rxBuf);
				panel.setLive(true);
				logger.info(String.format(
						"The humidity set of Panel %d is %d.", room, rxBuf));
				break;
			} else {
				panel.setLive(false);
				logger.warn(String.format("Panel %d is not live.", room));
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
			}
		}

		// ask panel its timer
		txBuf[0] = (byte) PANEL_CMD_TIMER_SET;
		while (true) {
			rxBuf = -1;
			synchronized (lock) {
				output.write(txBuf);
				logger.info(String.format("Send to panel : %x",
						((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf >= 0) {
				panel.setTimerSet(rxBuf);
				panel.setLive(true);
				logger.info(String.format("The timer set of Panel %d is %d.",
						room, rxBuf));
				break;
			} else {
				panel.setLive(false);
				logger.warn(String.format("Panel %d is not live.", room));
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
			}
		}

	}

	/**
	 * Before synchronizing panels, it is necessary to notify the specific
	 * dehumidifier
	 * 
	 * @param room
	 *            The dehumidifier belongs
	 * @param did
	 *            Device ID
	 * @return success or not
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private boolean notifyDeviceID(int room, int did) throws IOException,
			InterruptedException {

		byte[] txBuf = new byte[1];
		txBuf[0] = (byte) did;

		rxBuf = -1;
		synchronized (lock) {
			output.write(txBuf);
			logger.info(String.format("Send to dehumidifier : %x",
					((int) txBuf[0] & 0xff)));
			lock.wait(TIME_OUT);
		}

		switch (rxBuf) {
		case DEHUMID_REP_OK:
			dataStoreManager.getDehumidifier(room, did).setHighTempWarn(false);
			dataStoreManager.getDehumidifier(room, did).setTempWarn(false);
			dataStoreManager.getDehumidifier(room, did).setLive(true);
			logger.info(String.format(
					"The dehumidifier %d in room %d acks OK.", did, room));
			return true;
		case DEHUMID_REP_HIGH_TEMP_ABNORMAL:
			dataStoreManager.getDehumidifier(room, did).setHighTempWarn(true);
			dataStoreManager.getDehumidifier(room, did).setLive(true);
			logger.info(String.format(
					"The dehumidifier %d in room %d acks high temp abnormal.",
					did, room));
			return true;
		case DEHUMID_REP_DEFROST_TEMP_ABNORMAL:
			dataStoreManager.getDehumidifier(room, did).setTempWarn(true);
			dataStoreManager.getDehumidifier(room, did).setLive(true);
			logger.info(String
					.format("The dehumidifier %d in room %d acks defrost temp abnormal.",
							did, room));
			return true;
		default:
			dataStoreManager.getDehumidifier(room, did).setLive(false);
			return false;
		}
	}

	/**
	 * To synchronize data between dehumidifier status and modbus dataStore by
	 * panel status
	 * 
	 * @param room
	 *            The dehumidifier belongs
	 * @param did
	 *            Device ID
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void synDehumidifier(int room, int did) throws IOException,
			InterruptedException {

		byte[] txBuf = new byte[1];
		byte err = 3;
		IReferenceable panel = dataStoreManager.getDehumidifier(room, did), dehumidifier = dataStoreManager
				.getPanel(room);

		// synchronize onoff status between panel and dehumidifier
		while (true) {

			if (!notifyDeviceID(room, did)) {
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
				continue;
			}

			rxBuf = -1;
			boolean on = dehumidifier.isOn();
			txBuf[0] = (on) ? (byte) DEHUMID_CMD_ON : (byte) DEHUMID_CMD_OFF;
			synchronized (lock) {
				output.write(txBuf);
				logger.info(String.format("Send to dehumidifier : %x",
						((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf == DEHUMID_REP_OK
					|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
					|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
				panel.setOn(on);
				break;
			} else {
				panel.setLive(false);
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
			}
		}

		// synchronize dehumid mode between panel and dehumidifier
		while (true) {

			if (!notifyDeviceID(room, did)) {
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
				continue;
			}

			rxBuf = -1;
			boolean dehumidMode = dehumidifier.isModeDehumid();
			if (dehumidMode) {
				txBuf[0] = (byte) DEHUMID_CMD_DEHUMID_MODE;
			} else {
				break;
			}
			synchronized (lock) {
				output.write(txBuf);
				logger.info(String.format("Send to dehumidifier : %x",
						((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf == DEHUMID_REP_OK
					|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
					|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
				panel.setModeDehumid(true);
				panel.setModeDry(false);
				break;
			} else {
				panel.setLive(false);
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
			}
		}

		// synchronize dry clothes mode between panel and dehumidifier
		while (true) {

			if (!notifyDeviceID(room, did)) {
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
				continue;
			}

			rxBuf = -1;
			boolean dryMode = dehumidifier.isModeDry();
			if (dryMode) {
				txBuf[0] = (byte) DEHUMID_CMD_DRY_CLOTHES_MODE;
			} else {
				break;
			}
			synchronized (lock) {
				output.write(txBuf);
				logger.info(String.format("Send to dehumidifier : %x",
						((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf == DEHUMID_REP_OK
					|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
					|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
				panel.setModeDry(true);
				panel.setModeDehumid(false);
				break;
			} else {
				panel.setLive(false);
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
			}
		}

		// synchronize dehumidity set between panel and dehumidifier
		while (true) {

			if (!notifyDeviceID(room, did)) {
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
				continue;
			}

			rxBuf = -1;
			txBuf[0] = (byte) DEHUMID_CMD_DEHUMIDITY_SET;
			synchronized (lock) {
				output.write(txBuf);
				logger.info(String.format("Send to dehumidifier : %x",
						((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf == DEHUMID_REP_OK
					|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
					|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
				rxBuf = -1;
				txBuf[0] = (byte) dehumidifier.getHumidSet();
				synchronized (lock) {
					output.write(txBuf);
					logger.info(String.format("Send to dehumidifier : %x",
							((int) txBuf[0] & 0xff)));
					lock.wait(TIME_OUT);
				}
				if (rxBuf == DEHUMID_REP_OK
						|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
						|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
					panel.setHumidSet(txBuf[0]);
					break;
				}
			} else {
				panel.setLive(false);
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
			}
		}

		// synchronize timer set between panel and dehumidifier
		while (true) {

			if (!notifyDeviceID(room, did)) {
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
				continue;
			}

			rxBuf = -1;
			txBuf[0] = (byte) DEHUMID_CMD_TIMER_SET;
			synchronized (lock) {
				output.write(txBuf);
				logger.info(String.format("Send to dehumidifier : %x",
						((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf == DEHUMID_REP_OK
					|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
					|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
				rxBuf = -1;
				txBuf[0] = (byte) dehumidifier.getTimerSet();
				synchronized (lock) {
					output.write(txBuf);
					logger.info(String.format("Send to dehumidifier : %x",
							((int) txBuf[0] & 0xff)));
					lock.wait(TIME_OUT);
				}
				if (rxBuf == DEHUMID_REP_OK
						|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
						|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
					panel.setTimerSet(txBuf[0]);
					break;
				}
			} else {
				panel.setLive(false);
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
			}
		}

		// ask the real humidity of the dehumidifier
		while (true) {

			if (!notifyDeviceID(room, did)) {
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
				continue;
			}

			rxBuf = -1;
			txBuf[0] = (byte) DEHUMID_CMD_DEHUMIDITY_DIGIT_ONES;
			int humidity = 0;
			synchronized (lock) {
				output.write(txBuf);
				logger.info(String.format("Send to dehumidifier : %x",
						((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf >= 0) {
				humidity += rxBuf;
			} else {
				panel.setLive(false);
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
				continue;
			}

			if (!notifyDeviceID(room, did)) {
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
				continue;
			}

			rxBuf = -1;
			txBuf[0] = (byte) DEHUMID_CMD_DEHUMIDITY_DIGIT_TENS;
			synchronized (lock) {
				output.write(txBuf);
				logger.info(String.format("Send to dehumidifier : %x",
						((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf >= 0) {
				humidity += rxBuf * 10;
				panel.setHumid(humidity);
				break;
			} else {
				panel.setLive(false);
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
			}
		}
	}
}
