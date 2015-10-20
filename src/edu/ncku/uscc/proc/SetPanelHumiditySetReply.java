package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class SetPanelHumiditySetReply extends AbstractReply implements IPanelReplySet{

	public SetPanelHumiditySetReply(DehumidRoomControllerEX controller) {
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
			panel.setHumidSetValue(panel.getHumidSet());
			Log.info(String.format(
					"Change set of humidity of Panel %d success",
					offsetRoomIndex, (int) rxBuf));
		}
	}

	@Override
	public void ackHandler() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub

	}

}
