package edu.ncku.uscc.process.panel;

import java.util.Properties;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.util.PanelBackupSet;

public class SetPanelBackupSetCmd extends SynPanelCommand {
	
	private static Properties prop;

	public SetPanelBackupSetCmd(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		prop = PanelBackupSet.getProp();
		
		panel.setOn(PanelBackupSet.strToBool(prop.getProperty(
				new String(offsetRoomIndex + SynPanelPowerCmd.class.getSimpleName()))));
		
		panel.setModeDehumid(PanelBackupSet.strToBool(prop.getProperty(
				new String(offsetRoomIndex + SynPanelModeCmd.class.getSimpleName()))));
		
		panel.setModeDry(!PanelBackupSet.strToBool(prop.getProperty(
				new String(offsetRoomIndex + SynPanelModeCmd.class.getSimpleName()))));
		
		panel.setHumidSetValue(Integer.valueOf(prop.getProperty(
				new String(offsetRoomIndex + SynPanelHumiditySetCmd.class.getSimpleName()))));
		
		panel.setTimerSetValue(Integer.valueOf(prop.getProperty(
				new String(offsetRoomIndex + SynPanelTimerSetCmd.class.getSimpleName()))));
		
		controller.jumpCmdQueue(new SetPanelBackupTimerSetCmd(controller));
		controller.jumpCmdQueue(new SetPanelBackupHumiditySetCmd(controller));
		controller.jumpCmdQueue(new SetPanelBackupModeCmd(controller, panel.isModeDehumid()));
		controller.jumpCmdQueue(new SetPanelBackupPowerCmd(controller, panel.isOn()));

		// Check the power status whether iFix has changed or not
		return (byte) PROPERTY_CMD;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.nextCmd(null);
	}

}
