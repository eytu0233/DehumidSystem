package edu.ncku.uscc.process.dehumidifier;

import edu.ncku.uscc.io.DehumidRoomController;

public class SynDehumidifierByItselfModeCmd extends SynDehumidifierCmd {

	public SynDehumidifierByItselfModeCmd(DehumidRoomController controller, int did) {
		super(controller, did);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if (dehumidifier.isOn())
			controller.jumpCmdQueue(new SynDehumidifierByItselfHumidSetCmd(controller, did));
		if (dataStoreManager.isDehumidifiersModeChange(offsetRoomIndex, did)) {
			if (dehumidifier.isModeDehumid())
				return (byte) DEHUMID_REQ_DEHUMID_MODE;
			else if (dehumidifier.isModeDry())
				return (byte) DEHUMID_REQ_DRY_CLOTHES_MODE;
			else
				return SKIP;
		}
		return SKIP;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == DEHUMID_REP_OK || rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL || rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL
				|| rxBuf == DEHUMID_REP_FAN_ABNORMAL || rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL) {
			dehumidifier.setModeDehumid(dehumidifier.isModeDehumid());
			dehumidifier.setModeDry(dehumidifier.isModeDry());
			controller.log_debug(String.format("Dehumidifier %d Mode is set.", did));
			return true;
		} else {
			return false;
		}
	}

}