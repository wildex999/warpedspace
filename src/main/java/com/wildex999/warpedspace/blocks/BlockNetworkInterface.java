package com.wildex999.warpedspace.blocks;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.gui.NetworkAgentGui;
import com.wildex999.warpedspace.gui.NetworkInterfaceGui;
import com.wildex999.warpedspace.tiles.TileNetworkAgent;
import com.wildex999.warpedspace.tiles.TileNetworkInterface;

public class BlockNetworkInterface extends BlockBase {
	public final String name = "Network Interface";
	
	public BlockNetworkInterface()
	{
		this.setBlockName(WarpedSpace.MODID + ":" + name);
		this.setHardness(1f);
		this.setResistance(3f);
		this.setCreativeTab(CreativeTabs.tabBlock);
		this.setStepSound(Block.soundTypeMetal);
		
		BlockLibrary.register(this);
		registerTile(TileNetworkInterface.class);
	}
	
	@Override
	public boolean hasTileEntity(int meta)
	{
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int meta)
	{
		return new TileNetworkInterface();
	}
	
	@Override
    public boolean onBlockActivated(World world, int blockX, int blockY, int blockZ, EntityPlayer player, int side, float offX, float offY, float offZ)
    {
		player.openGui(WarpedSpace.instance, NetworkInterfaceGui.GUI_ID, world, blockX, blockY, blockZ);
        return true;
    }
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int metadata)
	{
		TileNetworkInterface tile = (TileNetworkInterface)world.getTileEntity(x, y, z);
		if(tile != null)
			dropInventory(tile, world, x, y, z);
		
		super.breakBlock(world, x, y, z, block, metadata);
	}
}