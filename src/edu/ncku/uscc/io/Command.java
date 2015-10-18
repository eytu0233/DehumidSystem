package edu.ncku.uscc.io;

public class Command {
	
	private static int TIME_OUT = 400;
	
	public static final int UNACK = -1;

	private Object referenceLock;

	private AbstractRequest request;
	private AbstractReply reply;
	private Command subCommand;

	private int init_tolerance = 2;
	private int err_tolerance = init_tolerance;
	private byte rxBuf;	
	private boolean skip = false;
	private boolean ack = false;
	
	public Command(DehumidRoomControllerEX controller, AbstractRequest request,
			AbstractReply reply) {
		super();
		this.referenceLock = controller.getLock();
		this.request = request;
		this.request.setCmd(this);
		this.request.setController(controller);
		this.reply = reply;
		this.reply.setCmd(this);
		this.reply.setController(controller);
	}

	public Command(DehumidRoomControllerEX controller, AbstractRequest request,
			AbstractReply reply, int tolerance) {
		super();
		this.referenceLock = controller.getLock();
		this.request = request;
		this.request.setCmd(this);
		this.request.setController(controller);
		this.reply = reply;
		this.reply.setCmd(this);
		this.reply.setController(controller);
		this.init_tolerance = tolerance;
		this.err_tolerance = tolerance;
	}

	public Command(DehumidRoomControllerEX controller, AbstractRequest request,
			AbstractReply reply, int tolerance, Command subCommand) {
		super();
		this.referenceLock = controller.getLock();
		this.request = request;
		this.request.setCmd(this);
		this.request.setController(controller);
		this.reply = reply;
		this.reply.setCmd(this);
		this.reply.setController(controller);
		this.init_tolerance = tolerance;
		this.err_tolerance = tolerance;
		this.subCommand = subCommand;
	}

	public void startCommand() throws Exception {
		if (request != null) {
			rxBuf = UNACK;
			request.requestEvent();
			
			if (!skip) {
				synchronized (referenceLock) {
					request.requestEmit();
					referenceLock.wait(TIME_OUT);
				}
			}
			
			if (reply != null) {
				if (!skip)
					reply.replyEvent(rxBuf);

				if(ack || skip){
					init();
					reply.ackHandler();
				}else if(isShutDown()){
					init();
					reply.timeoutHandler();
				}
			} else {
				// throw new NullReplyException();
			}
		} else {
			// throw new NullRequestException();
		}

		if (subCommand != null)
			subCommand.startCommand();
	}

	public void init() {
		this.skip = false;
		this.ack = false;
		this.err_tolerance = init_tolerance;
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

	public void setAck(boolean ack) {
		this.ack = ack;
	}

	public boolean isAck() {
		return this.ack;
	}

	public void cmdTimeout() {
		--err_tolerance;
	}

	public boolean isShutDown() {
		return err_tolerance <= 0;
	}

}
