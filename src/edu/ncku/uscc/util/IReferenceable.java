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
	public boolean isDeforstTempWarning();
	public void setDeforstTempWarn(boolean tempWarn);
	
	public boolean isHumidWarning();
	public void setHumidWarn(boolean HumidWarn);
	public boolean isFanWarning();
	public void setFanWarn(boolean FanWarn);
	public boolean isCompressorWarning();
	public void setCompressorWarn(boolean CompressorWarn);
	
	public boolean isLive();
	public void setLive(boolean live);
	public int getHumid();
	public void setHumid(int humid);
	public int getHumidSet();
	public void setHumidSetValue(int humid);
	public int getTimerSet();
	public void setTimerSetValue(int timer);
	
}