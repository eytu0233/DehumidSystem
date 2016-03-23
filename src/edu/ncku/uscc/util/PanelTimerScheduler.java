package edu.ncku.uscc.util;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.ncku.uscc.io.DehumidRoomController;
import edu.ncku.uscc.process.panel.SynPanelTimerCountDownCmd;
import edu.ncku.uscc.process.panel.SynPanelTimerCountDownFinishCmd;

public class PanelTimerScheduler{
	
	private static final TimeUnit UNIT = TimeUnit.HOURS;
	private static final int DELAY = 1;
	private static final int ROOMS = 4;

	private static PanelTimerScheduler panelTimerScheduler;
	private static HashMap<Integer, DehumidRoomController> controllers = new HashMap<Integer, DehumidRoomController>();
	
	private HashMap<Integer, Integer> backupTimerSettings = new HashMap<Integer, Integer>();
	private HashMap<Integer, ScheduledExecutorService> panelTimerThreadPools = new HashMap<Integer, ScheduledExecutorService>();

	private PanelTimerScheduler() {
		super();
		for(int roomIndex = 2; roomIndex < 2 + ROOMS; roomIndex++){
			backupTimerSettings.put(roomIndex, 0);
			panelTimerThreadPools.put(roomIndex, Executors.newScheduledThreadPool(1));
		}
	}

	public static PanelTimerScheduler getInstance(DehumidRoomController controller) {		
		if (panelTimerScheduler == null) {
			panelTimerScheduler = new PanelTimerScheduler();
		}
		
		controllers.put(controller.getRoomIndex(), controller);
		
		return panelTimerScheduler;
	}

	public void newScheduleThread(int timerSet, int roomIndex) {
		
		ScheduledExecutorService executor = panelTimerThreadPools.get(roomIndex);
		if (!executor.isShutdown()) {
			executor.shutdownNow();
		}

		executor = Executors.newScheduledThreadPool(1);
		executor.schedule(new PanelTimer(roomIndex, timerSet, executor), DELAY, UNIT);
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
		private DehumidRoomController controllerRef;
		
		private int currentTimer;
		private int roomIndex;

		public PanelTimer(int roomIndex, int currentTimer, ScheduledExecutorService panelTimerScheduleRef) {
			super();
			this.roomIndex = roomIndex;
			this.currentTimer = currentTimer;
			this.panelTimerScheduleRef = panelTimerScheduleRef;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(panelTimerScheduleRef.isShutdown()) return;
			
			currentTimer--;
			
			controllerRef = controllers.get(roomIndex);
			
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