package com.wildex999.warpedspace.blocks;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.gui.NetworkAgentGui;
import com.wildex999.warpedspace.tiles.TileNetworkAgent;
import com.wildex999.warpedspace.tiles.TileNetworkManager;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;


public class BlockNetworkAgent extends BlockBase {
	public final String name = "Network Agent";
	
	public BlockNetworkAgent()
	{
		this.setBlockName(name);
		this.setHardness(1f);
		this.setResistance(3f);
		this.setCreativeTab(CreativeTabs.tabBlock);
		this.setStepSound(Block.soundTypeMetal);
		
		BlockLibrary.register(this);
		registerTile(TileNetworkAgent.class);
	}
	
	@Override
	public boolean hasTileEntity(int meta)
	{
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int meta)
	{
		return new TileNetworkAgent();
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block otherBlock) {
		TileNetworkAgent tile = (TileNetworkAgent) world.getTileEntity(x, y, z);
    	if(tile != null)
    		tile.updateNeighborTiles();
	}
	
	@Override
    public boolean onBlockActivated(World world, int blockX, int blockY, int blockZ, EntityPlayer player, int side, float offX, float offY, float offZ)
    {
		player.openGui(WarpedSpace.instance, NetworkAgentGui.GUI_ID, world, blockX, blockY, blockZ);
        return true;
    }
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int metadata)
	{
		TileNetworkAgent tile = (TileNetworkAgent)world.getTileEntity(x, y, z);
		if(tile != null)
			dropInventory(tile, world, x, y, z);
		
		super.breakBlock(world, x, y, z, block, metadata);
	}

}
