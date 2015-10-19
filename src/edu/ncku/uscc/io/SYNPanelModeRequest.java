package edu.ncku.uscc.io;

import java.io.OutputStream;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.IReferenceable;

public class SYNPanelModeRequest extends AbstractRequest implements IPanelReqSet{

	public SYNPanelModeRequest(DataStoreManager dataStoreManager,
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

		if (dataStoreManager.isPanelModeChange(offsetRoomIndex)) {
			if (panel.isModeDehumid()) {
				txBuf[0] = (byte) PANEL_REQ_DEHUMID_MODE;
			} else if (panel.isModeDry()) {
				txBuf[0] = (byte) PANEL_REQ_DRYCLOTHES_MODE;
			}
		} else {
			txBuf[0] = (byte) PANEL_REQ_MODE;
		}

		this.setTxBuf(txBuf);
	}

}
