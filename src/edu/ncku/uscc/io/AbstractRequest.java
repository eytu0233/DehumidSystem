package edu.ncku.uscc.io;

import java.io.IOException;
import java.io.OutputStream;

public abstract class AbstractRequest {
	
	private static final int PANEL_REQ_ONOFF = 0x80;
	private static final int PANEL_REQ_MODE = 0x81;
	private static final int PANEL_REQ_SET = 0x82;
	private static final int PANEL_REQ_HUMID_SET = 0x83;
	private static final int PANEL_REQ_TIMER_SET = 0x84;
	private static final int PANEL_REQ_START = 0x85;
	private static final int PANEL_REQ_SHUTDOWM = 0x86;
	private static final int PANEL_REQ_TEMP_ABNORMAL = 0x87;
	private static final int PANEL_REQ_DEFROST_TEMP_ABNORMAL = 0x88;
	private static final int PANEL_REQ_MINUS_TIMER = 0x89;
	private static final int PANEL_REQ_DEHUMID_MODE = 0x8A;
	private static final int PANEL_REQ_DRYCLOTHES_MODE = 0x8B;
	private static final int PANEL_REQ_HUMID_ABNORMAL = 0x8D;
	private static final int PANEL_REQ_FAN_ABNORMAL = 0x8E;
	private static final int PANEL_REQ_COMPRESSOR_ABNORMAL = 0x8F;
	private static final int PANEL_REQ_SETTING_HUMID = 0xCE;
	private static final int PANEL_REQ_SETTING_TIMER = 0xCF;
	private static final int PANEL_REQ_HUMID = 0x68;
	
	private static final int DEHUMID_REQ_ON = 0x30;
	private static final int DEHUMID_REQ_OFF = 0x31;
	private static final int DEHUMID_REQ_DEHUMID_MODE = 0x32;
	private static final int DEHUMID_REQ_DRY_CLOTHES_MODE = 0x33;
	private static final int DEHUMID_REQ_DEHUMIDITY_SET = 0x34;
	private static final int DEHUMID_REQ_TIMER_SET = 0x35;
	private static final int DEHUMID_REQ_DEHUMIDITY_DIGIT_ONES = 0x38;
	private static final int DEHUMID_REQ_DEHUMIDITY_DIGIT_TENS = 0x39;
	
	protected byte[] txBuf = new byte[1];
	
	private DehumidRoomController controller;
	
	abstract public void requestEvent() throws Exception;
	
	public void requestEmit() throws IOException{
		OutputStream output = controller.getOutputStream();
		if(output != null){
			output.write(txBuf);
		}else{
			//throw new NullOutputSreamException();
		}
	}

}
