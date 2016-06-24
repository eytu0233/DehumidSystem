package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.BackupData;
import edu.ncku.uscc.io.DehumidRoomController;

public class SetPanelBackupTimerSetCmd extends SynPanelCommand {
	
	private BackupData data;
	
	public SetPanelBackupTimerSetCmd(DehumidRoomController controller, BackupData data) {
		super(controller);
		// TODO Auto-generated constructor stub
		this.data = data;
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		
		panel.setTimerSetValue(data.isPanelOn() ? data.getPanelTimerSet() : 0);
		
		return (byte) PANEL_REQ_SETTING_TIMER;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if(rxBuf == PANEL_REP_OK) {
			panel.setLive(true);
			controller.initPanelTimeoutCounter();
			controller.jumpCmdQueue(new SetPanelBackupHumiditySetCmd(controller, data));
			// followCmd will not exec finishHandler()
			followCmd(new SetPanelTimerSetCmd(controller), this);
			return true;
		}else {
			return false;
		}
	}
	
	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.jumpCmdQueue(new SetPanelBackupHumiditySetCmd(controller, data));
		controller.nextCmd(null);
		controller.initPanelTimeoutCounter();
	}

}