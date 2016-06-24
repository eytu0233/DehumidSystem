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
		
		int humidSet = panel.getHumidSet();
		humidSet = humidSet <= 50 ? 50 : humidSet >= 90 ? 90 : humidSet;
		humidSet = (humidSet - 45) / 5;
		
		return (byte) humidSet;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == DEHUMID_REP_OK || rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL || rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL
				|| rxBuf == DEHUMID_REP_FAN_ABNORMAL || rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL) {
			dehumidifier.setHumidSetValue(panel.getHumidSet());
			controller.log_debug(String.format("Dehumid %d in panel %d humid set", did, offsetRoomIndex));
			return true;
		} else {
			return false;
		}
	}

}
