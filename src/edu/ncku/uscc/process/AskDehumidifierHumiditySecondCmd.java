package edu.ncku.uscc.process;

public class AskDehumidifierHumiditySecondCmd extends SynDehumidifierCmd {

	private int digitOnes;
	
	public AskDehumidifierHumiditySecondCmd(DehumidRoomControllerEX controller, int did, int digitOnes) {
		super(controller, did);
		// TODO Auto-generated constructor stub
		this.digitOnes = digitOnes;
	}

	@Override
	protected void requestHandler() throws Exception {
		// TODO Auto-generated method stub
		this.setTxBuf((byte) DEHUMID_REQ_DEHUMIDITY_DIGIT_TENS);
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf >= 0) {
			dehumidifier.setHumid(rxBuf * 10 + digitOnes);
		}
	}

	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		// avoid to call twice nextCmd method
	}	
	

}
