package edu.ncku.uscc.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.ncku.uscc.process.BackupDataCmd;
import edu.ncku.uscc.process.Command;
import edu.ncku.uscc.process.ScanRoomCmd;
import edu.ncku.uscc.process.dehumidifier.SetDehumidifierBackupByItselfPowerCmd;
import edu.ncku.uscc.process.panel.SetPanelBackupPowerCmd;
import edu.ncku.uscc.process.panel.SynPanelPowerCmd;
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
	private static final int RATE_CONSTANT = 4;
	private static final double DROP_RATIO = 0.5;
	
	private static final int BACKUP_DATA_PERIOD_MINUTES = 30;

//	private static final long DELAY_TIME = 500;
	
	private static final String FILE_ADDRESS[] = {
			"/home/pi/workspace/backupD1",
			"/home/pi/workspace/backupD2",
			"/home/pi/workspace/backupD3",
			"/home/pi/workspace/backupD4"
	};

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
	/** ConnectListeners List to trigger Connection event */
	private List<SerialPortConnectListener> connect_listeners = new ArrayList<SerialPortConnectListener>();
	/** A interface to manager modbus dataStore for DehumidSystem */
	private DataStoreManager dataStoreManager;
	/** Buffered input stream from the port */
	private InputStream input;
	/** The output stream to the port */
	private OutputStream output;
	/** The command is running */
	private Command currentCmd;

	private byte rxBuf;
	private String logCommand;
	private boolean isLog = false;

	/** The index of the room after room index scan */
	private int roomIndex = 0;

	/** These integers are used to count check rate */
	private int[] checkRates = new int[DEHUMIDIFIERS_A_ROOM];
	
	/** A counter for panel timeout */
	private int panelTimeoutCounter;
	
	/** A counter for scan room command if there is a ttyUSB but no device */
	private int scanRoomCounter = 40;

	/**
	 * Constructor
	 * 
	 * @param dataStoreManager
	 * @param serialPort
	 */
	public DehumidRoomController(DataStoreManager dataStoreManager, SerialPort serialPort, String logCommand) {
		super();
		this.dataStoreManager = dataStoreManager;		
		this.serialPort = serialPort;
		this.logCommand = logCommand;
		Runtime.getRuntime().addShutdownHook(new JVMShutdownHook(serialPort));
	}

	public SerialPort getSerialPort() {
		SerialPort sp;
		synchronized (lock) {
			sp = serialPort;
		}
		return sp;
	}
	
	public DehumidRoomController getController() {
		return this;
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

	public void setRoomIndex(final int roomIndex) {
		this.roomIndex = roomIndex;
		for (SerialPortConnectListener listener : connect_listeners) {
			listener.onConnectEvent(roomIndex);
		}
		/*this.dataStoreManager.addListener(new Listener(){
		 * 		@override
		 * 		public void modifyEvent(int addr, int value){
		 * 			此時利用拿到的roomIndex針對特定addr的register值變化做對應處理
		 * 		}
		 * });*/
	}

	public int getRoomIndex() {
		return this.roomIndex;
	}

	public Object getLock() {
		return lock;
	}

	public synchronized void setRxBuf(byte rxBuf) {
		this.rxBuf = rxBuf;
	}

	public synchronized byte getRxBuf() {
		return rxBuf;
	}

	public int getCheckRate(int did) {
		return checkRates[did];
	}
	
	public boolean isPanelTimeoutCounter() {
		return (panelTimeoutCounter < 0);
	}
	
	public boolean minusPanelTimeoutCounter() {
		if (panelTimeoutCounter >= 0) {
			panelTimeoutCounter -= 1;
		}
		return (panelTimeoutCounter < 0);
	}
	
	public void initPanelTimeoutCounter() {
		panelTimeoutCounter = 10;
	}
	
	public boolean minusScanRoomTimeoutCounter() {
		if (scanRoomCounter >= 0) {
			scanRoomCounter -= 1;
		}
		return (scanRoomCounter < 0);
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
	public synchronized void addScanRoomQueue(ScanRoomCmd cmd) {
		scanRoomQueue.add(cmd);
	}

	/**
	 * Jump the command which you choice to command queue
	 * 
	 * @param cmd
	 */
	public synchronized void jumpCmdQueue(Command cmd) {
		if (cmd != null)
			cmdQueue.addFirst(cmd);
	}

	/**
	 * Pointers the currentCmd to flwCmd directly instead of jumping to command queue
	 * 
	 * @param flwCmd
	 * @param cmd
	 * @throws NullPointerException
	 */
	public synchronized void followCmd(Command flwCmd, Command cmd) throws NullPointerException{
		if (flwCmd != null)
			currentCmd = flwCmd;
		else
			throw new NullPointerException();
	}

	/**
	 * Sets the first command in command queue and current command
	 * 
	 * @param cmd
	 *            Add this command to the queue last
	 * @throws Exception
	 */
	public synchronized void nextCmd(Command cmd) throws Exception {
		if (cmd != null)
			cmdQueue.add(cmd);

//		Log.debug("cmdQueue : " + cmdQueue.size());

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
	public synchronized void nextScanRoomCmd(ScanRoomCmd cmd) throws Exception {
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
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(
				new BackupDataTask(), BACKUP_DATA_PERIOD_MINUTES, BACKUP_DATA_PERIOD_MINUTES, TimeUnit.MINUTES);
		
		// clear the first backup data command
		clearQueue();
		
		backupDataDeSerialization();
		
		addCmdQueue(new SynPanelPowerCmd(this));
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

		// initial check rate
		for (int did = 0; did < DEHUMIDIFIERS_A_ROOM; initCheckRate(did++))
			;
		
		initPanelTimeoutCounter();

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
	
	/**
	 * Adds connect listener
	 * 
	 * @param listener
	 * @throws Exception
	 */
	public void addConnectListener(SerialPortConnectListener listener) throws Exception {
		connect_listeners.add(listener);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			nextScanRoomCmd(null);
			while (currentCmd != null) {
				currentCmd.start();
//				Thread.sleep(DELAY_TIME);
			}
			Log.error("Current command is null!!");
		} catch (Exception e) {
			Log.error(e, e);
			for (SerialPortDisconnectListener listener : listeners) {
				listener.onDisconnectEvent(serialPort.getName(), roomIndex);
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

				for (byte b : chunk) {
					setRxBuf(b);
				}

				synchronized (lock) {
					lock.notifyAll();
				}

			} catch (IOException e) {
				Log.error(serialPort.getName() + " disconnected!", e);
				for (SerialPortDisconnectListener listener : listeners) {
					listener.onDisconnectEvent(serialPort.getName(), roomIndex);
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
	
	public void detectLogRoomIndex() {
		for (char ch : logCommand.toCharArray()) {
			if ((char)((roomIndex - 1) + '0') == ch) {
				isLog = true;
				break;
			}
		}
	}

	public void log_debug(String message) {
		if (isLog)
			Log.debug(message);
	}

	public void log_error(Object message) {
		if (isLog)
			Log.error(message);
	}

	public void log_warn(Object message) {
		if (isLog)
			Log.warn(message);
	}

	public void log_info(Object message) {
		if (isLog)
			Log.info(message);
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
	
	public BackupData getBackupData() {
		
		BackupData data = new BackupData();
		
		try {
			ObjectInputStream ois = new ObjectInputStream(
					new FileInputStream(FILE_ADDRESS[getRoomIndex() - 2]));
			data = (BackupData) ois.readObject();
			ois.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			Log.warn("Backup file not found, creat a new one.");
			data.setDefaultValue();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			data.setDefaultValue();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			data.setDefaultValue();
		}
		
		return data;
	}
	
	public void backupDataSerialization(BackupData data) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(FILE_ADDRESS[getRoomIndex() - 2]));
			oos.writeObject(data);
			oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void backupDataDeSerialization() {
		BackupData data = getBackupData();

		if (isPanelTimeoutCounter()) {
			// no panel mode
			for (int did = DEHUMIDIFIERS_A_ROOM - 1; did >= 0; did--) {
				if (getCheckRate(did) >= (int) (Math.random() * 100)) {
					jumpCmdQueue(new SetDehumidifierBackupByItselfPowerCmd(this, did, data));
				}
			}
		} else {
			// panel mode
			jumpCmdQueue(new SetPanelBackupPowerCmd(this, data));
		}
	}
	
	public void backupDataDeSerialization(int did) {
		BackupData data = getBackupData();

		// no panel mode
		jumpCmdQueue(new SetDehumidifierBackupByItselfPowerCmd(this, did, data));
	}
	
	public class BackupDataTask extends Thread {
		@Override
		public void run() {
			cmdQueue.add(new BackupDataCmd(getController()));
		}
	}

}
