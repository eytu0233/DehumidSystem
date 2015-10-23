package edu.ncku.uscc.process;
import edu.ncku.uscc.util.IReferenceable;
import edu.ncku.uscc.util.Log;


public abstract class SynPanelCommand extends Command implements IPanelProtocal{
	
	protected int offsetRoomIndex;
	protected IReferenceable panel;

	public SynPanelCommand(DehumidRoomControllerEX controller) {
		super(controller);
		// TODO Auto-generated constructor stub
		this.offsetRoomIndex = controller.getRoomIndex() - DehumidRoomControllerEX.ROOM_ID_MIN;
		this.panel = dataStoreManager.getPanel(offsetRoomIndex);
	}

	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.nextCmd(this);
	}

	@Override
	protected void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		Log.warn(String.format("Panel %d is not live.", offsetRoomIndex));
		panel.setLive(false);
		controller.nextCmd(this);
	}

}
