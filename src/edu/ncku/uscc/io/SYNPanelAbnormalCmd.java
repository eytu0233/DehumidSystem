package edu.ncku.uscc.io;

import edu.ncku.uscc.proc.Command;

public class SYNPanelAbnormalCmd extends Command {

	public SYNPanelAbnormalCmd(DehumidRoomControllerEX controller) {
		super(controller, new SYNPanelAbnormalRequest(controller), new SYNPanelAbnormalReply(controller));
		// TODO Auto-generated constructor stub
	}

}
