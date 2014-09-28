package com.wildex999.warpedspace.blocks.render;

import com.wildex999.utils.ModLog;
import com.wildex999.utils.TessellatorMirror;
import com.wildex999.warpedspace.blocks.BlockNetworkInterface;
import org.lwjgl.opengl.GL11;

import com.wildex999.warpedspace.tiles.TileBasicNetworkRelay;
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
public class RenderNetworkInterfaceBlock implements ISimpleBlockRenderingHandler {

	private TileNetworkInterface networkInterface = new TileNetworkInterface();
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId,
			RenderBlocks renderer) {
		RendererTileBase.renderInventory = true;
		TileEntityRendererDispatcher.instance.renderTileEntityAt(networkInterface, 0, 0, 0, 0);
		RendererTileBase.renderInventory = false;
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {

		World worldObj = Minecraft.getMinecraft().theWorld;
		TileEntity baseTile = worldObj.getTileEntity(x, y, z);
		if(baseTile == null || !(baseTile instanceof TileNetworkInterface)) {
            fillBuffer();
            return true;
        }
		
		TileNetworkInterface tileInterface = (TileNetworkInterface)baseTile;
		Block hostBlock = tileInterface.hostBlock;
        int hostMeta = tileInterface.hostMeta;
		
		if(hostBlock == null || hostBlock == block) {
            fillBuffer();
            return true;
        }

        if(!hostBlock.canRenderInPass(BlockNetworkInterface.currentRenderPass)) {
            fillBuffer();
            return true;
        }
		
		//This will wreck the rendering performance of blocks, about the same cost as rendering a tile entity?
		float scale = 0.65f;
		float scaleInv = 1f/scale;
		
		int chunkX = x>>4;
		int chunkY = y>>4;
		int chunkZ = z>>4;
		
		//Positioning of relative to chunk
		float moveX = x*(1f-scale);
		float moveY = y*(1f-scale);
	    float moveZ = z*(1f-scale);
		
		//Correct offset so we are in the middle
		moveX += 0.5*(1-scale);
		moveY += 0.5*(1-scale);
		moveZ += 0.5*(1-scale);

        /*Tessellator.instance.draw();
        GL11.glTranslatef(moveX, moveY, moveZ);
        GL11.glScalef(scale, scale, scale);
        Tessellator.instance.startDrawingQuads();*/
        Tessellator originalTessellator = Tessellator.instance;
        TessellatorMirror mirrorTessellator = new TessellatorMirror(originalTessellator);
        Tessellator.instance = mirrorTessellator;

        mirrorTessellator.setScale(scale, scale, scale);
        mirrorTessellator.setOffset(moveX, moveY, moveZ);


        //Some Blocks need to access the Block/TileEntity to render correctly
        Block prevBlock = worldObj.getBlock(x, y, z);


        ModLog.logger.info("TEST2: " + hostBlock + " M: " + hostMeta + " RenderType: " + hostBlock.getRenderType() + " RenderPass: " + hostBlock.getRenderBlockPass());
        worldObj.setBlock(x, y, z, hostBlock, hostMeta, 0);
        if(tileInterface.proxyTile != null) {
            tileInterface.proxyTile.validate();
            worldObj.setTileEntity(x, y, z, tileInterface.proxyTile);
        }

        renderer.renderBlockByRenderType(hostBlock, x, y, z);

        //Compensate positions with scale
        /*Tessellator.instance.draw();
        GL11.glScalef(scaleInv, scaleInv, scaleInv);
        GL11.glTranslatef(-moveX, -moveY, -moveZ);
        Tessellator.instance.startDrawingQuads();*/


        Tessellator.instance = originalTessellator;

        worldObj.setBlock(x, y, z, prevBlock, hostMeta, 4);
        if(tileInterface.proxyTile != null) {
            tileInterface.validate();
            worldObj.setTileEntity(x, y, z, tileInterface);
            tileInterface.proxyTile.validate();
        }

		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getRenderId() {
		// TODO Auto-generated method stub
		return 999;
	}

    public void fillBuffer() {
        //Since we always run multiple passes, it will crash if given an empty draw buffer
        Tessellator.instance.addVertex(0,0,0);
        Tessellator.instance.addVertex(0,0,0);
        Tessellator.instance.addVertex(0,0,0);
        Tessellator.instance.addVertex(0,0,0);
    }

}
