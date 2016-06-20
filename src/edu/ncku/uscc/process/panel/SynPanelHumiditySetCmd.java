package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;

public class SynPanelHumiditySetCmd extends SynPanelCommand {

	public SynPanelHumiditySetCmd(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if (!panel.isLive()) {
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
			followCmd(new SetPanelHumiditySetCmd(controller), this);
			panel.setLive(true);
			return true;
		} else if (rxBuf > 0 && rxBuf < 10) {
			panel.setHumidSetValue(45 + 5 * (int) rxBuf);
			panel.setLive(true);
			
			controller.log_info(String.format("The humidity set of Panel %d is %d.",
					offsetRoomIndex, (int) rxBuf));
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.jumpCmdQueue(new SynPanelTimerSetCmd(controller));
		controller.nextCmd(null);
		controller.initPanelTimeoutCounter();
	}

}
