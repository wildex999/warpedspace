package com.wildex999.warpedspace.gui;

import javax.swing.plaf.basic.BasicOptionPaneUI.ButtonActionListener;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import com.wildex999.utils.BlockItemName;
import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.GuiHandler;
import com.wildex999.warpedspace.Messages;
import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.gui.WarpedControllerGui.GUI;
import com.wildex999.warpedspace.gui.elements.GuiButtonStretched;
import com.wildex999.warpedspace.gui.elements.GuiLabel;
import com.wildex999.warpedspace.gui.elements.GuiList;
import com.wildex999.warpedspace.gui.elements.GuiListEntry;
import com.wildex999.warpedspace.gui.elements.GuiListEntryTile;
import com.wildex999.warpedspace.inventory.ControllerContainer;
import com.wildex999.warpedspace.inventory.InterfaceContainer;
import com.wildex999.warpedspace.items.ItemPortableNetworkInterface;
import com.wildex999.warpedspace.networking.MessageActivate;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.MessageWatchGui;
import com.wildex999.warpedspace.networking.netinterface.MessageCSInterfaceUpdate;
import com.wildex999.warpedspace.networking.netinterface.MessageCSWatchList;
import com.wildex999.warpedspace.tiles.TileNetworkInterface;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.common.network.IGuiHandler;

