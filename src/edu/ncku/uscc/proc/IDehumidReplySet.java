package edu.ncku.uscc.proc;

public interface IDehumidReplySet {

	public static final int DEHUMID_REP_OK = 0x55;
	public static final int DEHUMID_REP_HIGH_TEMP_ABNORMAL = 0x56;
	public static final int DEHUMID_REP_DEFROST_TEMP_ABNORMAL = 0x57;
	public static final int DEHUMID_REP_DEHUMID_ABNORMAL = 0x58;
	public static final int DEHUMID_REP_FAN_ABNORMAL = 0x59;
	public static final int DEHUMID_REP_COMPRESSOR_ABNORMAL = 0x5A;
	
}
