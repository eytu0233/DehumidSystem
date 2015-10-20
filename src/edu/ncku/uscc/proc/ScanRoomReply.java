package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.Log;

public class ScanRoomReply extends AbstractReply implements IDehumidReplySet{
	
	private int roomScanIndex;

	public ScanRoomReply(DehumidRoomControllerEX controller,
			int roomIndex) {
		super(controller);
		// TODO Auto-generated constructor stub
		this.roomScanIndex = roomIndex;		
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
		controller.addScanRoomQueue((ScanRoomCmd) cmd);
		controller.nextCmd(null);
	}

	@Override
	public void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.nextScanRoomCmd((ScanRoomCmd) cmd);
	}

}
