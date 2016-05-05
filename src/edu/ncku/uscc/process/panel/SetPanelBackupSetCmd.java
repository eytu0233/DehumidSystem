package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.util.PanelBackupSet;

public class SetPanelBackupSetCmd extends SynPanelCommand {

	public SetPanelBackupSetCmd(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		
		panel.setOn(PanelBackupSet.getOnCheckpoint(offsetRoomIndex));
		
		panel.setHumidSetValue(PanelBackupSet.getHumidSetValueCP(offsetRoomIndex));
		
		controller.jumpCmdQueue(new SetPanelBackupHumiditySetCmd(controller));
		controller.jumpCmdQueue(new SetPanelBackupPowerCmd(controller, panel.isOn()));

		// Check the power status whether iFix has changed or not
		return (byte) SKIP;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

}
