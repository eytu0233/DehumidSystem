package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.util.IReferenceable;

public class SynPanelAbnormalCmd extends SynPanelCommand {
	
	private static final int CHECKRATE_THRESHOLD = 15;
	private boolean is_temp_abnormal = false;
	private boolean is_defrost_abnormal = false;
	private boolean is_humid_abnormal = false;
	private boolean is_fan_abnormal = false;
	private boolean is_compressor_abnormal = false;
	private int countAbnormal = 0;

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

		byte tmpTxBuf = 0;
		for (int did = 0; did < DehumidRoomController.DEHUMIDIFIERS_A_ROOM; did++) {
			if (controller.getCheckRate(did) > CHECKRATE_THRESHOLD) {
				IReferenceable dehumidifier = dataStoreManager.getDehumidifier(
						offsetRoomIndex, did);
				if (dehumidifier.isHighTempWarning() && !is_temp_abnormal) {
					tmpTxBuf = (byte) PANEL_REQ_TEMP_ABNORMAL;
					is_temp_abnormal = true;
					++countAbnormal;
				} else if (dehumidifier.isDeforstTempWarning() && !is_defrost_abnormal) {
					tmpTxBuf = (byte) PANEL_REQ_DEFROST_TEMP_ABNORMAL;
					is_defrost_abnormal = true;
					++countAbnormal;
				} else if (dehumidifier.isHumidWarning() && !is_humid_abnormal) {
					tmpTxBuf = (byte) PANEL_REQ_HUMID_ABNORMAL;
					is_humid_abnormal = true;
					++countAbnormal;
				} else if (dehumidifier.isFanWarning() && !is_fan_abnormal) {
					tmpTxBuf = (byte) PANEL_REQ_FAN_ABNORMAL;
					is_fan_abnormal = true;
					++countAbnormal;
				} else if (dehumidifier.isCompressorWarning() && !is_compressor_abnormal) {
					tmpTxBuf = (byte) PANEL_REQ_COMPRESSOR_ABNORMAL;
					is_compressor_abnormal = true;
					++countAbnormal;
				}
			}
		}
		
		if (countAbnormal == 0) {
			panel.setHighTempWarn(false);
			panel.setDeforstTempWarn(false);
			panel.setHumidWarn(false);
			panel.setFanWarn(false);
			panel.setCompressorWarn(false);
			return SKIP;
		} else {
			return (countAbnormal == 1) ? (byte) tmpTxBuf : // one abnormal
				(byte) PANEL_REQ_MULTIPLE_ABNORMAL;			// multiple abnormal
		}
		
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			switch (getTxBuf()) {
			case (byte) PANEL_REQ_TEMP_ABNORMAL:
				panel.setHighTempWarn(is_temp_abnormal);
				controller.log_debug(String.format("Panel %d is high temperature abnormal.",
						offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_DEFROST_TEMP_ABNORMAL:
				panel.setDeforstTempWarn(is_defrost_abnormal);
				controller.log_debug(String.format("Panel %d is defrost temperature abnormal.",
						offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_HUMID_ABNORMAL:
				panel.setHumidWarn(is_humid_abnormal);
				controller.log_debug(String.format("Panel %d is humid abnormal.",
						offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_FAN_ABNORMAL:
				panel.setFanWarn(is_fan_abnormal);
				controller.log_debug(String.format("Panel %d is fan abnormal.",
						offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_COMPRESSOR_ABNORMAL:
				panel.setCompressorWarn(is_compressor_abnormal);
				controller.log_debug(String.format("Panel %d is compressor abnormal.",
						offsetRoomIndex));
				break;
			case (byte) PANEL_REQ_MULTIPLE_ABNORMAL:
				panel.setHighTempWarn(is_temp_abnormal);
				panel.setDeforstTempWarn(is_defrost_abnormal);
				panel.setHumidWarn(is_humid_abnormal);
				panel.setFanWarn(is_fan_abnormal);
				panel.setCompressorWarn(is_compressor_abnormal);
				controller.log_debug(String.format("Panel %d is multi abnormal.",
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
		panel.setLive(true);
		controller.jumpCmdQueue(new SynPanelHumidityCmd(controller));
		controller.nextCmd(null);
		controller.initPanelTimeoutCounter();
	}

}
