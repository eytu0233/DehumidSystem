package edu.ncku.uscc.process.dehumidifier;

import edu.ncku.uscc.io.BackupData;
import edu.ncku.uscc.io.DehumidRoomController;

public class SetDehumidifierBackupByItselfHumidSetCmd extends SynDehumidifierCmd {
	
	private BackupData data;
	
	public SetDehumidifierBackupByItselfHumidSetCmd(DehumidRoomController controller, int did, BackupData data) {
		super(controller, did);
		// TODO Auto-generated constructor stub
		this.data = data;
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		
		dehumidifier.setHumidSetValue(data.getDehumidHumidSet(did));
		
		return (byte) DEHUMID_REQ_DEHUMIDITY_SET;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		dehumidifier.setCompressorRunning(rxBuf == DEHUMID_REP_COMPRESSOR_RUNNING);
		
		if (rxBuf == DEHUMID_REP_OK || rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL || rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL
				|| rxBuf == DEHUMID_REP_FAN_ABNORMAL || rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL
				|| rxBuf == DEHUMID_REP_COMPRESSOR_RUNNING) {
			followCmd(new SetDehumidifierByItselfHumidSetCmd(controller, did), null);
			return true;
		} else {
			return false;
		}

	}

}
