package com.wildex999.warpedspace;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.blocks.render.RenderNetworkControllerBlock;
import com.wildex999.warpedspace.blocks.render.RenderNetworkInterfaceBlock;
import com.wildex999.warpedspace.tiles.TileNetworkController;
import com.wildex999.warpedspace.tiles.TileNetworkInterface;
import com.wildex999.warpedspace.tiles.renderers.RendererNetworkController;
import com.wildex999.warpedspace.tiles.renderers.RendererNetworkInterface;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenderers() {
		//Tile entity renderers
		ClientRegistry.bindTileEntitySpecialRenderer(TileNetworkInterface.class, new RendererNetworkInterface());
		ClientRegistry.bindTileEntitySpecialRenderer(TileNetworkController.class, new RendererNetworkController());
		
		//Block renderers
		RenderingRegistry.registerBlockHandler(new RenderNetworkInterfaceBlock());
		RenderingRegistry.registerBlockHandler(new RenderNetworkControllerBlock());

	}
}
