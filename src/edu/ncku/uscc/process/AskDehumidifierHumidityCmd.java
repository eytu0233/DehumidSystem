package edu.ncku.uscc.process;

import edu.ncku.uscc.io.DehumidRoomControllerEX;

public class AskDehumidifierHumidityCmd extends SynDehumidifierCmd {
	

	public AskDehumidifierHumidityCmd(DehumidRoomControllerEX controller, int did) {
		super(controller, did);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		return (byte) DEHUMID_REQ_DEHUMIDITY_DIGIT_ONES;	
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf >= 0) {
			this.setSubCommand(new AskDehumidifierHumiditySecondCmd(controller, did, rxBuf));		
			return true;
		} else{
			return false;
		}
	}

}
