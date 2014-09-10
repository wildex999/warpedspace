package com.wildex999.warpedspace.tiles.renderers;

import org.lwjgl.opengl.GL11;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.tiles.TileNetworkInterface;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class RendererNetworkInterface extends TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity baseTile, double x,
			double y, double z, float time) {
		World worldObj = Minecraft.getMinecraft().theWorld;
		if(baseTile == null || !(baseTile instanceof TileNetworkInterface))
			return; //TODO: Render default
		TileNetworkInterface tileInterface = (TileNetworkInterface)baseTile;
		Block hostBlock = tileInterface.hostBlock;
		
		if(hostBlock == null || hostBlock.renderAsNormalBlock())
			return;
		
		TileEntityRendererDispatcher renderer = TileEntityRendererDispatcher.instance;
		
		//Get TileEntity if loaded on client, or create a proxy if not
		int otherX = tileInterface.x;
		int otherY = tileInterface.y;
		int otherZ = tileInterface.z;
		
		TileEntity renderTile = worldObj.getTileEntity(otherX, otherY, otherZ);
		if(renderTile == null)
			return;
		
		renderer.renderTileEntityAt(renderTile, x, y, z, time);
		
		return;
	}

}
