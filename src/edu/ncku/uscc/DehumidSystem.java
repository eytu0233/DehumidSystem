package edu.ncku.uscc;

import org.apache.log4j.Logger;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.io.ModbusTCPSlave;
import edu.ncku.uscc.util.Log;

public class DehumidSystem {

	private static final int NUM_ROOMS = 1;
	private static final int REGISTERS_A_DEVICE = 4;
	private static final int DEVICES_A_ROOM = 9;
	
	private static Logger logger = Log.getLogger();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try{
			Log.init();
			
			ModbusTCPSlave slave = new ModbusTCPSlave(NUM_ROOMS * DEVICES_A_ROOM * REGISTERS_A_DEVICE);
			slave.initialize();
			System.out.println("Modbus TCP Slave Started...");
			
		}catch(Exception e){
			logger.error(e.toString(), e);
		}
		
		
	}

}
