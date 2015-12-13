package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.util.Log;
import edu.ncku.uscc.util.PanelTimerScheduler;

public class SynPanelTimerSetCmd extends SynPanelCommand {

	private DehumidRoomController controller;
	
	public SynPanelTimerSetCmd(DehumidRoomController controller) {
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
		
		if (dataStoreManager.isPanelTimerSetChange(offsetRoomIndex)) {
			return (byte) PANEL_REQ_SETTING_TIMER;
		} else {
			return (byte) PANEL_REQ_TIMER_SET;
		}
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf >= 0 && rxBuf <= 12) {
			panel.setTimerSetValue(rxBuf);
			Log.info(String.format("The timer set of Panel %d is %d.",
					offsetRoomIndex, rxBuf));
			
			PanelTimerScheduler pts = PanelTimerScheduler.getInstance(controller);
			if(rxBuf > 0 && pts.getBackupTimerSet(controller.getRoomIndex()) != panel.getTimerSet()){				
				pts.newScheduleThread(rxBuf, controller.getRoomIndex());
			}
			
			return true;
		} else if(rxBuf == PANEL_REP_OK) {
			Log.info(String.format("Add change command set of timer of Panel %d", offsetRoomIndex));
			followCmd(new SetPanelTimerSetCmd(controller), this);
			return true;
		}else {
			return false;
		}
	}

}
