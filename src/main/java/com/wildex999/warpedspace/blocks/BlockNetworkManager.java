package com.wildex999.warpedspace.blocks;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.gui.NetworkManagerGui;
import com.wildex999.warpedspace.gui.WarpedControllerGui;
import com.wildex999.warpedspace.tiles.TileNetworkManager;
import com.wildex999.warpedspace.tiles.TileNetworkController;

public class BlockNetworkManager extends BlockBase {
	
	public final String name = "Network Manager";
	
	public BlockNetworkManager() {
		this.setBlockName(WarpedSpace.MODID + ":" + name);
		this.setHardness(1f);
		this.setResistance(3f);
		this.setCreativeTab(CreativeTabs.tabBlock);
		
		BlockLibrary.register(this);
		registerTile(TileNetworkManager.class);
	}
	
	@Override
	public boolean hasTileEntity(int meta)
	{
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int meta)
	{
		return new TileNetworkManager();
	}
	
	@Override
    public boolean onBlockActivated(World world, int blockX, int blockY, int blockZ, EntityPlayer player, int side, float offX, float offY, float offZ)
    {
		player.openGui(WarpedSpace.instance, NetworkManagerGui.GUI_ID, world, blockX, blockY, blockZ);
        return true;
    }
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int metadata)
	{
		TileNetworkManager tile = (TileNetworkManager)world.getTileEntity(x, y, z);
		if(tile != null)
			dropInventory(tile, world, x, y, z);
		
		super.breakBlock(world, x, y, z, block, metadata);
	}
	
}
