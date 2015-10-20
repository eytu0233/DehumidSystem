package edu.ncku.uscc.io;

import edu.ncku.uscc.proc.AbstractRequest;

public class NotifyDeviceIDRequest extends AbstractRequest {

	public NotifyDeviceIDRequest(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void requestEvent() throws Exception {
		// TODO Auto-generated method stub
		int roomIndex = controller.getRoomIndex();
		int did = ((NotifyDeviceIDCmd)cmd).getDid();		

		byte[] txBuf = new byte[1];
		txBuf[0] = (byte) ((roomIndex << 3) + did);
		this.setTxBuf(txBuf);
	}

}
