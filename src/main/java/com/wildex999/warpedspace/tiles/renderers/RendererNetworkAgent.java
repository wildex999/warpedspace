package com.wildex999.warpedspace.tiles.renderers;

import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.models.ModelNetworkAgent;
import com.wildex999.warpedspace.tiles.TileNetworkAgent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class RendererNetworkAgent extends RendererTileBase {

	public ModelNetworkAgent model;
	
	public RendererNetworkAgent() {
		model = new ModelNetworkAgent();
	}
	
	@Override
	public ModelBase getModel() {
		return model;
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y,
			double z, float time) {
		//Texture
		ResourceLocation textures = new ResourceLocation(WarpedSpace.MODID, "textures/models/NetworkAgent.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(textures);
		
		//Render sides with entries
		if(tile == null || tile.getWorldObj() == null)
		{
			model.setRenderSides(true, true, true, true, true, true);
		}
		else
		{
			TileNetworkAgent agent = (TileNetworkAgent)tile;
			model.setRenderSides(agent.sideUsed(agent.NORTH), agent.sideUsed(agent.SOUTH), agent.sideUsed(agent.WEST),
					agent.sideUsed(agent.EAST), agent.sideUsed(agent.TOP), agent.sideUsed(agent.BOTTOM));
		}
		
		super.renderTileEntityAt(tile, x, y, z, time);
	}

}
