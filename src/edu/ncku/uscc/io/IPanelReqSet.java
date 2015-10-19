package edu.ncku.uscc.io;

public interface IPanelReqSet {

	public static final int PANEL_REQ_ONOFF = 0x80;
	public static final int PANEL_REQ_MODE = 0x81;
	public static final int PANEL_REQ_SET = 0x82;
	public static final int PANEL_REQ_HUMID_SET = 0x83;
	public static final int PANEL_REQ_TIMER_SET = 0x84;
	public static final int PANEL_REQ_START = 0x85;
	public static final int PANEL_REQ_SHUTDOWM = 0x86;
	public static final int PANEL_REQ_TEMP_ABNORMAL = 0x87;
	public static final int PANEL_REQ_DEFROST_TEMP_ABNORMAL = 0x88;
	public static final int PANEL_REQ_MINUS_TIMER = 0x89;
	public static final int PANEL_REQ_DEHUMID_MODE = 0x8A;
	public static final int PANEL_REQ_DRYCLOTHES_MODE = 0x8B;
	public static final int PANEL_REQ_HUMID_ABNORMAL = 0x8D;
	public static final int PANEL_REQ_FAN_ABNORMAL = 0x8E;
	public static final int PANEL_REQ_COMPRESSOR_ABNORMAL = 0x8F;
	public static final int PANEL_REQ_SETTING_HUMID_Set = 0xCE;
	public static final int PANEL_REQ_SETTING_TIMER = 0xCF;
	
}
