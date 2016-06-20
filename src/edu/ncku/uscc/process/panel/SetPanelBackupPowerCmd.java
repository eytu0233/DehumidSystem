package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.BackupData;
import edu.ncku.uscc.io.DehumidRoomController;

public class SetPanelBackupPowerCmd extends SynPanelCommand {
	
	private BackupData data;

	public SetPanelBackupPowerCmd(DehumidRoomController controller, BackupData data) {
		super(controller);
		// TODO Auto-generated constructor stub
		this.data = data;
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub

		return data.isPanelOn() ? (byte) PANEL_REQ_START : (byte) PANEL_REQ_SHUTDOWM;
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
			panel.setLive(false);
			return false;
		}
	}
	
	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.jumpCmdQueue(new SetPanelBackupModeCmd(controller, data));
		controller.nextCmd(null);
		controller.initPanelTimeoutCounter();
	}

}
