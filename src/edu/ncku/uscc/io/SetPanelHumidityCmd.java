package edu.ncku.uscc.io;

public class SetPanelHumidityCmd extends Command {

	public SetPanelHumidityCmd(DehumidRoomControllerEX controller) {
		super(controller, new SetPanelHumidityRequest(controller.getDataStoreManager(), controller.getOutputStream(), controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN), 
				new SetPanelHumidityReply(controller, controller.getDataStoreManager(), controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN));
		// TODO Auto-generated constructor stub
	}

}
