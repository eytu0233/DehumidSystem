package edu.ncku.uscc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Log{
	
	private static Logger logger;
	private static boolean Debug = false;
	private static boolean Error = false;
	private static boolean Warn = false;
	private static boolean Info = false;

	public static void init(String logCommand){
		Properties logp = new Properties();
		try {
			logp.load(new FileInputStream(new File("/home/pi/workspace/log4j.properties")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PropertyConfigurator.configure(logp);
		
		logger = Logger.getLogger("Log");
		
		for (char ch : logCommand.toCharArray()) {
			switch (ch) {
			case 'd':
				Debug = true;
				break;
			case 'e':
				Error = true;
				break;
			case 'w':
				Warn = true;
				break;
			case 'i':
				Info = true;
				break;
			}
		}
	}

	public static void debug(Object message) {
		if (Debug)
			logger.debug(Terminal.toDebugColor(message));
	}

	public static void debug(Object message, Throwable t) {
		if (Debug)
			logger.debug(Terminal.toDebugColor(message), t);
	}

	public static void error(Object message) {
		if (Error)
			logger.error(Terminal.toErrorColor(message));
	}

	public static void error(Object message, Throwable t) {
		if (Error)
			logger.error(Terminal.toErrorColor(message), t);
	}

	public static void warn(Object message) {
		if (Warn)
			logger.warn(Terminal.toWarnColor(message));
	}

	public static void warn(Object message, Throwable t) {
		if (Warn)
			logger.warn(Terminal.toWarnColor(message), t);
	}

	public static void info(Object message) {
		if (Info)
			logger.info(Terminal.toInfoColor(message));
	}

	public static void info(Object message, Throwable t) {
		if (Info)
			logger.info(Terminal.toInfoColor(message), t);
	}
	
}