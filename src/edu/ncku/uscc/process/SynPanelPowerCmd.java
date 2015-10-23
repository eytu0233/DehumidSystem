package edu.ncku.uscc.process;

import edu.ncku.uscc.util.Log;

public class SynPanelPowerCmd extends SynPanelCommand {

	public SynPanelPowerCmd(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void requestHandler() throws Exception {
		// TODO Auto-generated method stub

		// Check the power status whether iFix has changed or not
		if (dataStoreManager.isPanelONOFFChange(offsetRoomIndex)) {
			/*
			 * When the power status has been changed, it synchronizes the power
			 * status of the real panel
			 */
			this.setTxBuf((panel.isOn()) ? (byte) PANEL_REQ_START
					: (byte) PANEL_REQ_SHUTDOWM);
		} else {
			// Ask panel it is on or off
			this.setTxBuf((byte) PANEL_REQ_ONOFF);
		}
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub		
		if (rxBuf == PANEL_REP_ON) {
			panel.setOn(true);
			Log.info(String.format("Panel %d is ON.", offsetRoomIndex));
		} else if (rxBuf == PANEL_REP_OFF) {
			panel.setOn(false);
			Log.info(String.format("Panel %d is OFF.", offsetRoomIndex));
		} 
		panel.setLive(true);
	}

}
