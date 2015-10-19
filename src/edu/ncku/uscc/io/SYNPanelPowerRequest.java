package edu.ncku.uscc.io;

import java.io.OutputStream;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.IReferenceable;

public class SYNPanelPowerRequest extends AbstractRequest implements IPanelReqSet{

	public SYNPanelPowerRequest(DataStoreManager dataStoreManager,
			OutputStream output, int roomIndex) {
		super(dataStoreManager, output, roomIndex);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void requestEvent() throws Exception {
		// TODO Auto-generated method stub
		IReferenceable panel = dataStoreManager.getPanel(offsetRoomIndex);

		byte[] txBuf = new byte[1];

		// Check the power status whether iFix has changed or not
		if (dataStoreManager.isPanelONOFFChange(offsetRoomIndex)) {
			/*
			 * When the power status has been changed, it synchronizes the power
			 * status of the real panel
			 */
			txBuf[0] = (panel.isOn()) ? (byte) PANEL_REQ_START
					: (byte) PANEL_REQ_SHUTDOWM;
		} else {
			// Ask panel it is on or off
			txBuf[0] = (byte) PANEL_REQ_ONOFF;
		}

		this.setTxBuf(txBuf);
	}

}
