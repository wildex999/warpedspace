package com.wildex999.warpedspace.gui.elements;

import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;

import com.ibm.icu.impl.Assert;
import com.wildex999.utils.ModLog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

/*
 * Draws a list of selectable entries that can be scrolled.
 * Only draw entries that are visible.
 * 
 * For list movement, just use getLower/higher key on the current
 * top element to move up/down the list.
 * 
 */

public class GuiList extends Gui {
	public int posX, posY;
	public int width, height;
	public boolean noUpdate; //Don't update when removing/adding entries. Use when batching.
	
	protected int entryWidth = 220;
	public int entryHeight = 16;
	protected GuiScreen screen;
	
	//Use treemap for sorting and searching(subMap) while keeping log(N) lookup.
	//GuiListEntry has an internal linked list
	protected TreeMap<String, GuiListEntry> list;
	protected SortedMap<String, GuiListEntry> searchList; //sublist of list used when searching
	
	protected int scrollbarPosX, scrollbarPosY;
	protected int scrollbarWidth;
	protected int scrollbarAreaHeight; //Usually same as height
	
	protected GuiListEntry topEntry; //Top visible entry
	protected int topEntryIndex; //Index of top entry, used to calculate scroll
	
	protected int overflow; //Height above the set GuiList height
	protected int visibleEntries;
	
	protected GuiButton scrollBar;
	protected int yScroll; //List scroll.
	
	public GuiListEntry selectedEntry;
	public int selectedEntryIndex;
	
	public GuiList(GuiScreen screen, int x, int y, int width, int height) {
		list = new TreeMap<String, GuiListEntry>(String.CASE_INSENSITIVE_ORDER);
		posX = x;
		posY = y;
		this.width = width;
		this.entryWidth = width;
		this.height = height;
		overflow = 0;
		visibleEntries = 0;
		this.screen = screen;
		
		update();
	}
	
	//The button must be created and given a ID in the parent GuiScreen.
	//We then manage it's position and size.
	public void setScrollbarButton(GuiButton button) {
		scrollBar = button;
		
		scrollBar.xPosition = posX + width + 5;
		scrollBar.yPosition = posY;
		
		scrollBar.width = 10;
		scrollBar.height = 20;
		
		recalculateScrollButtonHeight();
	}
	
	//Filter input to any name starting with 'search' and jump to
	//first entry if currently looking outside search area
	public void setSearchString(String search) {
		if(search.length() == 0)
		{
			searchList = null;
			return;
		}
		
		//Create a subMap with the search entries
		char lastChar = search.charAt(search.length()-1);
		String searchEnd = search.substring(0, search.length()-1) + Character.toString((char)(lastChar+1));
		searchList = list.subMap(search, searchEnd);
		
		if(searchList.size() > 0)
		{
			if(topEntry.name.compareToIgnoreCase(searchList.firstKey()) < 0 || topEntry.name.compareToIgnoreCase(searchList.lastKey()) > 0)
			{
				GuiListEntry newTop = list.get(searchList.firstKey());
				this.setSelectedEntry(newTop);
				this.scrollToSelected();
			}
		}
	}
	
	public void update() {
		if(noUpdate)
			return;
		
		overflow = (list.size() * entryHeight) - height;
		visibleEntries = (int)Math.ceil((double)height/(double)entryHeight);
		
		recalculateScrollButtonHeight();
		updateTop();
		updateScrollBar();
	}
	
