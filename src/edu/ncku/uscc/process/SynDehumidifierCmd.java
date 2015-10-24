package edu.ncku.uscc.process;

import edu.ncku.uscc.io.DehumidRoomControllerEX;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;

public abstract class SynDehumidifierCmd extends Command implements IDehumidProtocal{

	protected int did;
	protected int offsetRoomIndex;
	protected IReferenceable panel;
	protected IReferenceable dehumidifier;

	public SynDehumidifierCmd(DehumidRoomControllerEX controller, int did) {
		super(controller, new NotifyDeviceIDCmd(controller, did));
		// TODO Auto-generated constructor stub
		this.did = did;
		this.offsetRoomIndex = controller.getRoomIndex()
				- DehumidRoomControllerEX.ROOM_ID_MIN;
		this.panel = dataStoreManager.getPanel(offsetRoomIndex);
		this.dehumidifier = dataStoreManager.getDehumidifier(offsetRoomIndex,
				did);
	}

	@Override
	protected void finishCommandHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.nextCmd(this);
	}

	@Override
	protected void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		Log.debug(String.format("Dehumidifier %d in room %d timeout.", did, offsetRoomIndex));
		dehumidifier.setLive(false);
		controller.nextCmd(this);
	}

}
