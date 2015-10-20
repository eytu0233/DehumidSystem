package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class SYNPanelModeReply extends AbstractReply implements IPanelRequestSet, IPanelReplySet{

	public SYNPanelModeReply(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void replyEvent(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		int offsetRoomIndex = controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN;
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
		int offsetRoomIndex = controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN;
		Log.warn(String.format("Panel %d is not live.", offsetRoomIndex));
		controller.nextCmd(cmd);
	}

}
