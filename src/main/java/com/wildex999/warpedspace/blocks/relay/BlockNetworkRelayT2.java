package com.wildex999.warpedspace.blocks.relay;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.blocks.BlockBase;
import com.wildex999.warpedspace.blocks.BlockLibrary;
import com.wildex999.warpedspace.gui.BasicNetworkRelayGui;
import com.wildex999.warpedspace.gui.WarpedControllerGui;
import com.wildex999.warpedspace.tiles.TileBasicNetworkRelay;
import com.wildex999.warpedspace.tiles.TileNetworkController;

public class BlockNetworkRelayT2 extends BlockRelayBase {
	protected final String name = "Network Relay - Tier 2";
	protected final int relayRadius = 64;
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public int getTier() {
		return 2;
	}
	
	@Override
	public int getRadius() {
		return relayRadius;
	}

}
