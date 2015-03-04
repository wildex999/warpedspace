package com.wildex999.warpedspace.tiles.renderers;

import com.wildex999.warpedspace.blocks.BlockLibrary;
import org.lwjgl.opengl.GL11;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.models.ModelNetworkInterface;
import com.wildex999.warpedspace.tiles.TileNetworkInterface;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class RendererNetworkInterface extends RendererTileBase {

	private ModelNetworkInterface model;
	
	public RendererNetworkInterface() {
		model = new ModelNetworkInterface();
	}
	
	@Override
	public void renderTileEntityAt(TileEntity baseTile, double x,
			double y, double z, float time) {
		World worldObj = Minecraft.getMinecraft().theWorld;
		if(baseTile == null || !(baseTile instanceof TileNetworkInterface))
		{
			renderInterfaceOutline(x, y, z);
			return;
		}
		TileNetworkInterface tileInterface = (TileNetworkInterface)baseTile;
		Block hostBlock = tileInterface.hostBlock;
        int hostMeta = tileInterface.hostMeta;
        int tileX = tileInterface.xCoord;
        int tileY = tileInterface.yCoord;
        int tileZ = tileInterface.zCoord;
		
		if(hostBlock == null)
		{
			renderInterfaceOutline(x, y, z);
			return;
		}
		
		TileEntityRendererDispatcher renderer = TileEntityRendererDispatcher.instance;
		
		//Get TileEntity if loaded on client, or create a proxy if not
		//TODO: Get and create tile (No need to place, as we pass it right in, but set world and position correctly) if not loaded on client
		int otherX = tileInterface.x;
		int otherY = tileInterface.y;
		int otherZ = tileInterface.z;

        TileEntity renderTile;
		//TileEntity renderTile = worldObj.getTileEntity(otherX, otherY, otherZ);
		//if(renderTile == null)
		//{
			renderTile = tileInterface.proxyTile;
			if(renderTile == null)
			{
				renderInterfaceOutline(x, y, z);
				return;
			}
		//}

        //ModLog.logger.info("TEST1");
        //Block prevBlock = worldObj.getBlock(tileX, tileY, tileZ);
        if(tileInterface.proxyTile != null) {
            worldObj.setTileEntity(tileX, tileY, tileZ, tileInterface.proxyTile);
        }

		float scale = 0.65f;
		float scaleInv = 1f/0.65f;
		float center = 1*(1-scale);
		GL11.glScalef(scale, scale, scale);
		renderer.renderTileEntityAt(renderTile, (x*scaleInv)+center, (y*scaleInv)+center, (z*scaleInv)+center, time);
		GL11.glScalef(scaleInv, scaleInv, scaleInv);

        if(tileInterface.proxyTile != null) {
            tileInterface.validate();
            worldObj.setTileEntity(tileX, tileY, tileZ, tileInterface);
            tileInterface.proxyTile.validate();
        }

        renderInterfaceOutline(x, y, z);



		return;
	}
	
	@Override
	public ModelBase getModel() {
		return model;
	}
	
	
	public void renderInterfaceOutline(double x, double y, double z) {
		//Texture
		ResourceLocation textures = new ResourceLocation(WarpedSpace.MODID, "textures/models/NetworkInterface.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(textures);
		
		super.renderTileEntityAt(null, x, y, z, 0);
	}

}
