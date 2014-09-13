package com.wildex999.warpedspace.tiles.renderers;

import org.lwjgl.opengl.GL11;

import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.models.ModelController;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class RendererNetworkController extends RendererTileBase  {
	
	private final ModelController model;
	
	public RendererNetworkController() {
		model = new ModelController();
	}
	
	@Override
	public ModelBase getModel() {
		return model;
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y,
			double z, float time) {
		
		//Texture
		ResourceLocation textures = new ResourceLocation(WarpedSpace.MODID, "textures/models/Controller.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(textures);
		
		super.renderTileEntityAt(tile, x, y, z, time);
	}

}