	public boolean addEntry(GuiListEntry entry) {
		String name = entry.name;
		
		if(list.containsKey(name))
			return false;
		
		list.put(name, entry);
		entry.list = this;
		
		//Check if before current top entry(Keep track of top entry index)
		if(topEntry != null)
		{
			int compare = name.compareToIgnoreCase(topEntry.name);
			if(compare < 0) //if new entry is before
				topEntryIndex++;
		}
		else
			setTopEntry(entry, 0);
		//Check if before selected entry
		if(selectedEntry != null)
		{
			int compare = name.compareToIgnoreCase(selectedEntry.name);
			if(compare < 0)
				selectedEntryIndex++;
		}
		
		//Find Previous/Next item, and link it in.
		Entry<String, GuiListEntry> link = list.lowerEntry(name);
		if(link != null)
		{
			linkAfter(entry, link.getValue());
			update();
			return true;
		}
		link = list.higherEntry(name);
		if(link != null)
		{
			linkBefore(entry, link.getValue());
			update();
			return true;
		}
		//No other entry, don't link
		//TODO: Assert this on dev.
		
		update();
		return true;
	}
	
	public boolean removeEntry(GuiListEntry entry) {
		String name = entry.name;
		
		entry = list.remove(name);
		if(entry == null)
			return false;
		
		if(entry == selectedEntry)
		{
			selectedEntry.onUnselect();
			selectedEntry = null;
		}
		
		if(topEntry != null)
		{
			//Are we removing the top entry?
			if(entry == topEntry)
			{
				boolean oldNoUpdate = noUpdate;
				noUpdate = true;
				if(topEntry.next != null)
					setTopEntry(topEntry.next, topEntryIndex);
				else
					setTopEntry(null, 0);
				noUpdate = oldNoUpdate;
			}
			else
			{
				//Check if removing entry before top entry
				int compare = name.compareToIgnoreCase(topEntry.name);
				if(compare < 0)
					topEntryIndex--;
			}
		}
		
		//Check if removing entry before selected entry
		if(selectedEntry != null)
		{
			int compare = name.compareToIgnoreCase(selectedEntry.name);
			if(compare < 0)
				selectedEntryIndex--;
		}
		
		unlink(entry);
		update();
		
		return true;
	}
	
	public boolean renameEntry(GuiListEntry entry, String newName) {
		if(!removeEntry(entry))
			return false;
		
		entry.name = newName;
		
		if(!addEntry(entry))
			return false;
		
		return true;
	}
	
	public void setSelectedEntry(GuiListEntry entry) {
		if(!list.containsKey(entry.name))
			return;
		
		int index = findEntryIndex(entry);
		setSelectedEntry(entry, index);
	}
	
	protected void setSelectedEntry(GuiListEntry entry, int entryIndex) {
		if(selectedEntry != null)
			selectedEntry.onUnselect();
		
		selectedEntry = entry;
		
		if(selectedEntry != null)
		{
			selectedEntryIndex = entryIndex;
			selectedEntry.onSelect();
		}
			
	}
	
	public int getSelectedEntryY() {
		if(selectedEntry == null)
			return -1;
		return posY + (selectedEntryIndex*entryHeight) - yScroll;
	}
	
	
	//Set the current top entry and it's index in the list.
	public void setTopEntry(GuiListEntry entry, int index) {
		topEntry = entry;
		topEntryIndex = index;
		
		update();
		
	}
	
	//Set top entry to current top entry - n entries.
	//Return how far up it moved(<n if reached beginning)
	public int topEntryMoveUp(int n) {
		if(topEntry == null)
			return 0;
		
		GuiListEntry current = topEntry;
		
		while(n-- > 0)
		{
			if(current.prev == null)
				break;
			
			current = current.prev;
			topEntryIndex--;
		}
		
		topEntry = current;
		if(topEntryIndex < 0)
			topEntryIndex = 0;
		
		return n;
	}
	
	//Move the top entry down to the next entry
	//Return how far down it moved(<n if reached end)
	public int topEntryMoveDown(int n) {
		if(topEntry == null)
			return 0;
		
		GuiListEntry current = topEntry;
		
		while(n-- > 0)
		{
			if(current.next == null)
				break;
			
			current = current.next;
			topEntryIndex++;
		}
		
		topEntry = current;
		if(topEntryIndex >= list.size())
			topEntryIndex = list.size() -1;
		
		return n;
	}
	
