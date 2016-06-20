package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.util.IReferenceable;

public class SynPanelAbnormalCmd extends SynPanelCommand {

	public SynPanelAbnormalCmd(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if (!panel.isLive()) {
			return SKIP;
		}

		byte countAbnormal = 0, tmpTxBuf = 0;
		for (int did = 0; did < DehumidRoomController.DEHUMIDIFIERS_A_ROOM; did++) {
			IReferenceable dehumidifier = dataStoreManager.getDehumidifier(
					offsetRoomIndex, did);
			if (dehumidifier.isHighTempWarning()) {
				tmpTxBuf = (byte) PANEL_REQ_TEMP_ABNORMAL;
				countAbnormal++;
			} else if (dehumidifier.isDeforstTempWarning()) {
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
			panel.setHighTempWarn(false);
			panel.setDeforstTempWarn(false);
			panel.setHumidWarn(false);
			panel.setFanWarn(false);
			panel.setCompressorWarn(false);
			return SKIP;
		} else if (countAbnormal > 1) {
			tmpTxBuf = (byte) PANEL_REQ_MULTIPLE_ABNORMAL;
		}
		
		return tmpTxBuf;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			switch (getTxBuf()) {
			case (byte) PANEL_REQ_TEMP_ABNORMAL:
				panel.setHighTempWarn(true);
				controller.log_debug(String.format(
						"Panel %d is high temperature abnormal.",
						offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_DEFROST_TEMP_ABNORMAL:
				panel.setDeforstTempWarn(true);
				controller.log_debug(String.format(
						"Panel %d is defrost temperature abnormal.",
						offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_HUMID_ABNORMAL:
				panel.setHumidWarn(true);
				controller.log_debug(String.format("Panel %d is humid abnormal.",
						offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_FAN_ABNORMAL:
				panel.setFanWarn(true);
				controller.log_debug(String.format("Panel %d is fan abnormal.",
						offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_COMPRESSOR_ABNORMAL:
				panel.setCompressorWarn(true);
				controller.log_debug(String.format("Panel %d is compressor abnormal.",
						offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_MULTIPLE_ABNORMAL:
				controller.log_debug(String.format("Panel %d is multiple abnormal.",
						offsetRoomIndex));
				break;
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.jumpCmdQueue(new SynPanelHumidityCmd(controller));
		controller.nextCmd(null);
		controller.initPanelTimeoutCounter();
	}

}
