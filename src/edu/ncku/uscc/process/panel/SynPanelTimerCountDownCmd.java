package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.util.Log;
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
			PanelTimerScheduler pts = PanelTimerScheduler.getInstance(controller);
			pts.backpuTimerMinusOne();
			panel.setTimerSetValue(pts.getBackupTimerSet());
			Log.info(String.format("The timer set of Panel %d minus one hour. : %d", offsetRoomIndex, panel.getTimerSet()));
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.nextCmd(null);
	}

	@Override
	protected void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		Log.warn(String.format("Panel %d is not live.", offsetRoomIndex));
		controller.nextCmd(null);
	}
}
