package edu.ncku.uscc.process.dehumidifier;

import edu.ncku.uscc.io.DehumidRoomController;

public class SynDehumidifierhumidSetCmd extends SynDehumidifierCmd {

	public SynDehumidifierhumidSetCmd(DehumidRoomController controller, int did) {
		super(controller, did);
		// TODO Auto-generated constructor stub
	}

//	@Override
//	public void startCommand() throws Exception {
//		// TODO Auto-generated method stub
//		if(!dehumidifier.isOn() || !panel.isHumidSet()) return;
//		super.startCommand();
//	}
	
	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		return (byte) DEHUMID_REQ_DEHUMIDITY_SET;	
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if(rxBuf == DEHUMID_REP_OK){
			this.setSubCommand(new SetDehumidifierHumidSetCmd(controller, did));
			controller.initCheckRate(did);
			return true;
		}else{
			controller.dropRate(did);
			return false;
		}
		
	}

}
