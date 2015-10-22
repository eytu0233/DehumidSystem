package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.Log;
import gnu.io.SerialPort;

public class ScanRoomCmd extends Command implements IDehumidProtocal {
	
	private static final String LCK_REMOVE_CMD = "sudo rm -f /var/lock/LCK..ttyUSB";
	private SerialPort serialPort;
	private int roomScanIndex;
	private int did;

	public ScanRoomCmd(DehumidRoomControllerEX controller, int roomScanIndex,
			int did, int tolerance) {
		// TODO Auto-generated constructor stub
		super(controller, tolerance);
		this.serialPort = controller.getSerialPort();
		this.roomScanIndex = roomScanIndex;
		this.did = did;
	}

	@Override
	protected void requestHandler() throws Exception {
		// TODO Auto-generated method stub
		for (int usbIndex = 0; usbIndex < 4; usbIndex++) {
			Runtime.getRuntime().exec(LCK_REMOVE_CMD + usbIndex);
		}

		byte txBuf;
		txBuf = (byte) ((roomScanIndex << 3) + did);
		this.setTxBuf(txBuf);

		Log.info(String.format("Scan roomIndex : %x in %s",
				((int) txBuf & 0xff), serialPort.getName()));
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == DEHUMID_REP_OK
				|| rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
			Log.info("Scan room index : " + roomScanIndex);
			controller.setRoomIndex(roomScanIndex);
		}
	}

	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.addScanRoomQueue(this);
		controller.initCmdQueue();
		controller.nextCmd(null);
	}

	@Override
	protected void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.nextScanRoomCmd(this);
	}

}
