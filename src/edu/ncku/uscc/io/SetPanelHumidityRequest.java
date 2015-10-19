package edu.ncku.uscc.io;

import java.io.OutputStream;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class SetPanelHumidityRequest extends AbstractRequest {

	public SetPanelHumidityRequest(DataStoreManager dataStoreManager,
			OutputStream output, int roomIndex) {
		super(dataStoreManager, output, roomIndex);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void requestEvent() throws Exception {
		// TODO Auto-generated method stub
		IReferenceable panel = dataStoreManager.getPanel(offsetRoomIndex);
		
		Log.info(String.format(
				"Start to change set of humidity of Panel %d",
				offsetRoomIndex));
		int humidSet = panel.getHumidSet();
		
		byte[] txBuf = new byte[1];
		txBuf[0] = (byte) humidSet;
		
		this.setTxBuf(txBuf);
	}

}
