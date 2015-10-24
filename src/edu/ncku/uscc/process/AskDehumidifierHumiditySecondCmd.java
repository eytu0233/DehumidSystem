package edu.ncku.uscc.process;

import edu.ncku.uscc.io.DehumidRoomControllerEX;

public class AskDehumidifierHumiditySecondCmd extends SynDehumidifierCmd {

	private int digitOnes;
	
	public AskDehumidifierHumiditySecondCmd(DehumidRoomControllerEX controller, int did, int digitOnes) {
		super(controller, did);
		// TODO Auto-generated constructor stub
		this.digitOnes = digitOnes;
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		return (byte) DEHUMID_REQ_DEHUMIDITY_DIGIT_TENS;
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf >= 0) {
			dehumidifier.setHumid(rxBuf * 10 + digitOnes);
		}
	}

	@Override
	protected void finishCommandHandler() throws Exception {
		// TODO Auto-generated method stub
		// avoid to call twice nextCmd method
	}	
	

}
