package edu.ncku.uscc.util;

import edu.ncku.uscc.io.ModbusTCPSlave;

public class DataStoreManager {	
	
	private static final int ADDR_STATUS = 0x00;
	private static final int ADDR_HUMID = 0x01;
	private static final int ADDR_HUMID_SET = 0x02;
	private static final int ADDR_TIMER_SET = 0x03;
	private static final int OFFSET_A_DEVICE = 4;
	private static final int DEVICES_A_ROOM = 9;

	private int[] backupPanel;
	
	private ModbusTCPSlave modbusSlave;
	
	private Panel[] panels;
	private Dehumidifier[][] dehumidifiers;

	public DataStoreManager(ModbusTCPSlave modbusSlave, int numRooms) {
		super();
		this.modbusSlave = modbusSlave;
		
		this.backupPanel = new int[numRooms * 4];
		this.panels = new Panel[numRooms];
		this.dehumidifiers = new Dehumidifier[numRooms][DEVICES_A_ROOM - 1];
		for(int room = 0; room < numRooms; room++){
			panels[room] = new Panel(room);
			for(int did = 0; did < DEVICES_A_ROOM - 1; did++){
				dehumidifiers[room][did] = new Dehumidifier(room, did);
			}
		}
	}
	
	public boolean isPanelONOFFChange(int room) {
		int nowStatus = modbusSlave.getResgister(ADDR_STATUS + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupStatus = backupPanel[room + ADDR_STATUS];
		int mask = 0x01;
		return (backupStatus & mask) == (nowStatus & mask);
	}
	
	public boolean isPanelModeChange(int room) {
		int nowStatus = modbusSlave.getResgister(ADDR_STATUS + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupStatus = backupPanel[room + ADDR_STATUS];
		int mask = 0x06;
		return (backupStatus & mask) == (nowStatus & mask);
	}
	
	public boolean isPanelTimerSetFlagChange(int room) {
		int nowStatus = modbusSlave.getResgister(ADDR_STATUS + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupStatus = backupPanel[room + ADDR_STATUS];
		int mask = 0x08;
		return (backupStatus & mask) == (nowStatus & mask);
	}
	
	public boolean isPanelDehumiditySetFlagChange(int room) {
		int nowStatus = modbusSlave.getResgister(ADDR_STATUS + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupStatus = backupPanel[room + ADDR_STATUS];
		int mask = 0x10;
		return (backupStatus & mask) == (nowStatus & mask);
	}
	
	public boolean isPanelHighTempAbnormalChange(int room) {
		int nowStatus = modbusSlave.getResgister(ADDR_STATUS + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupStatus = backupPanel[room + ADDR_STATUS];
		int mask = 0x20;
		return (backupStatus & mask) == (nowStatus & mask);
	}
	
	public boolean isPanelTempAbnormalChange(int room) {
		int nowStatus = modbusSlave.getResgister(ADDR_STATUS + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupStatus = backupPanel[room + ADDR_STATUS];
		int mask = 0x40;
		return (backupStatus & mask) == (nowStatus & mask);
	}
	
	public boolean isPanelDehumiditySetChange(int room) {
		int nowDehumiditySet = modbusSlave.getResgister(ADDR_HUMID_SET + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupDehumiditySet = backupPanel[room + ADDR_HUMID_SET];
		return backupDehumiditySet == nowDehumiditySet;
	}
	
	public boolean isPanelTimerSetChange(int room) {
		int nowTimerSet = modbusSlave.getResgister(ADDR_TIMER_SET + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupTimerSet = backupPanel[room + ADDR_TIMER_SET];
		return backupTimerSet == nowTimerSet;
	}
	
	public IReferenceable getPanel(int room){
		return panels[room];
	}
	
	public IReferenceable getDehumidifier(int room, int deviceID){
		return dehumidifiers[room][deviceID];
	}
	
	class Panel implements IReferenceable{
		
		
		private int room, offset;

		public Panel(int room) {
			super();
			this.room = room;
			this.offset = room * DEVICES_A_ROOM * OFFSET_A_DEVICE;
		}

		@Override
		public boolean isOn() {
			// TODO Auto-generated method stub
			final int mask = 0x01;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isModeDehumid() {
			// TODO Auto-generated method stub
			final int mask = 0x02;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isModeDry() {
			// TODO Auto-generated method stub
			final int mask = 0x04;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isTimerSet() {
			// TODO Auto-generated method stub
			final int mask = 0x08;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isHumidSet() {
			// TODO Auto-generated method stub
			final int mask = 0x10;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isHighTempWarning() {
			// TODO Auto-generated method stub
			final int mask = 0x20;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isTempWarning() {
			// TODO Auto-generated method stub
			final int mask = 0x40;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isLive() {
			// TODO Auto-generated method stub
			final int mask = 0x80;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public int getHumid() {
			// TODO Auto-generated method stub
			return 0;
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
			int mask = 0x01;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(onoff){
				tempRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
			backupPanel[room + ADDR_STATUS] = tempRegister;
		}

		@Override
		public void setModeDehumid(boolean modeDehumid) {
			// TODO Auto-generated method stub
			int mask = 0x02;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(modeDehumid){
				tempRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
			backupPanel[room + ADDR_STATUS] = tempRegister;
		}

		@Override
		public void setModeDry(boolean modeDry) {
			// TODO Auto-generated method stub
			int mask = 0x04;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(modeDry){
				tempRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
			backupPanel[room + ADDR_STATUS] = tempRegister;
		}

		@Override
		public void setTimerSet(boolean timerSet) {
			// TODO Auto-generated method stub
			int mask = 0x08;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(timerSet){
				tempRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
			backupPanel[room + ADDR_STATUS] = tempRegister;
		}

		@Override
		public void setHumidSet(boolean humidSet) {
			// TODO Auto-generated method stub
			int mask = 0x10;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(humidSet){
				tempRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
			backupPanel[room + ADDR_STATUS] = tempRegister;
		}

		@Override
		public void setHighTempWarn(boolean highTempWarn) {
			// TODO Auto-generated method stub
			int mask = 0x20;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(highTempWarn){
				tempRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
			backupPanel[room + ADDR_STATUS] = tempRegister;
		}

		@Override
		public void setTempWarn(boolean tempWarn) {
			// TODO Auto-generated method stub
			int mask = 0x40;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(tempWarn){
				tempRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
			backupPanel[room + ADDR_STATUS] = tempRegister;
		}

		@Override
		public void setLive(boolean live) {
			// TODO Auto-generated method stub
			int mask = 0x80;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(live){
				tempRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
			backupPanel[room + ADDR_STATUS] = tempRegister;
		}

		@Override
		public void setHumid(int humid) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setHumidSet(int humid) {
			// TODO Auto-generated method stub
			modbusSlave.setResgister(ADDR_HUMID_SET + offset, humid);
			backupPanel[room + ADDR_HUMID_SET] = humid;
		}

		@Override
		public void setTimerSet(int timer) {
			// TODO Auto-generated method stub
			modbusSlave.setResgister(ADDR_TIMER_SET + offset, timer);
			backupPanel[room + ADDR_TIMER_SET] = timer;
		}

	}

	class Dehumidifier implements IReferenceable{
		
		int offset;

		public Dehumidifier(int room, int deviceID) {
			super();
			this.offset = room * DEVICES_A_ROOM * OFFSET_A_DEVICE + OFFSET_A_DEVICE + deviceID;
		}

		@Override
		public boolean isOn() {
			// TODO Auto-generated method stub
			final int mask = 0x01;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isModeDehumid() {
			// TODO Auto-generated method stub
			final int mask = 0x02;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isModeDry() {
			// TODO Auto-generated method stub
			final int mask = 0x04;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isTimerSet() {
			// TODO Auto-generated method stub
			final int mask = 0x08;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isHumidSet() {
			// TODO Auto-generated method stub
			final int mask = 0x10;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isHighTempWarning() {
			// TODO Auto-generated method stub
			final int mask = 0x20;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isTempWarning() {
			// TODO Auto-generated method stub
			final int mask = 0x40;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public boolean isLive() {
			// TODO Auto-generated method stub
			final int mask = 0x80;
			int register = modbusSlave.getResgister(ADDR_STATUS + offset) & mask;
			return register == mask;
		}

		@Override
		public int getHumid() {
			// TODO Auto-generated method stub
			return 0;
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
			int mask = 0x01;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(onoff){
				tempRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setModeDehumid(boolean modeDehumid) {
			// TODO Auto-generated method stub
			int mask = 0x02;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(modeDehumid){
				tempRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setModeDry(boolean modeDry) {
			// TODO Auto-generated method stub
			int mask = 0x04;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(modeDry){
				tempRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setTimerSet(boolean timerSet) {
			// TODO Auto-generated method stub
			int mask = 0x08;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(timerSet){
				tempRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setHumidSet(boolean humidSet) {
			// TODO Auto-generated method stub
			int mask = 0x10;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(humidSet){
				tempRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setHighTempWarn(boolean highTempWarn) {
			// TODO Auto-generated method stub
			int mask = 0x20;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(highTempWarn){
				tempRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setTempWarn(boolean tempWarn) {
			// TODO Auto-generated method stub
			int mask = 0x40;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(tempWarn){
				tempRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setLive(boolean live) {
			// TODO Auto-generated method stub
			int mask = 0x80;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset);
			if(live){
				tempRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
		}

		@Override
		public void setHumid(int humid) {
			// TODO Auto-generated method stub
			modbusSlave.setResgister(ADDR_HUMID + offset, humid);
		}

		@Override
		public void setHumidSet(int humid) {
			// TODO Auto-generated method stub
			modbusSlave.setResgister(ADDR_HUMID_SET + offset, humid);
		}

		@Override
		public void setTimerSet(int timer) {
			// TODO Auto-generated method stub
			modbusSlave.setResgister(ADDR_TIMER_SET + offset, timer);
		}
	}

}


