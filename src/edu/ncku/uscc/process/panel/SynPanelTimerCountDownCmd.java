package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.util.PanelTimerScheduler;

public class SynPanelTimerCountDownCmd extends SynPanelCommand {

	public SynPanelTimerCountDownCmd(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if (!panel.isOn()) {
			return SKIP;
		}

		return (byte) PANEL_REQ_MINUS_TIMER;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			panel.setLive(true);
			PanelTimerScheduler pts = PanelTimerScheduler.getInstance(controller);
			pts.backpuTimerMinusOne(controller.getRoomIndex());
			panel.setTimerSetValue(pts.getBackupTimerSet(controller.getRoomIndex()));
			
			controller.log_info(String.format("The timer set of Panel %d minus one hour. : %d", 
					offsetRoomIndex, panel.getTimerSet()));
			return true;
		} else {
			return false;
		}
	}
}
