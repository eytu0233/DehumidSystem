package edu.ncku.uscc.io;

public class ScanRoomCmd extends Command {
	

	public ScanRoomCmd(DehumidRoomControllerEX controller, int roomScanIndex, int did, int tolerance) {		
		super(controller, new ScanRoomRequest(controller.getDataStoreManager(), controller.getOutputStream(), controller.getSerialPort(), roomScanIndex, did), new ScanRoomReply(controller, controller.getDataStoreManager(), roomScanIndex), tolerance);
		// TODO Auto-generated constructor stub
	}

}
