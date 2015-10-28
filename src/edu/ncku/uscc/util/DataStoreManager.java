package edu.ncku.uscc.util;

import edu.ncku.uscc.io.ModbusTCPSlave;

public class DataStoreManager {	
	
	private static final int ADDR_STATUS = 0x00;
	private static final int ADDR_HUMID = 0x01;
	private static final int ADDR_HUMID_SET = 0x02;
	private static final int ADDR_TIMER_SET = 0x03;
	private static final int OFFSET_A_DEVICE = 4;
	private static final int NUM_PANEL = 1;
	private static final int DEVICES_A_ROOM = 9;
	private static final int DEHUMIDIFIER_A_ROOM = 8;
	private static final int NUM_ROOMS = 4;

	private int[] backupPanel;
	
	private ModbusTCPSlave modbusSlave;
	
	private Panel[] panels;
	private Dehumidifier[][] dehumidifiers;

	public DataStoreManager(ModbusTCPSlave modbusSlave) {
		super();
		this.modbusSlave = modbusSlave;
		
		this.panels = new Panel[NUM_ROOMS];
		this.backupPanel = new int[NUM_ROOMS * 4];
		this.dehumidifiers = new Dehumidifier[NUM_ROOMS][DEHUMIDIFIER_A_ROOM];
		for(int room = 0; room < NUM_ROOMS; room++){
			panels[room] = new Panel(room);
			for(int did = 0; did < DEHUMIDIFIER_A_ROOM; did++){
				dehumidifiers[room][did] = new Dehumidifier(room, did);
			}
		}
	}
	
	private int getPanelStatus(int room){
		return modbusSlave.getResgister(ADDR_STATUS + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
	}

	private int getPanelHumid(int room) {
		return modbusSlave.getResgister(ADDR_HUMID + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
	}
	
	private int getPanelHumidSet(int room){
		return modbusSlave.getResgister(ADDR_HUMID_SET + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
	}
	
	private int getPanelTimerSet(int room){
		return modbusSlave.getResgister(ADDR_TIMER_SET + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
	}
	
	private int getPanelBackupStatus(int room){
		return backupPanel[room * OFFSET_A_DEVICE + ADDR_STATUS];
	}
	
	private int getPanelBackupHumidSet(int room){
		return backupPanel[room * OFFSET_A_DEVICE + ADDR_HUMID_SET];
	}
	
	private int getPanelBackupTimerSet(int room){
		return backupPanel[room * OFFSET_A_DEVICE + ADDR_TIMER_SET];
	}
	
	private void setPanelBackup(int value, int room, int offset){
		backupPanel[room * OFFSET_A_DEVICE + offset] = value;
	}
	
	public boolean isPanelONOFFChange(int room) {
		final int mask = 0x01 << 0;
		return (getPanelStatus(room) & mask) != (getPanelBackupStatus(room) & mask);
	}
	
	public boolean isPanelModeChange(int room) {
		final int mask = 0x06;
		return (getPanelStatus(room) & mask) != (getPanelBackupStatus(room) & mask);
	}
	
	public boolean isPanelTimerSetFlagChange(int room) {
		int mask = 0x01 << 3;
		return (getPanelStatus(room) & mask) != (getPanelBackupStatus(room) & mask);
	}
	
	public boolean isPanelDehumiditySetFlagChange(int room) {
		int mask = 0x01 << 4;
		return (getPanelStatus(room) & mask) != (getPanelBackupStatus(room) & mask);
	}
	
	public boolean isPanelDehumiditySetChange(int room) {
		return getPanelHumidSet(room) != getPanelBackupHumidSet(room);
	}
	
	public boolean isPanelTimerSetChange(int room) {
		return getPanelTimerSet(room) != getPanelBackupTimerSet(room);
	}
	
	public IReferenceable getPanel(int room){
		return panels[room];
	}
	
	public IReferenceable getDehumidifier(int room, int deviceID){
		return dehumidifiers[room][deviceID];
	}
	
	class Panel implements IReferenceable{
			
		private int room, offset;

		public Panel(int roomIndex) {
			super();
			this.room = roomIndex;
			this.offset = roomIndex * DEVICES_A_ROOM * OFFSET_A_DEVICE;
		}
		
		private void setFlag(int mask, boolean flag){
			int tempRegister = getPanelStatus(room), tempBackRegister = getPanelBackupStatus(room);
			
			if(flag){
				tempRegister |= mask;
				tempBackRegister |= mask;
			}else{
				tempRegister &= ~mask;				
				tempBackRegister &= ~mask;
			}
			modbusSlave.setRegister(ADDR_STATUS + offset, tempRegister);
			setPanelBackup(tempBackRegister, room, ADDR_STATUS);
		}

		@Override
		public boolean isOn() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 0;
			int register = getPanelStatus(room) & mask;
			return register == mask;
		}

		@Override
		public boolean isModeDehumid() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 1;
			int register = getPanelStatus(room) & mask;
			return register == mask;
		}

		@Override
		public boolean isModeDry() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 2;
			int register = getPanelStatus(room) & mask;
			return register == mask;
		}

		@Override
		public boolean isTimerSet() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 3;
			int register = getPanelStatus(room) & mask;
			return register == mask;
		}

		@Override
		public boolean isHumidSet() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 4;
			int register = getPanelStatus(room) & mask;
			return register == mask;
		}

