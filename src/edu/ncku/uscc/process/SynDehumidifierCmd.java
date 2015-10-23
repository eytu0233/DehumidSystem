package edu.ncku.uscc.process;

import edu.ncku.uscc.util.IReferenceable;

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
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.nextCmd(this);
	}

	@Override
	protected void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		dehumidifier.setLive(false);
		controller.nextCmd(this);
	}

}
