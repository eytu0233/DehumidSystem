package edu.ncku.uscc.io;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class SYNPanelHumidityReply extends AbstractReply implements IPanelRepSet{
	
	private static final int PANEL_CMD_HUMID = 68;

	public SYNPanelHumidityReply(DehumidRoomControllerEX controller, DataStoreManager dataStoreManager, int roomIndex) {
		super(controller, dataStoreManager, roomIndex);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void replyEvent(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		IReferenceable panel = dataStoreManager.getPanel(offsetRoomIndex);
		
		int avgHumidity = rxBuf - PANEL_CMD_HUMID;
		
		if (rxBuf == PANEL_REP_OK) {
			Log.info(String.format("The humidity of Panel %d is %d.",
					offsetRoomIndex, avgHumidity));
			panel.setHumid(avgHumidity);
		} else {
			panel.setLive(false);
			Log.warn(String.format("Panel %d is not live.",
					offsetRoomIndex));
		}
	}

	@Override
	public void ackHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.nextCmd(cmd);
	}

	@Override
	public void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		Log.warn(String.format("Panel %d is not live.", offsetRoomIndex));
		controller.nextCmd(cmd);
	}

}
