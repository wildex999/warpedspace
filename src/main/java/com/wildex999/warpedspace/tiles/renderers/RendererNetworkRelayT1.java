package com.wildex999.warpedspace.tiles.renderers;

import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.models.ModelRelayT1;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class RendererNetworkRelayT1 extends RendererTileBase {

	private ModelRelayT1 model;
	
	public RendererNetworkRelayT1() {
		model = new ModelRelayT1();
	}
	
	@Override
	public ModelBase getModel() {
		return model;
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y,
			double z, float time) {
		//Texture
		ResourceLocation textures = new ResourceLocation(WarpedSpace.MODID, "textures/models/RelayT1.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(textures);
		
		super.renderTileEntityAt(tile, x, y, z, time);
	}

}
