package edu.ncku.uscc.proc;

import java.io.OutputStream;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.Log;
import gnu.io.SerialPort;

public class ScanRoomRequest extends AbstractRequest {

	private static final String LCK_REMOVE_CMD = "sudo rm -f /var/lock/LCK..ttyUSB";
	
	private SerialPort serialPort;
	private int roomScanIndex;
	private int did;

	public ScanRoomRequest(DehumidRoomControllerEX controller, int roomIndex, int did) {
		super(controller);
		// TODO Auto-generated constructor stub
		this.serialPort = controller.getSerialPort();
		this.roomScanIndex = roomIndex;
		this.did = did;
	}

	@Override
	public void requestEvent() throws Exception {
		// TODO Auto-generated method stub
		// Below commands are used to avoid the warnings of RXTXcomm
		for (int usbIndex = 0; usbIndex < 4; usbIndex++) {
			Runtime.getRuntime().exec(LCK_REMOVE_CMD + usbIndex);
		}

		byte[] txBuf = new byte[1];
		txBuf[0] = (byte) ((roomScanIndex << 3) + did);
		this.setTxBuf(txBuf);

		Log.info(String.format("Scan roomIndex : %x in %s",
				((int) txBuf[0] & 0xff), serialPort.getName()));
	}

}
