package edu.ncku.uscc.process;

import edu.ncku.uscc.io.DehumidRoomControllerEX;

public class SynDehumidifierPowerCmd extends SynDehumidifierCmd {

	public SynDehumidifierPowerCmd(DehumidRoomControllerEX controller, int did) {
		super(controller, did);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void requestHandler() throws Exception {
		// TODO Auto-generated method stub
		this.setTxBuf((panel.isOn()) ? (byte) DEHUMID_REQ_ON : (byte) DEHUMID_REQ_OFF);
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == DEHUMID_REP_OK
				|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
			dehumidifier.setOn(panel.isOn());
//			checkRates[did] = INITIAL_RATE;
		} else {			
//			checkRates[did] = drop(checkRates[did]);
		}
	}

}
