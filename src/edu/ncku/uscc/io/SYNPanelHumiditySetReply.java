package edu.ncku.uscc.io;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class SYNPanelHumiditySetReply extends AbstractReply implements IPanelRepSet {

	public SYNPanelHumiditySetReply(DehumidRoomControllerEX controller,
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
			cmd.setSubCommand(new SetPanelHumidityCmd(controller));
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
		Log.warn(String.format("Panel %d is not live.", offsetRoomIndex));
		controller.nextCmd(cmd);
	}

}
