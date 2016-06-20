package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.BackupData;
import edu.ncku.uscc.io.DehumidRoomController;

public class SetPanelBackupModeCmd extends SynPanelCommand {
	
	private BackupData data;

	public SetPanelBackupModeCmd(DehumidRoomController controller, BackupData data) {
		super(controller);
		// TODO Auto-generated constructor stub
		this.data = data;
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		
		// dehumid mode true; dry clothes mode false
		return data.isPanelModeDry() ? (byte) PANEL_REQ_DRYCLOTHES_MODE : (byte) PANEL_REQ_DEHUMID_MODE;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			boolean dehumid_mode = ((getTxBuf() == (byte) PANEL_REQ_DEHUMID_MODE));
			panel.setModeDehumid(dehumid_mode);
			panel.setModeDry(!dehumid_mode);
			panel.setLive(true);
//			Log.info(String.format("Panel %d changes mode.",
//					offsetRoomIndex));
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.jumpCmdQueue(new SetPanelBackupTimerSetCmd(controller, data));
		controller.nextCmd(null);
		controller.initPanelTimeoutCounter();
	}

}