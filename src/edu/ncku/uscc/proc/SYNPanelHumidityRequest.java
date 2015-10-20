package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.IReferenceable;

public class SYNPanelHumidityRequest extends AbstractRequest {
	
	private static final int MIN_HUMIDITY = 40;
	private static final int MAX_HUMIDITY = 90;
	private static final int PANEL_CMD_HUMID = 68;

	public SYNPanelHumidityRequest(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void requestEvent() throws Exception {
		// TODO Auto-generated method stub
		int offsetRoomIndex = controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN;
		
		byte[] txBuf = new byte[1];
		
		int humidity = 0, avgHumidity = 0, count = 0;
		
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
			cmd.skipCommand();
		} else {
			txBuf[0] = (byte) (PANEL_CMD_HUMID + avgHumidity);
			this.setTxBuf(txBuf);
		}
	}

}
