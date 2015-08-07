package edu.ncku.uscc.util;

public class Terminal {
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	
	public static String toInfoColor(Object message){
		if(message == null) return "";
		return ANSI_WHITE + message + ANSI_RESET;
	}
	
	public static String toErrorColor(Object message){
		if(message == null) return "";
		return ANSI_RED + message + ANSI_RESET;
	}
	
	public static String toDebugColor(Object message){
		if(message == null) return "";
		return ANSI_GREEN + message + ANSI_RESET;
	}
	
	public static String toWarnColor(Object message){
		if(message == null) return "";
		return ANSI_YELLOW + message + ANSI_RESET;
	}
	
	public static String toBlue(String string){
		return ANSI_BLUE + string + ANSI_RESET;
	}
}
