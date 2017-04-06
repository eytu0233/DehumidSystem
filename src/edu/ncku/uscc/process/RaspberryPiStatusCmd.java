package edu.ncku.uscc.process;

import java.lang.ProcessBuilder;
import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.util.DataStoreManager.RaspberryPi;

public class RaspberryPiStatusCmd extends Command {
	private static final String[] SHUTDOWN_SCRIPT = {"/bin/bash", 
			"/home/pi/workspace/pi_shutdown.sh"};
	
	private RaspberryPi pi;

	public RaspberryPiStatusCmd(DehumidRoomController controller) {
		super(controller);
		this.pi = dataStoreManager.getRaspberryPi();
	}

	@Override
	protected byte requestHandler() throws Exception {
		if (pi.isShutdown()) {
			Process p = new ProcessBuilder(SHUTDOWN_SCRIPT).start();
		}
		return SKIP;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		return false;
	}

	@Override
	protected void finishHandler() throws Exception {
		controller.nextCmd(this);
	}

	@Override
	protected void timeoutHandler() throws Exception {
		controller.nextCmd(this);
	}
	
}