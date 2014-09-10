package com.wildex999.warpedspace.gui;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import com.wildex999.warpedspace.GuiHandler;
import com.wildex999.warpedspace.Messages;
import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.blocks.relay.BlockRelayBase;
import com.wildex999.warpedspace.gui.elements.GuiLabel;
import com.wildex999.warpedspace.inventory.ControllerContainer;
import com.wildex999.warpedspace.tiles.TileBasicNetworkRelay;
import com.wildex999.warpedspace.tiles.TileNetworkController;

import cpw.mods.fml.common.network.IGuiHandler;

public class BasicNetworkRelayGui implements IGuiHandler  {
	public static final int GUI_ID = GuiHandler.getNextGuiID();
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileBasicNetworkRelay inventory = (TileBasicNetworkRelay)world.getTileEntity(x, y, z);
		return new ControllerContainer(player.inventory, inventory);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,int x, int y, int z) {
		TileEntity tile = world.getTileEntity(x, y, z);
		return new GUI(player, (TileBasicNetworkRelay)tile);
	}
	
	public static class GUI extends GuiContainer {
		
		public static final ResourceLocation backgroundImage = new ResourceLocation(WarpedSpace.MODID, "textures/gui/node_base.png");
		
		private static final short backgroundWidth = 175;
		private static final short backgroundHeight = 221;
		
		private TileBasicNetworkRelay tile;
		private ControllerContainer container;
		private EntityPlayer player;
		
		private int networkState;
		private int nodes;
		
		private GuiLabel labelContainerName;
		private GuiLabel labelNetworkState;
		private GuiLabel labelRelayRadius;
		private GuiLabel labelNodes;
		
		private String relayRadiusPrefix = "Relay Radius: ";
		private String nodesPrefix = "Connected Nodes: ";
	
		public GUI(EntityPlayer player, TileBasicNetworkRelay tile) {
			super(new ControllerContainer(player.inventory, tile));
			
			this.tile = tile;
			container = (ControllerContainer)this.inventorySlots;
			this.player = player;
			
			if(tile == null)
				player.closeScreen();
			
			networkState = Messages.noNetwork;
		}
		

		public void clientUpdate(int networkState, int nodes) {
			this.networkState = networkState;
			this.nodes = nodes;
			updateGui();
		}
		
		public void updateGui() {
			labelNetworkState.label = Messages.get(networkState);
			if(networkState == Messages.online)
				labelNetworkState.color = Messages.colorOk;
			else
				labelNetworkState.color = Messages.colorProblem;
			
			Block block = tile.getBlockType();
			if((block instanceof BlockRelayBase))
				labelRelayRadius.label = relayRadiusPrefix + ((BlockRelayBase)block).getRadius();
			labelNodes.label = nodesPrefix + nodes;
		}
		
		@Override
		public void setWorldAndResolution(Minecraft mc, int width, int height) {
			super.setWorldAndResolution(mc, width, height);
			
			this.guiLeft = (width-backgroundWidth)/2;
			this.guiTop = (height-backgroundHeight)/2;
			this.xSize = backgroundWidth;
			this.ySize = backgroundHeight;
			
			labelContainerName = new GuiLabel(tile.getInventoryName(), guiLeft + 5, guiTop + 5, 4210752);
			labelNetworkState = new GuiLabel(Messages.get(Messages.noNetwork), guiLeft + 25, guiTop + 20, Messages.colorProblem);
			labelRelayRadius = new GuiLabel(relayRadiusPrefix + "0", guiLeft + 5, guiTop + 35, 4210752);
			labelNodes = new GuiLabel(nodesPrefix, guiLeft + 5, guiTop + 45, 4210752);
			
			updateGui();
		}
	
		@Override
		public void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY) {
			this.mc.getTextureManager().bindTexture(backgroundImage);
			this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, backgroundWidth, backgroundHeight);
			
			labelContainerName.draw(fontRendererObj);
			labelNetworkState.draw(fontRendererObj);
			labelRelayRadius.draw(fontRendererObj);
			labelNodes.draw(fontRendererObj);
		}
	
		@Override
		public boolean doesGuiPauseGame() {
			return false;
		}
		
	}
}
