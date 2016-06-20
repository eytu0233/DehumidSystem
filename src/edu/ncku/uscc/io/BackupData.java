package edu.ncku.uscc.io;

import java.io.Serializable;

public class BackupData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -37497677905221101L;
	
	
	private boolean panelOn;
	private boolean panelModeDry;
	private int panelTimerSet;
	private int panelHumidSet;
	private boolean[] dehumidOn = new boolean [8];
	private boolean[] dehumidModeDry = new boolean[8];
	private int[] dehumidHumidSet = new int [8];// power, mode; humid
	
	public BackupData() {
		super();
	}
	
	public boolean isPanelOn() {
		return panelOn ? true : false;
	}
	
	public boolean isPanelModeDry() {
		return panelModeDry ? true : false;
	}
	
	public int getPanelTimerSet() {
		return panelTimerSet;
	}
	
	public int getPanelHumidSet() {
		return panelHumidSet;
	}
	
	public boolean isDehumidOn(int did) {
		return dehumidOn[did] ? true : false;
	}
	
	public boolean isDehumidModeDry(int did) {
		return dehumidModeDry[did] ? true : false;
	}
	
	public int getDehumidHumidSet(int did) {
		return dehumidHumidSet[did];
	}
	
	
	public void setPanelOn(boolean on) {
		panelOn = on;
	}
	
	public void setPanelModeDry(boolean dry) {
		panelModeDry = dry;
	}
	
	public void setPanelTimerSet(int timer) {
		panelTimerSet = timer;
	}
	
	public void setPanelHumidSet(int humid) {
		panelHumidSet = humid;
	}
	
	public void setDehumidOn(int did, boolean on) {
		dehumidOn[did] = on;
	}
	
	public void setDehumidModeDry(int did, boolean dry) {
		dehumidModeDry[did] = dry;
	}
	
	public void setDehumidHumid(int did, int humid) {
		dehumidHumidSet[did] = humid;
	}
	
	public void setDefaultValue() {
		panelOn = false;
		panelModeDry = false;
		panelTimerSet = 0;
		panelHumidSet = 65;
		for (int did = 0; did < 8; did++) {
			dehumidOn[did] = false;
			dehumidModeDry[did] = false;
			dehumidHumidSet[did] = 65;
		}
	}
}
