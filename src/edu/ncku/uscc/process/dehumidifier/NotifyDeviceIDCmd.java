package edu.ncku.uscc.process.dehumidifier;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.process.Command;
import edu.ncku.uscc.util.IReferenceable;

public class NotifyDeviceIDCmd extends Command implements IDehumidProtocal {
	
	private static final int CHECKRATE_THRESHOLD = 15;

	private int roomIndex;
	private int did;
	private int offsetRoomIndex;
	private IReferenceable dehumidifier;

	public NotifyDeviceIDCmd(DehumidRoomController controller, int did) {
		super(controller);
		// TODO Auto-generated constructor stub
		this.did = did;
		this.roomIndex = controller.getRoomIndex();
		this.offsetRoomIndex = roomIndex - DehumidRoomController.ROOM_ID_MIN;
		this.dehumidifier = dataStoreManager.getDehumidifier(offsetRoomIndex, did);
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		return (byte) ((roomIndex << 3) + did);
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		switch (rxBuf) {
		// if return compressor running, then the dehumidifier must be ok
		case DEHUMID_REP_COMPRESSOR_RUNNING:
			dehumidifier.setCompressorRunning(true);
			controller.log_debug(String.format("The dehumidifier %d in room %d acks compressor running.", 
					did, offsetRoomIndex));
//			return true;
		case DEHUMID_REP_OK:
			dehumidifier.setHighTempWarn(false);
			dehumidifier.setDeforstTempWarn(false);
			dehumidifier.setHumidWarn(false);
			dehumidifier.setFanWarn(false);
			dehumidifier.setCompressorWarn(false);
			controller.log_info(String.format("The dehumidifier %d in room %d OK", 
					did, offsetRoomIndex));
			return true;
		case DEHUMID_REP_HIGH_TEMP_ABNORMAL:
			dehumidifier.setHighTempWarn(true);
			controller.log_warn(String.format("The dehumidifier %d in room %d acks high temp abnormal.", 
					did, offsetRoomIndex));
			return true;
		case DEHUMID_REP_DEFROST_TEMP_ABNORMAL:
			dehumidifier.setDeforstTempWarn(true);
			controller.log_warn(String.format("The dehumidifier %d in room %d acks defrost temp abnormal.", 
					did, offsetRoomIndex));
			return true;
		case DEHUMID_REP_DEHUMID_ABNORMAL:
			dehumidifier.setHumidWarn(true);
			controller.log_warn(String.format("The dehumidifier %d in room %d acks dehumid abnormal.", 
					did, offsetRoomIndex));
			return true;
		case DEHUMID_REP_FAN_ABNORMAL:
			dehumidifier.setFanWarn(true);
			controller.log_warn(String.format("The dehumidifier %d in room %d acks fan abnormal.", 
					did, offsetRoomIndex));
			return true;
		case DEHUMID_REP_COMPRESSOR_ABNORMAL:
			dehumidifier.setCompressorWarn(true);
			controller.log_warn(String.format("The dehumidifier %d in room %d acks compressor abnormal.", 
					did, offsetRoomIndex));
			return true;
		default:
			// if dehumidifier checkrate < CHECKRATE_THRESHOLD, it is timeout or no panel mode.
			if (controller.getCheckRate(did) > CHECKRATE_THRESHOLD)
				controller.log_debug(String.format("The dehumidifier %d in room %d time out.", 
						did, offsetRoomIndex));
			return false;
		}
	}

	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		// Notify command doesn't need to implement finishHandler method
		if (controller.getCheckRate(did) < CHECKRATE_THRESHOLD && controller.isPanelTimeoutCounter())
			controller.backupDataDeSerialization(did);
		controller.initCheckRate(did);
		dehumidifier.setLive(true);
	}

	@Override
	protected void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		
		dehumidifier.setLive(false);
		controller.dropRate(did);
		controller.nextCmd(null);
		if (controller.getCheckRate(did) < CHECKRATE_THRESHOLD)
			dehumidifier.clearAll();
	}

}
