package edu.ncku.uscc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Log extends Logger {
	
	static Logger logger;

	protected Log(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public static void init(){
		Properties logp = new Properties();
		try {
			logp.load(new FileInputStream(new File("log4j.properties")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PropertyConfigurator.configure(logp);
	}
	
	public static Logger getLogger(){
		return Log.getLogger("Log");
	}
	
}