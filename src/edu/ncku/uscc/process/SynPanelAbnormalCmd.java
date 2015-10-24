package edu.ncku.uscc.process;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class SynPanelAbnormalCmd extends SynPanelCommand {

	public SynPanelAbnormalCmd(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if (!panel.isOn()) {
			return SKIP;
		}

		byte countAbnormal = 0, tmpTxBuf = 0;
		for (int did = 0; did < DehumidRoomControllerEX.DEHUMIDIFIERS_A_ROOM; did++) {
			IReferenceable dehumidifier = dataStoreManager.getDehumidifier(
					offsetRoomIndex, did);
			if (dehumidifier.isHighTempWarning()) {
				tmpTxBuf = (byte) PANEL_REQ_TEMP_ABNORMAL;
				countAbnormal++;
			} else if (dehumidifier.isTempWarning()) {
				tmpTxBuf = (byte) PANEL_REQ_DEFROST_TEMP_ABNORMAL;
				countAbnormal++;
			} else if (dehumidifier.isHumidWarning()) {
				tmpTxBuf = (byte) PANEL_REQ_HUMID_ABNORMAL;
				countAbnormal++;
			} else if (dehumidifier.isFanWarning()) {
				tmpTxBuf = (byte) PANEL_REQ_FAN_ABNORMAL;
				countAbnormal++;
			} else if (dehumidifier.isCompressorWarning()) {
				tmpTxBuf = (byte) PANEL_REQ_COMPRESSOR_ABNORMAL;
				countAbnormal++;
			}
		}

		if (countAbnormal == 0) {
			return SKIP;
		} else if (countAbnormal > 1) {
			tmpTxBuf = (byte) PANEL_CMD_MULTIPLE_ABNORMAL;
		}
		
		return tmpTxBuf;
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			switch (getTxBuf()) {
			case (byte) PANEL_REQ_TEMP_ABNORMAL:
				panel.setHighTempWarn(true);
				Log.debug(String.format(
						"Panel %d is high temperature abnormal.",
						offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_DEFROST_TEMP_ABNORMAL:
				panel.setTempWarn(true);
				Log.debug(String.format(
						"Panel %d is defrost temperature abnormal.",
						offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_HUMID_ABNORMAL:
				panel.setHumidWarn(true);
				Log.debug(String.format("Panel %d is humid abnormal.",
						offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_FAN_ABNORMAL:
				panel.setFanWarn(true);
				Log.debug(String.format("Panel %d is fan abnormal.",
						offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_COMPRESSOR_ABNORMAL:
				panel.setCompressorWarn(true);
				Log.debug(String.format("Panel %d is compressor abnormal.",
						offsetRoomIndex));
				break;
			case (byte) PANEL_CMD_MULTIPLE_ABNORMAL:
				Log.debug(String.format("Panel %d is multiple abnormal.",
						offsetRoomIndex));
				break;
			}
		} else {
			this.setAck(false);
		}
	}

}
