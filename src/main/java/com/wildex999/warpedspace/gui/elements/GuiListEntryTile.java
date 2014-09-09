package com.wildex999.warpedspace.gui.elements;

import org.lwjgl.opengl.GL11;

import com.wildex999.utils.ModLog;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

//GuiList entry for Tile Entities, showing an Icon of the Tile Entity block.

public class GuiListEntryTile extends GuiListEntry{
	protected static RenderItem itemRender = new RenderItem();
	
	public String tileName;
	protected ItemStack item;
	
	public GuiListEntryTile(String name, String tileName) {
		super(name);
		this.tileName = tileName;
		ModLog.logger.info("Added tileName: " + tileName);

		if(tileName.length() != 0)
		{
			item = new ItemStack((Item)Item.itemRegistry.getObject(tileName));
			if(item.getItem() == null)
			{
				Block drawBlock = Block.getBlockFromName(tileName);
				item = new ItemStack(drawBlock);
				if(item.getItem() == null)
					item = null;
			}
		}
		else
			item = null;
	}
	
	@Override
	public void draw(Minecraft mc, int x, int y, int width, int height, boolean shade) {

		if(item != null)
		{
			RenderHelper.disableStandardItemLighting(); //Put us in a known render state
			if(shade)
				GL11.glEnable(GL11.GL_LIGHTING);
			itemRender.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), item, x+2, y+(height/2)-8);
			if(shade)
				GL11.glDisable(GL11.GL_LIGHTING);
			RenderHelper.disableStandardItemLighting(); //Put us in a known render state
		}
		
		int color = 0xFF000000;
		int colorFont = 0xFFFFFF;
		
		if(selected)
			color = 0xFF3DBF1D;
		if(shade)
			colorFont = 0x808080;
		
		this.drawRect(x, y, x+width, y+height - border, color);
		this.drawRect(x, y+height - border, x+width, y+height, 0xFFFFFFFF);
		this.drawString(mc.fontRenderer, name, x + 20, y + (height/2) - 4, colorFont);
		
		if(shade)
			GL11.glColor4f(1f, 1f, 1f, 1f);
	}

}
