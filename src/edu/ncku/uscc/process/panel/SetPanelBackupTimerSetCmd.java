package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.util.Log;

public class SetPanelBackupTimerSetCmd extends SynPanelCommand {

	private DehumidRoomController controller;
	
	public SetPanelBackupTimerSetCmd(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
		this.controller = controller;
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if (!panel.isOn()) {
			return SKIP;
		}
		
		return (byte) PANEL_REQ_SETTING_TIMER;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if(rxBuf == PANEL_REP_OK) {
			Log.info(String.format("Add change command set of timer of Panel %d", offsetRoomIndex));
			followCmd(new SetPanelTimerSetCmd(controller), this);
			return true;
		}else {
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
