package edu.ncku.uscc.process.dehumidifier;

import edu.ncku.uscc.io.DehumidRoomController;

public class SynDehumidifierByItselfHumidSetCmd extends SynDehumidifierCmd {

	public SynDehumidifierByItselfHumidSetCmd(DehumidRoomController controller, int did) {
		super(controller, did);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		
		return (byte) DEHUMID_REQ_DEHUMIDITY_SET;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		dehumidifier.setCompressorRunning(rxBuf == DEHUMID_REP_COMPRESSOR_RUNNING);
		
		if (rxBuf == DEHUMID_REP_OK || rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL || rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL
				|| rxBuf == DEHUMID_REP_FAN_ABNORMAL || rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL
				|| rxBuf == DEHUMID_REP_COMPRESSOR_RUNNING) {
			controller.jumpCmdQueue(new AskDehumidifierHumidityCmd(controller, did));
			followCmd(new SetDehumidifierByItselfHumidSetCmd(controller, did), null);
			return true;
		} else {
			return false;
		}

	}

}
