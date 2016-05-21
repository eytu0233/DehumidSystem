package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;

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
			
			controller.log_info(String.format("Panel %d is turned off by timer.",
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
		controller.initPanelTimeoutCounter();
	}

}
