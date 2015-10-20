package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class SYNPanelHumiditySetReply extends AbstractReply implements
		IPanelReplySet {

	public SYNPanelHumiditySetReply(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void replyEvent(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		int offsetRoomIndex = controller.getRoomIndex()
				- DehumidRoomControllerEX.ROOM_ID_MIN;
		IReferenceable panel = dataStoreManager.getPanel(offsetRoomIndex);

		if (rxBuf == PANEL_REP_OK) {
			panel.setLive(true);
			cmd.setSubCommand(new SetPanelHumiditySetCmd(controller));
		} else if (rxBuf >= 0) {
			panel.setHumidSetValue((int) rxBuf);
			panel.setLive(true);
			Log.info(String.format("The humidity set of Panel %d is %d.",
					offsetRoomIndex, (int) rxBuf));
		} else {
			panel.setLive(false);
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
