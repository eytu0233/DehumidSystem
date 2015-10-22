package edu.ncku.uscc.process;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.Log;

public class SynPanelModeCmd extends SynPanelCommand {

	public SynPanelModeCmd(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void requestHandler() throws Exception {
		// TODO Auto-generated method stub
		byte txBuf = 0;
		
		if (!panel.isOn()) {
			this.skipCommand();
			return;
		}

		if (dataStoreManager.isPanelModeChange(offsetRoomIndex)) {
			if (panel.isModeDehumid()) {
				txBuf = (byte) PANEL_REQ_DEHUMID_MODE;
			} else if (panel.isModeDry()) {
				txBuf = (byte) PANEL_REQ_DRYCLOTHES_MODE;
			}
		} else {
			txBuf = (byte) PANEL_REQ_MODE;
		}

		this.setTxBuf(txBuf);
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_DEHUMID) {

			panel.setModeDehumid(true);
			panel.setModeDry(false);
			panel.setLive(true);
			Log.info(String.format("Panel %d is dehumid mode.",
					offsetRoomIndex));
		} else if (rxBuf == PANEL_REP_DRY_CLOTHES) {
			panel.setModeDehumid(false);
			panel.setModeDry(true);
			panel.setLive(true);
			Log.info(String.format("Panel %d is dry clothes mode.",
					offsetRoomIndex));
		} else if (rxBuf == PANEL_REP_OK) {

			boolean dehumid_mode = ((getTxBuf() == (byte) PANEL_REQ_DEHUMID_MODE));
			panel.setModeDehumid(dehumid_mode);
			panel.setModeDry(!dehumid_mode);
			panel.setLive(true);
			Log.info(String.format("Panel %d changes mode.",
					offsetRoomIndex));
		} 
	}


}
