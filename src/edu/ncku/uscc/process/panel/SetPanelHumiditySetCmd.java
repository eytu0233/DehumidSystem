package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;

public class SetPanelHumiditySetCmd extends SynPanelCommand {

	public SetPanelHumiditySetCmd(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if (!panel.isOn())
			return SKIP;
		
		int humidSet = panel.getHumidSet();
		if (humidSet < 50)
			humidSet = 50;
		else if (humidSet > 90)
			humidSet = 90;
		humidSet = (humidSet - 45) / 5;
		
		controller.log_info(String.format("Start to change set of humidity of Panel %d %d", 
				offsetRoomIndex, humidSet));
		return (byte) humidSet;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			panel.setLive(true);
			panel.setHumidSetValue(panel.getHumidSet());
			
			controller.log_info(String.format("Change set of humidity of Panel %d success", 
					offsetRoomIndex));
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
		controller.nextCmd(null);
	}

}
