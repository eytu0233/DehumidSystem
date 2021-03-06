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
	/* This constant is used to avoid modbus slave bug */
	private static final int TRICKY_OFFSET = DEVICES_A_ROOM * NUM_ROOMS * OFFSET_A_DEVICE;

	private int[] backupPanel;

	private ModbusTCPSlave modbusSlave;

	private Panel[] panels;
	private Dehumidifier[][] dehumidifiers;

	/**
	 * Constructor
	 * 
	 * @param modbusSlave
	 */
	public DataStoreManager(ModbusTCPSlave modbusSlave) {
		super();
		this.modbusSlave = modbusSlave;

		this.panels = new Panel[NUM_ROOMS];
		this.backupPanel = new int[NUM_ROOMS * 4];
		this.dehumidifiers = new Dehumidifier[NUM_ROOMS][DEHUMIDIFIER_A_ROOM];
		for (int room = 0; room < NUM_ROOMS; room++) {
			panels[room] = new Panel(room);
			for (int did = 0; did < DEHUMIDIFIER_A_ROOM; did++) {
				dehumidifiers[room][did] = new Dehumidifier(room, did);
			}
		}
	}

	public boolean isPanelONOFFChange(int room) {
		return (getPanelStatus(room) & Device.POWER_MASK) != (getPanelBackupStatus(room) & Device.POWER_MASK);
	}

	public boolean isPanelModeChange(int room) {
		return (getPanelStatus(room) & Device.MODE_DEHUMID_MASK) != (getPanelBackupStatus(room) & Device.MODE_DEHUMID_MASK);
	}

	public boolean isPanelTimerSetFlagChange(int room) {
		return (getPanelStatus(room) & Device.TIMER_SET_MASK) != (getPanelBackupStatus(room) & Device.TIMER_SET_MASK);
	}

	public boolean isPanelHumiditySetFlagChange(int room) {
		return (getPanelStatus(room) & Device.HUMID_SET_MASK) != (getPanelBackupStatus(room) & Device.HUMID_SET_MASK);
	}

	public boolean isPanelDehumiditySetChange(int room) {
		return getPanelHumidSet(room) != getPanelBackupHumidSet(room);
	}

	public boolean isPanelTimerSetChange(int room) {
		return getPanelTimerSet(room) != getPanelBackupTimerSet(room);
	}

	public Device getPanel(int room) {
		return panels[room];
	}

	public Device getDehumidifier(int room, int deviceID) {
		return dehumidifiers[room][deviceID];
	}

	private int getPanelStatus(int room) {
		return modbusSlave.getResgister(ADDR_STATUS + room * OFFSET_A_DEVICE * DEVICES_A_ROOM + TRICKY_OFFSET);
	}

	private int getPanelHumid(int room) {
		return modbusSlave.getResgister(ADDR_HUMID + room * OFFSET_A_DEVICE * DEVICES_A_ROOM + TRICKY_OFFSET);
	}

	private int getPanelHumidSet(int room) {
		return modbusSlave.getResgister(ADDR_HUMID_SET + room * OFFSET_A_DEVICE * DEVICES_A_ROOM + TRICKY_OFFSET);
	}

	private int getPanelTimerSet(int room) {
		return modbusSlave.getResgister(ADDR_TIMER_SET + room * OFFSET_A_DEVICE * DEVICES_A_ROOM + TRICKY_OFFSET);
	}

	private int getPanelBackupStatus(int room) {
		return backupPanel[room * OFFSET_A_DEVICE + ADDR_STATUS];
	}

	private int getPanelBackupHumidSet(int room) {
		return backupPanel[room * OFFSET_A_DEVICE + ADDR_HUMID_SET];
	}

	private int getPanelBackupTimerSet(int room) {
		return backupPanel[room * OFFSET_A_DEVICE + ADDR_TIMER_SET];
	}

	private void setPanelBackup(int value, int room, int offset) {
		backupPanel[room * OFFSET_A_DEVICE + offset] = value;
	}	

	public abstract class Device implements IReferenceable {

		public static final int POWER_MASK = 0x01 << 0;
		public static final int MODE_DEHUMID_MASK = 0x01 << 1;
		public static final int MODE_DRY_MASK = 0x01 << 2;
		public static final int TIMER_SET_MASK = 0x01 << 3;
		public static final int HUMID_SET_MASK = 0x01 << 4;
		public static final int HIGH_TEMP_WARN_MASK = 0x01 << 5;
		public static final int DEFORST_TEMP_WARN_MASK = 0x01 << 6;
		public static final int HUMID_WARN_MASK = 0x01 << 7;
		public static final int FAN_WARN_MASK = 0x01 << 8;
		public static final int COMPRESSOR_WARN_MASK = 0x01 << 9;
		public static final int LIVE_MASK = 0x01 << 10;

		protected int room;
		protected int offset;

		abstract protected void setStatusFlag(int mask, boolean flag);

	}

	class Panel extends Device {

		public Panel(int roomIndex) {
			super();
			this.room = roomIndex;
			this.offset = roomIndex * DEVICES_A_ROOM * OFFSET_A_DEVICE;
		}

		protected void setStatusFlag(int mask, boolean flag) {
			int tempRegister = getPanelStatus(room), tempBackRegister = getPanelBackupStatus(room);

			if (flag) {
				tempRegister |= mask;
				tempBackRegister |= mask;
			} else {
				tempRegister &= ~mask;
				tempBackRegister &= ~mask;
			}
			modbusSlave.setRegister(ADDR_STATUS + offset + TRICKY_OFFSET, tempRegister);
			setPanelBackup(tempBackRegister, room, ADDR_STATUS);
		}

		@Override
		public boolean isOn() {
			// TODO Auto-generated method stub
			int register = getPanelStatus(room) & POWER_MASK;
			return register == POWER_MASK;
		}

		@Override
		public boolean isModeDehumid() {
			// TODO Auto-generated method stub
			int register = getPanelStatus(room) & MODE_DEHUMID_MASK;
			return register == MODE_DEHUMID_MASK;
		}

		@Override
		public boolean isModeDry() {
			// TODO Auto-generated method stub
			int register = getPanelStatus(room) & MODE_DRY_MASK;
			return register == MODE_DRY_MASK;
		}

		@Override
		public boolean isTimerSet() {
			// TODO Auto-generated method stub
			int register = getPanelStatus(room) & TIMER_SET_MASK;
			return register == TIMER_SET_MASK;
		}

		@Override
		public boolean isHumidSet() {
			// TODO Auto-generated method stub
			int register = getPanelStatus(room) & HUMID_SET_MASK;
			return register == HUMID_SET_MASK;
		}

		@Override
		public boolean isHighTempWarning() {
			// TODO Auto-generated method stub
			int register = getPanelStatus(room) & HIGH_TEMP_WARN_MASK;
			return register == HIGH_TEMP_WARN_MASK;
		}

		@Override
		public boolean isDeforstTempWarning() {
			// TODO Auto-generated method stub
			int register = getPanelStatus(room) & DEFORST_TEMP_WARN_MASK;
			return register == DEFORST_TEMP_WARN_MASK;
		}

		@Override
		public boolean isHumidWarning() {
			// TODO Auto-generated method stub
			int register = getPanelStatus(room) & HUMID_WARN_MASK;
			return register == HUMID_WARN_MASK;
		}

		@Override
		public boolean isFanWarning() {
			// TODO Auto-generated method stub
			int register = getPanelStatus(room) & FAN_WARN_MASK;
			return register == FAN_WARN_MASK;
		}

		@Override
		public boolean isCompressorWarning() {
			// TODO Auto-generated method stub
			int register = getPanelStatus(room) & COMPRESSOR_WARN_MASK;
			return register == COMPRESSOR_WARN_MASK;
		}

		@Override
		public boolean isLive() {
			// TODO Auto-generated method stub
			int register = getPanelStatus(room) & LIVE_MASK;
			return register == LIVE_MASK;
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
			setStatusFlag(POWER_MASK, onoff);
		}

		@Override
		public void setModeDehumid(boolean modeDehumid) {
			// TODO Auto-generated method stub
			setStatusFlag(MODE_DEHUMID_MASK, modeDehumid);
		}

		@Override
		public void setModeDry(boolean modeDry) {
			// TODO Auto-generated method stub
			setStatusFlag(MODE_DRY_MASK, modeDry);
		}

		@Override
		public void setTimerSet(boolean timerSet) {
			// TODO Auto-generated method stub
			setStatusFlag(TIMER_SET_MASK, timerSet);
		}

		@Override
		public void setHumidSet(boolean humidSet) {
			// TODO Auto-generated method stub
			setStatusFlag(HUMID_SET_MASK, humidSet);
		}

		@Override
		public void setHighTempWarn(boolean highTempWarn) {
			// TODO Auto-generated method stub
			setStatusFlag(HIGH_TEMP_WARN_MASK, highTempWarn);
		}

		@Override
		public void setDeforstTempWarn(boolean tempWarn) {
			// TODO Auto-generated method stub
			setStatusFlag(DEFORST_TEMP_WARN_MASK, tempWarn);
		}

		@Override
		public void setHumidWarn(boolean HumidWarn) {
			// TODO Auto-generated method stub
			setStatusFlag(HUMID_WARN_MASK, HumidWarn);
		}

		@Override
		public void setFanWarn(boolean FanWarn) {
			// TODO Auto-generated method stub
			setStatusFlag(FAN_WARN_MASK, FanWarn);
		}

		@Override
		public void setCompressorWarn(boolean CompressorWarn) {
			// TODO Auto-generated method stub
			setStatusFlag(COMPRESSOR_WARN_MASK, CompressorWarn);
		}

		@Override
		public void setLive(boolean live) {
			// TODO Auto-generated method stub
			setStatusFlag(LIVE_MASK, live);
		}

		@Override
		public void setHumid(int humid) {
			// TODO Auto-generated method stub
			modbusSlave.setRegister(ADDR_HUMID + offset + TRICKY_OFFSET, humid);
			setPanelBackup(humid, room, ADDR_HUMID);
		}

		@Override
		public void setHumidSetValue(int humidSet) {
			// TODO Auto-generated method stub
			modbusSlave.setRegister(ADDR_HUMID_SET + offset + TRICKY_OFFSET, humidSet);
			setPanelBackup(humidSet, room, ADDR_HUMID_SET);
		}

		@Override
		public void setTimerSetValue(int timerSet) {
			// TODO Auto-generated method stub
			modbusSlave.setRegister(ADDR_TIMER_SET + offset + TRICKY_OFFSET, timerSet);
			setPanelBackup(timerSet, room, ADDR_TIMER_SET);
		}

	}

	class Dehumidifier extends Device {

		public Dehumidifier(int room, int deviceID) {
			super();
			this.offset = room * DEVICES_A_ROOM * OFFSET_A_DEVICE + OFFSET_A_DEVICE * (deviceID + NUM_PANEL);
		}

		@Override
		public boolean isOn() {
			// TODO Auto-generated method stub
			int register = getDehumidifierStatus() & POWER_MASK;
			return register == POWER_MASK;
		}

		@Override
		public boolean isModeDehumid() {
			// TODO Auto-generated method stub
			int register = getDehumidifierStatus() & MODE_DEHUMID_MASK;
			return register == MODE_DEHUMID_MASK;
		}

		@Override
		public boolean isModeDry() {
			// TODO Auto-generated method stub
			int register = getDehumidifierStatus() & MODE_DRY_MASK;
			return register == MODE_DRY_MASK;
		}

		@Override
		public boolean isTimerSet() {
			// TODO Auto-generated method stub
			int register = getDehumidifierStatus() & TIMER_SET_MASK;
			return register == TIMER_SET_MASK;
		}

		@Override
		public boolean isHumidSet() {
			// TODO Auto-generated method stub
			int register = getDehumidifierStatus() & HUMID_SET_MASK;
			return register == HUMID_SET_MASK;
		}

		@Override
		public boolean isHighTempWarning() {
			// TODO Auto-generated method stub
			int register = getDehumidifierStatus() & HIGH_TEMP_WARN_MASK;
			return register == HIGH_TEMP_WARN_MASK;
		}

		@Override
		public boolean isDeforstTempWarning() {
			// TODO Auto-generated method stub
			int register = getDehumidifierStatus() & DEFORST_TEMP_WARN_MASK;
			return register == DEFORST_TEMP_WARN_MASK;
		}

		@Override
		public boolean isHumidWarning() {
			// TODO Auto-generated method stub
			int register = getDehumidifierStatus() & HUMID_WARN_MASK;
			return register == HUMID_WARN_MASK;
		}

		@Override
		public boolean isFanWarning() {
			// TODO Auto-generated method stub
			int register = getDehumidifierStatus() & FAN_WARN_MASK;
			return register == FAN_WARN_MASK;
		}

		@Override
		public boolean isCompressorWarning() {
			// TODO Auto-generated method stub
			int register = getDehumidifierStatus() & COMPRESSOR_WARN_MASK;
			return register == COMPRESSOR_WARN_MASK;
		}

		@Override
		public boolean isLive() {
			// TODO Auto-generated method stub
			int register = getDehumidifierStatus() & LIVE_MASK;
			return register == LIVE_MASK;
		}

		@Override
		public int getHumid() {
			// TODO Auto-generated method stub
			return modbusSlave.getResgister(ADDR_HUMID + offset + TRICKY_OFFSET);
		}

		@Override
		public int getHumidSet() {
			// TODO Auto-generated method stub
			return modbusSlave.getResgister(ADDR_HUMID_SET + offset + TRICKY_OFFSET);
		}

		@Override
		public int getTimerSet() {
			// TODO Auto-generated method stub
			return modbusSlave.getResgister(ADDR_TIMER_SET + offset + TRICKY_OFFSET);
		}

		@Override
		public void setOn(boolean onoff) {
			// TODO Auto-generated method stub
			setStatusFlag(POWER_MASK, onoff);
		}

		@Override
		public void setModeDehumid(boolean modeDehumid) {
			// TODO Auto-generated method stub
			setStatusFlag(MODE_DEHUMID_MASK, modeDehumid);
		}

		@Override
		public void setModeDry(boolean modeDry) {
			// TODO Auto-generated method stub
			setStatusFlag(MODE_DRY_MASK, modeDry);
		}

		@Override
		public void setTimerSet(boolean timerSet) {
			// TODO Auto-generated method stub
			setStatusFlag(TIMER_SET_MASK, timerSet);
		}

		@Override
		public void setHumidSet(boolean humidSet) {
			// TODO Auto-generated method stub
			setStatusFlag(HUMID_SET_MASK, humidSet);
		}

		@Override
		public void setHighTempWarn(boolean highTempWarn) {
			// TODO Auto-generated method stub
			setStatusFlag(HIGH_TEMP_WARN_MASK, highTempWarn);
		}

		@Override
		public void setDeforstTempWarn(boolean tempWarn) {
			// TODO Auto-generated method stub
			setStatusFlag(DEFORST_TEMP_WARN_MASK, tempWarn);
		}

		@Override
		public void setHumidWarn(boolean HumidWarn) {
			// TODO Auto-generated method stub
			setStatusFlag(HUMID_WARN_MASK, HumidWarn);
		}

		@Override
		public void setFanWarn(boolean FanWarn) {
			// TODO Auto-generated method stub
			setStatusFlag(FAN_WARN_MASK, FanWarn);
		}

		@Override
		public void setCompressorWarn(boolean CompressorWarn) {
			// TODO Auto-generated method stub
			setStatusFlag(COMPRESSOR_WARN_MASK, CompressorWarn);
		}

		@Override
		public void setLive(boolean live) {
			// TODO Auto-generated method stub
			setStatusFlag(LIVE_MASK, live);
		}

		@Override
		public void setHumid(int humid) {
			// TODO Auto-generated method stub
			modbusSlave.setRegister(ADDR_HUMID + offset + TRICKY_OFFSET, humid);
		}

		@Override
		public void setHumidSetValue(int humidSet) {
			// TODO Auto-generated method stub
			modbusSlave.setRegister(ADDR_HUMID_SET + offset + TRICKY_OFFSET, humidSet);
		}

		@Override
		public void setTimerSetValue(int timerSet) {
			// TODO Auto-generated method stub
			modbusSlave.setRegister(ADDR_TIMER_SET + offset + TRICKY_OFFSET, timerSet);
		}

		@Override
		protected void setStatusFlag(int mask, boolean flag) {
			// TODO Auto-generated method stub
			int tempRegister = getDehumidifierStatus();
			if (flag) {
				tempRegister |= mask;
			} else {
				tempRegister &= ~mask;
			}
			modbusSlave.setRegister(ADDR_STATUS + offset + TRICKY_OFFSET, tempRegister);
		}
		
		private int getDehumidifierStatus() {
			return modbusSlave.getResgister(ADDR_STATUS + offset + TRICKY_OFFSET);
		}
	}

}
