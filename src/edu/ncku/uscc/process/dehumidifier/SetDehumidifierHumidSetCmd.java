package edu.ncku.uscc.process.dehumidifier;

import edu.ncku.uscc.io.DehumidRoomController;

public class SetDehumidifierHumidSetCmd extends SynDehumidifierCmd {

	public SetDehumidifierHumidSetCmd(DehumidRoomController controller, int did) {
		super(controller, did);
		// TODO Auto-generated constructor stub
		// cancel the preCommand which is notifyDeviceIDCmd
		this.setPreCommand(null);
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		return (byte) panel.getHumidSet();
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == DEHUMID_REP_OK || rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL || rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL
				|| rxBuf == DEHUMID_REP_FAN_ABNORMAL || rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL) {
			dehumidifier.setHumidSetValue(panel.getHumidSet());
			return true;
		} else {
			return false;
		}
	}

}