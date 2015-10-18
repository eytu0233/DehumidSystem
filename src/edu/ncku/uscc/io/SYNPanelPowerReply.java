package edu.ncku.uscc.io;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class SYNPanelPowerReply extends AbstractReply {

	@Override
	public void replyEvent(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		int offsetRoomIndex = controller.getRoomIndex()
				- DehumidRoomControllerEX.ROOM_ID_MIN;

		DataStoreManager dataStoreManager = controller.getDataStoreManager();
		IReferenceable panel = dataStoreManager.getPanel(offsetRoomIndex);

		if (rxBuf == PANEL_REP_ON) {
			panel.setOn(true);
			panel.setLive(true);
			cmd.setAck(true);
			Log.info(String.format("Panel %d is ON.", offsetRoomIndex));
		} else if (rxBuf == PANEL_REP_OFF) {
			panel.setOn(false);
			panel.setLive(true);
			cmd.setAck(true);
			Log.info(String.format("Panel %d is OFF.", offsetRoomIndex));
		} else {
			panel.setLive(false);
			cmd.cmdTimeout();
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
		int offsetRoomIndex = controller.getRoomIndex()
				- DehumidRoomControllerEX.ROOM_ID_MIN;
		Log.warn(String.format("Panel %d is not live.", offsetRoomIndex));
		controller.nextCmd(cmd);
	}

}
