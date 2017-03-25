package edu.ncku.uscc.process.dehumidifier;

import edu.ncku.uscc.io.DehumidRoomController;

public class SynDehumidifierPowerCmd extends SynDehumidifierCmd {

	public SynDehumidifierPowerCmd(DehumidRoomController controller, int did) {
		super(controller, did);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		return (panel.isOn()) ? (byte) DEHUMID_REQ_ON : (byte) DEHUMID_REQ_OFF;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		
		dehumidifier.setCompressorRunning(rxBuf == DEHUMID_REP_COMPRESSOR_RUNNING);
		controller.log_debug(String.format("dehumid %d receive compressor running %b", 
				did, rxBuf == DEHUMID_REP_COMPRESSOR_RUNNING));
		
		if (rxBuf == DEHUMID_REP_OK || rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL || rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL
				|| rxBuf == DEHUMID_REP_FAN_ABNORMAL || rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL
				|| rxBuf == DEHUMID_REP_COMPRESSOR_RUNNING) {
			dehumidifier.setOn(panel.isOn());
			controller.jumpCmdQueue(new SynDehumidifierModeCmd(controller, did));
			return true;
		} else {
			return false;
		}
	}

}
