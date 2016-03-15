package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;

public class SetPanelBackupModeCmd extends SynPanelCommand {
	
	private boolean backupMode;

	public SetPanelBackupModeCmd(DehumidRoomController controller, boolean b) {
		super(controller);
		// TODO Auto-generated constructor stub
		this.backupMode = b;
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if (!panel.isOn()) {
			return SKIP;
		}
		
		// dehumid mode true; dry clothes mode false
		return backupMode ? (byte) PANEL_REQ_DEHUMID_MODE : (byte) PANEL_REQ_DRYCLOTHES_MODE;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			boolean dehumid_mode = ((getTxBuf() == (byte) PANEL_REQ_DEHUMID_MODE));
			panel.setModeDehumid(dehumid_mode);
			panel.setModeDry(!dehumid_mode);
			panel.setLive(true);
			controller.log_info(String.format("Panel %d changes mode.",
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
		controller.log_warn(String.format("Panel %d is not live.", offsetRoomIndex));
	}

}
