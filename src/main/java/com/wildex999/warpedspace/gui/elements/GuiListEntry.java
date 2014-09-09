package com.wildex999.warpedspace.gui.elements;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

//Entry in a GuiList

public class GuiListEntry extends Gui {
	
	public String name;
	public Object value;
	public GuiList list;
	public int border = 1; //Space between two entries
	public boolean selected;
	
	//Linked list for fast traversal
	//Manages by GuiList.
	public GuiListEntry prev;
	public GuiListEntry next;
	
	public GuiListEntry(String name) {
		this.name = name;
	}

	//Called when the entry is visible
	//public void onEntryVisible() {}
	
	//Called when the entry is no longer visible
	//public void onEntryInvisible() {}
	
	public void onSelect() {
		selected = true;
	}
	
	public void onUnselect() {
		selected = false;
	}
	
	//Draw the entry
	//x and y is the top left corner of the entry in screen coordinates.
	//width and height is the size given to the entry by the List.
	public void draw(Minecraft mc, int x, int y, int width, int height, boolean shade) {
		int color = 0xFF000000;
		int fontColor = 0xFFFFFF;
		
		if(selected)
			color = 0xFF3DBF1D;
		
		if(shade)
			fontColor = 0x808080;
		
		this.drawRect(x, y, x+width, y+height - border, color);
		this.drawRect(x, y+height - border, x+width, y+height, 0xFFFFFFFF);
		this.drawString(mc.fontRenderer, name, x + 2, y + (height/2) - 4, fontColor);
		
		if(shade)
			GL11.glColor4f(1f, 1f, 1f, 1f);
	}
	
}
