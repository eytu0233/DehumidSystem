package edu.ncku.uscc.io;

import java.io.IOException;

public class Command {

	private static int TIME_OUT = 400;
	
	private Object referenceLock;
	
	private AbstractRequest request;
	private AbstractReply reply;
	private Command subCommand;

	public Command(Object referenceLock, AbstractRequest request,
			AbstractReply reply) {
		super();
		this.referenceLock = referenceLock;
		this.request = request;
		this.reply = reply;
	}

	public Command(Object referenceLock, AbstractRequest request,
			AbstractReply reply, Command subCommand) {
		super();
		this.referenceLock = referenceLock;
		this.request = request;
		this.reply = reply;
		this.subCommand = subCommand;
	}

	public void startCommand() throws Exception {
		if (request != null) {
			request.requestEvent();
			synchronized (referenceLock) {
				request.requestEmit();
				referenceLock.wait(TIME_OUT);
			}
			if(reply != null) {
				reply.checkReplyEvent();
			}else{
				//throw new NullReplyException();
			}
		} else {
			//throw new NullRequestException();
		}

		if (subCommand != null)
			subCommand.startCommand();
	}

	public void driveReply(byte rxBuf) {
		if(reply != null) {
			reply.replyEvent(rxBuf);
		}else{
			//throw new NullReplyException();
		}
	}

}
