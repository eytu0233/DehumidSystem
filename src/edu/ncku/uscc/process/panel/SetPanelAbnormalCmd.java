package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;

public class SetPanelAbnormalCmd extends SynPanelCommand {
	
	private boolean is_temp_abnormal = false;
	private boolean is_defrost_abnormal = false;
	private boolean is_humid_abnormal = false;
	private boolean is_fan_abnormal = false;
	private boolean is_compressor_abnormal = false;

	public SetPanelAbnormalCmd(DehumidRoomController controller, boolean temp, 
			boolean defrost, boolean humid, boolean fan, boolean compressor) {
		super(controller);
		// TODO Auto-generated constructor stub
		this.is_temp_abnormal = temp;
		this.is_defrost_abnormal = defrost;
		this.is_humid_abnormal = humid;
		this.is_fan_abnormal = fan;
		this.is_compressor_abnormal = compressor;
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		if (!panel.isLive()) {
			return SKIP;
		}

		if (is_temp_abnormal)
			return (byte) PANEL_REQ_TEMP_ABNORMAL;
		
		if (is_defrost_abnormal)
			return (byte) PANEL_REQ_DEFROST_TEMP_ABNORMAL;
			
		if (is_humid_abnormal)
			return (byte) PANEL_REQ_HUMID_ABNORMAL;

		if (is_fan_abnormal)
			return (byte) PANEL_REQ_FAN_ABNORMAL;

		if (is_compressor_abnormal)
			return (byte) PANEL_REQ_COMPRESSOR_ABNORMAL;
		
		return SKIP;
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
			}
			panel.setLive(true);
			return true;
		} else {
			return false;
		}
	}

}
