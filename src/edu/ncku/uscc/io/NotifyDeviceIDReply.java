package edu.ncku.uscc.io;

import edu.ncku.uscc.proc.AbstractReply;
import edu.ncku.uscc.proc.IDehumidReplySet;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class NotifyDeviceIDReply extends AbstractReply implements
		IDehumidReplySet {

	public NotifyDeviceIDReply(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void replyEvent(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		int roomIndex = controller.getRoomIndex();
		int did = ((NotifyDeviceIDCmd) cmd).getDid();
		int offsetRoomIndex = roomIndex - DehumidRoomControllerEX.ROOM_ID_MIN;
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
			// checkRates[did] = INITIAL_RATE;
		case DEHUMID_REP_HIGH_TEMP_ABNORMAL:
			dehumidifier.setHighTempWarn(true);
			dehumidifier.setLive(true);
			// checkRates[did] = INITIAL_RATE;
			Log.warn(String.format(
					"The dehumidifier %d in room %d acks high temp abnormal.",
					did, offsetRoomIndex));
		case DEHUMID_REP_DEFROST_TEMP_ABNORMAL:
			dehumidifier.setTempWarn(true);
			dehumidifier.setLive(true);
			// checkRates[did] = INITIAL_RATE;
			Log.warn(String
					.format("The dehumidifier %d in room %d acks defrost temp abnormal.",
							did, offsetRoomIndex));
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
		case DEHUMID_REP_COMPRESSOR_ABNORMAL:
			dehumidifier.setCompressorWarn(true);
			dehumidifier.setLive(true);
			// checkRates[did] = INITIAL_RATE;
			Log.warn(String.format(
					"The dehumidifier %d in room %d acks compressor abnormal.",
					did, offsetRoomIndex));
		default:
			dehumidifier.setLive(false);
			// checkRates[did] = drop(checkRates[did]);
		}
	}

	@Override
	public void ackHandler() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub

	}

}
