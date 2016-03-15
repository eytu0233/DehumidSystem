package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;

public class SetPanelBackupPowerCmd extends SynPanelCommand {
	
	private boolean backupONOFF;

	public SetPanelBackupPowerCmd(DehumidRoomController controller, boolean b) {
		super(controller);
		// TODO Auto-generated constructor stub
		this.backupONOFF = b;
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub

		return backupONOFF ? (byte) PANEL_REQ_START : (byte) PANEL_REQ_SHUTDOWM;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_ON) {
			panel.setOn(true);
			panel.setLive(true);
			return true;
		} else if (rxBuf == PANEL_REP_OFF) {
			panel.setOn(false);
			panel.setLive(true);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.nextCmd(null);
		controller.log_info(String.format("Panel %d power is been checked.", offsetRoomIndex));
	}
	
	@Override
	protected void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.nextCmd(null);
		controller.log_warn(String.format("Panel %d is not live.", offsetRoomIndex));
	}

}
