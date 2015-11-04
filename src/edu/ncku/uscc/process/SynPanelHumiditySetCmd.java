package edu.ncku.uscc.process;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.Log;

public class SynPanelHumiditySetCmd extends SynPanelCommand {

	public SynPanelHumiditySetCmd(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if (!panel.isOn()) {
			return SKIP;
		}

		if (dataStoreManager.isPanelDehumiditySetChange(offsetRoomIndex)) {
			return (byte) PANEL_REQ_SETTING_HUMID_Set;
		} else {
			return (byte) PANEL_REQ_HUMID_SET;
		}
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			this.setSubCommand(new SetPanelHumiditySetCmd(controller));
			panel.setLive(true);
			return true;
		} else if (rxBuf >= 0) {
			panel.setHumidSetValue((int) rxBuf);
			panel.setLive(true);
			Log.info(String.format("The humidity set of Panel %d is %d.",
					offsetRoomIndex, (int) rxBuf));
			return true;
		} else {
			return false;
		}
	}

}
