package edu.ncku.uscc.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.Log;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.util.Enumeration;

import org.apache.log4j.Logger;

public class Dehumid extends Thread implements SerialPortEventListener {

	private final int PANEL_CMD_ONOFF = 0x80;
	private final int PANEL_CMD_MODE = 0x81;
	private final int PANEL_CMD_SET = 0x82;
	private final int PANEL_CMD_HUMID = 0x83;
	private final int PANEL_CMD_TIMER = 0x84;
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
	private final int DEHUMID_CMD_DEHUMIDITY_DIGIT_ONES = 0x38;
	private final int DEHUMID_CMD_DEHUMIDITY_DIGIT_TENS = 0x39;

	private final int DEHUMID_REP_OK = 0x55;
	private final int DEHUMID_REP_HIGH_TEMP_ABNORMAL = 0x56;
	private final int DEHUMID_REP_DEFROST_TEMP_ABNORMAL = 0x57;

	/** A synchronization lock */
	private final Object lock = new Object();
	/** A logger to log messages */
	private final Logger logger = Log.getLogger();

	/** The port we're normally going to use. */
	private static final String PORT_NAMES[] = { "/dev/tty.usbserial-A9007UX1", // Mac
																				// OS
																				// X
			"/dev/ttyACM0", // Linux
			"COM8", // Windows
	};

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

	/**
	 * Constructor
	 * 
	 * @param slave
	 *            The instance of modbus slave
	 * @param numRooms
	 *            The number of rooms
	 */
	public Dehumid(ModbusTCPSlave slave, int numRooms) {
		super();
		this.dataStoreManager = new DataStoreManager(slave, numRooms);
	}

	public void initialize() throws Exception {
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		// iterate through, looking for the port
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum
					.nextElement();
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					System.out.println(portName);
					logger.info(portName);
					portId = currPortId;
					break;
				}
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
				synPanel(0);
			}
		} catch (Exception e){
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
	 *            The index of room
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void synPanel(int room) throws Exception {

		byte[] txBuf = new byte[1];
		byte err = 5;

		// ask panel it is on or off
		txBuf[0] = (byte) PANEL_CMD_ONOFF;
		while (true) {
			rxBuf = -1;
			synchronized (lock) {
				output.write(txBuf);
				logger.info(String.format("Send to panel : %x",
						((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf == PANEL_REP_ON) {
				dataStoreManager.getPanel(room).setOn(true);
				dataStoreManager.getPanel(room).setLive(true);
				break;
			} else if (rxBuf == PANEL_REP_OFF) {
				dataStoreManager.getPanel(room).setOn(false);
				dataStoreManager.getPanel(room).setLive(true);
				break;
			} else {
				dataStoreManager.getPanel(room).setLive(false);
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
				dataStoreManager.getPanel(room).setModeDehumid(true);
				dataStoreManager.getPanel(room).setModeDry(false);
				dataStoreManager.getPanel(room).setLive(true);
				break;
			} else if (rxBuf == PANEL_REP_DRY_CLOTHES) {
				dataStoreManager.getPanel(room).setModeDehumid(false);
				dataStoreManager.getPanel(room).setModeDry(true);
				dataStoreManager.getPanel(room).setLive(true);
				break;
			} else {
				dataStoreManager.getPanel(room).setLive(false);
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
				dataStoreManager.getPanel(room).setHumidSet(false);
				dataStoreManager.getPanel(room).setTimerSet(false);
				dataStoreManager.getPanel(room).setLive(true);
				break;
			} else if (rxBuf == PANEL_REP_HUMID_SET) {
				dataStoreManager.getPanel(room).setHumidSet(true);
				dataStoreManager.getPanel(room).setTimerSet(false);
				dataStoreManager.getPanel(room).setLive(true);
				break;
			} else if (rxBuf == PANEL_REP_TIMER_SET) {
				dataStoreManager.getPanel(room).setHumidSet(false);
				dataStoreManager.getPanel(room).setTimerSet(true);
				dataStoreManager.getPanel(room).setLive(true);
				break;
			} else {
				dataStoreManager.getPanel(room).setLive(false);
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
			}
		}

		// ask panel its humidity
		txBuf[0] = (byte) PANEL_CMD_HUMID;
		while (true) {
			rxBuf = -1;
			synchronized (lock) {
				output.write(txBuf);
				logger.info(String.format("Send to panel : %x",
						((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf >= 0) {
				dataStoreManager.getPanel(room).setHumid(rxBuf);
				dataStoreManager.getPanel(room).setLive(true);
				break;
			} else {
				dataStoreManager.getPanel(room).setLive(false);
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
			}
		}

		// ask panel its timer
		txBuf[0] = (byte) PANEL_CMD_TIMER;
		while (true) {
			rxBuf = -1;
			synchronized (lock) {
				output.write(txBuf);
				logger.info(String.format("Send to panel : %x",
						((int) txBuf[0] & 0xff)));
				lock.wait(TIME_OUT);
			}

			if (rxBuf >= 0) {
				dataStoreManager.getPanel(room).setTimer(rxBuf);
				dataStoreManager.getPanel(room).setLive(true);
				break;
			} else {
				dataStoreManager.getPanel(room).setLive(false);
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
			}
		}

	}

	private boolean notifyDeviceID(int room, int did) throws IOException,
			InterruptedException {

		byte[] txBuf = new byte[1];
		byte err = 5;
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
			return true;
		case DEHUMID_REP_HIGH_TEMP_ABNORMAL:
			dataStoreManager.getDehumidifier(room, did).setHighTempWarn(true);
			dataStoreManager.getDehumidifier(room, did).setLive(true);
			return true;
		case DEHUMID_REP_DEFROST_TEMP_ABNORMAL:
			dataStoreManager.getDehumidifier(room, did).setTempWarn(true);
			dataStoreManager.getDehumidifier(room, did).setLive(true);
			return true;
		default:
			dataStoreManager.getDehumidifier(room, did).setLive(false);
			return false;
		}
	}

	/**
	 * 施工中
	 * 
	 * @param room
	 * @param did
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void synDehumidifier(int room, int did) throws IOException,
			InterruptedException {

		byte[] txBuf = new byte[1];
		byte err = 5;

		// synchronize onoff status panel and dehumidifier
		while (true) {

			if(!notifyDeviceID(room, did)){
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
				continue;
			}

			rxBuf = -1;
			boolean on = dataStoreManager.getPanel(room).isOn();
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
				dataStoreManager.getDehumidifier(room, did).setOn(on);
				break;
			} else {
				dataStoreManager.getDehumidifier(room, did).setLive(false);
				if (--err <= 0) {
					logger.error("Timeout or data is not expected.");
					return;
				}
			}
		}
	}
}
