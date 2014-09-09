package com.wildex999.warpedspace;

import java.util.HashMap;
import java.util.Map;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.gui.NetworkManagerGui;
import com.wildex999.warpedspace.inventory.ControllerContainer;
import com.wildex999.warpedspace.inventory.NetworkContainer;
import com.wildex999.warpedspace.inventory.NetworkManagerContainer;
import com.wildex999.warpedspace.tiles.TileNetworkManager;
import com.wildex999.warpedspace.tiles.TileNetworkController;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
	
	private static int GUI_ID_BASE = 1000; //Auto id allocation starting at 1000. Static id's < 1000.
	
	public static TileEntity currentGuiTile;
	public static GuiScreen previousGui;
	public static TileEntity previousGuiTile;
	public static int previousGuiId;
	public static GuiScreen oldGui; //Used for re-opening gui to copy settings

	//Each GUI group handler registers itself to all the id's it handles
	private Map<Integer, IGuiHandler> customGuiHandlers;
	
	public GuiHandler() {
		customGuiHandlers = new HashMap<Integer, IGuiHandler>();
	}
	
	public void setGuiHandler(int id, IGuiHandler handler) {
		if(handler == null)
		{
			customGuiHandlers.remove(id);
			return;
		}
		
		customGuiHandlers.put(id, handler);
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		IGuiHandler handler = customGuiHandlers.get(ID);
		
		if(handler != null)
			return handler.getServerGuiElement(ID, player, world, x, y, z);
		
		ModLog.logger.warn("Missing GUI Handler for id: " + ID);
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		IGuiHandler handler = customGuiHandlers.get(ID);
		TileEntity tile = world.getTileEntity(x, y, z);
		
		/*if(ID == WarpedNodeGui.GUI_ID)
			return new WarpedNodeGui(player, tile);
		else if(ID == WarpedControllerGui.GUI_ID)
			return new WarpedControllerGui(player, (TileWarpedController)tile);*/
			
		if(handler != null)
			return handler.getClientGuiElement(ID, player, world, x, y, z);
		
		ModLog.logger.warn("Missing GUI Handler for id: " + ID);
		return null;
	}
	
	public static int getNextGuiID()
	{
		return GUI_ID_BASE++;
	}
	
	public static void setPreviousTileGui(GuiScreen gui, int guiId, TileEntity tile) {
		previousGui = gui;
		previousGuiId = guiId;
		previousGuiTile = tile;
	}

}
