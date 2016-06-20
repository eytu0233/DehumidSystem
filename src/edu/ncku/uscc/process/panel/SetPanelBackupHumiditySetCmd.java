package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.BackupData;
import edu.ncku.uscc.io.DehumidRoomController;

public class SetPanelBackupHumiditySetCmd extends SynPanelCommand {
	
	private BackupData data;

	public SetPanelBackupHumiditySetCmd(DehumidRoomController controller, BackupData data) {
		super(controller);
		// TODO Auto-generated constructor stub
		this.data = data;
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		
		panel.setHumidSetValue(data.getPanelHumidSet());

		return (byte) PANEL_REQ_SETTING_HUMID_Set;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			// set backup humidity set value
			followCmd(new SetPanelHumiditySetCmd(controller), this);
			panel.setLive(true);
			return true;
		} else {
			return false;
		}
	}

}
