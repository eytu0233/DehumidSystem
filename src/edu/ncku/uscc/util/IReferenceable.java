package edu.ncku.uscc.util;

public interface IReferenceable {
	
	public boolean isOn();
	public void setOn(boolean onoff);
	public boolean isModeDehumid();
	public void setModeDehumid(boolean modeDehumid);
	public boolean isModeDry();
	public void setModeDry(boolean modeDry);
	public boolean isTimerSet();
	public void setTimerSet(boolean timerSet);
	public boolean isHumidSet();
	public void setHumidSet(boolean humidSet);
	public boolean isHighTempWarning();
	public void setHighTempWarn(boolean highTempWarn);
	public boolean isTempWarning();
	public void setTempWarn(boolean tempWarn);
	public boolean isLive();
	public void setLive(boolean live);
	public int getHumid();
	public void setHumid(int humid);
	public void setTimer(int timer);
	
}
