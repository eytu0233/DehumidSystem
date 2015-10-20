package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;

public class Command {

	private static int TIME_OUT = 400;

	public static final int UNACK = -1;

	private Object referenceLock;

	private AbstractRequest request;
	private AbstractReply reply;
	private Command preCommand;
	private Command subCommand;

	private int init_tolerance = 2;
	private int err_tolerance = init_tolerance;
	private byte txBuf;
	private byte rxBuf;
	private boolean skip = false;

	public Command(DehumidRoomControllerEX controller, AbstractRequest request,
			AbstractReply reply) {
		super();
		this.referenceLock = controller.getLock();
		this.request = request;
		this.request.setCmd(this);
		this.reply = reply;
		this.reply.setCmd(this);
	}

	public Command(DehumidRoomControllerEX controller, AbstractRequest request,
			AbstractReply reply, int tolerance) {
		super();
		this.referenceLock = controller.getLock();
		this.request = request;
		this.request.setCmd(this);
		this.reply = reply;
		this.reply.setCmd(this);
		this.init_tolerance = tolerance;
		this.err_tolerance = tolerance;
	}

	public Command(DehumidRoomControllerEX controller, AbstractRequest request,
			AbstractReply reply, Command preCommand) {
		super();
		this.referenceLock = controller.getLock();
		this.request = request;
		this.request.setCmd(this);
		this.reply = reply;
		this.reply.setCmd(this);
		this.preCommand = preCommand;
	}

	public void startCommand() throws Exception {
		if (request == null || reply == null) {
			// throw new NullReplyException();
			return;
		}
		
		if(preCommand != null) {
			preCommand.startCommand();
			if(!preCommand.isAck() || !preCommand.isSkip()){
				return;
			}
		}
		
		rxBuf = UNACK;
		request.requestEvent();

		if (!skip) {
			synchronized (referenceLock) {
				request.requestEmit();
				referenceLock.wait(TIME_OUT);
			}
			reply.replyEvent(rxBuf);
		}			
		
		if(!isAck()) cmdTimeout();

		if (isAck() || skip) {
			init();
			reply.ackHandler();
			if (subCommand != null){
				subCommand.startCommand();
			}				
		} else if (overTolerance()) {
			init();
			reply.timeoutHandler();
		}
		
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

}
