package edu.ncku.uscc.process.panel;
import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.process.Command;
import edu.ncku.uscc.util.IReferenceable;


public abstract class SynPanelCommand extends Command implements IPanelProtocal{
	
	protected int offsetRoomIndex;
	protected IReferenceable panel;

	public SynPanelCommand(DehumidRoomController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
		this.offsetRoomIndex = controller.getRoomIndex() - DehumidRoomController.ROOM_ID_MIN;
		this.panel = dataStoreManager.getPanel(offsetRoomIndex);
	}

	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		//controller.nextCmd(this);
	}

	@Override
	protected void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		panel.setLive(false);
		//controller.nextCmd(this);
		
		controller.log_warn(String.format("Panel %d is not live.", 
				offsetRoomIndex));
	}

}
