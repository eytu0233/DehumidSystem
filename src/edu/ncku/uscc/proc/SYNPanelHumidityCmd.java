package edu.ncku.uscc.proc;

import edu.ncku.uscc.io.DehumidRoomControllerEX;

public class SYNPanelHumidityCmd extends Command {

	private int avgHumidity;

	public SYNPanelHumidityCmd(DehumidRoomControllerEX controller) {
		super(controller, new SYNPanelHumidityRequest(controller),
				new SYNPanelHumidityReply(controller));
		// TODO Auto-generated constructor stub
	}

	public int getAvgHumidity() {
		return avgHumidity;
	}

	public void setAvgHumidity(int avgHumidity) {
		this.avgHumidity = avgHumidity;
	}

}
