package edu.ncku.uscc.process;

import edu.ncku.uscc.util.Log;

public class SetPanelHumiditySetCmd extends SynPanelCommand {

	public SetPanelHumiditySetCmd(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void requestHandler() throws Exception {
		// TODO Auto-generated method stub
		Log.info(String.format("Start to change set of humidity of Panel %d",
				offsetRoomIndex));

		this.setTxBuf((byte) panel.getHumidSet());
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			panel.setLive(true);
			panel.setHumidSetValue(panel.getHumidSet());
			Log.info(String.format(
					"Change set of humidity of Panel %d success",
					offsetRoomIndex, (int) rxBuf));
		}
	}

	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	

}
