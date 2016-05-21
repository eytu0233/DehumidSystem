package edu.ncku.uscc.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import edu.ncku.uscc.process.panel.SynPanelPowerCmd;
import edu.ncku.uscc.process.panel.SynPanelHumiditySetCmd;

public class PanelBackupSet {

	private static final String PROPERTY_FILE_NAME = "/home/pi/workspace/backupSet4panel.properties";

	private static final String TRUE = "true";
	private static final String FALSE = "false";
	
	private static final String DEFALUT = FALSE;
	private static final String DEFALUT_DEHUMID_VALUE = "0";

	private static final Object lock = new Object();

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
			if(checkpoint.keySet().size() == 0){
				inputProp.close();
				inputProp = null;
				newProp();
			}
		} catch (IOException io) {
			newProp();
		} finally {
			if (inputProp != null) {
				try {
					inputProp.close();
					inputProp = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static boolean getOnCheckpoint(int roomIndex) {
		String value = checkpoint.getProperty(roomIndex + SynPanelPowerCmd.class.getSimpleName(), DEFALUT);
		return strToBool(value);
	}

	public static int getHumidSetValueCP(int roomIndex) {
		String value = checkpoint.getProperty(roomIndex + SynPanelHumiditySetCmd.class.getSimpleName(), DEFALUT_DEHUMID_VALUE);
		return Integer.valueOf(value);
	}

	public static void setProp(boolean b, String name, int room) {
		synchronized (lock) {
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
	}

	public static void setProp(int n, String name, int room) {
		synchronized (lock) {
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
	}

	private static boolean strToBool(String s) {
		if (TRUE.equals(s))
			return true;
		else if (FALSE.equals(s))
			return false;
		return false;
	}

	private static void newProp() {
		try {
			outputProp = new FileOutputStream(PROPERTY_FILE_NAME);

			for (int room = 0; room < 3; room++) {
				checkpoint.setProperty(new String(room + SynPanelPowerCmd.class.getSimpleName()), FALSE);
				checkpoint.setProperty(new String(room + SynPanelHumiditySetCmd.class.getSimpleName()), DEFALUT_DEHUMID_VALUE);
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

}