package edu.ncku.uscc.process;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class SynPanelHumidityCmd extends SynPanelCommand {

	private static final int MIN_HUMIDITY = 40;
	private static final int MAX_HUMIDITY = 90;
	private static final int PANEL_CMD_HUMID = 68;
	
	private int avgHumidity;

	public SynPanelHumidityCmd(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void requestHandler() throws Exception {
		// TODO Auto-generated method stub
		byte txBuf;
		
		int humidity = 0, count = 0;
		
		for (int did = 0; did < DehumidRoomControllerEX.DEHUMIDIFIERS_A_ROOM; did++) {
			IReferenceable dehumidifier = dataStoreManager.getDehumidifier(
					offsetRoomIndex, did);
			if (dehumidifier.isLive()) {
				humidity += dehumidifier.getHumid();
				count++;
			}
		}
		
		if (count > 0) {
			avgHumidity = humidity / count;
		} 
		
		if(count == 0 || avgHumidity < MIN_HUMIDITY || avgHumidity > MAX_HUMIDITY){
			this.skipCommand();
		} else {
			txBuf = (byte) (PANEL_CMD_HUMID + avgHumidity);
			this.setTxBuf(txBuf);
		}
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub		
		if (rxBuf == PANEL_REP_OK) {
			Log.info(String.format("The humidity of Panel %d is %d.",
					offsetRoomIndex, avgHumidity));
			panel.setHumid(avgHumidity);
		}
	}

	

}
