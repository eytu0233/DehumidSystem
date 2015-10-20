package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class SetPanelHumiditySetRequest extends AbstractRequest {

	public SetPanelHumiditySetRequest(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void requestEvent() throws Exception {
		// TODO Auto-generated method stub
		int offsetRoomIndex = controller.getRoomIndex()
				- DehumidRoomControllerEX.ROOM_ID_MIN;
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
