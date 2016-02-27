package edu.ncku.uscc.process.dehumidifier;

import edu.ncku.uscc.io.DehumidRoomController;

public class AskDehumidifierHumiditySecondCmd extends SynDehumidifierCmd {

	private int digitOnes;
	
	public AskDehumidifierHumiditySecondCmd(DehumidRoomController controller, int did, int digitOnes) {
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
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf >= 0 && rxBuf <= 9) {
			dehumidifier.setHumid(rxBuf * 10 + digitOnes);
			return true;
		}else{
			return false;
		}
	}	

}
