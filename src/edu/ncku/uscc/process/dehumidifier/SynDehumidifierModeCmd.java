package edu.ncku.uscc.process.dehumidifier;

import edu.ncku.uscc.io.DehumidRoomController;

public class SynDehumidifierModeCmd extends SynDehumidifierCmd {

	public SynDehumidifierModeCmd(DehumidRoomController controller, int did) {
		super(controller, did);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if (panel.isModeDehumid()) {
			return (byte) DEHUMID_REQ_DEHUMID_MODE;
		} else if (panel.isModeDry()) {
			return (byte) DEHUMID_REQ_DRY_CLOTHES_MODE;
		} else {
			return SKIP;
		}
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == DEHUMID_REP_OK || rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL || rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL
				|| rxBuf == DEHUMID_REP_FAN_ABNORMAL || rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL) {
			dehumidifier.setModeDehumid(panel.isModeDehumid());
			dehumidifier.setModeDry(panel.isModeDry());
			if (panel.isOn())
				controller.jumpCmdQueue(new SynDehumidifierHumidSetCmd(controller, did));
			return true;
		} else {
			return false;
		}
	}

}
