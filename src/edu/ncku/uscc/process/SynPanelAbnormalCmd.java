package edu.ncku.uscc.process;

import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class SynPanelAbnormalCmd extends SynPanelCommand {

	public SynPanelAbnormalCmd(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void requestHandler() throws Exception {
		// TODO Auto-generated method stub

		if (!panel.isOn()) {
			this.skipCommand();
			return;
		}
		
		byte countAbnormal = 0;
		for (int did = 0; did < DehumidRoomControllerEX.DEHUMIDIFIERS_A_ROOM; did++) {
			IReferenceable dehumidifier = dataStoreManager.getDehumidifier(offsetRoomIndex, did);
			if (dehumidifier.isHighTempWarning()) {
				this.setTxBuf((byte) PANEL_REQ_TEMP_ABNORMAL);
				countAbnormal++;
			} else if (dehumidifier.isTempWarning()) {
				this.setTxBuf((byte) PANEL_REQ_DEFROST_TEMP_ABNORMAL);
				countAbnormal++;
			} else if (dehumidifier.isHumidWarning()) {
				this.setTxBuf((byte) PANEL_REQ_HUMID_ABNORMAL);
				countAbnormal++;
			} else if (dehumidifier.isFanWarning()) {
				this.setTxBuf((byte) PANEL_REQ_FAN_ABNORMAL);
				countAbnormal++;
			} else if (dehumidifier.isCompressorWarning()) {
				this.setTxBuf((byte) PANEL_REQ_COMPRESSOR_ABNORMAL);
				countAbnormal++;
			}
		}

		if (countAbnormal == 0)
			this.skipCommand();
		else if (countAbnormal > 1)
			this.setTxBuf((byte) PANEL_CMD_MULTIPLE_ABNORMAL);
	}

	@Override
	protected void replyHandler(Byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			switch (getTxBuf()) {
			case (byte) PANEL_REQ_TEMP_ABNORMAL:
				panel.setHighTempWarn(true);
				Log.debug(String.format("Panel %d is high temperature abnormal.", offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_DEFROST_TEMP_ABNORMAL:
				panel.setTempWarn(true);
				Log.debug(String.format("Panel %d is defrost temperature abnormal.", offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_HUMID_ABNORMAL:
				panel.setHumidWarn(true);
				Log.debug(String.format("Panel %d is humid abnormal.", offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_FAN_ABNORMAL:
				panel.setFanWarn(true);
				Log.debug(String.format("Panel %d is fan abnormal.", offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_COMPRESSOR_ABNORMAL:
				panel.setCompressorWarn(true);
				Log.debug(String.format("Panel %d is compressor abnormal.", offsetRoomIndex));
				break;
			case (byte) PANEL_CMD_MULTIPLE_ABNORMAL:
				Log.debug(String.format("Panel %d is multiple abnormal.", offsetRoomIndex));
				break;
			}
		}
	}

}
