package edu.ncku.uscc.io;

import edu.ncku.uscc.proc.Command;

public class NotifyDeviceIDCmd extends Command {
	
	private int did;

	public NotifyDeviceIDCmd(DehumidRoomControllerEX controller, int did) {
		super(controller, new NotifyDeviceIDRequest(controller), new NotifyDeviceIDReply(controller));
		// TODO Auto-generated constructor stub
		this.did = did;
	}

	public int getDid() {
		return did;
	}

}
