package edu.ncku.uscc.process;

public interface IDehumidProtocal {

	public static final int DEHUMID_REP_OK = 0x55;
	public static final int DEHUMID_REP_HIGH_TEMP_ABNORMAL = 0x56;
	public static final int DEHUMID_REP_DEFROST_TEMP_ABNORMAL = 0x57;
	public static final int DEHUMID_REP_DEHUMID_ABNORMAL = 0x58;
	public static final int DEHUMID_REP_FAN_ABNORMAL = 0x59;
	public static final int DEHUMID_REP_COMPRESSOR_ABNORMAL = 0x5A;

	public static final int DEHUMID_REQ_ON = 0x30;
	public static final int DEHUMID_REQ_OFF = 0x31;
	public static final int DEHUMID_REQ_DEHUMID_MODE = 0x32;
	public static final int DEHUMID_REQ_DRY_CLOTHES_MODE = 0x33;
	public static final int DEHUMID_REQ_DEHUMIDITY_SET = 0x34;
	public static final int DEHUMID_REQ_TIMER_SET = 0x35;
	public static final int DEHUMID_REQ_DEHUMIDITY_DIGIT_ONES = 0x38;
	public static final int DEHUMID_REQ_DEHUMIDITY_DIGIT_TENS = 0x39;
	
}
