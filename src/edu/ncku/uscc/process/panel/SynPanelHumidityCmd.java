package edu.ncku.uscc.process.panel;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.process.dehumidifier.SynDehumidifierPowerCmd;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public class SynPanelHumidityCmd extends SynPanelCommand {

	private static final int MIN_HUMIDITY = 40;
	private static final int MAX_HUMIDITY = 90;
	private static final int PANEL_CMD_HUMID = 0x68;
	private static final int DEHUMIDIFIERS_A_ROOM = 8;

	private int avgHumidity;

	public SynPanelHumidityCmd(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		dehumidifierFate();

		if (!panel.isOn()) {
			return SKIP;
		}

		int humidity = 0, count = 0;

		for (int did = 0; did < DehumidRoomController.DEHUMIDIFIERS_A_ROOM; did++) {
			IReferenceable dehumidifier = dataStoreManager.getDehumidifier(offsetRoomIndex, did);
			if (dehumidifier.isLive()) {
				humidity += dehumidifier.getHumid();
				count++;
			}
		}

		if (count > 0) {
			avgHumidity = humidity / count;
		}

		if (count == 0 || avgHumidity < MIN_HUMIDITY || avgHumidity > MAX_HUMIDITY) {
			Log.debug("Humidity for panel is not in range : " + avgHumidity);
			return SKIP;
		} else {
			return (byte) (PANEL_CMD_HUMID + avgHumidity);
		}
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_OK) {
			Log.info(String.format("The humidity of Panel %d is %d.", offsetRoomIndex, avgHumidity));
			panel.setHumid(avgHumidity);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Decides whether the dehumidifier can add command or not
	 */
	private void dehumidifierFate() {

		for (int did = DEHUMIDIFIERS_A_ROOM - 1; did >= 0; did--) {
			if (controller.getCheckRate(did) >= (int) (Math.random() * 100)) {
				controller.jumpCmdQueue(new SynDehumidifierPowerCmd(controller, did));
			}
		}

	}

}