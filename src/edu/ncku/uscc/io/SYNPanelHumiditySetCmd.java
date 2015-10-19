package edu.ncku.uscc.io;

public class SYNPanelHumiditySetCmd extends Command {

	public SYNPanelHumiditySetCmd(DehumidRoomControllerEX controller) {
		super(controller,
				new SYNPanelHumiditySetRequest(controller.getDataStoreManager(), controller.getOutputStream(),
						controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN),
				new SYNPanelHumiditySetReply(controller, controller.getDataStoreManager(),
						controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN));
		// TODO Auto-generated constructor stub
	}

}
