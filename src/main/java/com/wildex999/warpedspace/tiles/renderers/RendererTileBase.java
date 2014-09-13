package com.wildex999.warpedspace.tiles.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.wildex999.warpedspace.WarpedSpace;

public abstract class RendererTileBase extends TileEntitySpecialRenderer {
	
	public abstract ModelBase getModel();
	public static boolean renderInventory; //Set to true when rendering in hand/inventory
	
	@Override
	public void renderTileEntityAt(TileEntity tile, double x,
			double y, double z, float time) {
		
        //Render
		GL11.glPushMatrix(); 
		GL11.glTranslated(x, y, z); //Do rendering at correct location
		
		if(renderInventory)
			GL11.glTranslatef(0f, 1.0f, 0f); //Inventory/hand uses a bit different positioning
		else
			GL11.glTranslatef(0.5F, 1.5F, 0.5F); //Translate to correct roation/render position
		GL11.glRotatef(-180F, 1F, 0F, 0F); //It renders upside down for some reason(I blame techne)
		getModel().render(null, 0f, 0f, 0f, -0.1f, 0f, 0.0625f);
		GL11.glPopMatrix();
		
	}
}
