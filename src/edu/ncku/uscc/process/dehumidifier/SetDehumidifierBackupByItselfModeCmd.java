package edu.ncku.uscc.process.dehumidifier;

import edu.ncku.uscc.io.BackupData;
import edu.ncku.uscc.io.DehumidRoomController;

public class SetDehumidifierBackupByItselfModeCmd extends SynDehumidifierCmd {
	
	private BackupData data;
	
	public SetDehumidifierBackupByItselfModeCmd(DehumidRoomController controller, int did, BackupData data) {
		super(controller, did);
		// TODO Auto-generated constructor stub
		this.data = data;
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		return data.isDehumidModeDry(did) ? (byte) DEHUMID_REQ_DRY_CLOTHES_MODE : (byte) DEHUMID_REQ_DEHUMID_MODE;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		dehumidifier.setCompressorRunning(rxBuf == DEHUMID_REP_COMPRESSOR_RUNNING);
		
		if (rxBuf == DEHUMID_REP_OK || rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL || rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL
				|| rxBuf == DEHUMID_REP_FAN_ABNORMAL || rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL
				|| rxBuf == DEHUMID_REP_COMPRESSOR_RUNNING) {
			dehumidifier.setModeDehumid(!data.isDehumidModeDry(did));
			dehumidifier.setModeDry(data.isDehumidModeDry(did));
//			controller.log_debug(String.format("Dehumidifier %d Mode is set.", did));
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.jumpCmdQueue(new SetDehumidifierBackupByItselfHumidSetCmd(controller, did, data));
		controller.nextCmd(null);
	}

}
