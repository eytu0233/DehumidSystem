package edu.ncku.uscc.proc;

import java.io.IOException;
import java.io.OutputStream;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.DataStoreManager;

public abstract class Command {

	private static int TIME_OUT = 400;

	public static final int UNACK = -1;

	private Object referenceLock;

	protected DehumidRoomControllerEX controller;
	protected DataStoreManager dataStoreManager;
	private Command preCommand;
	private Command subCommand;

	private int init_tolerance = 2;
	private int err_tolerance = init_tolerance;
	private byte txBuf;
	private byte rxBuf;
	private boolean skip = false;

	public Command(DehumidRoomControllerEX controller) {
		super();
		this.referenceLock = controller.getLock();
	}

	public Command(DehumidRoomControllerEX controller, int tolerance) {
		super();
		this.referenceLock = controller.getLock();
		this.init_tolerance = tolerance;
		this.init();
	}

	public Command(DehumidRoomControllerEX controller, Command preCommand) {
		super();
		this.referenceLock = controller.getLock();
		this.preCommand = preCommand;
		this.init();
	}

	public void init() {
		this.skip = false;
		this.err_tolerance = init_tolerance;
		this.subCommand = null;
	}
	
	public void setSubCommand(Command subCommand){
		this.subCommand = subCommand;
	}

	public byte getTxBuf() {
		return txBuf;
	}

	public void setTxBuf(byte txBuf) {
		this.txBuf = txBuf;
	}

	public void setRxBuf(byte rxBuf) {
		this.rxBuf = rxBuf;
	}

	public void skipCommand() {
		this.skip = true;
	}

	public boolean isSkip() {
		return this.skip;
	}

	public boolean isAck() {
		return rxBuf != UNACK;
	}

	public void cmdTimeout() {
		--err_tolerance;
	}

	public boolean overTolerance() {
		return err_tolerance <= 0;
	}
	
	private void requestEmit() throws IOException{
		OutputStream output = controller.getOutputStream();
		if(output != null){
			output.write(txBuf);
		}else{
			//throw new NullOutputSreamException();
		}
	}

	public void startCommand() throws Exception {
		
		if(preCommand != null) {
			preCommand.startCommand();
			if(!preCommand.isAck() && !preCommand.isSkip()){
				return;
			}
		}
		
		rxBuf = UNACK;
		requestHandler();

		if (!isSkip()) {
			synchronized (referenceLock) {
				requestEmit();
				referenceLock.wait(TIME_OUT);
			}
			replyHandler(rxBuf);
		}			
		
		if(!isAck()) {
			cmdTimeout();
			if (overTolerance()) {
				init();
				timeoutHandler();
				return;
			}
		}

		if (isAck() || isSkip()) {
			init();
			if (subCommand != null){
				subCommand.startCommand();
				if(!subCommand.isAck() && !subCommand.isSkip()){
					return;
				}
			}
			finishHandler();			
		}	
	}
	
	abstract protected void requestHandler() throws Exception;
	abstract protected void replyHandler(Byte rxBuf) throws Exception;
	abstract protected void finishHandler() throws Exception;
	abstract protected void timeoutHandler() throws Exception;

}
