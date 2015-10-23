package edu.ncku.uscc.process;

import edu.ncku.uscc.util.Log;

public class SynPanelHumiditySetCmd extends SynPanelCommand {

	public SynPanelHumiditySetCmd(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void requestHandler() throws Exception {
		// TODO Auto-generated method stub

		if (!panel.isOn()) {
			this.skipCommand();
			return;
		}
		
		if (dataStoreManager.isPanelDehumiditySetChange(offsetRoomIndex)) {
			this.setTxBuf((byte) PANEL_REQ_SETTING_HUMID_Set);
		} else {
			this.setTxBuf((byte) PANEL_REQ_HUMID_SET);
		}
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			this.setSubCommand(new SetPanelHumiditySetCmd(controller));
		} else if (rxBuf >= 0) {
			panel.setHumidSetValue((int) rxBuf);
			Log.info(String.format("The humidity set of Panel %d is %d.",
					offsetRoomIndex, (int) rxBuf));
		} 
		panel.setLive(true);
	}


}
