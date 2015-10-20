package edu.ncku.uscc;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.io.ModbusTCPSlave;
import edu.ncku.uscc.io.SerialPortDisconnectListener;
import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.Log;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class DehumidSystem {

	private static final int NUM_ROOMS = 4;
	private static final int REGISTERS_A_DEVICE = 4;
	private static final int DEVICES_A_ROOM = 9;
	private static final int TIME_OUT = 2000;
	private static final int BAUD_RATE = 9600;

	private static final String PORT_NAMES[] = { 
			"/dev/ttyUSB0", // Linux
			"/dev/ttyUSB1",
			"/dev/ttyUSB2",
			"/dev/ttyUSB3"
	};
	
	private static Map<String, Boolean> portRoomAvailable = new HashMap<String, Boolean>();
	/** A interface to manager modbus dataStore for DehumidSystem */
	private static DataStoreManager dataStoreManager;
	
	private final static CountDownLatch LATCH = new CountDownLatch(1);
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try{
			Log.init();

			for(String portName : PORT_NAMES){
				portRoomAvailable.put(portName, false);
			}
			
			ModbusTCPSlave slave = new ModbusTCPSlave(NUM_ROOMS * DEVICES_A_ROOM * REGISTERS_A_DEVICE);
			slave.initialize();
			Log.info("Modbus TCP Slave Started...");
			
			dataStoreManager = new DataStoreManager(slave);
			
			ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
			scheduledThreadPool.scheduleAtFixedRate(new PortScanTask(), 0, 1, TimeUnit.SECONDS);
			Log.info("PortScanTask Started...");
			LATCH.await();
			
		}catch(Exception e){
			Log.error(e, e);
		}
		
		Log.debug("System stop!");
	}
	
	public static class PortScanTask extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				@SuppressWarnings("unchecked")
				Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();

				// iterate through, looking for the port
				while (portEnum.hasMoreElements()) {
					CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
					for (String portName : PORT_NAMES) {
						if (currPortId.getName().equals(portName)) {							
							if(!portRoomAvailable.get(portName)){
								portRoomAvailable.put(portName, true);
								Log.info("Open " + portName);
								
								// open serial port, and use class name for the appName.
								SerialPort serialPort = (SerialPort) currPortId.open(this.getClass().getName(),
										TIME_OUT);
								
								// set port parameters
								serialPort.setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8,
										SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
								
								DehumidRoomControllerEX dehumid = new DehumidRoomControllerEX(dataStoreManager,
										serialPort);
								dehumid.addDisconnectListener(new SerialPortDisconnectListener(){

									@Override
									public void onDisconnectEvent(String portName) {
										// TODO Auto-generated method stub
										portRoomAvailable.put(portName, false);
										Log.info("Close " + portName);
									}
									
								});
								dehumid.initialize();
							}	

							break;
						}
					}
				}		
				
				
				
			}catch(Exception e){
				Log.error(e, e);
				LATCH.countDown();
			}
		}
		
	}

}
