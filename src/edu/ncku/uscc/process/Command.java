package edu.ncku.uscc.process;

import java.io.IOException;
import java.io.OutputStream;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.Log;

public abstract class Command {

	private static int TIME_OUT = 400;

	public static final int UNACK = -1;
	public static final int SKIP = -1;

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

	private boolean ack;

	public Command(DehumidRoomControllerEX controller) {
		super();
		this.controller = controller;
		this.referenceLock = controller.getLock();
		this.dataStoreManager = controller.getDataStoreManager();
	}

	public Command(DehumidRoomControllerEX controller, int tolerance) {
		super();
		this.controller = controller;
		this.referenceLock = controller.getLock();
		this.dataStoreManager = controller.getDataStoreManager();
		this.init_tolerance = tolerance;
		this.init();
	}

	public Command(DehumidRoomControllerEX controller, Command preCommand) {
		super();
		this.controller = controller;
		this.referenceLock = controller.getLock();
		this.dataStoreManager = controller.getDataStoreManager();
		this.preCommand = preCommand;
		this.init();
	}

	public void init() {
		this.skip = false;
		this.err_tolerance = init_tolerance;
		this.subCommand = null;
	}

	public void setPreCommand(Command preCommand) {
		this.preCommand = preCommand;
	}

	public void setSubCommand(Command subCommand) {
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

	/*
	 * This method makes that this command skips txBuf emiting and rxBuf
	 * receiving, but it still call finishHandler method
	 */
	public void setAck(boolean ack) {
		this.ack = ack;
	}

	public boolean isAck() {
		return ack;
	}

	public void cmdTimeout() {
		--err_tolerance;
	}

	public boolean overTolerance() {
		return err_tolerance <= 0;
	}

	public final void startCommand() throws Exception {

		// run the command first before this command
		if (preCommand != null) {
			preCommand.startCommand();

			if (!preCommand.isAck() && preCommand.getTxBuf() != SKIP) {
				if (preCommand.overTolerance()) {
					finishCommandHandler();
				}
				return;
			}
		}

		rxBuf = UNACK;
		txBuf = requestHandler();

		// when skip flag is true, it won't emit data and handle reply
		if (txBuf != SKIP) {
			synchronized (referenceLock) {
				requestEmit();
				referenceLock.wait(TIME_OUT);
			}
			// the hook method which handles reply
			replyHandler(rxBuf);
			if (!isAck()) {
				cmdTimeout();
				if (overTolerance()) {
					init();
					timeoutHandler();
				}
				return;
			}
		}else{
//			Log.debug(this.toString() + " : Skip");
		}

		if (isAck() || txBuf == SKIP) {	
			init();
			if (subCommand != null) {
				subCommand.startCommand();
				if (!subCommand.isAck() && subCommand.getTxBuf() != SKIP) {
					return;
				}
			}
			finishCommandHandler();
		}
	}

	private void requestEmit() throws IOException {
		OutputStream output = controller.getOutputStream();
		if (output != null) {
			output.write(txBuf);
		} else {
			// throw new NullOutputSreamException();
		}
	}

	abstract protected byte requestHandler() throws Exception;

	abstract protected void replyHandler(Byte rxBuf) throws Exception;

	abstract protected void finishCommandHandler() throws Exception;

	abstract protected void timeoutHandler() throws Exception;

}
