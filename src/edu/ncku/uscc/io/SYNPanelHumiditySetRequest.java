package edu.ncku.uscc.io;

import java.io.OutputStream;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.IReferenceable;

public class SYNPanelHumiditySetRequest extends AbstractRequest implements IPanelReqSet{

	public SYNPanelHumiditySetRequest(DataStoreManager dataStoreManager,
			OutputStream output, int roomIndex) {
		super(dataStoreManager, output, roomIndex);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void requestEvent() throws Exception {
		// TODO Auto-generated method stub		
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
