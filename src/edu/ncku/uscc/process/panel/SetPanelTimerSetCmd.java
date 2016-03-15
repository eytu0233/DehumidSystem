package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;

public class SetPanelTimerSetCmd extends SynPanelCommand {

	public SetPanelTimerSetCmd(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if (!panel.isOn())
			return SKIP;
		controller.log_info(String.format("Start to change set of timer of Panel %d", 
				offsetRoomIndex));
		return (byte) panel.getTimerSet();
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			panel.setLive(true);
			panel.setTimerSetValue(panel.getTimerSet());
			
			controller.log_info(String.format("Change set of timer of Panel %d success", 
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
		controller.nextCmd(null);
	}
}
