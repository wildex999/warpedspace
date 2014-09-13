package com.wildex999.warpedspace;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.MessageOpenTileGui;
import com.wildex999.warpedspace.tiles.IPreTickOneShotListener;

import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;

public class TickHandler {

	private static List<IPreTickOneShotListener> listenerList = new ArrayList<IPreTickOneShotListener>();
	public static boolean inWorldTick = false;
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event) {
		
		if(event.phase == Phase.START)
		{

			
			for(IPreTickOneShotListener listener : listenerList)
				listener.onLoadComplete();
			listenerList.clear();
			
			//Networking queued messages
			MessageBase.sendQueuedMessages();
		}
		else
		{
		}
	}
	
	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event) {
		if(event.phase == Phase.START)
			inWorldTick = true;
		else
			inWorldTick = false;
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
			if(Minecraft.getMinecraft().currentScreen == null && GuiHandler.previousGui != null)
			{
				Minecraft.getMinecraft().displayGuiScreen(GuiHandler.previousGui);
				MessageBase messageGui = new MessageOpenTileGui(GuiHandler.previousGuiTile, GuiHandler.previousGuiId);
				messageGui.sendToServer();
				GuiHandler.oldGui = GuiHandler.previousGui;
				GuiHandler.previousGui = null;
			}
	}
	
	public static void registerListener(IPreTickOneShotListener listener)
	{
		listenerList.add(listener);
	}
}
