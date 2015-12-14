package edu.ncku.uscc.util;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.process.panel.SynPanelTimerCountDownCmd;
import edu.ncku.uscc.process.panel.SynPanelTimerCountDownFinishCmd;

public class PanelTimerScheduler{
	
	private static final TimeUnit UNIT = TimeUnit.MINUTES;
	private static final int DELAY = 1;
	private static final int ROOMS = 4;

	private static PanelTimerScheduler panelTimerScheduler;
	
	private DehumidRoomController controllerRef;
	private HashMap<Integer, Integer> backupTimerSettings = new HashMap<Integer, Integer>();
	private HashMap<Integer, ScheduledExecutorService> panelTimerThreadPools = new HashMap<Integer, ScheduledExecutorService>();

	private PanelTimerScheduler(DehumidRoomController controllerRef) {
		super();
		this.controllerRef = controllerRef;
		for(int roomIndex = 0; roomIndex < ROOMS; roomIndex++){
			backupTimerSettings.put(roomIndex, 0);
			panelTimerThreadPools.put(roomIndex, Executors.newScheduledThreadPool(1));
		}
	}

	public static PanelTimerScheduler getInstance(DehumidRoomController controller) {		
		if (panelTimerScheduler == null) {
			panelTimerScheduler = new PanelTimerScheduler(controller);
		}
		
		return panelTimerScheduler;
	}

	public void newScheduleThread(int timerSet, int roomIndex) {
		
		ScheduledExecutorService executor = panelTimerThreadPools.get(roomIndex);
		if (!executor.isShutdown()) {
			executor.shutdownNow();
		}

		executor = Executors.newScheduledThreadPool(1);
		executor.schedule(new PanelTimer(timerSet, executor), DELAY, UNIT);
		panelTimerThreadPools.put(roomIndex, executor);
		backupTimerSettings.put(roomIndex, timerSet);
	}

	public int getBackupTimerSet(int roomIndex) {
		return backupTimerSettings.get(roomIndex);
	}

	public void backpuTimerMinusOne(int roomIndex) {
		backupTimerSettings.put(roomIndex, backupTimerSettings.get(roomIndex) - 1);
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
				controllerRef.jumpCmdQueue(new SynPanelTimerCountDownCmd(controllerRef));
			}
		}
		
	}
}