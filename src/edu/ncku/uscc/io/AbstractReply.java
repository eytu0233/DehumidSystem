package edu.ncku.uscc.io;

public abstract class AbstractReply {

	public static final int PANEL_REP_ON = 0x30;
	public static final int PANEL_REP_OFF = 0x31;
	public static final int PANEL_REP_DEHUMID = 0x32;
	public static final int PANEL_REP_DRY_CLOTHES = 0x33;
	public static final int PANEL_REP_NO_SET = 0x34;
	public static final int PANEL_REP_HUMID_SET = 0x35;
	public static final int PANEL_REP_TIMER_SET = 0x36;
	public static final int PANEL_REP_OK = 0x55;
	
	public static final int DEHUMID_REP_OK = 0x55;
	public static final int DEHUMID_REP_HIGH_TEMP_ABNORMAL = 0x56;
	public static final int DEHUMID_REP_DEFROST_TEMP_ABNORMAL = 0x57;
	public static final int DEHUMID_REP_DEHUMID_ABNORMAL = 0x58;
	public static final int DEHUMID_REP_FAN_ABNORMAL = 0x59;
	public static final int DEHUMID_REP_COMPRESSOR_ABNORMAL = 0x5A;
	
	protected DehumidRoomControllerEX controller;
	protected Command cmd;	
	

	public void setController(DehumidRoomControllerEX controller) {
		this.controller = controller;
	}

	public void setCmd(Command cmd) {
		this.cmd = cmd;
	}
	
	abstract public void replyEvent(Byte rxBuf) throws Exception;
	abstract public void ackHandler() throws Exception;
	abstract public void timeoutHandler() throws Exception;
	
}
