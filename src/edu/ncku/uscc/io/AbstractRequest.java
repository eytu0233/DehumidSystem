package edu.ncku.uscc.io;

import java.io.IOException;
import java.io.OutputStream;

public abstract class AbstractRequest {
	
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
	public static final int PANEL_REQ_SETTING_HUMID = 0xCE;
	public static final int PANEL_REQ_SETTING_TIMER = 0xCF;
	public static final int PANEL_REQ_HUMID = 0x68;
	
	public static final int DEHUMID_REQ_ON = 0x30;
	public static final int DEHUMID_REQ_OFF = 0x31;
	public static final int DEHUMID_REQ_DEHUMID_MODE = 0x32;
	public static final int DEHUMID_REQ_DRY_CLOTHES_MODE = 0x33;
	public static final int DEHUMID_REQ_DEHUMIDITY_SET = 0x34;
	public static final int DEHUMID_REQ_TIMER_SET = 0x35;
	public static final int DEHUMID_REQ_DEHUMIDITY_DIGIT_ONES = 0x38;
	public static final int DEHUMID_REQ_DEHUMIDITY_DIGIT_TENS = 0x39;
	
	private byte[] txBuf = new byte[1];
	
	protected DehumidRoomControllerEX controller;
	protected Command cmd;
	
	public void setTxBuf(byte[] txBuf) {
		this.txBuf = txBuf;
	}
	
	public void setController(DehumidRoomControllerEX controller){
		this.controller = controller;
	}

	public void setCmd(Command cmd) {
		this.cmd = cmd;
	}

	abstract public void requestEvent() throws Exception;
	
	public void requestEmit() throws IOException{
		OutputStream output = controller.getOutputStream();
		if(output != null){
			output.write(txBuf);
		}else{
			//throw new NullOutputSreamException();
		}
	}
	
	public void skipCommand(){
		cmd.skipCommand();
	}

}
