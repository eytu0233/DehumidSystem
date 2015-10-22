package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;

public class SYNDehumidifierModeCmd extends SYNDehumidifierCmd {

	public SYNDehumidifierModeCmd(DehumidRoomControllerEX controller, int did) {
		super(controller, did);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void startCommand() throws Exception {
		// TODO Auto-generated method stub
		if(!dehumidifier.isOn()) return;
		super.startCommand();
	}

	@Override
	protected void requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if(panel.isModeDehumid()){
			byte txBuf = (byte) DEHUMID_REQ_DEHUMID_MODE;
			this.setTxBuf(txBuf);			
		}else{
			this.skipCommand();
		}		
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == DEHUMID_REP_OK
				|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
			dehumidifier.setModeDehumid(true);
			dehumidifier.setModeDry(false);
//			checkRates[did] = INITIAL_RATE;
		}
	}

}
