package edu.ncku.uscc.io;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.Log;

public class ScanRoomReply extends AbstractReply implements IDehumidRepSet{
	
	private ScanRoomCmd scanRoomCmd;
	private int roomScanIndex;

	public ScanRoomReply(DehumidRoomControllerEX controller,
			DataStoreManager dataStoreManager,
			int roomIndex) {
		super(controller, dataStoreManager, roomIndex);
		// TODO Auto-generated constructor stub
		this.roomScanIndex = roomIndex;
		
		if(cmd instanceof ScanRoomCmd){
			scanRoomCmd = (ScanRoomCmd) cmd;
		}else{
			// throw new InvaidArgumentException();
		}		
	}

	@Override
	public void replyEvent(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		
		if (rxBuf == DEHUMID_REP_OK
				|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
			Log.info("Scan room index : " + roomScanIndex);
			controller.setRoomIndex(roomScanIndex);
		}
	}

	@Override
	public void ackHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.addScanRoomQueue(scanRoomCmd);
		controller.nextCmd(null);
	}

	@Override
	public void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.nextScanRoomCmd(scanRoomCmd);
	}

}
