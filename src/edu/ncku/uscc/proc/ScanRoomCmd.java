package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;

public class ScanRoomCmd extends Command {

	public ScanRoomCmd(DehumidRoomControllerEX controller, int roomScanIndex,
			int did, int tolerance) {
		super(controller, new ScanRoomRequest(controller, roomScanIndex, did),
				new ScanRoomReply(controller, roomScanIndex), tolerance);
		// TODO Auto-generated constructor stub
	}

}
