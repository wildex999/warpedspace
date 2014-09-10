package com.wildex999.warpedspace.tiles.renderers;

import org.lwjgl.opengl.GL11;

import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.models.ModelController;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class RendererNetworkController extends TileEntitySpecialRenderer  {
	
	private final ModelController model;
	
	public RendererNetworkController() {
		model = new ModelController();
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tile, double x,
			double y, double z, float time) {
		
		ResourceLocation textures = new ResourceLocation(WarpedSpace.MODID, "textures/models/Controller.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(textures);
		
		GL11.glPushMatrix(); 
		GL11.glTranslated(x, y, z); //Do rendering at correct location
		
		GL11.glTranslatef(0.5F, 1.5F, 0.5F); //Translate to correct roation/render position
		GL11.glRotatef(-180F, 1F, 0F, 0F); //It renders upside down for some reason(I blame techne)
		model.render(null, 0f, 0f, 0f, -0.1f, 0f, 0.0625f);
		GL11.glPopMatrix();
		
	}

}
