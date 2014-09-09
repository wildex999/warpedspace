package com.wildex999.warpedspace.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

public class BlockLibrary {
	
	public static Block networkAgent;
	public static Block networkController;
	public static Block networkManager;
	public static Block networkInterface;
	
	public static void init()
	{
		networkAgent = new BlockNetworkAgent();
		networkController = new BlockNetworkController();
		networkManager = new BlockNetworkManager();
		networkInterface = new BlockNetworkInterface();
	}
	
	public static void register(BlockBase block)
	{
		GameRegistry.registerBlock(block, block.getProperName());
	}
}
