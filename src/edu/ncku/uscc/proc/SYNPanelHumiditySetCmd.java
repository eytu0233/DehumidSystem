package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;

public class SYNPanelHumiditySetCmd extends Command {

	public SYNPanelHumiditySetCmd(DehumidRoomControllerEX controller) {
		super(controller, new SYNPanelHumiditySetRequest(controller),
				new SYNPanelHumiditySetReply(controller));
		// TODO Auto-generated constructor stub
	}

}
