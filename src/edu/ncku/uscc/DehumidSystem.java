package edu.ncku.uscc;

import java.util.concurrent.Executors;
import edu.ncku.uscc.io.ModbusTCPSlave;
import edu.ncku.uscc.io.PortScanTask;
import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.Log;

/**
 * 
 * This class is used to be as the entrance of this system.
 * This class has two main feature. One is to start modbus slave thread.
 * Another is to start usb port scanning thread. This thread is to implement the feature of hot usb which
 * lets the usb ports on raspberry pi be inserted and pulled arbitrarily.
 * 
 * @author steve chen
 *
 */
public class DehumidSystem {
	
	private static final String LOG_COMMAND_DEFAULT = "-Dew";

	private static final int NUM_ROOMS = 4;
	private static final int REGISTERS_A_DEVICE = 4;
	private static final int DEVICES_A_ROOM = 9;
	
	/** A interface to manager modbus dataStore for DehumidSystem */
	private static DataStoreManager dataStoreManager;	
	
	private static String logCommand = "";
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try{			
			for (String arg : args)
				logCommand = logCommand.concat(arg);
			if (!logCommand.matches("-D[deiw]+[1234]*"))
				logCommand = LOG_COMMAND_DEFAULT;
			
			Log.init(logCommand);
			
			// start the modbus tcp slave thread
			ModbusTCPSlave slave = new ModbusTCPSlave(NUM_ROOMS * DEVICES_A_ROOM * REGISTERS_A_DEVICE);
			slave.initialize();
			Log.info("Modbus TCP Slave Started...");
			
			// the data store manager is used by all DehumidRoomController thread
			dataStoreManager = new DataStoreManager(slave);
			
			
			/*
			 * Date : 2016/7/16
			 * Author : Steve Chen
			 * Here is a new code to simplify how PortScanTask class is initialized, and how to 
			 * lock main thread with future.get() method.
			 * There are two advantages to use this Future class.
			 * 1. We can use get method to lock main thread.
			 * 2. If and only if a exception of PortScanTask occurs, the get method will return.
			 * At that time we use catch like line 66 in this class to handle exception.
			 */
			Log.info("PortScanTask Started...");
			Executors.newSingleThreadExecutor().submit(new PortScanTask(dataStoreManager, NUM_ROOMS, logCommand)).get();
			
			
		}catch(Exception e){
			Log.error(e, e);
		}
		
		Log.debug("System stop!");
	}	
}
