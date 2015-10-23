package edu.ncku.uscc.process;

public class SynDehumidifierModeCmd extends SynDehumidifierCmd {

	public SynDehumidifierModeCmd(DehumidRoomControllerEX controller, int did) {
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
			this.setTxBuf((byte) DEHUMID_REQ_DEHUMID_MODE);			
		}else if(panel.isModeDry()){
			this.setTxBuf((byte) DEHUMID_REQ_DRY_CLOTHES_MODE);			
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
			dehumidifier.setModeDehumid(panel.isModeDehumid());
			dehumidifier.setModeDry(panel.isModeDry());
//			checkRates[did] = INITIAL_RATE;
		}
	}

}
