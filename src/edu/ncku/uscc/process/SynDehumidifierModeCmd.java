package edu.ncku.uscc.process;

import edu.ncku.uscc.io.DehumidRoomControllerEX;

public class SynDehumidifierModeCmd extends SynDehumidifierCmd {

	public SynDehumidifierModeCmd(DehumidRoomControllerEX controller, int did) {
		super(controller, did);
		// TODO Auto-generated constructor stub
	}

//	@Override
//	public void startCommand() throws Exception {
//		// TODO Auto-generated method stub
//		if(!dehumidifier.isOn()) return;
//		super.startCommand();
//	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if(panel.isModeDehumid()){
			return (byte) DEHUMID_REQ_DEHUMID_MODE;			
		}else if(panel.isModeDry()){
			return (byte) DEHUMID_REQ_DRY_CLOTHES_MODE;			
		}else{
			return SKIP;
		}		
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == DEHUMID_REP_OK
				|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
			dehumidifier.setModeDehumid(panel.isModeDehumid());
			dehumidifier.setModeDry(panel.isModeDry());
//			checkRates[did] = INITIAL_RATE;
		}
	}

}
