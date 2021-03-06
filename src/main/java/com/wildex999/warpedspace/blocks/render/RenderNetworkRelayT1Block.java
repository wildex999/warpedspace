package com.wildex999.warpedspace.blocks.render;

import org.lwjgl.opengl.GL11;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.tiles.TileBasicNetworkRelay;
import com.wildex999.warpedspace.tiles.TileNetworkController;
import com.wildex999.warpedspace.tiles.TileNetworkInterface;
import com.wildex999.warpedspace.tiles.renderers.RendererTileBase;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderNetworkRelayT1Block implements ISimpleBlockRenderingHandler {

	private TileBasicNetworkRelay relay = new TileBasicNetworkRelay();
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {	
		RendererTileBase.renderInventory = true;
		TileEntityRendererDispatcher.instance.renderTileEntityAt(relay, 0, 0, 0, 0);
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
		return 997;
	}

}
