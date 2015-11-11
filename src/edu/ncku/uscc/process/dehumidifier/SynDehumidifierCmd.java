package edu.ncku.uscc.process.dehumidifier;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.process.Command;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public abstract class SynDehumidifierCmd extends Command implements IDehumidProtocal{

	protected int did;
	protected int offsetRoomIndex;
	protected IReferenceable panel;
	protected IReferenceable dehumidifier;

	public SynDehumidifierCmd(DehumidRoomController controller, int did) {
		super(controller, new NotifyDeviceIDCmd(controller, did));
		// TODO Auto-generated constructor stub
		this.did = did;
		this.offsetRoomIndex = controller.getRoomIndex()
				- DehumidRoomController.ROOM_ID_MIN;
		this.panel = dataStoreManager.getPanel(offsetRoomIndex);
		this.dehumidifier = dataStoreManager.getDehumidifier(offsetRoomIndex,
				did);
	}

	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.nextCmd(null);
	}

	@Override
	protected void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		Log.error(String.format("Dehumidifier %d in room %d timeout.", did, offsetRoomIndex));
		controller.nextCmd(null);
	}

}
