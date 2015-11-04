package edu.ncku.uscc.process;

import edu.ncku.uscc.io.DehumidRoomControllerEX;

public class SetDehumidifierHumidSetCmd extends SynDehumidifierCmd {

	public SetDehumidifierHumidSetCmd(DehumidRoomControllerEX controller, int did) {
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
		if (rxBuf == DEHUMID_REP_OK
				|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
			dehumidifier.setHumidSetValue(panel.getHumidSet());
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
