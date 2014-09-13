package com.wildex999.warpedspace;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.blocks.render.RenderNetworkAgentBlock;
import com.wildex999.warpedspace.blocks.render.RenderNetworkControllerBlock;
import com.wildex999.warpedspace.blocks.render.RenderNetworkInterfaceBlock;
import com.wildex999.warpedspace.blocks.render.RenderNetworkRelayT1Block;
import com.wildex999.warpedspace.tiles.TileBasicNetworkRelay;
import com.wildex999.warpedspace.tiles.TileNetworkAgent;
import com.wildex999.warpedspace.tiles.TileNetworkController;
import com.wildex999.warpedspace.tiles.TileNetworkInterface;
import com.wildex999.warpedspace.tiles.renderers.RendererNetworkAgent;
import com.wildex999.warpedspace.tiles.renderers.RendererNetworkController;
import com.wildex999.warpedspace.tiles.renderers.RendererNetworkInterface;
import com.wildex999.warpedspace.tiles.renderers.RendererNetworkRelayT1;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenderers() {
		//Tile entity renderers
		ClientRegistry.bindTileEntitySpecialRenderer(TileNetworkInterface.class, new RendererNetworkInterface());
		ClientRegistry.bindTileEntitySpecialRenderer(TileNetworkController.class, new RendererNetworkController());
		ClientRegistry.bindTileEntitySpecialRenderer(TileBasicNetworkRelay.class, new RendererNetworkRelayT1());
		ClientRegistry.bindTileEntitySpecialRenderer(TileNetworkAgent.class, new RendererNetworkAgent());
		
		//Block renderers
		RenderingRegistry.registerBlockHandler(new RenderNetworkInterfaceBlock());
		RenderingRegistry.registerBlockHandler(new RenderNetworkControllerBlock());
		RenderingRegistry.registerBlockHandler(new RenderNetworkRelayT1Block());
		RenderingRegistry.registerBlockHandler(new RenderNetworkAgentBlock());
	}
}
