package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;

public class SetPanelBackupHumiditySetCmd extends SynPanelCommand {

	public SetPanelBackupHumiditySetCmd(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if (!panel.isOn()) {
			return SKIP;
		}

		return (byte) PANEL_REQ_SETTING_HUMID_Set;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			followCmd(new SetPanelHumiditySetCmd(controller), this);
			panel.setLive(true);
			return true;
		} else {
			return false;
		}
	}

}
