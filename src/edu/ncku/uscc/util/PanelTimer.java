package edu.ncku.uscc.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.process.panel.SynPanelTimerCountDownFinishCmd;

public class PanelTimer extends Thread {

	private static PanelTimer panelTimer;

	private int backupTimerSet = 0;
	private int currentTimerSet = 0;
	private boolean timerMinusOneFlag = false;

	private ScheduledExecutorService panelTimerScheduledThread = Executors.newScheduledThreadPool(1);
	
	private DehumidRoomController controller;

	private PanelTimer(DehumidRoomController controller) {
		super();
		this.controller = controller;
	}

	public static PanelTimer getInstance(DehumidRoomController controller) {
		if (panelTimer == null) {
			panelTimer = new PanelTimer(controller);
		}
		return panelTimer;
	}

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
		// super.run();
		currentTimerSet--;
		timerMinusOneFlag = true;

		if (currentTimerSet > 0) {
			panelTimerScheduledThread.schedule(this, 1, TimeUnit.HOURS);
		} else {
			controller.jumpCmdQueue(new SynPanelTimerCountDownFinishCmd(controller));
			panelTimerScheduledThread.shutdownNow();
		}
	}

}