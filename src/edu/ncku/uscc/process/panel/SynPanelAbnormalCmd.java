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
				if (dehumidifier.isHighTempWarning()) {
					tmpTxBuf = (byte) PANEL_REQ_TEMP_ABNORMAL;
					if (is_temp_abnormal)
						continue;
					
					is_temp_abnormal = true;
					++countAbnormal;
				} else if (dehumidifier.isDeforstTempWarning()) {
					tmpTxBuf = (byte) PANEL_REQ_DEFROST_TEMP_ABNORMAL;
					if (is_defrost_abnormal)
						continue;
					
					is_defrost_abnormal = true;
					++countAbnormal;
				} else if (dehumidifier.isHumidWarning()) {
					tmpTxBuf = (byte) PANEL_REQ_HUMID_ABNORMAL;
					if (is_humid_abnormal)
						continue;
					
					is_humid_abnormal = true;
					++countAbnormal;
				} else if (dehumidifier.isFanWarning()) {
					tmpTxBuf = (byte) PANEL_REQ_FAN_ABNORMAL;
					if (is_fan_abnormal)
						continue;
					
					is_fan_abnormal = true;
					++countAbnormal;
				} else if (dehumidifier.isCompressorWarning()) {
					tmpTxBuf = (byte) PANEL_REQ_COMPRESSOR_ABNORMAL;
					if (is_compressor_abnormal)
						continue;
					
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
			if (!is_temp_abnormal) 
				panel.setHighTempWarn(false);
			
			if (!is_defrost_abnormal)
				panel.setDeforstTempWarn(false);
				
			if (!is_humid_abnormal)
				panel.setHumidWarn(false);

			if (!is_fan_abnormal)
				panel.setFanWarn(false);

			if (!is_compressor_abnormal)
				panel.setCompressorWarn(false);
			
			if (countAbnormal == 1)
				return (byte) tmpTxBuf;
			
			// multiple abnormal
			return SKIP;
		}
		
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
				is_temp_abnormal = false;
				break;
			case (byte) PANEL_REQ_DEFROST_TEMP_ABNORMAL:
				panel.setDeforstTempWarn(true);
				controller.log_debug(String.format(
						"Panel %d is defrost temperature abnormal.",
						offsetRoomIndex));
				is_defrost_abnormal = false;
				break;
			case (byte) PANEL_REQ_HUMID_ABNORMAL:
				panel.setHumidWarn(true);
				controller.log_debug(String.format("Panel %d is humid abnormal.",
						offsetRoomIndex));
				is_humid_abnormal = false;
				break;
			case (byte) PANEL_REQ_FAN_ABNORMAL:
				panel.setFanWarn(true);
				controller.log_debug(String.format("Panel %d is fan abnormal.",
						offsetRoomIndex));
				is_fan_abnormal = false;
				break;
			case (byte) PANEL_REQ_COMPRESSOR_ABNORMAL:
				panel.setCompressorWarn(true);
				controller.log_debug(String.format("Panel %d is compressor abnormal.",
						offsetRoomIndex));
				is_compressor_abnormal = false;
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
		if (countAbnormal > 1) {
			if (is_temp_abnormal) 
				controller.jumpCmdQueue(new SetPanelAbnormalCmd(
						controller, true, false, false, false, false));
			
			if (is_defrost_abnormal)
				controller.jumpCmdQueue(new SetPanelAbnormalCmd(
						controller, false, true, false, false, false));
				
			if (is_humid_abnormal)
				controller.jumpCmdQueue(new SetPanelAbnormalCmd(
						controller, false, false, true, false, false));

			if (is_fan_abnormal)
				controller.jumpCmdQueue(new SetPanelAbnormalCmd(
						controller, false, false, false, true, false));

			if (is_compressor_abnormal)
				controller.jumpCmdQueue(new SetPanelAbnormalCmd(
						controller, false, false, false, false, true));

		}
	}

}
