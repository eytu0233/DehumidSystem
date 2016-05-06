package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.process.dehumidifier.SynDehumidifierByItselfPowerCmd;
import edu.ncku.uscc.util.PanelBackupSet;

public class SynPanelPowerCmd extends SynPanelCommand {
	
	private static final int DEHUMIDIFIERS_A_ROOM = 8;

	public SynPanelPowerCmd(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		// Check the power status whether iFix has changed or not
		if (dataStoreManager.isPanelONOFFChange(offsetRoomIndex)) {
			/*
			 * When the power status has been changed, it synchronizes the power
			 * status of the real panel
			 */
			return (panel.isOn()) ? (byte) PANEL_REQ_START
					: (byte) PANEL_REQ_SHUTDOWM;
		} else {
			// Ask panel it is on or off
			return (byte) PANEL_REQ_ONOFF;
		}
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_ON) {
			panel.setOn(true);
			panel.setLive(true);
			setBackupOn();
			
			controller.log_info(String.format("Panel %d is ON.", offsetRoomIndex));
			return true;
		} else if (rxBuf == PANEL_REP_OFF) {
			panel.setOn(false);
			panel.setLive(true);
			setBackupOn();
			
			controller.log_info(String.format("Panel %d is OFF.", offsetRoomIndex));
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.jumpCmdQueue(new SynPanelModeCmd(controller));
		controller.nextCmd(this);
	}
	
	@Override
	protected void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		panel.setLive(false);
		dehumidifierFate();
		controller.nextCmd(this);
		
		controller.log_warn(String.format("Panel %d power cmd is timeout.", 
				offsetRoomIndex));
	}
	
	private void setBackupOn() {
		PanelBackupSet.setProp(panel.isOn(),
				this.getClass().getSimpleName(), offsetRoomIndex);
	}
	
	/**
	 * If there are no panel in this room
	 * Decides whether the dehumidifier can add command or not
	 */
	private void dehumidifierFate() {

		for (int did = DEHUMIDIFIERS_A_ROOM - 1; did >= 0; did--) {
			if (controller.getCheckRate(did) >= (int) (Math.random() * 100)) {
				controller.jumpCmdQueue(new SynDehumidifierByItselfPowerCmd(controller, did));
			}
		}

	}

}
