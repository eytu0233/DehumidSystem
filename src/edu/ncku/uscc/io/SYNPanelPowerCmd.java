package edu.ncku.uscc.io;

public class SYNPanelPowerCmd extends Command{

	public SYNPanelPowerCmd(DehumidRoomControllerEX controller) {
		super(controller, new SYNPanelPowerRequest(controller.getDataStoreManager(), controller.getOutputStream(), controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN), new SYNPanelPowerReply(controller, controller.getDataStoreManager(), controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN));
		// TODO Auto-generated constructor stub
	}

}