		@Override
		public boolean isHighTempWarning() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 5;
			int register = getPanelStatus(room) & mask;
			return register == mask;
		}

		@Override
		public boolean isTempWarning() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 6;
			int register = getPanelStatus(room) & mask;
			return register == mask;
		}
		
		@Override
		public boolean isHumidWarning() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 7;
			int register = getPanelStatus(room) & mask;
			return register == mask;
		}

		@Override
		public boolean isFanWarning() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 8;
			int register = getPanelStatus(room) & mask;
			return register == mask;
		}

		@Override
		public boolean isCompressorWarning() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 9;
			int register = getPanelStatus(room) & mask;
			return register == mask;
		}

		@Override
		public boolean isLive() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 10;
			int register = getPanelStatus(room) & mask;
			return register == mask;
		}

		@Override
		public int getHumid() {
			// TODO Auto-generated method stub
			return getPanelHumid(room);
		}	

		@Override
		public int getHumidSet() {
			// TODO Auto-generated method stub			
			return getPanelHumidSet(room);
		}

		@Override
		public int getTimerSet() {
			// TODO Auto-generated method stub
			return getPanelTimerSet(room);
		}

		@Override
		public void setOn(boolean onoff) {
			// TODO Auto-generated method stub
			final int mask = 1 << 0;
			setFlag(mask, onoff);
		}

		@Override
		public void setModeDehumid(boolean modeDehumid) {
			// TODO Auto-generated method stub
			final int mask = 1 << 1;
			setFlag(mask, modeDehumid);
		}

		@Override
		public void setModeDry(boolean modeDry) {
			// TODO Auto-generated method stub
			final int mask = 1 << 2;
			setFlag(mask, modeDry);
		}

		@Override
		public void setTimerSet(boolean timerSet) {
			// TODO Auto-generated method stub
			final int mask = 1 << 3;
			setFlag(mask, timerSet);
		}

		@Override
		public void setHumidSet(boolean humidSet) {
			// TODO Auto-generated method stub
			final int mask = 1 << 4;
			setFlag(mask, humidSet);
		}

		@Override
		public void setHighTempWarn(boolean highTempWarn) {
			// TODO Auto-generated method stub
			final int mask = 1 << 5;
			setFlag(mask, highTempWarn);
		}

		@Override
		public void setTempWarn(boolean tempWarn) {
			// TODO Auto-generated method stub
			final int mask = 1 << 6;
			setFlag(mask, tempWarn);
		}
		
		@Override
		public void setHumidWarn(boolean HumidWarn) {
			// TODO Auto-generated method stub
			final int mask = 1 << 7;
			setFlag(mask, HumidWarn);
		}

		@Override
		public void setFanWarn(boolean FanWarn) {
			// TODO Auto-generated method stub
			final int mask = 1 << 8;
			setFlag(mask, FanWarn);
		}

		@Override
		public void setCompressorWarn(boolean CompressorWarn) {
			// TODO Auto-generated method stub
			final int mask = 1 << 9;
			setFlag(mask, CompressorWarn);
		}

		@Override
		public void setLive(boolean live) {
			// TODO Auto-generated method stub
			final int mask = 1 << 10;
			int tempRegister = getPanelStatus(room), tempBackRegister = getPanelBackupStatus(room);
			if(live){
				tempRegister |= mask;
				tempBackRegister |= mask;
			}else{
				tempRegister &= ~mask;
				tempBackRegister &= ~mask;
			}
			modbusSlave.setRegister(ADDR_STATUS + offset, tempRegister);
			setPanelBackup(tempBackRegister, room, ADDR_STATUS);
		}

		@Override
		public void setHumid(int humid) {
			// TODO Auto-generated method stub
			modbusSlave.setRegister(ADDR_HUMID + offset, humid);
			setPanelBackup(humid, room, ADDR_HUMID);
		}

		@Override
		public void setHumidSetValue(int humidSet) {
			// TODO Auto-generated method stub
			modbusSlave.setRegister(ADDR_HUMID_SET + offset, humidSet);
			setPanelBackup(humidSet, room, ADDR_HUMID_SET);
		}

		@Override
		public void setTimerSetValue(int timerSet) {
			// TODO Auto-generated method stub
			modbusSlave.setRegister(ADDR_TIMER_SET + offset, timerSet);
			setPanelBackup(timerSet, room, ADDR_TIMER_SET);
		}

	}

	class Dehumidifier implements IReferenceable{
		
		int offset;

		public Dehumidifier(int room, int deviceID) {
			super();
			this.offset = room * DEVICES_A_ROOM * OFFSET_A_DEVICE + OFFSET_A_DEVICE * (deviceID + NUM_PANEL);
		}

		@Override
		public boolean isOn() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 0;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isModeDehumid() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 1;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isModeDry() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 2;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isTimerSet() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 3;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isHumidSet() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 4;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isHighTempWarning() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 5;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isTempWarning() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 6;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}
		
		@Override
		public boolean isHumidWarning() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 7;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isFanWarning() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 8;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isCompressorWarning() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 9;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isLive() {
			// TODO Auto-generated method stub
			final int mask = 0x01 << 10;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public int getHumid() {
			// TODO Auto-generated method stub
			return modbusSlave.getResgister(ADDR_HUMID + offset);
		}	

		@Override
		public int getHumidSet() {
			// TODO Auto-generated method stub			
			return modbusSlave.getResgister(ADDR_HUMID_SET + offset);
		}

		@Override
		public int getTimerSet() {
			// TODO Auto-generated method stub
			return modbusSlave.getResgister(ADDR_TIMER_SET + offset);
		}

		@Override
		public void setOn(boolean onoff) {
			// TODO Auto-generated method stub
			final int mask = 1 << 0;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(onoff){
				tempRegister |= mask;
			}else{
				tempRegister &= ~mask;
			}
			modbusSlave.setRegister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setModeDehumid(boolean modeDehumid) {
			// TODO Auto-generated method stub
			final int mask = 1 << 1;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(modeDehumid){
				tempRegister |= mask;
			}else{
				tempRegister &= ~mask;
			}
			modbusSlave.setRegister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setModeDry(boolean modeDry) {
			// TODO Auto-generated method stub
			final int mask = 1 << 2;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(modeDry){
				tempRegister |= mask;
			}else{
				tempRegister &= ~mask;
			}
			modbusSlave.setRegister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setTimerSet(boolean timerSet) {
			// TODO Auto-generated method stub
			final int mask = 1 << 3;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(timerSet){
				tempRegister |= mask;
			}else{
				tempRegister &= ~mask;
			}
			modbusSlave.setRegister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setHumidSet(boolean humidSet) {
			// TODO Auto-generated method stub
			final int mask = 1 << 4;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(humidSet){
				tempRegister |= mask;
			}else{
				tempRegister &= ~mask;
			}
			modbusSlave.setRegister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setHighTempWarn(boolean highTempWarn) {
			// TODO Auto-generated method stub
			final int mask = 1 << 5;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(highTempWarn){
				tempRegister |= mask;
			}else{
				tempRegister &= ~mask;
			}
			modbusSlave.setRegister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setTempWarn(boolean tempWarn) {
			// TODO Auto-generated method stub
			final int mask = 1 << 6;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(tempWarn){
				tempRegister |= mask;
			}else{
				tempRegister &= ~mask;
			}
			modbusSlave.setRegister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setHumidWarn(boolean HumidWarn) {
			// TODO Auto-generated method stub
			final int mask = 1 << 7;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(HumidWarn){
				tempRegister |= mask;
			}else{
				tempRegister &= ~mask;
			}
			modbusSlave.setRegister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setFanWarn(boolean FanWarn) {
			// TODO Auto-generated method stub
			final int mask = 1 << 8;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(FanWarn){
				tempRegister |= mask;
			}else{
				tempRegister &= ~mask;
			}
			modbusSlave.setRegister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setCompressorWarn(boolean CompressorWarn) {
			// TODO Auto-generated method stub
			final int mask = 1 << 9;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(CompressorWarn){
				tempRegister |= mask;
			}else{
				tempRegister &= ~mask;
			}
			modbusSlave.setRegister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setLive(boolean live) {
			// TODO Auto-generated method stub
			final int mask = 1 << 10;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(live){
				tempRegister |= mask;
			}else{
				tempRegister &= ~mask;
			}
			modbusSlave.setRegister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setHumid(int humid) {
			// TODO Auto-generated method stub
			modbusSlave.setRegister(ADDR_HUMID + offset, humid);
		}

		@Override
		public void setHumidSetValue(int humidSet) {
			// TODO Auto-generated method stub
			modbusSlave.setRegister(ADDR_HUMID_SET + offset, humidSet);
		}

		@Override
		public void setTimerSetValue(int timerSet) {
			// TODO Auto-generated method stub
			modbusSlave.setRegister(ADDR_TIMER_SET + offset, timerSet);
		}
	}

}


