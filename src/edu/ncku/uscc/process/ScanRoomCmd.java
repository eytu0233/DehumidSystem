package edu.ncku.uscc.process;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.process.dehumidifier.IDehumidProtocal;
import edu.ncku.uscc.util.Log;
//import gnu.io.SerialPort;

public class ScanRoomCmd extends Command implements IDehumidProtocal {
	
	private static final String LCK_REMOVE_CMD = "sudo rm -f /var/lock/LCK..ttyUSB";
	// private SerialPort serialPort;
	private int roomScanIndex;
	private int did;

	public ScanRoomCmd(DehumidRoomController controller, int roomScanIndex, int did, int tolerance) {
		// TODO Auto-generated constructor stub
		super(controller, tolerance);
//		 this.serialPort = controller.getSerialPort();
		this.roomScanIndex = roomScanIndex;
		this.did = did;
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		for (int usbIndex = 0; usbIndex < 4; usbIndex++) {
			Runtime.getRuntime().exec(LCK_REMOVE_CMD + usbIndex);
		}

		byte txBuf;
		txBuf = (byte) ((roomScanIndex << 3) + did);

//		 controller.log_info(String.format("Scan roomIndex : %x in %s", 
//				 ((int) txBuf & 0xff), serialPort.getName()));
		return txBuf;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == DEHUMID_REP_OK || rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL || rxBuf == DEHUMID_REP_DEHUMID_ABNORMAL
				|| rxBuf == DEHUMID_REP_FAN_ABNORMAL || rxBuf == DEHUMID_REP_COMPRESSOR_ABNORMAL
				|| rxBuf == DEHUMID_REP_COMPRESSOR_RUNNING) {
			Log.info("Scan room index : " + roomScanIndex + " from " + controller.getSerialPort().getName());
			controller.setRoomIndex(roomScanIndex);
			controller.detectLogRoomIndex();
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub

		// add this scanRoomCommand to scanRoomQueue last
		controller.addScanRoomQueue(this);

		// initial commandQueue
		controller.initCmdQueue();

		// switch to the commandQueue to run command
		controller.nextCmd(null);
	}

	@Override
	protected void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.nextScanRoomCmd(this);
		
	}

}
