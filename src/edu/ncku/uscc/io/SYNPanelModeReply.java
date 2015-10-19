package edu.ncku.uscc.io;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class SYNPanelModeReply extends AbstractReply implements IPanelReqSet, IPanelRepSet{

	public SYNPanelModeReply(DehumidRoomControllerEX controller,
			DataStoreManager dataStoreManager, int roomIndex) {
		super(controller, dataStoreManager, roomIndex);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void replyEvent(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		IReferenceable panel = dataStoreManager.getPanel(offsetRoomIndex);
		
		if (rxBuf == PANEL_REP_DEHUMID) {

			panel.setModeDehumid(true);
			panel.setModeDry(false);
			panel.setLive(true);
			Log.info(String.format("Panel %d is dehumid mode.",
					offsetRoomIndex));
		} else if (rxBuf == PANEL_REP_DRY_CLOTHES) {
			panel.setModeDehumid(false);
			panel.setModeDry(true);
			panel.setLive(true);
			Log.info(String.format("Panel %d is dry clothes mode.",
					offsetRoomIndex));
		} else if (rxBuf == PANEL_REP_OK) {

			boolean dehumid_mode = (cmd.getTxBuf() == (byte) PANEL_REQ_DEHUMID_MODE);
			panel.setModeDehumid(dehumid_mode);
			panel.setModeDry(!dehumid_mode);
			panel.setLive(true);
			Log.info(String.format("Panel %d changes mode.",
					offsetRoomIndex));
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
		Log.warn(String.format("Panel %d is not live.", offsetRoomIndex));
		controller.nextCmd(cmd);
	}

}
