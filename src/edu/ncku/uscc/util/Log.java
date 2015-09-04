package edu.ncku.uscc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Log{
	
	private static Logger logger;

	public static void init(){
		Properties logp = new Properties();
		try {
			logp.load(new FileInputStream(new File("./log4j.properties")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PropertyConfigurator.configure(logp);
		
		logger = Logger.getLogger("Log");
	}
	
	public static void debug(Object message){
		logger.debug(Terminal.toDebugColor(message));
	}
	
	public static void debug(Object message, Throwable t){
		logger.debug(Terminal.toDebugColor(message), t);
	}
	
	public static void error(Object message){
		logger.error(Terminal.toErrorColor(message));
	}
	
	public static void error(Object message, Throwable t){
		logger.error(Terminal.toErrorColor(message), t);
	}
	
	public static void warn(Object message){
		logger.warn(Terminal.toWarnColor(message));
	}
	
	public static void warn(Object message, Throwable t){
		logger.warn(Terminal.toWarnColor(message), t);
	}
	
	public static void info(Object message){
		logger.info(Terminal.toInfoColor(message));
	}
	
	public static void info(Object message, Throwable t){
		logger.info(Terminal.toInfoColor(message), t);
	}
	
}