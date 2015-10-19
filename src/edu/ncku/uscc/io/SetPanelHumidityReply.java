package edu.ncku.uscc.io;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class SetPanelHumidityReply extends AbstractReply implements IPanelRepSet{

	public SetPanelHumidityReply(DehumidRoomControllerEX controller,
			DataStoreManager dataStoreManager, int roomIndex) {
		super(controller, dataStoreManager, roomIndex);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void replyEvent(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
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
