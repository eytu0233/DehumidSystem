package edu.ncku.uscc.io;

import java.io.OutputStream;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.IReferenceable;

public class SYNPanelHumidityRequest extends AbstractRequest {
	
	private static final int MIN_HUMIDITY = 40;
	private static final int MAX_HUMIDITY = 90;
	private static final int PANEL_CMD_HUMID = 68;
	
	private SYNPanelHumidityCmd synPanelHumidityCmd;

	public SYNPanelHumidityRequest(DataStoreManager dataStoreManager, OutputStream output, int roomIndex) {
		super(dataStoreManager, output, roomIndex);
		// TODO Auto-generated constructor stub
		if(cmd instanceof SYNPanelHumidityCmd){
			synPanelHumidityCmd = (SYNPanelHumidityCmd)cmd;
		}else{
			// throw new InvalidArgumentException();
		}
	}

	@Override
	public void requestEvent() throws Exception {
		// TODO Auto-generated method stub
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
			synPanelHumidityCmd.skipCommand();
		} else {
			txBuf[0] = (byte) (PANEL_CMD_HUMID + avgHumidity);
			this.setTxBuf(txBuf);
		}
	}

}
