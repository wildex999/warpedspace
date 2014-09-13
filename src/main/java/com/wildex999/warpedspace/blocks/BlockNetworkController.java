package com.wildex999.warpedspace.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.gui.WarpedControllerGui;
import com.wildex999.warpedspace.gui.NetworkAgentGui;
import com.wildex999.warpedspace.tiles.TileNetworkManager;
import com.wildex999.warpedspace.tiles.TileNetworkController;
import com.wildex999.warpedspace.tiles.TileNetworkAgent;

public class BlockNetworkController extends BlockBase {
	public final String name = "Network Controller";
	
	public BlockNetworkController()
	{
		this.setBlockName(name);
		this.setHardness(1f);
		this.setResistance(3f);
		this.setCreativeTab(CreativeTabs.tabBlock);
		this.setStepSound(Block.soundTypeMetal);
		
		BlockLibrary.register(this);
		registerTile(TileNetworkController.class);
	}
	
	@Override
	public boolean hasTileEntity(int meta)
	{
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int meta)
	{
		return new TileNetworkController();
	}
	
	@Override
    public boolean onBlockActivated(World world, int blockX, int blockY, int blockZ, EntityPlayer player, int side, float offX, float offY, float offZ)
    {
		player.openGui(WarpedSpace.instance, WarpedControllerGui.GUI_ID, world, blockX, blockY, blockZ);
        return true;
    }
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int metadata)
	{
		TileNetworkController tile = (TileNetworkController)world.getTileEntity(x, y, z);
		if(tile != null)
			dropInventory(tile, world, x, y, z);
		
		super.breakBlock(world, x, y, z, block, metadata);
	}
	
	@Override
	public int getRenderType() {
		return 998;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
}
