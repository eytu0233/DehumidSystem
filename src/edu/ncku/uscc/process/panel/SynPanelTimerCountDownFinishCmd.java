package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.util.Log;

public class SynPanelTimerCountDownFinishCmd extends SynPanelCommand {

	public SynPanelTimerCountDownFinishCmd(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if (!panel.isOn()) {
			return SKIP;
		}
		
		return (byte) PANEL_REQ_SHUTDOWM;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OFF) {
			panel.setOn(false);
			Log.debug(String.format("Panel %d is turned off by timer.",
					offsetRoomIndex));
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.nextCmd(null);
	}

	@Override
	protected void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
//		Log.warn(String.format("Panel %d is not live.", offsetRoomIndex));
		controller.nextCmd(null);
	}

}
