package edu.ncku.uscc.io;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.Log;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class PortScanTask implements Callable<Void> {

	private static final int TIME_OUT = 2000;
	private static final int TIME_INTERVAL = 1000;
	private static final int BAUD_RATE = 9600;

	private static final String PORT_NAMES[] = { "/dev/ttyUSB0", // Linux
			"/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3" };

	private boolean[] isRoomLive;
	private String logCommand;

	private DataStoreManager dataStoreManager;

	private Map<String, Boolean> portRoomAvailable = new HashMap<String, Boolean>() {
		private static final long serialVersionUID = 1L;
		{
			for (String portName : PORT_NAMES) {
				portRoomAvailable.put(portName, false);
			}
		}
	};

	public PortScanTask(DataStoreManager dataStoreManager, int roomNum, String logCommand) {
		super();
		this.dataStoreManager = dataStoreManager;
		this.isRoomLive = new boolean[roomNum];
		this.logCommand = logCommand;
	}

	@Override
	public Void call() throws Exception {
		// TODO Auto-generated method stub
		while (true) {
			@SuppressWarnings("unchecked")
			Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();

			// iterate through, looking for the port
			while (portEnum.hasMoreElements()) {
				CommPortIdentifier currPortId = portEnum.nextElement();
				for (String portName : PORT_NAMES) {
					if (currPortId.getName().equals(portName)) {
						if (!portRoomAvailable.get(portName)) {
							portRoomAvailable.put(portName, true);
							Log.info("Open " + portName);
							controllerInit(currPortId);
						}
						break;
					}
				}
			}
			Thread.sleep(TIME_INTERVAL);
		}
	}

	private void controllerInit(CommPortIdentifier currPortId) throws Exception {
		// open serial port, and use class name for the appName.
		SerialPort serialPort = (SerialPort) currPortId.open(this.getClass().getName(), TIME_OUT);

		// set port parameters
		serialPort.setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

		DehumidRoomController dehumid = new DehumidRoomController(dataStoreManager, serialPort, logCommand);
		dehumid.addDisconnectListener(new SerialPortDisconnectListener() {

			@Override
			public void onDisconnectEvent(String portName, int room) {
				// TODO Auto-generated method stub
				portRoomAvailable.put(portName, false);
				dataStoreManager.clearRoomDevices(room - 2);
				isRoomLive[room - 2] = false;
				Log.info("Close " + portName);
			}

		});
		dehumid.addConnectListener(new SerialPortConnectListener() {

			@Override
			public void onConnectEvent(int room) {
				// TODO Auto-generated method stub
				isRoomLive[room - 2] = true;
			}
		});
		dehumid.initialize();
	}
}
