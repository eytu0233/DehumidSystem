package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.util.Log;
import edu.ncku.uscc.util.PanelTimer;

public class SynPanelTimerSetCmd extends SynPanelCommand {
	
	private PanelTimer panelTimer;

	public SynPanelTimerSetCmd(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
		panelTimer = PanelTimer.getInstance(controller);
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if (!panel.isOn() || panel.getTimerSet() <= 0) {
			return SKIP;
		}
		
		if (panelTimer.getBackupTimerSet() != panel.getTimerSet()) {
			panelTimer.newScheduleThread(panel.getTimerSet());
		} else if (panelTimer.getTimerMinusOneFlag()) {
			return (byte) PANEL_REQ_MINUS_TIMER;
		}
		
		return SKIP;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			panelTimer.backpuTimerMinusOne();
			panel.setTimerSetValue(panelTimer
					.getBackupTimerSet());
			panelTimer.setTimerMinusOneFlag(false);
			Log.info(String
					.format("The timer set of Panel %d minus one hour. : %d",
							offsetRoomIndex, panel.getTimerSet()));
			return true;
		} else {
			return false;
		}
	}

}
