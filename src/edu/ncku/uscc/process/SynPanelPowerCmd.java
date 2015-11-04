package edu.ncku.uscc.process;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.Log;

public class SynPanelPowerCmd extends SynPanelCommand {

	public SynPanelPowerCmd(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub

		// Check the power status whether iFix has changed or not
		if (dataStoreManager.isPanelONOFFChange(offsetRoomIndex)) {
			/*
			 * When the power status has been changed, it synchronizes the power
			 * status of the real panel
			 */
			return (panel.isOn()) ? (byte) PANEL_REQ_START
					: (byte) PANEL_REQ_SHUTDOWM;
		} else {
			// Ask panel it is on or off
			return (byte) PANEL_REQ_ONOFF;
		}
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_ON) {
			panel.setOn(true);
			panel.setLive(true);
			Log.info(String.format("Panel %d is ON.", offsetRoomIndex));
			return true;
		} else if (rxBuf == PANEL_REP_OFF) {
			panel.setOn(false);
			panel.setLive(true);
			Log.info(String.format("Panel %d is OFF.", offsetRoomIndex));
			return true;
		} else {
			return false;
		}
	}

}