public class NetworkInterfaceGui implements IGuiHandler {
	public static final int GUI_ID = GuiHandler.getNextGuiID();
	
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileNetworkInterface inventory = (TileNetworkInterface)world.getTileEntity(x, y, z);
		return new InterfaceContainer(player.inventory, inventory);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,int x, int y, int z) {
		TileNetworkInterface tile = (TileNetworkInterface)world.getTileEntity(x, y, z);
		return new GUI(player, tile);
	}
	
	public static class GUI extends GuiContainer {
		
		public static final ResourceLocation backgroundImage = new ResourceLocation(WarpedSpace.MODID, "textures/gui/node_base.png");
		public static final ResourceLocation backgroundImageTiles = new ResourceLocation(WarpedSpace.MODID, "textures/gui/network_interface_tiles.png");
		public int thisId = GUI_ID;
		
		private static final short backgroundWidth = 175;
		private static final short backgroundHeight = 221;
		private static final short backgroundWidthTiles = 256;
		private static final short backgroundHeightTiles = 215;
		
		private final String defaultNoTile = "<No Tile>";
		private static int colorGotTile = 0x47D147;
		private static int colorOfflineTile = 0xFFA500;
		private static int colorInvalidTile = 0xFF6600;
		
		private TileNetworkInterface tile;
		private InterfaceContainer container;
		
		private int networkState;
		private int entryState;
		private String entryName;
		private String itemName;
		private byte itemMeta;
		private ItemStack item;
		private long gid;
		
		private long selectedEntry;
		private boolean watchingTiles;
		
		//Main GUI
		private GuiLabel labelContainerName;
		private GuiLabel labelNetworkState;
		
		private GuiButton buttonSetTile;
		private GuiButton buttonOpenTileGui;
		
		//Tiles GUI
		private GuiButton buttonExit;
		private GuiButton buttonSelect;
		private GuiTextField inputName;
		private GuiList tilesList;
		
		private boolean showTiles;
		private GuiButton currentButton;
		
		public GUI(EntityPlayer player, TileNetworkInterface tile) {
			//Slot positions set in ControllerContainer
			super(new InterfaceContainer(player.inventory, tile));
			
			this.tile = tile;
			this.container = (InterfaceContainer)inventorySlots;
			showTiles = false;
			if(tile == null)
				player.closeScreen();
			
			networkState = Messages.noNetwork;
			entryState = Messages.notSet;
			entryName = "";
			itemName = "";
			itemMeta = 0;
			gid = -1;
			item = null;
			watchingTiles = false;
			GuiHandler.currentGuiTile = tile;
		}
		
		public void networkUpdate(int networkState, int entryState, String entryName, long gid, String entryItemName, byte itemMeta) {
			this.networkState = networkState;
			this.entryState = entryState;
			this.entryName = entryName;
			this.gid = gid;
			this.itemName = entryItemName;
			this.itemMeta = itemMeta;
			
			updateGui();
		}
		
		public void updateGui() {
			if(labelNetworkState == null)
				return;
				
			labelNetworkState.label = Messages.get(networkState);
			if(networkState == Messages.online)
				labelNetworkState.color = Messages.colorOk;
			else
				labelNetworkState.color = Messages.colorProblem;
			
			buttonSetTile.displayString = defaultNoTile;
			buttonSetTile.packedFGColour = colorInvalidTile;
			
			if(entryName.length() != 0)
			{
				buttonSetTile.displayString = entryName;
				
				if(networkState == Messages.online)
				{
					if(entryState == Messages.online)
						buttonSetTile.packedFGColour = colorGotTile;
					else
						buttonSetTile.packedFGColour = colorOfflineTile;
				}
				
				//Try to get ItemStack using item name
				item = BlockItemName.getItem(itemName, itemMeta);
				
				if(gid == selectedEntry)
				{
					selectedEntry = -1;
					hideTileList();
				}
			}
			
			selectedEntry = -1;
		}
		
		public void showTileList() {
				
			showTiles = true;
			buttonList.clear();
			
			this.guiLeft = (width-backgroundWidthTiles)/2;
			this.guiTop = (height-backgroundHeightTiles)/2;
			this.xSize = backgroundWidthTiles;
			this.ySize = backgroundHeightTiles;
			
			inputName = new GuiTextField(mc.fontRenderer, guiLeft+8, guiTop+6, 200, 20);
			inputName.setFocused(true); //Default focused
			
			tilesList = new GuiList(this, guiLeft + 8, guiTop + 30, 226, 119);
			tilesList.entryHeight = 24;
			
			GuiButton listScrollButton = new GuiButtonStretched(10, 0, 0, 0, 0, "");
			tilesList.setScrollbarButton(listScrollButton);
			buttonList.add(listScrollButton);
			
			buttonExit = new GuiButton(0, guiLeft + xSize - 25, guiTop + 5, 20, 20, "X");
			buttonSelect = new GuiButton(0, guiLeft + 5, guiTop + ySize - 25, 50, 20, "Select");
			buttonOpenTileGui = new GuiButton(0, guiLeft + 60, guiTop + ySize - 25, 50, 20, "Activate");
			buttonList.add(buttonExit);
			buttonList.add(buttonSelect);
			buttonList.add(buttonOpenTileGui);
			
			
			container.setPlayerInventoryPosition(-1000, -1000);
			container.showNetworkCard(false);
			
			//Tell server we want the list of Tiles
			if(!watchingTiles)
			{
				MessageBase messageWatch = new MessageCSWatchList(tile, thisId == PortableNetworkInterfaceGui.GUI_ID, true);
				messageWatch.sendToServer();
				watchingTiles = true;
			}
		}
		
		public void hideTileList() {
			showTiles = false;
			buttonList.clear();
			tilesList = null;
			
			this.guiLeft = (width-backgroundWidth)/2;
			this.guiTop = (height-backgroundHeight)/2;
			this.xSize = backgroundWidth;
			this.ySize = backgroundHeight;

			labelContainerName = new GuiLabel(tile.getInventoryName(), guiLeft + 5, guiTop + 5, 4210752);
			labelNetworkState = new GuiLabel(Messages.get(Messages.noNetwork), guiLeft +25, guiTop + 20, Messages.colorProblem);
			
			buttonSetTile = new GuiButton(0, guiLeft + 26, guiTop + 35, 145, 20, defaultNoTile);
			buttonSetTile.packedFGColour = colorInvalidTile;
			buttonOpenTileGui = new GuiButton(0, guiLeft + 5, guiTop + 90, 50, 20, "Activate");
			
			buttonList.add(buttonSetTile);
			buttonList.add(buttonOpenTileGui);

			container.setPlayerInventoryPosition(8, 140);
			container.showNetworkCard(true);
			
			//Tell server we are no longer interested in the Tiles list
			if(watchingTiles)
			{
				MessageBase messageWatch = new MessageCSWatchList(tile, thisId == PortableNetworkInterfaceGui.GUI_ID, false);
				messageWatch.sendToServer();
				watchingTiles = false;
			}
			
			updateGui();
		}
		
		//Add tile to TileList
		public void addTile(String entryName, String tileName, byte tileMeta, long gid, boolean active) {
			if(tilesList == null)
				return;
			tilesList.addEntry(new GuiListEntryTile(entryName, tileName, tileMeta, gid));
		}
		
		//Remove tile from TileList
		public void removeTile(String entryName) {
			if(tilesList == null)
				return;
			GuiListEntry entry = tilesList.getEntry(entryName);
			if(entry == null)
				return;
			tilesList.removeEntry(entry);
		}
		
		//Clear the tile list
		public void clearTileList() {
			if(tilesList == null)
				return;
			
		}
		
		//Whether to automatically update the list for every add/remove
		public void autoUpdateTileList(boolean auto) {
			if(tilesList == null)
				return;
			tilesList.noUpdate = auto;
		}
		
		//Update tile list to reflect changes
		public void updateTileList() {
			if(tilesList == null)
				return;
			tilesList.update();
			tilesList.updateScrollBar();
			tilesList.updateTop();
		}
		
		@Override
		public void setWorldAndResolution(Minecraft mc, int width, int height) {
			super.setWorldAndResolution(mc, width, height);
			
			Keyboard.enableRepeatEvents(true);
			
			//If re-opening, copy settings
			if(GuiHandler.oldGui instanceof GUI)
			{
				GUI oldGui = (GUI)GuiHandler.oldGui;
				GuiHandler.oldGui = null;
				
				showTiles = oldGui.showTiles;
				networkState = oldGui.networkState;
				entryState = oldGui.entryState;
				entryName = oldGui.entryName;
				gid = oldGui.gid;
				itemName = oldGui.itemName;
			}
			
			if(showTiles)
				showTileList();
			else
				hideTileList();
		}
		
		@Override
	    public void onGuiClosed()
	    {
	        Keyboard.enableRepeatEvents(false);
	        if(watchingTiles)
	        {
				//Tell server we are no longer interested in the Tiles list
				MessageBase messageWatch = new MessageCSWatchList(tile, thisId == PortableNetworkInterfaceGui.GUI_ID, false);
				messageWatch.sendToServer();
				watchingTiles = false;
	        }
	    }
	
		@Override
		public void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY) {
			if(showTiles)
			{
				
				this.mc.getTextureManager().bindTexture(backgroundImageTiles);
				this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, backgroundWidthTiles, backgroundHeightTiles);
				
				inputName.drawTextBox();
				
				return;
			}
			
			this.mc.getTextureManager().bindTexture(backgroundImage);
			this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, backgroundWidth, backgroundHeight);
			
			labelContainerName.draw(fontRendererObj);
			labelNetworkState.draw(fontRendererObj);
		}
		
		@Override
		public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
			super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
			
			if(showTiles)
				tilesList.draw(mc);
			else
			{	
				if(item != null)
				{
					RenderHelper.disableStandardItemLighting(); //Put us in a known render state
					itemRender.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), item, guiLeft + 6, guiTop + 38);
					RenderHelper.disableStandardItemLighting(); //Put us in a known render state
				}
			}
		}
		
		@Override
		protected void actionPerformed(GuiButton button) {
			
			currentButton = button;
			
			if(!showTiles)
			{
				if(button == buttonSetTile)
				{
					showTileList();
				}
				else if(button == buttonOpenTileGui)
				{
					if(networkState == Messages.online && entryState == Messages.online)
					{
						GuiHandler.setPreviousTileGui(this, thisId, tile);
						MessageBase messageActivate = new MessageActivate(tile, gid, thisId == PortableNetworkInterfaceGui.GUI_ID);
						messageActivate.sendToServer();
					}
				}
			}
			else
			{
				if(button == buttonExit)
				{
					hideTileList();
				}
				else if(button == buttonSelect)
				{
					GuiListEntry entry = tilesList.selectedEntry;
					MessageBase messageUpdate;
					if(entry == null || !(entry instanceof GuiListEntryTile))
					{
						selectedEntry = -1;
						messageUpdate = new MessageCSInterfaceUpdate(tile, thisId == PortableNetworkInterfaceGui.GUI_ID, -1);
					}
					else
					{
						GuiListEntryTile tileEntry = (GuiListEntryTile)entry;
						selectedEntry = tileEntry.gid;
						messageUpdate = new MessageCSInterfaceUpdate(tile, thisId == PortableNetworkInterfaceGui.GUI_ID, tileEntry.gid);
					}
					messageUpdate.sendToServer();
				}
				else if(button == buttonOpenTileGui)
				{
					if(networkState == Messages.online && entryState == Messages.online)
					{
						GuiListEntry entry = tilesList.selectedEntry;
						if(entry != null)
						{
							if(entry instanceof GuiListEntryTile)
							{
								GuiListEntryTile tileEntry = (GuiListEntryTile)entry;
								GuiHandler.setPreviousTileGui(this, thisId, tile);
								MessageBase messageActivate = new MessageActivate(tile, tileEntry.gid, thisId == PortableNetworkInterfaceGui.GUI_ID);
								messageActivate.sendToServer();
							}
						}
					}
				}
			}
		}
		
		@Override
		public void updateScreen() {
			super.updateScreen();
			if(showTiles)
			{
				inputName.updateCursorCounter();
			}
		}
		
		@Override
		protected void keyTyped(char eventChar, int eventKey) {
			if(showTiles)
			{
				if(inputName.textboxKeyTyped(eventChar, eventKey))
				{
					tilesList.setSearchString(inputName.getText());
					return;
				}
			}
			
			//Stop it from re-opening this gui
	        if ((eventKey == 1 || eventKey == this.mc.gameSettings.keyBindInventory.getKeyCode()) && GuiHandler.previousGui == this)
	        	GuiHandler.previousGui = null;
	        
			super.keyTyped(eventChar, eventKey);
		}
		
		@Override
		protected void mouseClicked(int x, int y, int event) {
			if(showTiles)
			{
				inputName.mouseClicked(x, y, event);
				
				if(x >= tilesList.posX && x <= tilesList.posX + tilesList.width)
				{
					if(y >= tilesList.posY && y <= tilesList.posY + tilesList.height)
					{
						if(tilesList.onMouseClick(x, y)) {
							//Entry selection changed, update GUI
						}
					}
				}
			}
			super.mouseClicked(x, y, event);
		}
		
		@Override
		protected void mouseMovedOrUp(int x, int y, int event) {
			super.mouseMovedOrUp(x, y, event);
			
			if(event == 0) //Mouse Up
				currentButton = null;
		}
		
		@Override
		protected void mouseClickMove(int x, int y,
				int event, long time) {
			super.mouseClickMove(x, y, event, time);
			
			if(showTiles)
			{
				//Pass on to list for scrollbar
				tilesList.onButtonDrag(currentButton, x, y);
			}
		}
		
		@Override
		public void handleMouseInput() {
			super.handleMouseInput();
			
			if(showTiles)
			{
				//Scroll Wheel
				int scroll = Mouse.getDWheel();
                ModLog.logger.info("Scroll: " + scroll);
				
				if(scroll != 0)
					tilesList.onScroll(scroll);
			}
		}
	
		@Override
		public boolean doesGuiPauseGame() {
			return false;
		}
	}
	
}
