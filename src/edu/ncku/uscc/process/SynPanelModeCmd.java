package edu.ncku.uscc.process;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.Log;

public class SynPanelModeCmd extends SynPanelCommand {

	public SynPanelModeCmd(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub		
		if (!panel.isOn()) {
			return SKIP;
		}

		if (dataStoreManager.isPanelModeChange(offsetRoomIndex)) {
			if (panel.isModeDehumid()) {
				return (byte) PANEL_REQ_DEHUMID_MODE;
			} else if (panel.isModeDry()) {
				return (byte) PANEL_REQ_DRYCLOTHES_MODE;
			} else {
				return 0;
			}
		} else {
			return (byte) PANEL_REQ_MODE;
		}
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_DEHUMID) {
			panel.setModeDehumid(true);
			panel.setModeDry(false);
			panel.setLive(true);
			this.setAck(true);
			Log.info(String.format("Panel %d is dehumid mode.",
					offsetRoomIndex));
		} else if (rxBuf == PANEL_REP_DRY_CLOTHES) {
			panel.setModeDehumid(false);
			panel.setModeDry(true);
			panel.setLive(true);
			this.setAck(true);
			Log.info(String.format("Panel %d is dry clothes mode.",
					offsetRoomIndex));
		} else if (rxBuf == PANEL_REP_OK) {
			boolean dehumid_mode = ((getTxBuf() == (byte) PANEL_REQ_DEHUMID_MODE));
			panel.setModeDehumid(dehumid_mode);
			panel.setModeDry(!dehumid_mode);
			panel.setLive(true);
			this.setAck(true);
			Log.info(String.format("Panel %d changes mode.",
					offsetRoomIndex));
		} else {
			this.setAck(false);
		}
	}


}
