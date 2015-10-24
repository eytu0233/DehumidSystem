package edu.ncku.uscc.process;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class NotifyDeviceIDCmd extends Command implements IDehumidProtocal {

	private int did;

	public NotifyDeviceIDCmd(DehumidRoomControllerEX controller, int did) {
		super(controller);
		// TODO Auto-generated constructor stub
		this.did = did;
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		int roomIndex = controller.getRoomIndex();

		return (byte) ((roomIndex << 3) + did);
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		int offsetRoomIndex = controller.getRoomIndex()
				- DehumidRoomControllerEX.ROOM_ID_MIN;
		IReferenceable dehumidifier = dataStoreManager.getDehumidifier(
				offsetRoomIndex, did);

		switch (rxBuf) {
		case DEHUMID_REP_OK:
			dehumidifier.setHighTempWarn(false);
			dehumidifier.setTempWarn(false);
			dehumidifier.setHumidWarn(false);
			dehumidifier.setFanWarn(false);
			dehumidifier.setCompressorWarn(false);
			dehumidifier.setLive(true);
			Log.debug(String.format("The dehumidifier %d in room %d OK", did,
					offsetRoomIndex));
			// checkRates[did] = INITIAL_RATE;
			break;
		case DEHUMID_REP_HIGH_TEMP_ABNORMAL:
			dehumidifier.setHighTempWarn(true);
			dehumidifier.setLive(true);
			// checkRates[did] = INITIAL_RATE;
			Log.warn(String.format(
					"The dehumidifier %d in room %d acks high temp abnormal.",
					did, offsetRoomIndex));
			break;
		case DEHUMID_REP_DEFROST_TEMP_ABNORMAL:
			dehumidifier.setTempWarn(true);
			dehumidifier.setLive(true);
			// checkRates[did] = INITIAL_RATE;
			Log.warn(String
					.format("The dehumidifier %d in room %d acks defrost temp abnormal.",
							did, offsetRoomIndex));
			break;
		case DEHUMID_REP_DEHUMID_ABNORMAL:
			dehumidifier.setHumidWarn(true);
			dehumidifier.setLive(true);
			// checkRates[did] = INITIAL_RATE;
			Log.warn(String.format(
					"The dehumidifier %d in room %d acks dehumid abnormal.",
					did, offsetRoomIndex));
		case DEHUMID_REP_FAN_ABNORMAL:
			dehumidifier.setFanWarn(true);
			dehumidifier.setLive(true);
			// checkRates[did] = INITIAL_RATE;
			Log.warn(String.format(
					"The dehumidifier %d in room %d acks fan abnormal.", did,
					offsetRoomIndex));
			break;
		case DEHUMID_REP_COMPRESSOR_ABNORMAL:
			dehumidifier.setCompressorWarn(true);
			dehumidifier.setLive(true);
			// checkRates[did] = INITIAL_RATE;
			Log.warn(String.format(
					"The dehumidifier %d in room %d acks compressor abnormal.",
					did, offsetRoomIndex));
			break;
		default:
			dehumidifier.setLive(false);
			// checkRates[did] = drop(checkRates[did]);
			break;
		}
	}

	@Override
	protected void finishCommandHandler() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub

	}

}