	//Draw the visible entries in the list
	public void draw(Minecraft mc) {
		//Start with top entry, iterate down until outside bounds
		if(topEntry == null)
			return;
		
		ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		int scale = sr.getScaleFactor();
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(posX*scale, (mc.currentScreen.height-(posY+height))*scale, width*scale, height*scale);
		
		int drawCount = 0;
		GuiListEntry current = topEntry;
		while(current != null && drawCount < visibleEntries+1)
		{
			int drawOffset = (topEntryIndex+drawCount)*entryHeight - yScroll;
			boolean outsideSearch = false;
			if(searchList != null)
			{
				if(searchList.size() == 0)
					outsideSearch = true;
				else if(current.name.compareToIgnoreCase(searchList.firstKey()) < 0 || current.name.compareToIgnoreCase(searchList.lastKey()) > 0)
					outsideSearch = true;
			}
			
			current.draw(mc, posX, posY + drawOffset, entryWidth, entryHeight, outsideSearch);
			drawCount++;
			current = current.next;
		}
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}
	
	//Link element before 'before'.
	protected void linkBefore(GuiListEntry entry, GuiListEntry before) {
		entry.next = before;
		if(before.prev != null)
		{	
			//Change their links
			before.prev.next = entry;
			entry.prev = before.prev;
		}
		before.prev = entry;	
	}
	
	//Link element after 'after'
	protected void linkAfter(GuiListEntry entry, GuiListEntry after) {
		entry.prev = after;
		if(after.next != null)
		{
			//Change their links
			after.next.prev = entry;
			entry.next = after.next;
		}
		after.next = entry;
	}
	
	//Remove links from entry, relinking remaining entries
	protected void unlink(GuiListEntry entry) {
		if(entry.prev == null && entry.next == null)
			return;
		
		//Entry has at least one link
		if(entry.prev != null && entry.next != null)
		{
			entry.prev.next = entry.next;
			entry.next.prev = entry.prev;
			entry.next = null;
			entry.prev = null;
			
			return;
		}
		
		if(entry.prev != null)
		{
			entry.prev.next = null;
			entry.prev = null;
		} else {
			entry.next.prev = null;
			entry.next = null;
		}
	}
	
	public GuiListEntry getEntry(int index) {
		if(topEntry == null)
			return null;
		
		GuiListEntry current = topEntry;
		int currentIndex = topEntryIndex;
		
		while(currentIndex < index)
		{
			current = current.next;
			currentIndex++;
			if(current == null)
				return null;
		}
		while(currentIndex > index)
		{
			current = current.prev;
			currentIndex--;
			if(current == null)
				return null;
		}
		
		return current;
	}
	
	//Get the entry with the given name, or null if it does not exist
	public GuiListEntry getEntry(String name) {
		return list.get(name);
	}
	
	//Get the index of the given entry
	public int findEntryIndex(GuiListEntry entry) {
		if(entry == topEntry)
			return topEntryIndex;
		
		//We don't know the index, so we have to iterate the list starting from current top.
		int compare = entry.name.compareToIgnoreCase(topEntry.name);
		GuiListEntry current = topEntry;
		int currentIndex = topEntryIndex;
		
		//Two while's instead of one with if inside, dat branching.
		if(compare < 0)
		{
			while(current != null)
			{
				current = current.prev;
				currentIndex--;
				if(current == entry)
					return currentIndex;
			}
		} else {
			while(current != null)
			{
				current = current.next;
				currentIndex++;
				if(current == entry)
					return currentIndex;
			}
		}
		//Assert: This should not happen. This means the entry is not in the linked list!
		return -1;
	}
	
	//Calculate and set the height og the scrollbar button.
	//Depends on the List element size, and number of entries.
	public void recalculateScrollButtonHeight() {
		if(scrollBar == null)
			return;
		
		float ratio = (float)height / (float)(list.size()*entryHeight); 
		scrollBar.height = (int) Math.ceil(height * ratio);
		
		if(scrollBar.height > height)
			scrollBar.height = height;
		if(scrollBar.height < 3)
			scrollBar.height = 3;
		
	}
	
