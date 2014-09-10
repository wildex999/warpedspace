package com.wildex999.warpedspace.blocks.relay;

import scala.Int;
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

public class BlockNetworkRelayT8 extends BlockRelayBase {
	protected final String name = "Network Relay - Tier 8";
	protected final int relayRadius = Int.MaxValue();
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public int getTier() {
		return 8;
	}
	
	@Override
	public int getRadius() {
		return relayRadius;
	}
}

