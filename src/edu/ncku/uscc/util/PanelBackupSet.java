package edu.ncku.uscc.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import edu.ncku.uscc.process.panel.SynPanelPowerCmd;
import edu.ncku.uscc.process.panel.SynPanelModeCmd;
import edu.ncku.uscc.process.panel.SynPanelHumiditySetCmd;
import edu.ncku.uscc.process.panel.SynPanelTimerSetCmd;

public class PanelBackupSet {
	
	private static final String PROPERTY_FILE_NAME = "workspace/backupSet4panel.properties";
	
	private static Properties checkpoint;
	private static InputStream inputProp;
	private static OutputStream outputProp;
	
	public static void init() {
		checkpoint = new Properties();
		inputProp = null;
		outputProp = null;
		try {
			inputProp = new FileInputStream(PROPERTY_FILE_NAME);
			checkpoint.load(inputProp);
		} catch (IOException io) {
			newProp();
		} finally {
			if (inputProp != null) {
				try {
					inputProp.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void newProp() {
		try {
			outputProp = new FileOutputStream(PROPERTY_FILE_NAME);

			for (int room = 0; room < 3; room++) {
				checkpoint.setProperty(new String(room + SynPanelPowerCmd.class.getSimpleName()), "false");
				checkpoint.setProperty(new String(room + SynPanelModeCmd.class.getSimpleName()), "true");
				checkpoint.setProperty(new String(room + SynPanelHumiditySetCmd.class.getSimpleName()), "4");
				checkpoint.setProperty(new String(room + SynPanelTimerSetCmd.class.getSimpleName()), "0");
			}

			checkpoint.store(outputProp, null);
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (outputProp != null) {
				try {
					outputProp.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}
	
	public static Properties getProp() {
		try {
			inputProp = new FileInputStream(PROPERTY_FILE_NAME);
			checkpoint.load(inputProp);
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (inputProp != null) {
				try {
					inputProp.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return checkpoint;
	}
	
	public static void setProp(boolean b, String name, int room) {
		try {
			outputProp = new FileOutputStream(PROPERTY_FILE_NAME);
			checkpoint.setProperty(new String(room + name), String.valueOf(b));
			checkpoint.store(outputProp, null);
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (outputProp != null) {
				try {
					outputProp.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void setProp(int n, String name, int room) {
		try {
			outputProp = new FileOutputStream(PROPERTY_FILE_NAME);
			checkpoint.setProperty(new String(room + name), String.valueOf(n));
			checkpoint.store(outputProp, null);
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (outputProp != null) {
				try {
					outputProp.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static boolean strToBool(String s) {
		if (s.equals("true"))
			return true;
		else if (s.equals("false"))
			return false;
		return false;
	}
	
}