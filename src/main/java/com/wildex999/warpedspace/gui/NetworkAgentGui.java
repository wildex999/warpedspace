package com.wildex999.warpedspace.gui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.wildex999.utils.BlockItemName;
import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.GuiHandler;
import com.wildex999.warpedspace.Messages;
import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.gui.WarpedControllerGui.GUI;
import com.wildex999.warpedspace.gui.elements.GuiButtonCustom;
import com.wildex999.warpedspace.gui.elements.GuiLabel;
import com.wildex999.warpedspace.inventory.ControllerContainer;
import com.wildex999.warpedspace.inventory.NetworkAgentContainer;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.MessageNetworkAgentInput;
import com.wildex999.warpedspace.networking.MessageNetworkAgentUpdate;
import com.wildex999.warpedspace.tiles.TileNetworkAgent;
import com.wildex999.warpedspace.tiles.TileNetworkController;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/*
 * TODO: Option to clear all Interfaces stored for side(clear old gid)
 */

public class NetworkAgentGui implements IGuiHandler
{
	public static final int GUI_ID = GuiHandler.getNextGuiID();

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileNetworkAgent inventory = (TileNetworkAgent)world.getTileEntity(x, y, z);
		return new NetworkAgentContainer(player.inventory, inventory);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,int x, int y, int z) {
		TileEntity tile = world.getTileEntity(x, y, z);
		return new GUI(player, (TileNetworkAgent)tile);
	}
	
	public static class TileState {
		public String name;
		public String tileName;
		public byte tileMeta;
		public boolean active;
		
		public TileState(String name, String tileName, byte tileMeta, boolean active) {
			this.name = name;
			this.tileName = tileName;
			this.tileMeta = tileMeta;
			this.active = active;
		}
	}

	public static class GUI extends GuiContainer {
		public static final ResourceLocation backgroundImage = new ResourceLocation(WarpedSpace.MODID, "textures/gui/network_agent.png");
		public static final ResourceLocation cubesImage = new ResourceLocation(WarpedSpace.MODID, "textures/gui/cubes.png");
		public static final ResourceLocation playerInventoryImage = new ResourceLocation(WarpedSpace.MODID, "textures/gui/player_inventory.png");
		
		private static final short backgroundWidth = 256;
		private static final short backgroundHeight = 207;
		
		private static final short tabPlayerInventoryWidth = 30;
		private static final short tabPlayerInventoryWidthHidden = tabPlayerInventoryWidth-3;
		private static final short tabPlayerInventoryHeight = 33;
		
		private static final short playerInventoryWidth = 176;
		private static final short playerInventoryHeight = 91;

		private List<GuiTextField> inputList;
		private List<ItemStack> renderItems;
		private GuiTextField currentInput;
		private String currentInputOriginalString; 
		private int currentInputOriginalColor;
		
		private TileNetworkAgent agent;
		private boolean showPlayerInventory = false;

		
		//GUI Elements
		private GuiLabel labelContainerName;
		private GuiLabel labelNodeName;
		private GuiLabel labelConnectionState;
		
		private GuiButtonCustom tabPlayerInventory;
		
		private int colorAccepted = 0xFF009900; //Tile name has been accepted
		private int colorChanged = 0xFFFF6600; //Tile name not yet accepted(Changed, or network unavailable)
		private int colorDenied = 0xFFFF0000;  //Tile name was denied(Already in use, or tile blacklisted)
		
		public GUI(EntityPlayer player, TileNetworkAgent tile)
		{
			super(new NetworkAgentContainer(player.inventory, tile));
			agent = tile;
			inputList = new ArrayList<GuiTextField>();
			renderItems = new ArrayList<ItemStack>(TileNetworkAgent.sideCount-1);
			
			for(int i = 0; i< TileNetworkAgent.sideCount-1; i++)
				renderItems.add(null);
		}
		
		//Update entries from server
		public void updateEntries(int networkState, List<TileState> tiles) {
			//Order/Direction defined by TileNetworkAgent Tile Direction Index(North = 0 etc.)
			GuiTextField field;
			for(int i = 0; i < TileNetworkAgent.sideCount; i++)
			{
				field = inputList.get(i);
				
				if(field == currentInput) //Don't interrupt current edit
				{
					currentInputOriginalString = tiles.get(i).name;
					if(networkState != Messages.online)
						currentInputOriginalColor = colorChanged;
					else if(!tiles.get(i).active)
						currentInputOriginalColor = colorDenied;
					else
						currentInputOriginalColor = colorAccepted;
					
					continue;
				}
				
				if(i < TileNetworkAgent.sideCount-1)
				{
					TileState tile = tiles.get(i);
					renderItems.set(i,BlockItemName.getItem(tile.tileName, tile.tileMeta));
				}
					
				
				field.setText(tiles.get(i).name);

				if(networkState != Messages.online)
					field.setTextColor(colorChanged);
				else if(!tiles.get(i).active)
					field.setTextColor(colorDenied);
				else
					field.setTextColor(colorAccepted);

			}
			
			labelConnectionState.label = Messages.get(networkState);
			if(networkState == Messages.online)
				labelConnectionState.color = Messages.colorOk;
			else
				labelConnectionState.color = Messages.colorProblem;
		}
		
		//Setting the current input, reverting previous input and storing old state
		public void setCurrentInput(GuiTextField input) {
			if(currentInput != null)
			{
				currentInput.setText(currentInputOriginalString);
				currentInput.setTextColor(currentInputOriginalColor);
				currentInput.setFocused(false);
			}
			
			currentInput = input;
			
			if(currentInput == null)
				return;
			
			currentInputOriginalString = currentInput.getText();
			
			//Someone forgot a getTextColor method
			try {
				Field f = currentInput.getClass().getDeclaredField("enabledColor");
				f.setAccessible(true);
				currentInputOriginalColor = (int)f.getInt(currentInput);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			currentInput.setTextColor(colorChanged);
			
		}
		
		@Override
	    public void updateScreen()
	    {
			for(GuiTextField input : inputList)
				input.updateCursorCounter();
	    }
		
		@Override
	    public void onGuiClosed()
	    {
	        Keyboard.enableRepeatEvents(false);
	    }
		
		@Override
		public void setWorldAndResolution(Minecraft mc, int width, int height) {
			super.setWorldAndResolution(mc, width, height);
			
			Keyboard.enableRepeatEvents(true);
			
			this.guiLeft = (width-backgroundWidth)/2;
			this.guiTop = (height-backgroundHeight)/2;
			this.xSize = backgroundWidth;
			this.ySize = backgroundHeight;
			
			labelContainerName = new GuiLabel(agent.getInventoryName(), this.guiLeft + 5, this.guiTop + 5, 4210752);
			labelNodeName = new GuiLabel("Node Name:", this.guiLeft + 5, this.guiTop + 42, 4210752);
			labelConnectionState = new GuiLabel(Messages.get(Messages.noNetwork), this.guiLeft + 28, this.guiTop + 20, Messages.colorProblem);
			
			tabPlayerInventory = new GuiButtonCustom(0, this.guiLeft-(tabPlayerInventoryWidthHidden), this.guiTop + 64, 
					0, 91, playerInventoryImage, tabPlayerInventoryWidthHidden, tabPlayerInventoryHeight, null);
			tabPlayerInventory.hoverOffsetX = tabPlayerInventoryWidth;
			
			buttonList.clear();
			buttonList.add(tabPlayerInventory);
			
			int inputHeight = 24;
			
			inputList.clear();
			inputList.add(new GuiTextField(fontRendererObj, this.guiLeft + 65, this.guiTop + 36 + (inputHeight*1), 170, 20)); //North
			inputList.add(new GuiTextField(fontRendererObj, this.guiLeft + 65, this.guiTop + 36 + (inputHeight*2), 170, 20)); //South
			inputList.add(new GuiTextField(fontRendererObj, this.guiLeft + 65, this.guiTop + 36 + (inputHeight*3), 170, 20)); //West
			inputList.add(new GuiTextField(fontRendererObj, this.guiLeft + 65, this.guiTop + 36 + (inputHeight*4), 170, 20)); //East
			inputList.add(new GuiTextField(fontRendererObj, this.guiLeft + 65, this.guiTop + 36 + (inputHeight*5), 170, 20)); //Top
			inputList.add(new GuiTextField(fontRendererObj, this.guiLeft + 65, this.guiTop + 36 + (inputHeight*6), 170, 20)); //Bottom
			
			inputList.add(new GuiTextField(fontRendererObj, this.guiLeft + 65, this.guiTop + 36, 170, 20)); //Node Name
			
			NetworkAgentContainer container = (NetworkAgentContainer)this.inventorySlots;
			container.setPlayerInventoryPosition(-1000, -1000);
			
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY) {
			this.mc.getTextureManager().bindTexture(backgroundImage);
			this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, backgroundWidth, backgroundHeight);
			
			//Render cubes
			this.mc.getTextureManager().bindTexture(cubesImage);
			int filterPrev = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			
			int offsetY = 32;
			int height = 24;
			//West
			this.drawTexturedModalRectUV(this.guiLeft+5, this.guiTop+offsetY+(3*height), 128, 0, 192, 64, 24, height);
			//East
			this.drawTexturedModalRectUV(this.guiLeft+5, this.guiTop+offsetY+(4*height), 128, 64, 192, 128, 24, height);
			//South
			this.drawTexturedModalRectUV(this.guiLeft+5, this.guiTop+offsetY+(2*height), 0, 0, 64, 64, 24, height);
			//North
			this.drawTexturedModalRectUV(this.guiLeft+5, this.guiTop+offsetY+(1*height), 64, 0, 128, 64, 24, height);
			//Top
			this.drawTexturedModalRectUV(this.guiLeft+5, this.guiTop+offsetY+(5*height), 0, 64, 64, 128, 24, height);
			//Bottom
			this.drawTexturedModalRectUV(this.guiLeft+5, this.guiTop+offsetY+(6*height), 64, 64, 128, 128, 24, height);
			
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filterPrev);

			//Draw Tile Entities
			int offset = 0;
			for(ItemStack item : renderItems)
			{
				offset++;
				if(item == null || (offset <= 4 && showPlayerInventory))
					continue;
				RenderHelper.disableStandardItemLighting(); //Put us in a known render state
				itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), item, guiLeft + 36, guiTop + 38 + (offset*24));
				RenderHelper.disableStandardItemLighting(); //Put us in a known render state
			}	
			
			//Draw labels
			labelContainerName.draw(fontRendererObj);
			labelNodeName.draw(fontRendererObj);
			labelConnectionState.draw(fontRendererObj);
			
			for(GuiTextField input : inputList)
				input.drawTextBox();
			
			//Player inventory
			if(showPlayerInventory)
			{
				//Previous rendering sometimes leaves the color values at dark/black, 
				//causing the inventory texture to render weird.
				GL11.glColor4f(1F,1F,1F,1F);
				
				tabPlayerInventory.width = tabPlayerInventory.texWidth = tabPlayerInventoryWidth;
				this.mc.getTextureManager().bindTexture(playerInventoryImage);
				this.drawTexturedModalRect(this.guiLeft, this.guiTop + 60, 0, 0, playerInventoryWidth, playerInventoryHeight);
			}
			else
				tabPlayerInventory.width = tabPlayerInventory.texWidth = tabPlayerInventoryWidthHidden;
			

			
			
		}
		
		@Override
		protected void actionPerformed(GuiButton button) {
			super.actionPerformed(button);
			
			if(button == tabPlayerInventory)
			{
				//Show/Hide inventory
				showPlayerInventory = !showPlayerInventory;
				
				//Disable/Enable input and buttons under inventory
				NetworkAgentContainer container = (NetworkAgentContainer)this.inventorySlots;
				if(showPlayerInventory)
					container.setPlayerInventoryPosition(8, 69);
				else
					container.setPlayerInventoryPosition(-1000, -1000);

			}
		}
		
		
		@Override
		public boolean doesGuiPauseGame() {
			return false;
		}
		
		@Override
		protected void keyTyped(char eventChar, int eventKey) {
			if(showPlayerInventory)
			{
				super.keyTyped(eventChar, eventKey);
				return;
			}
			
			//Send textfield on <Enter>
			if(eventKey == 28 && currentInput != null)
			{
				int index = inputList.indexOf(currentInput);
				ModLog.logger.info("Send update for index: " + index +  " New name: " + currentInput.getText());

				//Mark the edited input as 'changed'. Reply from server should finalize the color.
				currentInputOriginalColor = colorChanged;
				currentInputOriginalString = currentInput.getText();

				//TODO: Send change to server
				MessageBase messageInput = new MessageNetworkAgentInput(agent, index, currentInput.getText());
				messageInput.sendToServer();
				
				setCurrentInput(null);
				
				return;
			}
			
			//Input fields
			for(GuiTextField input : inputList)
				if(input.textboxKeyTyped(eventChar, eventKey))
					return;
			
			super.keyTyped(eventChar, eventKey);
			
		}
		
		@Override
		protected void mouseClicked(int x, int y, int event) {
			super.mouseClicked(x, y, event);
			
			if(showPlayerInventory)
				return;
			

			boolean wasFocused = false;
			for(GuiTextField input : inputList)
			{
				input.mouseClicked(x, y, event);
				if(input.isFocused())
				{
					wasFocused = true;
					if(currentInput != input)
						setCurrentInput(input);
				}
			}
			if(!wasFocused)
				setCurrentInput(null);
			
		}
		
		//Draw textured rect, specifying the texture size independent of rect size.
		public void drawTexturedModalRectUV(int x, int y, int u1, int v1, int u2, int v2, int width, int height) {
	        float f = 0.00390625F;
	        float f1 = 0.00390625F;
	        Tessellator tessellator = Tessellator.instance;
	        tessellator.startDrawingQuads();
	        tessellator.addVertexWithUV((double)(x + 0), (double)(y + height), (double)this.zLevel, (double)((float)(u1) * f), (double)((float)(v2) * f1));
	        tessellator.addVertexWithUV((double)(x + width), (double)(y + height), (double)this.zLevel, (double)((float)(u2) * f), (double)((float)(v2) * f1));
	        tessellator.addVertexWithUV((double)(x + width), (double)(y + 0), (double)this.zLevel, (double)((float)(u2) * f), (double)((float)(v1) * f1));
	        tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), (double)this.zLevel, (double)((float)(u1) * f), (double)((float)(v1) * f1));
	        tessellator.draw();
		}
	
	}
}