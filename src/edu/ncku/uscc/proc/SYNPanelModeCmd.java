package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;

public class SYNPanelModeCmd extends Command {

	public SYNPanelModeCmd(DehumidRoomControllerEX controller) {
		super(controller, new SYNPanelModeRequest(controller),
				new SYNPanelModeReply(controller));
		// TODO Auto-generated constructor stub
	}

}
