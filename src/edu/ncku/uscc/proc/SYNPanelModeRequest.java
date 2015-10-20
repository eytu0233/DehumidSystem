package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.IReferenceable;

public class SYNPanelModeRequest extends AbstractRequest implements IPanelRequestSet{

	public SYNPanelModeRequest(DehumidRoomControllerEX controller) {
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
