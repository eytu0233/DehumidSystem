package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.util.PanelTimerScheduler;

public class SynPanelTimerSetCmd extends SynPanelCommand {
	
	public SynPanelTimerSetCmd(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if (!panel.isLive()) {
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
			panel.setLive(true);
			panel.setTimerSetValue(rxBuf);
			
			PanelTimerScheduler pts = PanelTimerScheduler.getInstance(controller);
			if(rxBuf > 0 && pts.getBackupTimerSet(controller.getRoomIndex()) != panel.getTimerSet()){				
				pts.newScheduleThread(rxBuf, controller.getRoomIndex());
			}
			
			controller.log_info(String.format("The timer set of Panel %d is %d.",
					offsetRoomIndex, rxBuf));
			return true;
		} else if(rxBuf == PANEL_REP_OK) {
			panel.setLive(true);
			controller.initPanelTimeoutCounter();
			controller.jumpCmdQueue(new SynPanelAbnormalCmd(controller));
			// followCmd will not exec finishHandler()
			followCmd(new SetPanelTimerSetCmd(controller), this);
			
			controller.log_debug(String.format("Add change command set of timer of Panel %d", 
					offsetRoomIndex));
			return true;
		}else {
			return false;
		}
	}
	
	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.initPanelTimeoutCounter();
		controller.jumpCmdQueue(new SynPanelAbnormalCmd(controller));
		controller.nextCmd(null);
	}

}
