package edu.ncku.uscc.proc;

import java.io.IOException;
import java.io.OutputStream;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.DataStoreManager;

public abstract class AbstractRequest {	
	
	private byte[] txBuf = new byte[1];
	
	protected DehumidRoomControllerEX controller;
	protected DataStoreManager dataStoreManager;
	protected OutputStream output;
	protected Command cmd;
	
	public AbstractRequest(DehumidRoomControllerEX controller) {
		super();
		this.controller = controller;
		this.dataStoreManager = controller.getDataStoreManager();
		this.output = controller.getOutputStream();
	}

	public void setTxBuf(byte[] txBuf) {
		this.txBuf = txBuf;
		cmd.setTxBuf(txBuf[0]);
	}

	public void setCmd(Command cmd) {
		this.cmd = cmd;
	}

	abstract public void requestEvent() throws Exception;
	
	public void requestEmit() throws IOException{
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
