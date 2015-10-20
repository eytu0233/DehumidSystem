package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;

public class SYNPanelPowerCmd extends Command {

	public SYNPanelPowerCmd(DehumidRoomControllerEX controller) {
		super(controller, new SYNPanelPowerRequest(controller), new SYNPanelPowerReply(controller));
		// TODO Auto-generated constructor stub
	}

}
