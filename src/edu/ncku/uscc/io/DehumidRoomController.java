package edu.ncku.uscc.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.ncku.uscc.process.Command;
import edu.ncku.uscc.process.ScanRoomCmd;
import edu.ncku.uscc.process.panel.SynPanelAbnormalCmd;
import edu.ncku.uscc.process.panel.SynPanelHumidityCmd;
import edu.ncku.uscc.process.panel.SynPanelHumiditySetCmd;
import edu.ncku.uscc.process.panel.SynPanelModeCmd;
import edu.ncku.uscc.process.panel.SynPanelPowerCmd;
import edu.ncku.uscc.process.panel.SynPanelTimerSetCmd;
import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.Log;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class DehumidRoomController extends Thread implements SerialPortEventListener {

	/** Constant */
	public static final int DEHUMIDIFIERS_A_ROOM = 8;

	public static final int ROOM_ID_MIN = 2;
	public static final int ROOM_ID_MAX = 5;

	/** These constants are used to accelerate this process */
	private static final int INITIAL_RATE = 100;
	private static final int RATE_CONSTANT = 3;
	private static final double DROP_RATIO = 0.77;

	/** A synchronization lock */
	private final Object lock = new Object();
	/** The command queue which store commands */
	private final LinkedList<Command> cmdQueue = new LinkedList<Command>();
	/** The ScanRoomCommand queue which is used to scan roomIndex */
	private final LinkedList<ScanRoomCmd> scanRoomQueue = new LinkedList<ScanRoomCmd>();

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
	/** The command is running */
	private Command currentCmd;

	/** The index of the room after room index scan */
	private int roomIndex = 0;

	/** These integers are used to count check rate */
	private int[] checkRates = new int[DEHUMIDIFIERS_A_ROOM];

	/**
	 * Constructor
	 * 
	 * @param dataStoreManager
	 * @param serialPort
	 */
	public DehumidRoomController(DataStoreManager dataStoreManager, SerialPort serialPort) {
		super();
		this.dataStoreManager = dataStoreManager;
		this.serialPort = serialPort;
	}

	public SerialPort getSerialPort() {
		SerialPort sp;
		synchronized (lock) {
			sp = serialPort;
		}
		return sp;
	}

	public DataStoreManager getDataStoreManager() {
		return dataStoreManager;
	}

	public OutputStream getOutputStream() {
		OutputStream ops;
		synchronized (lock) {
			ops = output;
		}
		return ops;
	}

	public InputStream getInputStream() {
		InputStream is;
		synchronized (lock) {
			is = input;
		}
		return is;
	}

	public void setRoomIndex(int roomIndex) {
		this.roomIndex = roomIndex;
	}

	public int getRoomIndex() {
		return this.roomIndex;
	}

	public Object getLock() {
		return lock;
	}

	public int getCheckRate(int did) {
		return checkRates[did];
	}

	/**
	 * Initializes the check rate
	 * 
	 * @param did
	 *            Device ID
	 */
	public void initCheckRate(int did) {
		checkRates[did] = INITIAL_RATE;
	}

	/**
	 * Drops the check rate for one of the dehumidifiers.
	 * 
	 * @param did
	 *            Device ID
	 */
	public void dropRate(int did) {
		checkRates[did] = (int) (checkRates[did] * DROP_RATIO + RATE_CONSTANT);
	}

	/**
	 * Adds scan room command to scan room command queue
	 * 
	 * @param cmd
	 */
	public void addScanRoomQueue(ScanRoomCmd cmd) {
		scanRoomQueue.add(cmd);
	}

	/**
	 * Jump the command which you choice to command queue
	 * 
	 * @param cmd
	 */
	public void jumpCmdQueue(Command cmd) {
		cmdQueue.addFirst(cmd);
	}

	/**
	 * Sets the first command in command queue and current command
	 * 
	 * @param cmd
	 *            Add this command to the queue last
	 * @throws Exception
	 */
	public void nextCmd(Command cmd) throws Exception {
		if (cmd != null)
			cmdQueue.add(cmd);

		currentCmd = (cmdQueue != null) ? cmdQueue.pollFirst() : null;

		if (currentCmd == null)
			throw new Exception();
	}

	/**
	 * Sets the first command to scanRoomQueue and current command
	 * 
	 * @param cmd
	 *            Add this command to the scanRoomQueue last
	 * @throws Exception
	 */
	public void nextScanRoomCmd(ScanRoomCmd cmd) throws Exception {
		if (cmd != null)
			scanRoomQueue.add(cmd);

		currentCmd = (scanRoomQueue != null) ? scanRoomQueue.pollFirst() : null;

		if (currentCmd == null)
			throw new Exception();
	}

	/**
	 * Initializes the command queue field
	 */
	public void initCmdQueue() {
		clearQueue();

		addCmdQueue(new SynPanelPowerCmd(this));
		addCmdQueue(new SynPanelModeCmd(this));
		addCmdQueue(new SynPanelHumiditySetCmd(this));
		addCmdQueue(new SynPanelTimerSetCmd(this));
		addCmdQueue(new SynPanelAbnormalCmd(this));
		addCmdQueue(new SynPanelHumidityCmd(this));
	}

	/**
	 * Initializes this object and start a thread
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

		for (int did = 0; did < DEHUMIDIFIERS_A_ROOM; initCheckRate(did++))
			;

		// initial scanRoomQueue
		for (int roomScanIndex = ROOM_ID_MIN; roomScanIndex <= ROOM_ID_MAX; roomScanIndex++) {
			for (int did = 0; did < DEHUMIDIFIERS_A_ROOM; did++) {
				addScanRoomQueue(new ScanRoomCmd(this, roomScanIndex, did, 1));
			}
		}

		setDaemon(true);
		start();
	}

	/**
	 * Adds disconnect listener
	 * 
	 * @param listener
	 * @throws Exception
	 */
	public void addDisconnectListener(SerialPortDisconnectListener listener) throws Exception {
		listeners.add(listener);
	}

	/**
	 * Removes disconnect listener
	 * 
	 * @param listener
	 * @throws Exception
	 */
	public void removeDisconnectListener(SerialPortDisconnectListener listener) throws Exception {
		listeners.remove(listener);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			nextScanRoomCmd(null);
			while (currentCmd != null) {
				currentCmd.start();
			}
			Log.error("Current command is null!!");
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
	public void close() throws IOException {
		synchronized (lock) {
			if (input != null) {
				input.close();
				input = null;
			}

			if (output != null) {
				output.close();
				output = null;
			}

			if (serialPort != null) {
				serialPort.removeEventListener();
				serialPort.close();
			}
		}
	}

	/**
	 * Receives the events about serial
	 */
	@Override
	public void serialEvent(SerialPortEvent oEvent) {
		// TODO Auto-generated method stub
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {

			try {
				int available = input.available();
				byte chunk[] = new byte[available];

				if (getInputStream() == null)
					return;
				input.read(chunk, 0, available);

				byte rxBuf = -1;
				for (byte b : chunk) {
					rxBuf = b;
				}
				currentCmd.setRxBuf(rxBuf);

				synchronized (lock) {
					lock.notifyAll();
				}

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

	/**
	 * Clears command queue
	 */
	private void clearQueue() {
		cmdQueue.clear();
	}

	/**
	 * Adds command to command queue
	 * 
	 * @param cmd
	 */
	private void addCmdQueue(Command cmd) {
		cmdQueue.add(cmd);
	}

}
