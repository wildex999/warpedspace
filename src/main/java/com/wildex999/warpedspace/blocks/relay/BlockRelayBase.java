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
import com.wildex999.warpedspace.items.ItemRelayBlock;
import com.wildex999.warpedspace.tiles.TileBasicNetworkRelay;

public abstract class BlockRelayBase extends BlockBase {
	private static boolean registeredTile = false;
	
	public BlockRelayBase()
	{
		this.setBlockName(getName());
		this.setHardness(1f);
		this.setResistance(3f);
		this.setCreativeTab(CreativeTabs.tabBlock);
		this.setStepSound(Block.soundTypeMetal);
		
		BlockLibrary.register(this, ItemRelayBlock.class);
		if(!registeredTile)
		{
			registerTile(TileBasicNetworkRelay.class);
			registeredTile = true;
		}
	}
	
	public abstract String getName();
	public abstract int getTier();
	public abstract int getRadius();
	
	@Override
	public boolean hasTileEntity(int meta)
	{
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int meta)
	{
		TileBasicNetworkRelay tile = new TileBasicNetworkRelay();
		return tile;
	}
	
	@Override
    public boolean onBlockActivated(World world, int blockX, int blockY, int blockZ, EntityPlayer player, int side, float offX, float offY, float offZ)
    {
		player.openGui(WarpedSpace.instance, BasicNetworkRelayGui.GUI_ID, world, blockX, blockY, blockZ);
        return true;
    }
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int metadata)
	{
		TileBasicNetworkRelay tile = (TileBasicNetworkRelay)world.getTileEntity(x, y, z);
		if(tile != null)
			dropInventory(tile, world, x, y, z);
		
		super.breakBlock(world, x, y, z, block, metadata);
	}
}
