package edu.ncku.uscc.io;

public class SYNPanelHumidityCmd extends Command {

	private int avgHumidity;
	
	public SYNPanelHumidityCmd(DehumidRoomControllerEX controller) {
		super(controller,
				new SYNPanelHumidityRequest(controller.getDataStoreManager(), controller.getOutputStream(),
						controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN),
				new SYNPanelHumidityReply(controller, controller.getDataStoreManager(),
						controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN));
		// TODO Auto-generated constructor stub
	}

	public int getAvgHumidity() {
		return avgHumidity;
	}

	public void setAvgHumidity(int avgHumidity) {
		this.avgHumidity = avgHumidity;
	}
	

}
