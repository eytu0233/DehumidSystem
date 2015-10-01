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
	private static final int NUM_ROOMS = 4;

	private int[] backupPanel;
	
	private ModbusTCPSlave modbusSlave;
	
	private Panel[] panels;
	private Dehumidifier[][] dehumidifiers;

	public DataStoreManager(ModbusTCPSlave modbusSlave) {
		super();
		this.modbusSlave = modbusSlave;
		
		this.backupPanel = new int[NUM_ROOMS * 4];
		this.panels = new Panel[NUM_ROOMS];
		this.dehumidifiers = new Dehumidifier[NUM_ROOMS][DEVICES_A_ROOM - 1];
		for(int room = 0; room < NUM_ROOMS; room++){
			panels[room] = new Panel(room);
			for(int did = 0; did < DEVICES_A_ROOM - 1; did++){
				dehumidifiers[room][did] = new Dehumidifier(room, did);
			}
		}
	}
	
	public boolean isChnage(int room) {
		int nowStatus = modbusSlave.getResgister(ADDR_STATUS + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupStatus = backupPanel[room + ADDR_STATUS];
		return backupStatus != nowStatus;
	}
	
	public boolean isPanelONOFFChange(int room) {
		int nowStatus = modbusSlave.getResgister(ADDR_STATUS + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupStatus = backupPanel[room + ADDR_STATUS];
		int mask = 0x01;
		return (backupStatus & mask) != (nowStatus & mask);
	}
	
	public boolean isPanelModeChange(int room) {
		int nowStatus = modbusSlave.getResgister(ADDR_STATUS + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupStatus = backupPanel[room + ADDR_STATUS];
		int mask = 0x06;
		return (backupStatus & mask) != (nowStatus & mask);
	}
	
	public boolean isPanelTimerSetFlagChange(int room) {
		int nowStatus = modbusSlave.getResgister(ADDR_STATUS + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupStatus = backupPanel[room + ADDR_STATUS];
		int mask = 0x08;
		return (backupStatus & mask) != (nowStatus & mask);
	}
	
	public boolean isPanelDehumiditySetFlagChange(int room) {
		int nowStatus = modbusSlave.getResgister(ADDR_STATUS + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupStatus = backupPanel[room + ADDR_STATUS];
		int mask = 0x10;
		return (backupStatus & mask) != (nowStatus & mask);
	}
	
	public boolean isPanelHighTempAbnormalChange(int room) {
		int nowStatus = modbusSlave.getResgister(ADDR_STATUS + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupStatus = backupPanel[room + ADDR_STATUS];
		int mask = 0x20;
		return (backupStatus & mask) != (nowStatus & mask);
	}
	
	public boolean isPanelTempAbnormalChange(int room) {
		int nowStatus = modbusSlave.getResgister(ADDR_STATUS + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupStatus = backupPanel[room + ADDR_STATUS];
		int mask = 0x40;
		return (backupStatus & mask) != (nowStatus & mask);
	}
	
	public boolean isPanelDehumiditySetChange(int room) {
		int nowDehumiditySet = modbusSlave.getResgister(ADDR_HUMID_SET + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupDehumiditySet = backupPanel[room + ADDR_HUMID_SET];
		return backupDehumiditySet != nowDehumiditySet;
	}
	
	public boolean isPanelTimerSetChange(int room) {
		int nowTimerSet = modbusSlave.getResgister(ADDR_TIMER_SET + room * OFFSET_A_DEVICE * DEVICES_A_ROOM);
		int backupTimerSet = backupPanel[room + ADDR_TIMER_SET];
		return backupTimerSet != nowTimerSet;
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
			int mask = 1 << 0;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset), tempBackRegister = backupPanel[room + ADDR_STATUS];
			
			if(onoff){
				tempRegister |= mask;
				tempBackRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;				
				tempBackRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
			backupPanel[room + ADDR_STATUS] = tempBackRegister;
		}

		@Override
		public void setModeDehumid(boolean modeDehumid) {
			// TODO Auto-generated method stub
			int mask = 1 << 1;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset), tempBackRegister = backupPanel[room + ADDR_STATUS];
			
			if(modeDehumid){
				tempRegister |= mask;
				tempBackRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
				tempBackRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
			backupPanel[room + ADDR_STATUS] = tempBackRegister;
		}

		@Override
		public void setModeDry(boolean modeDry) {
			// TODO Auto-generated method stub
			int mask = 1 << 2;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset), tempBackRegister = backupPanel[room + ADDR_STATUS];
			if(modeDry){
				tempRegister |= mask;
				tempBackRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
				tempBackRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
			backupPanel[room + ADDR_STATUS] = tempBackRegister;
		}

		@Override
		public void setTimerSet(boolean timerSet) {
			// TODO Auto-generated method stub
			int mask = 1 << 3;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset), tempBackRegister = backupPanel[room + ADDR_STATUS];
			if(timerSet){
				tempRegister |= mask;
				tempBackRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
				tempBackRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
			backupPanel[room + ADDR_STATUS] = tempBackRegister;
		}

		@Override
		public void setHumidSet(boolean humidSet) {
			// TODO Auto-generated method stub
			int mask = 1 << 4;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset), tempBackRegister = backupPanel[room + ADDR_STATUS];
			if(humidSet){
				tempRegister |= mask;
				tempBackRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
				tempBackRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
			backupPanel[room + ADDR_STATUS] = tempBackRegister;
		}

		@Override
		public void setHighTempWarn(boolean highTempWarn) {
			// TODO Auto-generated method stub
			int mask = 1 << 5;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset), tempBackRegister = backupPanel[room + ADDR_STATUS];
			if(highTempWarn){
				tempRegister |= mask;
				tempBackRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
				tempBackRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
			backupPanel[room + ADDR_STATUS] = tempBackRegister;
		}

		@Override
		public void setTempWarn(boolean tempWarn) {
			// TODO Auto-generated method stub
			int mask = 1 << 6;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset), tempBackRegister = backupPanel[room + ADDR_STATUS];
			if(tempWarn){
				tempRegister |= mask;
				tempBackRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
				tempBackRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
			backupPanel[room + ADDR_STATUS] = tempBackRegister;
		}

		@Override
		public void setLive(boolean live) {
			// TODO Auto-generated method stub
			int mask = 1 << 7;
			int tempRegister = modbusSlave.getResgister(ADDR_STATUS + offset), tempBackRegister = backupPanel[room + ADDR_STATUS];
			if(live){
				tempRegister |= mask;
				tempBackRegister |= mask;
			}else{
				mask = ~mask;
				mask &= 0xff;
				tempRegister &= mask;
				tempBackRegister &= mask;
			}
			modbusSlave.setResgister(ADDR_STATUS + offset, tempRegister);
			backupPanel[room + ADDR_STATUS] = tempBackRegister;
		}

		@Override
		public void setHumid(int humid) {
			// TODO Auto-generated method stub
			modbusSlave.setResgister(ADDR_HUMID + offset, humid);
			backupPanel[room + ADDR_HUMID] = humid;
		}

		@Override
		public void setHumidSetValue(int humidSet) {
			// TODO Auto-generated method stub
			modbusSlave.setResgister(ADDR_HUMID_SET + offset, humidSet);
			backupPanel[room + ADDR_HUMID_SET] = humidSet;
		}

		@Override
		public void setTimerSetValue(int timerSet) {
			// TODO Auto-generated method stub
			modbusSlave.setResgister(ADDR_TIMER_SET + offset, timerSet);
			backupPanel[room + ADDR_TIMER_SET] = timerSet;
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
			int mask = 1 << 0;
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
			int mask = 1 << 1;
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
			int mask = 1 << 2;
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
			int mask = 1 << 3;
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
			int mask = 1 << 4;
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
			int mask = 1 << 5;
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
			int mask = 1 << 6;
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
			int mask = 1 << 7;
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
		public void setHumidSetValue(int humidSet) {
			// TODO Auto-generated method stub
			modbusSlave.setResgister(ADDR_HUMID_SET + offset, humidSet);
		}

		@Override
		public void setTimerSetValue(int timerSet) {
			// TODO Auto-generated method stub
			modbusSlave.setResgister(ADDR_TIMER_SET + offset, timerSet);
		}
	}

}


