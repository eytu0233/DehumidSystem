package edu.ncku.uscc.io;

public class SYNPanelModeCmd extends Command {

	public SYNPanelModeCmd(DehumidRoomControllerEX controller) {
		super(controller, new SYNPanelModeRequest(controller.getDataStoreManager(), controller.getOutputStream(), controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN), new SYNPanelModeReply(controller, controller.getDataStoreManager(), controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN));
		// TODO Auto-generated constructor stub
	}
	
}
