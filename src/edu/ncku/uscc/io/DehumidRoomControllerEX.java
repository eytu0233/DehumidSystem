package edu.ncku.uscc.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.Log;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class DehumidRoomControllerEX extends Thread implements
		SerialPortEventListener {

	private static final String LCK_REMOVE_CMD = "sudo rm -f /var/lock/LCK..ttyUSB";

	private static final int DEHUMIDIFIERS_A_ROOM = 8;

	private static final int ROOM_ID_MIN = 2;
	private static final int ROOM_ID_MAX = 5;

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

	private LinkedList<Command> cmdQueue = new LinkedList<Command>();

	/** The index of the room after room index scan */
	private int roomIndex;
	/** Initial flag */
	private boolean init = true;

	/** These constants are used to accel this process */
	private static final int INITIAL_RATE = 100;
	private static final int RATE_CONSTANT = 3;
	private static final double DROP_RATIO = 0.77;

	/** These integers are used to count check rate */
	private int[] checkRates = new int[DEHUMIDIFIERS_A_ROOM];

	public OutputStream getOutputStream() {
		return output;
	}

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
				
				final int finalRoomScanIndex = roomScanIndex, finalDid = did;
				
				cmdQueue.add(new Command(lock, new AbstractRequest() {

					@Override
					public void requestEvent() throws Exception {
						// TODO Auto-generated method stub
						for (int usbIndex = 0; usbIndex < 4; Runtime
								.getRuntime().exec(LCK_REMOVE_CMD + usbIndex++))
							;
						txBuf[0] = (byte) ((finalRoomScanIndex << 3) + finalDid);
					}
				}, null));
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

				byte rxBuf;
				for (byte b : chunk) {
					rxBuf = b;
				}

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
	private int drop(int originCheckRate) {
		return (int) (originCheckRate * DROP_RATIO + RATE_CONSTANT);
	}

}
