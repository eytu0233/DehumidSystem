package edu.ncku.uscc.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class panelTimerThread extends Thread {
	private int backupTimerSet = 0;
	private int currentTimerSet = 0;
	private boolean timerMinusOneFlag = false;
	ScheduledExecutorService panelTimerScheduledThread = Executors.newScheduledThreadPool(1);
	
	public void newScheduleThread(int timerSet) {
		if (panelTimerScheduledThread != null && !panelTimerScheduledThread.isShutdown()) {
			panelTimerScheduledThread.shutdownNow();
		}
		panelTimerScheduledThread = Executors.newScheduledThreadPool(1);
		panelTimerScheduledThread.schedule(this, 1, TimeUnit.HOURS);
		backupTimerSet = timerSet;
		currentTimerSet = timerSet;
	}
	
	public int getBackupTimerSet() {
		return backupTimerSet;
	}
	
	public void backpuTimerMinusOne() {
		backupTimerSet--;
	}
	
	public void setTimerMinusOneFlag(boolean b) {
		timerMinusOneFlag = b;
	}
	
	public boolean getTimerMinusOneFlag() {
		return timerMinusOneFlag;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
//		super.run();
		currentTimerSet--;
		timerMinusOneFlag = true;
		
		if (currentTimerSet > 0) {
			panelTimerScheduledThread.schedule(this, 1, TimeUnit.HOURS);
		} else {
			panelTimerScheduledThread.shutdownNow();
		}
	}
	
}