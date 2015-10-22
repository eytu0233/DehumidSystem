package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.Log;

public class SYNPanelHumiditySetCmd extends SYNPanelCommand {

	public SYNPanelHumiditySetCmd(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void requestHandler() throws Exception {
		// TODO Auto-generated method stub
		byte txBuf;

		if (!panel.isOn()) {
			this.skipCommand();
			return;
		}
		
		if (dataStoreManager.isPanelDehumiditySetChange(offsetRoomIndex)) {
			txBuf = (byte) PANEL_REQ_SETTING_HUMID_Set;
		} else {
			txBuf = (byte) PANEL_REQ_HUMID_SET;
		}
		
		this.setTxBuf(txBuf);
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			panel.setLive(true);
			this.setSubCommand(new SetPanelHumiditySetCmd(controller));
		} else if (rxBuf >= 0) {
			panel.setHumidSetValue((int) rxBuf);
			panel.setLive(true);
			Log.info(String.format("The humidity set of Panel %d is %d.",
					offsetRoomIndex, (int) rxBuf));
		} 
	}


}
