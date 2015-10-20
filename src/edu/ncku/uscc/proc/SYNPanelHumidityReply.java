package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class SYNPanelHumidityReply extends AbstractReply implements IPanelReplySet{
	
	private static final int PANEL_CMD_HUMID = 68;

	public SYNPanelHumidityReply(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void replyEvent(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		int offsetRoomIndex = controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN;
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
		int offsetRoomIndex = controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN;
		Log.warn(String.format("Panel %d is not live.", offsetRoomIndex));
		controller.nextCmd(cmd);
	}

}
