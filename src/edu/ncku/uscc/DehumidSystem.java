package edu.ncku.uscc;

import edu.ncku.uscc.io.Dehumid;
import edu.ncku.uscc.io.ModbusTCPSlave;

public class DehumidSystem {

	private static final int NUM_ROOMS = 1;
	private static final int REGISTERS_A_DEVICE = 4;
	private static final int DEVICES_A_ROOM = 9;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			ModbusTCPSlave slave = new ModbusTCPSlave(NUM_ROOMS * DEVICES_A_ROOM * REGISTERS_A_DEVICE);
			slave.initialize();
			System.out.println("Modbus TCP Slave Started...");

			Dehumid demumid = new Dehumid(slave, NUM_ROOMS);
			demumid.initialize();
			System.out.println("Started");
		}catch(Exception e){
//			System.err.println(e.toString());
			e.printStackTrace();
		}
		
		
	}

}
