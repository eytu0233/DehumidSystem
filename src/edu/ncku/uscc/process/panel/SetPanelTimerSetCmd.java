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
		if (!panel.isLive())
			return SKIP;

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
}
