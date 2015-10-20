package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.IReferenceable;

public class SYNPanelHumiditySetRequest extends AbstractRequest implements IPanelRequestSet{

	public SYNPanelHumiditySetRequest(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void requestEvent() throws Exception {
		// TODO Auto-generated method stub		
		int offsetRoomIndex = controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN;
		IReferenceable panel = dataStoreManager.getPanel(offsetRoomIndex);
		
		byte[] txBuf = new byte[1];

		if (!panel.isOn()) {
			cmd.skipCommand();
			return;
		}
		
		if (dataStoreManager.isPanelDehumiditySetChange(offsetRoomIndex)) {
			txBuf[0] = (byte) PANEL_REQ_SETTING_HUMID_Set;
		} else {
			txBuf[0] = (byte) PANEL_REQ_HUMID_SET;
		}
		
		this.setTxBuf(txBuf);
		
	}

}
