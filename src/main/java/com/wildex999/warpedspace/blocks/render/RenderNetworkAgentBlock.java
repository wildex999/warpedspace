package com.wildex999.warpedspace.blocks.render;

import com.wildex999.warpedspace.tiles.TileNetworkAgent;
import com.wildex999.warpedspace.tiles.renderers.RendererTileBase;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderNetworkAgentBlock implements ISimpleBlockRenderingHandler {

	private TileNetworkAgent agent = new TileNetworkAgent();
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {		
		RendererTileBase.renderInventory = true;
		TileEntityRendererDispatcher.instance.renderTileEntityAt(agent, 0, 0, 0, 0);
		RendererTileBase.renderInventory = false;
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public int getRenderId() {
		// TODO Auto-generated method stub
		return 996;
	}

}
