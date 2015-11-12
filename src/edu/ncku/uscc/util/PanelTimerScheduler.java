package edu.ncku.uscc.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.process.panel.SynPanelTimerCountDownCmd;
import edu.ncku.uscc.process.panel.SynPanelTimerCountDownFinishCmd;

public class PanelTimerScheduler{
	
	private static final TimeUnit UNIT = TimeUnit.MINUTES;
	private static final int DELAY = 1;

	private static PanelTimerScheduler panelTimerScheduler;
	
	private static DehumidRoomController controllerRef;	
	

	private int backupTimerSet = 0;

	private ScheduledExecutorService panelTimerScheduledThread;

	private PanelTimerScheduler() {
		super();
	}

	public static PanelTimerScheduler getInstance(DehumidRoomController controller) {
		if(controllerRef == null){
			controllerRef = controller;
		}
		
		if (panelTimerScheduler == null) {
			panelTimerScheduler = new PanelTimerScheduler();
		}
		
		return panelTimerScheduler;
	}

	public void newScheduleThread(int timerSet) {
		
		if (panelTimerScheduledThread != null && !panelTimerScheduledThread.isShutdown()) {
			panelTimerScheduledThread.shutdownNow();
		}

		panelTimerScheduledThread = Executors.newScheduledThreadPool(1);
		panelTimerScheduledThread.schedule(new PanelTimer(timerSet, panelTimerScheduledThread), DELAY, UNIT);
		backupTimerSet = timerSet;
	}

	public int getBackupTimerSet() {
		return backupTimerSet;
	}

	public void backpuTimerMinusOne() {
		backupTimerSet--;
	}

	
	private class PanelTimer implements Runnable{
		
		private ScheduledExecutorService panelTimerScheduleRef;
		
		private int currentTimer;			

		public PanelTimer(int currentTimer, ScheduledExecutorService panelTimerScheduleRef) {
			super();
			this.currentTimer = currentTimer;
			this.panelTimerScheduleRef = panelTimerScheduleRef;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(panelTimerScheduleRef.isShutdown()) return;
			
			currentTimer--;
			
			if (currentTimer > 0) {
				panelTimerScheduleRef.schedule(this, DELAY, UNIT);
				controllerRef.jumpCmdQueue(new SynPanelTimerCountDownCmd(controllerRef));
			} else {
				panelTimerScheduleRef.shutdownNow();
				controllerRef.jumpCmdQueue(new SynPanelTimerCountDownFinishCmd(controllerRef));
			}
		}
		
	}
}