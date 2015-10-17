package edu.ncku.uscc.io;

public abstract class AbstractReply {

	private static final int PANEL_REP_ON = 0x30;
	private static final int PANEL_REP_OFF = 0x31;
	private static final int PANEL_REP_DEHUMID = 0x32;
	private static final int PANEL_REP_DRY_CLOTHES = 0x33;
	private static final int PANEL_REP_NO_SET = 0x34;
	private static final int PANEL_REP_HUMID_SET = 0x35;
	private static final int PANEL_REP_TIMER_SET = 0x36;
	private static final int PANEL_REP_OK = 0x55;
	
	private static final int DEHUMID_REP_OK = 0x55;
	private static final int DEHUMID_REP_HIGH_TEMP_ABNORMAL = 0x56;
	private static final int DEHUMID_REP_DEFROST_TEMP_ABNORMAL = 0x57;
	private static final int DEHUMID_REP_DEHUMID_ABNORMAL = 0x58;
	private static final int DEHUMID_REP_FAN_ABNORMAL = 0x59;
	private static final int DEHUMID_REP_COMPRESSOR_ABNORMAL = 0x5A;
	
	abstract public void replyEvent(Byte rxBuf);
	abstract public void checkReplyEvent();
	
}
