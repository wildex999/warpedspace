package com.wildex999.warpedspace.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.GuiHandler;
import com.wildex999.warpedspace.Messages;
import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.gui.NetworkManagerGui.GUI;
import com.wildex999.warpedspace.gui.elements.GuiLabel;
import com.wildex999.warpedspace.inventory.ControllerContainer;
import com.wildex999.warpedspace.inventory.NetworkContainer;
import com.wildex999.warpedspace.inventory.NetworkManagerContainer;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.tiles.TileNetworkManager;
import com.wildex999.warpedspace.tiles.TileNetworkController;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.common.network.IGuiHandler;

public class WarpedControllerGui implements IGuiHandler
{
	public static final int GUI_ID = GuiHandler.getNextGuiID();
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileNetworkController inventory = (TileNetworkController)world.getTileEntity(x, y, z);
		return new ControllerContainer(player.inventory, inventory);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,int x, int y, int z) {
		TileEntity tile = world.getTileEntity(x, y, z);
		return new GUI(player, (TileNetworkController)tile);
	}

	public static class GUI extends GuiContainer {
	
		public static final ResourceLocation backgroundImage = new ResourceLocation(WarpedSpace.MODID, "textures/gui/node_base.png");
		
		private static final short backgroundWidth = 175;
		private static final short backgroundHeight = 221;
		
		private TileNetworkController controller;
		private ControllerContainer container;
		private EntityPlayer player;
	
		GuiLabel labelContainerName;
		GuiLabel labelNetworkName;
		GuiLabel labelNodes;
		GuiLabel labelTiles;
		GuiLabel labelRelays;
		
		GuiLabel labelNodesValue;
		GuiLabel labelTilesValue;
		GuiLabel labelRelaysValue;
		
		private String networkName;
		private Integer nodes, tiles, relays;
	
		public GUI(EntityPlayer player, TileNetworkController tile) {
			//Slot positions set in ControllerContainer
			super(new ControllerContainer(player.inventory, tile));
			controller = tile;
			container = (ControllerContainer)this.inventorySlots;
			this.player = player;
			
			if(tile == null)
				player.closeScreen();
		}
		
		//Called when new packet is received from server
		public void updateNetworkInfo(String networkName, Integer nodes, Integer tiles, Integer relays) {
			this.networkName = networkName;
			this.nodes = nodes;
			this.tiles = tiles;
			this.relays = relays;
			
			updateValues();
		}
		
		public void updateValues() {
			if(networkName == null)
				return;
			
			labelNodesValue.label = nodes.toString();
			labelTilesValue.label = tiles.toString();
			labelRelaysValue.label = relays.toString();
			
			if(!networkName.equals(""))
			{
				labelNetworkName.label = networkName;
				labelNetworkName.color = Messages.colorOk;
			}
			else
			{
				labelNetworkName.label = Messages.get(Messages.noNetwork);
				labelNetworkName.color = Messages.colorProblem;
			}
		}
		
		@Override
		public void setWorldAndResolution(Minecraft mc, int width, int height) {
			super.setWorldAndResolution(mc, width, height);
			
			this.guiLeft = (width-backgroundWidth)/2;
			this.guiTop = (height-backgroundHeight)/2;
			this.xSize = backgroundWidth;
			this.ySize = backgroundHeight;
			
			labelContainerName = new GuiLabel(controller.getInventoryName(), guiLeft + 5, guiTop + 5, 4210752);
			
			labelNetworkName = new GuiLabel("<No Network>", guiLeft + 5, guiTop + 40, Messages.colorProblem);
			labelNodes = new GuiLabel("Network Nodes:", guiLeft + 5, guiTop + 50, 4210752);
			labelTiles = new GuiLabel("TileEntities:", guiLeft + 5, guiTop + 60, 4210752);
			labelRelays = new GuiLabel("Network Relays:", guiLeft + 5, guiTop + 70, 4210752);
			
			labelNodesValue = new GuiLabel("0", guiLeft + 85, guiTop + 50, 4210752);
			labelTilesValue = new GuiLabel("0", guiLeft + 85, guiTop + 60, 4210752);
			labelRelaysValue = new GuiLabel("0", guiLeft + 85, guiTop + 70, 4210752);
			
			updateValues();
		}
	
		@Override
		public void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY) {
			
			this.mc.getTextureManager().bindTexture(backgroundImage);
	
			
			this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, backgroundWidth, backgroundHeight);
			labelContainerName.draw(fontRendererObj);
			labelNetworkName.draw(fontRendererObj);
			labelNodes.draw(fontRendererObj);
			labelTiles.draw(fontRendererObj);
			labelRelays.draw(fontRendererObj);
			
			labelNodesValue.draw(fontRendererObj);
			labelTilesValue.draw(fontRendererObj);
			labelRelaysValue.draw(fontRendererObj);
		}
	
		@Override
		public boolean doesGuiPauseGame() {
			return false;
		}
		
	}
}
