package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.DataStoreManager;

public abstract class AbstractReply {

	protected DehumidRoomControllerEX controller;
	protected DataStoreManager dataStoreManager;
	protected Command cmd;	

	public AbstractReply(DehumidRoomControllerEX controller) {
		super();
		this.controller = controller;
		this.dataStoreManager = controller.getDataStoreManager();
	}

	public void setCmd(Command cmd) {
		this.cmd = cmd;
	}
	
	abstract public void replyEvent(Byte rxBuf) throws Exception;
	abstract public void ackHandler() throws Exception;
	abstract public void timeoutHandler() throws Exception;
	
}
