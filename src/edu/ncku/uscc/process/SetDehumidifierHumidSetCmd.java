package edu.ncku.uscc.process;

public class SetDehumidifierHumidSetCmd extends SynDehumidifierCmd {

	public SetDehumidifierHumidSetCmd(DehumidRoomControllerEX controller, int did) {
		super(controller, did);
		// TODO Auto-generated constructor stub
		// cancel the preCommand which is notifyDeviceIDCmd
		this.setPreCommand(null);
	}

	@Override
	protected void requestHandler() throws Exception {
		// TODO Auto-generated method stub
		this.setTxBuf((byte) panel.getHumidSet());
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == DEHUMID_REP_OK
				|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
			dehumidifier.setHumidSetValue(panel.getHumidSet());
		}
	}
	
	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
