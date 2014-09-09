package com.wildex999.utils;

import org.apache.logging.log4j.Logger;

import com.wildex999.warpedspace.WarpedSpace;

import cpw.mods.fml.common.FMLLog;

public class ModLog {
	public static Logger logger;
	
	public static void init(Logger logger)
	{
		ModLog.logger = logger;
	}
}
