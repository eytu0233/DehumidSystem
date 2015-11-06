package edu.ncku.uscc.process.dehumidifier;

import edu.ncku.uscc.io.DehumidRoomController;

public class SynDehumidifierModeCmd extends SynDehumidifierCmd {

	public SynDehumidifierModeCmd(DehumidRoomController controller, int did) {
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
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == DEHUMID_REP_OK
				|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
			dehumidifier.setModeDehumid(panel.isModeDehumid());
			dehumidifier.setModeDry(panel.isModeDry());
			controller.initCheckRate(did);
			return true;
		}else{
			controller.dropRate(did);
			return false;
		}
	}

}
