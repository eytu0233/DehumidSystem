package edu.ncku.uscc.io;

import java.io.IOException;
import java.io.OutputStream;

import edu.ncku.uscc.util.DataStoreManager;

public abstract class AbstractRequest {	
	
	private byte[] txBuf = new byte[1];
	
	protected DataStoreManager dataStoreManager;
	protected OutputStream output;
	protected int offsetRoomIndex;
	protected Command cmd;
	
	public AbstractRequest(DataStoreManager dataStoreManager,
			OutputStream output, int roomIndex) {
		super();
		this.dataStoreManager = dataStoreManager;
		this.output = output;
		this.offsetRoomIndex = roomIndex - DehumidRoomControllerEX.ROOM_ID_MIN;
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
