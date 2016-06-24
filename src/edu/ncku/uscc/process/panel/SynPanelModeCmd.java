package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;

public class SynPanelModeCmd extends SynPanelCommand {

	public SynPanelModeCmd(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub		
		if (!panel.isLive()) {
			return SKIP;
		}

		if (dataStoreManager.isPanelModeChange(offsetRoomIndex)) {
			if (panel.isModeDehumid()) {
				return (byte) PANEL_REQ_DEHUMID_MODE;
			} else if (panel.isModeDry()) {
				return (byte) PANEL_REQ_DRYCLOTHES_MODE;
			} else {
				return SKIP;
			}
		} else {
			return (byte) PANEL_REQ_MODE;
		}
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_DEHUMID) {
			panel.setModeDehumid(true);
			panel.setModeDry(false);
			panel.setLive(true);
			
//			controller.log_info(String.format("Panel %d is dehumid mode.",
//					offsetRoomIndex));
			return true;
		} else if (rxBuf == PANEL_REP_DRY_CLOTHES) {
			panel.setModeDehumid(false);
			panel.setModeDry(true);
			panel.setLive(true);
			
//			controller.log_info(String.format("Panel %d is dry clothes mode.",
//					offsetRoomIndex));
			return true;
		} else if (rxBuf == PANEL_REP_OK) {
			boolean dehumid_mode = ((getTxBuf() == (byte) PANEL_REQ_DEHUMID_MODE));
			panel.setModeDehumid(dehumid_mode);
			panel.setModeDry(!dehumid_mode);
			panel.setLive(true);
			
			controller.log_info(String.format("Panel %d changes mode.",
					offsetRoomIndex));
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.jumpCmdQueue(new SynPanelHumiditySetCmd(controller));
		controller.nextCmd(null);
		controller.initPanelTimeoutCounter();
	}


}
