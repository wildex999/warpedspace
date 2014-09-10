package com.wildex999.warpedspace.blocks;

import com.wildex999.warpedspace.blocks.relay.BlockNetworkRelayT1;
import com.wildex999.warpedspace.blocks.relay.BlockNetworkRelayT2;
import com.wildex999.warpedspace.blocks.relay.BlockNetworkRelayT3;
import com.wildex999.warpedspace.blocks.relay.BlockNetworkRelayT4;
import com.wildex999.warpedspace.blocks.relay.BlockNetworkRelayT5;
import com.wildex999.warpedspace.blocks.relay.BlockNetworkRelayT6;
import com.wildex999.warpedspace.blocks.relay.BlockNetworkRelayT7;
import com.wildex999.warpedspace.blocks.relay.BlockNetworkRelayT8;
import com.wildex999.warpedspace.blocks.relay.BlockRelayBase;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class BlockLibrary {
	
	public static Block networkAgent;
	public static Block networkController;
	public static Block networkManager;
	public static Block networkInterface;
	
	public static BlockRelayBase networkRelayT1;
	public static BlockRelayBase networkRelayT2;
	public static BlockRelayBase networkRelayT3;
	public static BlockRelayBase networkRelayT4;
	public static BlockRelayBase networkRelayT5;
	public static BlockRelayBase networkRelayT6;
	public static BlockRelayBase networkRelayT7;
	public static BlockRelayBase networkRelayT8;
	
	public static void init()
	{
		networkAgent = new BlockNetworkAgent();
		networkController = new BlockNetworkController();
		networkManager = new BlockNetworkManager();
		networkInterface = new BlockNetworkInterface();
		
		networkRelayT1 = new BlockNetworkRelayT1();
		networkRelayT2 = new BlockNetworkRelayT2();
		networkRelayT3 = new BlockNetworkRelayT3();
		networkRelayT4 = new BlockNetworkRelayT4();
		networkRelayT5 = new BlockNetworkRelayT5();
		networkRelayT6 = new BlockNetworkRelayT6();
		networkRelayT7 = new BlockNetworkRelayT7();
		networkRelayT8 = new BlockNetworkRelayT8();
		
	}
	
	public static void register(BlockBase block)
	{
		GameRegistry.registerBlock(block, block.getProperName());
	}
	
	public static void register(BlockBase block, Class<? extends ItemBlock> itemclass) 
	{
		GameRegistry.registerBlock(block, itemclass, block.getProperName());
	}
}
