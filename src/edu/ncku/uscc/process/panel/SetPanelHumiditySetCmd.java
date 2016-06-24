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
		if (!panel.isLive())
			return SKIP;
		
		int humidSet = panel.getHumidSet();
		humidSet = humidSet <= 50 ? 50 : humidSet >= 90 ? 90 : humidSet;
		humidSet = (humidSet - 45) / 5;
		
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

}
