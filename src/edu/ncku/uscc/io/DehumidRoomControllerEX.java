package edu.ncku.uscc.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.ncku.uscc.proc.Command;
import edu.ncku.uscc.proc.SYNDehumidifierPowerCmd;
import edu.ncku.uscc.proc.SYNPanelHumidityCmd;
import edu.ncku.uscc.proc.SYNPanelHumiditySetCmd;
import edu.ncku.uscc.proc.SYNPanelModeCmd;
import edu.ncku.uscc.proc.SYNPanelPowerCmd;
import edu.ncku.uscc.proc.ScanRoomCmd;
import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.Log;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class DehumidRoomControllerEX extends Thread implements
		SerialPortEventListener {	

	public static final int DEHUMIDIFIERS_A_ROOM = 8;

	public static final int ROOM_ID_MIN = 2;
	public static final int ROOM_ID_MAX = 5;

	/** A synchronization lock */
	private final Object lock = new Object();
	
	private final LinkedList<Command> cmdQueue = new LinkedList<Command>();
	
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
	
	private Command currentCmd;

	/** The index of the room after room index scan */
	private int roomIndex = 0;
	/** Initial flag */
	private boolean init = true;

	/** These constants are used to accel this process */
	private static final int INITIAL_RATE = 100;
	private static final int RATE_CONSTANT = 3;
	private static final double DROP_RATIO = 0.77;

	/** These integers are used to count check rate */
	private int[] checkRates = new int[DEHUMIDIFIERS_A_ROOM];

	/**
	 * Constructor
	 * 
	 * @param slave
	 * @param serialPort
	 */
	public DehumidRoomControllerEX(DataStoreManager dataStoreManager,
			SerialPort serialPort) {
		super();
		this.dataStoreManager = dataStoreManager;
		this.serialPort = serialPort;
	}
	
	public SerialPort getSerialPort(){
		SerialPort sp;
		synchronized (lock) {
			sp = serialPort;
		}
		return sp;
	}

	public DataStoreManager getDataStoreManager() {
		return dataStoreManager;
	}
	
	public OutputStream getOutputStream(){
		OutputStream ops;
		synchronized (lock) {
			ops = output;
		}
		return ops;
	}
	
	public void setRoomIndex(int roomIndex){
		this.roomIndex = roomIndex;
	}
	
	public int getRoomIndex(){
		return this.roomIndex;
	}

	public Object getLock() {
		return lock;
	}
	
	public void addScanRoomQueue(ScanRoomCmd cmd){
		scanRoomQueue.add(cmd);
	}
	
	public void jumpCmdQueue(Command cmd){
		cmdQueue.addFirst(cmd);
	}
	
	public void nextCmd(Command cmd) throws Exception{
		if(cmd != null) cmdQueue.add(cmd);
		
		currentCmd = (cmdQueue != null)?cmdQueue.pollFirst():null;
		
		if(currentCmd == null) throw new Exception();
	}
	
	public void nextScanRoomCmd(ScanRoomCmd cmd) throws Exception{
		if(cmd != null) scanRoomQueue.add(cmd);
		
		currentCmd = (scanRoomQueue != null)?scanRoomQueue.pollFirst():null;
		
		if(currentCmd == null) throw new Exception();
	}
	
	public void initCmdQueue(){
		clearQueue();
		
		addCmdQueue(new SYNPanelPowerCmd(this));
		addCmdQueue(new SYNPanelModeCmd(this));
		addCmdQueue(new SYNPanelHumiditySetCmd(this));
//		addQueue(new SYNPanelTimerSetCmd(this));		
//		addQueue(new SYNPanelAbnormalCmd(this));
//		addQueue(new SYNPanelHumidityCmd(this));
		
		for (int did = 0; did < DEHUMIDIFIERS_A_ROOM; did++) {
			addCmdQueue(new SYNDehumidifierPowerCmd(this, did));
		}
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

		for (int roomScanIndex = ROOM_ID_MIN; roomScanIndex <= ROOM_ID_MAX; roomScanIndex++) {
			for (int did = 0; did < DEHUMIDIFIERS_A_ROOM; did++) {				
				addScanRoomQueue(new ScanRoomCmd(this, roomScanIndex, did, 1));
			}
		}

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
			nextScanRoomCmd(null);
			while(currentCmd != null){
				currentCmd.startCommand();
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

	@Override
	public void serialEvent(SerialPortEvent oEvent) {
		// TODO Auto-generated method stub
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {

			try {
				int available = input.available();
				byte chunk[] = new byte[available];

				if (input == null)
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
	 * To drop the check rate for one of the dehumidifiers.
	 * 
	 * @param originCheckRate
	 * @return The value after drop action
	 */
	public int drop(int originCheckRate) {
		return (int) (originCheckRate * DROP_RATIO + RATE_CONSTANT);
	}
	
	private void clearQueue(){
		cmdQueue.clear();
	}
	
	private void addCmdQueue(Command cmd){
		cmdQueue.add(cmd);
	}

}
