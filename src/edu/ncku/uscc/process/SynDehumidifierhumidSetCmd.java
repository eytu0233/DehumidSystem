package edu.ncku.uscc.process;

import edu.ncku.uscc.io.DehumidRoomControllerEX;

public class SynDehumidifierhumidSetCmd extends SynDehumidifierCmd {

	public SynDehumidifierhumidSetCmd(DehumidRoomControllerEX controller, int did) {
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
			return true;
		}else{
			return false;
		}
		
	}

}
