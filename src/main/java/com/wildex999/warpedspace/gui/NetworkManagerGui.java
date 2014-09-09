package com.wildex999.warpedspace.gui;

import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.GuiHandler;
import com.wildex999.warpedspace.Messages;
import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.gui.elements.GuiButtonStretched;
import com.wildex999.warpedspace.gui.elements.GuiLabel;
import com.wildex999.warpedspace.gui.elements.GuiList;
import com.wildex999.warpedspace.gui.elements.GuiListEntry;
import com.wildex999.warpedspace.gui.interfaces.IGuiWatchers;
import com.wildex999.warpedspace.gui.interfaces.INetworkListGui;
import com.wildex999.warpedspace.inventory.ControllerContainer;
import com.wildex999.warpedspace.inventory.NetworkManagerContainer;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.MessageNetworkManagerUpdate;
import com.wildex999.warpedspace.networking.warpednetwork.MessageCSNetworkCreate;
import com.wildex999.warpedspace.networking.warpednetwork.MessageCSNetworkUpdate;
import com.wildex999.warpedspace.tiles.TileNetworkManager;
import com.wildex999.warpedspace.tiles.TileNetworkController;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class NetworkManagerGui implements IGuiHandler
{
	public static final int GUI_ID = GuiHandler.getNextGuiID();

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileNetworkManager inventory = (TileNetworkManager)world.getTileEntity(x, y, z);
		return new NetworkManagerContainer(player.inventory, inventory);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,int x, int y, int z) {
		TileEntity tile = world.getTileEntity(x, y, z);
		return new GUI(player, (TileNetworkManager)tile);
	}
	
	//==GUI==
	public static class GUI extends GuiContainer implements INetworkListGui {
		public static final ResourceLocation backgroundImage = new ResourceLocation(WarpedSpace.MODID, "textures/gui/network_manager.png");
		private static final short backgroundWidth = 256;
		private static final short backgroundHeight = 240;
		
		protected GuiButton currentButton;
		protected TileNetworkManager tile;
		
		private GuiList networkList;
		private GuiLabel labelWriteNetwork;
		private GuiTextField inputNetworkName;
		private GuiButton buttonWriteNetworkCard;
		private GuiButton buttonSetNetwork;
		private GuiButton buttonNetwork;
		private GuiButton buttonClear;
		private GuiButton buttonDestroy;
		
		private final String writeEnabled = "-->";
		private final String writeDisabled = "|";
		private final float writeNetworkScale = 0.9F;
		private final float writeNetworkScaleReverse = 1.11F;
		
		private boolean buttonCreate = true; //When true, buttonNetwork is set to create, else it's set to rename
		
		//Used to check if update includes a network we just created(Whether to select it)
		private String createdNetwork = null;
		
		public GUI(EntityPlayer player, TileNetworkManager tile) {
			super(new NetworkManagerContainer(player.inventory, tile));
			this.tile = tile;
		}
		
		@Override
		public void setWorldAndResolution(Minecraft mc, int width, int height) {
			super.setWorldAndResolution(mc, width, height);
			
			Keyboard.enableRepeatEvents(true);
			
			this.guiLeft = (width-backgroundWidth)/2;
			this.guiTop = (height-backgroundHeight)/2;
			this.xSize = backgroundWidth;
			this.ySize = backgroundHeight;

			networkList = new GuiList(this, guiLeft + 8, guiTop + 30, 220, 83);
			
			GuiButton listScrollButton = new GuiButtonStretched(10, 0, 0, 0, 0, "");
			networkList.setScrollbarButton(listScrollButton);
			buttonList.add(listScrollButton);
			
			//Populate Network list
			loadNetworkList(true);
			
			
			String oldNetworkName = "";
			if(inputNetworkName != null)
				oldNetworkName = inputNetworkName.getText();
			inputNetworkName = new GuiTextField(mc.fontRenderer, guiLeft+8, guiTop+6, 200, 20);
			inputNetworkName.setText(oldNetworkName);

			labelWriteNetwork = new GuiLabel(Messages.get(Messages.noNetwork), (int) ((guiLeft+84)*writeNetworkScaleReverse), (int) ((guiTop+128)*writeNetworkScaleReverse), 0xFF000000);
			labelWriteNetwork.centered = false;
			
			buttonWriteNetworkCard = new GuiButtonStretched(14, guiLeft+28, guiTop+119, 32, 15, writeDisabled);
			buttonSetNetwork = new GuiButtonStretched(15, guiLeft+xSize-164, guiTop+112, 128, 13, "v Set to write v");
			buttonList.add(buttonWriteNetworkCard);
			buttonList.add(buttonSetNetwork);
			
			buttonNetwork = new GuiButton(11, guiLeft + xSize - 46, guiTop + 6, 16, 20, "C");
			buttonClear = new GuiButton(12, guiLeft + xSize - 25, guiTop + 6, 20, 20, "Clr");
			buttonDestroy = new GuiButton(13, guiLeft + xSize - 13, guiTop + 46, 10, 20, "X");
			buttonList.add(buttonNetwork);
			buttonList.add(buttonClear);
			buttonList.add(buttonDestroy);
			
			buttonDestroy.enabled = false;
			
		}
		
		@Override
	    public void onGuiClosed()
	    {
	        Keyboard.enableRepeatEvents(false);
	    }
		
		@Override
		public void updateScreen() {
			super.updateScreen();
			
			inputNetworkName.updateCursorCounter();
			
			//Check Tile for current write selection and button state
			if(tile == null)
				return;
			
			if(tile.currentNetwork != null)
				labelWriteNetwork.label = tile.currentNetwork.name;
			else
				labelWriteNetwork.label = Messages.get(Messages.noNetwork);
			
			setWriteEnabled(tile.writeNetworkCard);
		}
		
		public void loadNetworkList(boolean clear) {
			if(clear)
				networkList.clear();
			
			CoreNetworkManager networkManager = CoreNetworkManager.clientNetworkManager;
			networkList.noUpdate = true;
			for(Map.Entry<Integer, WarpedNetwork> network : networkManager.networks.entrySet())
			{
				networkList.addEntry(new GuiListEntry(network.getValue().name));
			}
			networkList.noUpdate = false;
			networkList.update();
		}
		
		@Override
		public void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY) {
			//Draw list first, so texture overlap overflow.
			networkList.draw(mc);
			
			this.mc.getTextureManager().bindTexture(backgroundImage);		
			this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, backgroundWidth, backgroundHeight);
			
			inputNetworkName.drawTextBox();
			GL11.glScalef(writeNetworkScale, writeNetworkScale, 1);
			labelWriteNetwork.draw(fontRendererObj);
			GL11.glScalef(writeNetworkScaleReverse, writeNetworkScaleReverse, 1);
		}
		
		@Override
		protected void keyTyped(char eventChar, int eventKey) {
			if(inputNetworkName.textboxKeyTyped(eventChar, eventKey))
			{
				networkList.setSearchString(inputNetworkName.getText());
				return;
			}
			
			super.keyTyped(eventChar, eventKey);
		}

		@Override
		public boolean doesGuiPauseGame() {
			return false;
		}
		
		public void addButton(GuiButton button) {
			buttonList.add(button);
		}
		
		public void setWriteEnabled(boolean write) {
			if(write)
				buttonWriteNetworkCard.displayString = writeEnabled;
			else
				buttonWriteNetworkCard.displayString = writeDisabled;
		}
		
		@Override
		protected void actionPerformed(GuiButton button) {
			super.actionPerformed(button);
			currentButton = button;
			
			CoreNetworkManager networkManager = CoreNetworkManager.clientNetworkManager;
			
			if(button == buttonNetwork)
			{
				if(buttonCreate) 
				{
					String networkName = inputNetworkName.getText();
					if(networkName.length() == 0)
						return;
					
					//Do some client side checking before bothering the server
					if(networkList.getEntry(networkName) != null)
						return; //TODO: Notify somehow that the name is taken(Text field blink red?)(Maybe select existing entry instead?)
					
					createdNetwork = networkName;
					
					MessageBase createMessage = new MessageCSNetworkCreate(networkName);
					createMessage.sendToServer();
					
				} else 
				{
					GuiListEntry renameEntry = networkList.selectedEntry;
					if(renameEntry == null)
					{
						setNetworkButton(true);
						return;
					}
					if(inputNetworkName.getText().length() == 0)
						return;
					if(inputNetworkName.getText().equals(renameEntry.name))
						return;
					
					Integer networkId = networkManager.networkNames.get(renameEntry.name);
					if(networkId == null)
						return;
					
					MessageBase renameMessage = new MessageCSNetworkUpdate(networkId, inputNetworkName.getText(), false);
					renameMessage.sendToServer();
				}
			} else if(button == buttonClear)
			{
				inputNetworkName.setText("");
			} else if(button == buttonDestroy)
			{
				GuiListEntry removeEntry = networkList.selectedEntry;
				if(removeEntry == null)
					return;
				
				Integer networkId = networkManager.networkNames.get(removeEntry.name);
				if(networkId == null)
					return;
				
				MessageBase removeMessage = new MessageCSNetworkUpdate(networkId);
				removeMessage.sendToServer();
			} else if(button == buttonSetNetwork)
			{
				if(tile != null)
				{
					WarpedNetwork network = null;
					GuiListEntry entry = networkList.selectedEntry;
					if(entry != null)
						network = networkManager.networks.get(networkManager.networkNames.get(entry.name));
					MessageBase messageUpdate = new MessageNetworkManagerUpdate(tile.writeNetworkCard, network, tile);
					messageUpdate.sendToServer();
				}
			} else if(button == buttonWriteNetworkCard)
			{
				if(tile != null)
				{
					MessageBase messageUpdate = new MessageNetworkManagerUpdate(!tile.writeNetworkCard, tile.currentNetwork, tile);
					messageUpdate.sendToServer();
				}
			}
		}
		
		@Override
		protected void mouseClicked(int x, int y, int event) {
			super.mouseClicked(x, y, event);
			
			inputNetworkName.mouseClicked(x, y, event);
			
			if(x >= networkList.posX && x <= networkList.posX + networkList.width)
			{
				if(y >= networkList.posY && y <= networkList.posY + networkList.height)
				{
					if(networkList.onMouseClick(x, y)) {
						//Entry selection changed, update GUI
						onNetworkEntryUpdate();
						createdNetwork = null;
					}
				}
			}
			

		}
		
		protected void onNetworkEntryUpdate() {
			if(networkList.selectedEntry == null)
			{
				setNetworkButton(true);
			}
			else
			{
				setNetworkButton(false);
				buttonDestroy.yPosition = networkList.getSelectedEntryY() - 2;
				inputNetworkName.setText(networkList.selectedEntry.name);
			}
		}
		
		protected void setNetworkButton(boolean create) {
			if(create)
			{
				buttonCreate = true;
				buttonNetwork.displayString = "C";
				buttonDestroy.enabled = false;
			}
			else
			{
				buttonCreate = false;
				buttonNetwork.displayString = "R";
				buttonDestroy.enabled = true;
			}
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
			//Pass on to list for scrollbar
			networkList.onButtonDrag(currentButton, x, y);
		}
		
		@Override
		public void handleMouseInput() {
			super.handleMouseInput();
			
			//Scroll Wheel
			int scroll = Mouse.getDWheel();
			
			if(scroll != 0)
				networkList.onScroll(scroll);
		}
		
		public GuiListEntry getEntryFromNetworkId(int id) {
			CoreNetworkManager networkManager = CoreNetworkManager.clientNetworkManager;
			
			
			WarpedNetwork network = networkManager.networks.get(id);
			if(network == null)
			{
				//Out of sync
				//TODO: Request full new list
				ModLog.logger.warn("Network List out of sync, network not found: " + id);
				return null;
			}
			
			GuiListEntry entry = networkList.getEntry(network.name);
			if(entry == null)
			{
				//Out of sync
				//TODO: Request full new list
				ModLog.logger.warn("Network GUI list out of sync, network name not found in list: " + network.name);
				return null;
			}
			
			return entry;
		}

		
		//Updates from Server
		@Override
		public void networkCreated(int id, String name, String owner) {
			GuiListEntry newEntry = new GuiListEntry(name);
			boolean added = networkList.addEntry(newEntry);
			
			if(createdNetwork == null || !createdNetwork.equals(name))
				return;
			
			if(added)
			{
				networkList.setSelectedEntry(newEntry);
				networkList.scrollToSelected();
				onNetworkEntryUpdate();
			}
			
			createdNetwork = null;
				
		}

		@Override
		public void networkRename(int id, String newName) {
			GuiListEntry entry = getEntryFromNetworkId(id);
			if(entry == null)
			{
				ModLog.logger.warn("Entry not found during rename task from server. Id: " + id + " newName: " + newName);
				return;
			}
			
			boolean wasSelected = false;
			if(networkList.selectedEntry == entry)
				wasSelected = true;
			
			if(!networkList.renameEntry(entry, newName))
			{
				ModLog.logger.warn("Rename failed for " + entry.name + " to " + newName);
				return;
			}
			
			//Make sure we scroll to it if selected
			if(wasSelected)
			{
				networkList.setSelectedEntry(entry);
				networkList.scrollToSelected();
				onNetworkEntryUpdate();
			}
		}

		@Override
		public void networkRemoved(int id) {
			GuiListEntry entry = getEntryFromNetworkId(id);
			if(entry == null)
				return;
			
			networkList.removeEntry(entry);
			onNetworkEntryUpdate();
		}

		@Override
		public void reloadNetworkList() {
			loadNetworkList(true);
		}
	}
	
}


