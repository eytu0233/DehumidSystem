package edu.ncku.uscc.process.dehumidifier;

import edu.ncku.uscc.io.BackupData;
import edu.ncku.uscc.io.DehumidRoomController;

public class SetDehumidifierBackupByItselfPowerCmd extends SynDehumidifierCmd {
	
	private BackupData data;
	
	public SetDehumidifierBackupByItselfPowerCmd(DehumidRoomController controller, int did, BackupData data) {
		super(controller, did);
		// TODO Auto-generated constructor stub
		this.data = data;
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub		
		return data.isDehumidOn(did) ? (byte) DEHUMID_REQ_ON : (byte) DEHUMID_REQ_OFF;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == DEHUMID_REP_OK || rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL || rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL
				|| rxBuf == DEHUMID_REP_FAN_ABNORMAL || rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL) {
			dehumidifier.setOn(data.isDehumidOn(did));
//			controller.log_debug(String.format("Dehumidifier %d Power is set.", did));
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.jumpCmdQueue(new SetDehumidifierBackupByItselfModeCmd(controller, did, data));
		controller.nextCmd(null);
	}

}
