package edu.ncku.uscc;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.io.ModbusTCPSlave;
import edu.ncku.uscc.io.SerialPortDisconnectListener;
import edu.ncku.uscc.util.Log;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class DehumidSystem {

	private static final int NUM_ROOMS = 1;
	private static final int REGISTERS_A_DEVICE = 4;
	private static final int DEVICES_A_ROOM = 9;
	private static final int TIME_OUT = 2000;
	private static final int DATA_RATE = 9600;
	
	private static Logger logger = Log.getLogger();
	private static SerialPort serialPort;
	private static ModbusTCPSlave slave;
	private final static CountDownLatch LATCH = new CountDownLatch(1);
	
	private static Map<String, Boolean> portRoomAvailable = new HashMap<String, Boolean>();
	private static final String PORT_NAMES[] = { 
			"/dev/ttyUSB0", // Linux
			"/dev/ttyUSB1",
			"/dev/ttyUSB2",
			"/dev/ttyUSB3"
	};
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		portRoomAvailable.put("/dev/ttyUSB0", false);
		portRoomAvailable.put("/dev/ttyUSB1", false);
		portRoomAvailable.put("/dev/ttyUSB2", false);
		portRoomAvailable.put("/dev/ttyUSB3", false);
		
		try{
			Log.init();
			
			slave = new ModbusTCPSlave(NUM_ROOMS * DEVICES_A_ROOM * REGISTERS_A_DEVICE);
			slave.initialize();
			System.out.println("Modbus TCP Slave Started...");
			
			Timer timer = new Timer();
			timer.schedule(new PortScanTask(), 0, 1000);
			LATCH.await();
			
		}catch(Exception e){
			logger.error(e.toString(), e);
		}
		
		
	}
	
	public static class PortScanTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

				// iterate through, looking for the port
				while (portEnum.hasMoreElements()) {
					CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
					for (String portName : PORT_NAMES) {
						if (currPortId.getName().equals(portName) && !portRoomAvailable.get(portName)) {
							System.out.println(portName);
							portRoomAvailable.put(portName, true);
							logger.info(portName);
							
							// open serial port, and use class name for the appName.
							serialPort = (SerialPort) currPortId.open(this.getClass().getName(),
									TIME_OUT);
							
							// set port parameters
							serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8,
									SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
							
							DehumidRoomController dehumid = new DehumidRoomController(slave,
									serialPort);
							dehumid.addDisconnectListener(new SerialPortDisconnectListener(){

								@Override
								public void onDisconnectEvent(String portName) {
									// TODO Auto-generated method stub
									portRoomAvailable.put(portName, false);
								}
								
							});
							dehumid.initialize();
							break;
						}
					}
				}			
			}catch(Exception e){
				LATCH.countDown();
			}
		}
		
	}

}
