package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;

public class SetPanelHumiditySetCmd extends Command {

	public SetPanelHumiditySetCmd(DehumidRoomControllerEX controller) {
		super(controller, new SetPanelHumiditySetRequest(controller),
				new SetPanelHumiditySetReply(controller));
		// TODO Auto-generated constructor stub
	}

}
