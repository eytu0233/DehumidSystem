package edu.ncku.uscc.io;

import edu.ncku.uscc.util.DataStoreManager;

public abstract class AbstractReply {

	protected DehumidRoomControllerEX controller;
	protected DataStoreManager dataStoreManager;
	protected int offsetRoomIndex;
	protected Command cmd;	

	public AbstractReply(DehumidRoomControllerEX controller,
			DataStoreManager dataStoreManager,
			int roomIndex) {
		super();
		this.controller = controller;
		this.dataStoreManager = dataStoreManager;
		this.offsetRoomIndex = roomIndex;
	}

	public void setCmd(Command cmd) {
		this.cmd = cmd;
	}
	
	abstract public void replyEvent(Byte rxBuf) throws Exception;
	abstract public void ackHandler() throws Exception;
	abstract public void timeoutHandler() throws Exception;
	
}