	//Scroll content to scrollbar position.
	//Called when scrollbar is moved by player.
	public void scrollToScrollbar() {
		//How much one unit of scrollbar movement equals
		float scrollFactor = (float)overflow / (float)(height - scrollBar.height);
		yScroll = (int) Math.ceil((scrollBar.yPosition - posY) * scrollFactor);
		
		updateTop();
	}
	
	//Update scrollbar position to point to current yScroll position.
	//Called when entries are added/removed, or when scrolling with scroll wheel.
	public void updateScrollBar() {
		recalculateScrollButtonHeight();
		
		if(scrollBar == null)
			return;
		
		float scrolled =  (float)yScroll / (float)((entryHeight*list.size()) - height); //How much we have scrolled(0.0 to 1.0)
		scrollBar.yPosition = posY + (int)Math.ceil((height - scrollBar.height) * scrolled);
	}
	
	//Scroll to the currently selected entry
	public void scrollToSelected() {
		if(selectedEntry == null)
			return;
		
		yScroll = selectedEntryIndex * entryHeight;
		
		if(yScroll > list.size()*entryHeight - height)
			yScroll = list.size()*entryHeight - height;
		if(yScroll < 0)
			yScroll = 0;
		
		updateTop();
		updateScrollBar();
	}
	
	//Set the new Top Entry depending on yScroll
	public void updateTop() {
		int newEntryIndex = (int) Math.ceil(yScroll/entryHeight);
		
		//ModLog.logger.info("New Entry Index: " + newEntryIndex);
		
		if(newEntryIndex >= list.size())
			newEntryIndex = list.size() -1;
		else if(newEntryIndex < 0)
			newEntryIndex = 0;
		
		if(newEntryIndex < topEntryIndex)
			topEntryMoveUp(topEntryIndex - newEntryIndex);
		else if(newEntryIndex > topEntryIndex)
			topEntryMoveDown(newEntryIndex - topEntryIndex);
	}
	
	//Clear all entries, and resetting any scroll, top entry and selection
	public void clear() {
		list.clear();
		selectedEntry = null;
		topEntry = null;
		
		update();
	}
	
	//Called when mouse is clicked on GuiList.
	//Used to check which entry is currently selected
	//Return true if an entry was clicked
	public boolean onMouseClick(int x, int y) {
		//Get entry which is clicked
		if(list.size() == 0)
			return false;
		
		int entryIndex = ((y + yScroll) - posY) / entryHeight;
		if(entryIndex < 0)
			entryIndex = 0;
		if(entryIndex >= list.size())
			entryIndex = list.size() - 1;
		
		GuiListEntry entry = getEntry(entryIndex);
		
		if(entry == null)
			return false;
		
		if(selectedEntry == entry)
			setSelectedEntry(null, 0);
		else
			setSelectedEntry(entry, entryIndex);
		
		return true;
		
	}
	
	public void onButtonDrag(GuiButton button, int x, int y) {
		if(scrollBar == null || button != scrollBar)
			return;
		
		scrollBar.yPosition = y - (scrollBar.height/2);
		if(scrollBar.yPosition < posY)
			scrollBar.yPosition = posY;
		else if(scrollBar.yPosition + scrollBar.height > posY + height)
			scrollBar.yPosition = posY + height - scrollBar.height;
		
		scrollToScrollbar();
	}
	
	//Scroll list up/down
	public void onScroll(int scroll) {
		if(scroll > 0)
			yScroll -= entryHeight;
		else if(scroll < 0)
			yScroll += entryHeight;
		
		if(yScroll < 0)
			yScroll = 0;
		else if(yScroll > (entryHeight*list.size()) - height)
			yScroll = (entryHeight*list.size()) - height;
		
		updateTop();
		updateScrollBar();
	}
	
}
