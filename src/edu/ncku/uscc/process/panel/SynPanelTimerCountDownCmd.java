package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.util.PanelTimerScheduler;

public class SynPanelTimerCountDownCmd extends SynPanelCommand {
	
	private DehumidRoomController controller;

	public SynPanelTimerCountDownCmd(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
		this.controller = controller;
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
			pts.backpuTimerMinusOne(controller.getRoomIndex());
			panel.setTimerSetValue(pts.getBackupTimerSet(controller.getRoomIndex()));
			
			controller.log_info(String.format("The timer set of Panel %d minus one hour. : %d", 
					offsetRoomIndex, panel.getTimerSet()));
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
		
		controller.log_warn(String.format("Panel %d is not live.", offsetRoomIndex));
		
		controller.nextCmd(null);
	}
}
