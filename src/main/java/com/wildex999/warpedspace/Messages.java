package com.wildex999.warpedspace;

import java.util.HashMap;
import java.util.Map;

public class Messages {
	private static int idCount = 0;
	private static Map<Integer, String> state = new HashMap<Integer, String>();
	
	public static int colorOk = 0x248F24;
	public static int colorProblem = 0xCC0000;
	
	//Connection state
	public static int online = add("<Online>");
	public static int noNetwork = add("<No Network>");
	public static int networkOffline = add("<Network Offline>");
	public static int noRelay = add("<No Relay>");
	public static int notReachable = add("<Not Reachable>");
	
	//Connection entry state
	public static int notSet = add("<None>");
	public static int offline = add("<Offline>");
	//online
	
	protected static int add(String str) {
		state.put(idCount++, str);
		return idCount-1;
	}
	
	public static String get(int id) {
		String str = state.get(id);
		if(str == null)
			return "<!INTERNAL ERROR!>";
		return str;
	}
}
