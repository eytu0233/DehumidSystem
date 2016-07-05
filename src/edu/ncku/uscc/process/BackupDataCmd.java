package edu.ncku.uscc.process;

import edu.ncku.uscc.io.BackupData;
import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.process.dehumidifier.IDehumidProtocal;
import edu.ncku.uscc.util.IReferenceable;
//import edu.ncku.uscc.util.Log;

public class BackupDataCmd extends Command implements IDehumidProtocal {

	protected int offsetRoomIndex;
	protected IReferenceable panel;
	protected IReferenceable[] dehumidifier = new IReferenceable[DehumidRoomController.DEHUMIDIFIERS_A_ROOM];

	public BackupDataCmd(DehumidRoomController controller) {
		// TODO Auto-generated constructor stub
		super(controller);
		this.offsetRoomIndex = controller.getRoomIndex() - DehumidRoomController.ROOM_ID_MIN;
		this.panel = dataStoreManager.getPanel(offsetRoomIndex);
		
		for (int did = 0; did < DehumidRoomController.DEHUMIDIFIERS_A_ROOM; did++) {
			this.dehumidifier[did] = dataStoreManager.getDehumidifier(offsetRoomIndex, did);
		}
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		BackupData data = controller.getBackupData();
		
		if (!panel.isDeviceStateAllZero()) {
			data.setPanelOn(panel.isOn());
			data.setPanelModeDry(panel.isModeDry());
			data.setPanelTimerSet(panel.getTimerSet());
			data.setPanelHumidSet(panel.getHumidSet());
		}

		for (int did = 0; did < DehumidRoomController.DEHUMIDIFIERS_A_ROOM; did++) {
			if (dehumidifier[did].isDeviceStateAllZero())
				continue;
			
			data.setDehumidOn(did, dehumidifier[did].isOn());
			data.setDehumidModeDry(did, dehumidifier[did].isModeDry());
			data.setDehumidHumid(did, dehumidifier[did].getHumidSet());
		}
		
		
		controller.backupDataSerialization(data);
		
		return SKIP;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.nextCmd(null);
	}

	@Override
	protected void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		controller.nextCmd(null);
	}

}
