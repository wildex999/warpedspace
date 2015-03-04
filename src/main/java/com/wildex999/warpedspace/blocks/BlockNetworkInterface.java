package com.wildex999.warpedspace.blocks;

import com.wildex999.warpedspace.TickHandler;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.gui.NetworkAgentGui;
import com.wildex999.warpedspace.gui.NetworkInterfaceGui;
import com.wildex999.warpedspace.tiles.TileNetworkAgent;
import com.wildex999.warpedspace.tiles.TileNetworkInterface;

public class BlockNetworkInterface extends BlockBase {
	public String name = "Network Interface";
    public static int currentRenderPass = 0;
	
	public BlockNetworkInterface()
	{
		this.setBlockName(name);
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
		//ModLog.logger.info("CREATE INTERFACE TILE ENTITY: " + world.isRemote);
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
		if(tile != null) {
            boolean inWorldTick = TickHandler.inWorldTick;
            TickHandler.inWorldTick = false; //Make sure we drop the Interface inventory, not the hosted one
            dropInventory(tile, world, x, y, z);
            TickHandler.inWorldTick = inWorldTick;
        }
		
		super.breakBlock(world, x, y, z, block, metadata);
	}
	
	@Override
	public int getRenderType() {
		return 999;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

    @Override
    public boolean isNormalCube() {
        return false;
    }

    @Override
	public boolean renderAsNormalBlock() {
		return false;
	}

    @Override
    public boolean canRenderInPass(int pass) {
        currentRenderPass = pass;
        return true;
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    //---REDSTONE---//
	
	@Override
	public boolean canProvidePower() {
		//Interface can provide power if we are hosting a block which can.
		//We have no way to actually check that, so we just return true always.
		return true;
	}
	
	//We simply proxy what is provided by the host, -1 in power
	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x,int y, int z, int dir) {
		TileNetworkInterface tile = (TileNetworkInterface)world.getTileEntity(x, y, z);
		if(tile == null)
			return 0;
		return tile.getRedstoneManager().getHostedWeakPower(dir);
	}
	
	//We simply proxy what is provided by the host, -1 in power
	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int dir) {
		TileNetworkInterface tile = (TileNetworkInterface)world.getTileEntity(x, y, z);
		if(tile == null)
			return 0;
		return tile.getRedstoneManager().getHostedStrongPower(dir);
	}
	
	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
		return true;
	}
	
	//---LIGHT---//
	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		TileNetworkInterface tile = (TileNetworkInterface)world.getTileEntity(x, y, z);
		if(tile == null)
			return 0;
		//ModLog.logger.info("LIGHT: " + tile.getLightManager().getLightLevel());
		return tile.getLightManager().getLightLevel();
	}
}
