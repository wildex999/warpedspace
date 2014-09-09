package com.wildex999.warpedspace;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.MessageOpenTileGui;
import com.wildex999.warpedspace.proxyplayer.ProxyInfo;
import com.wildex999.warpedspace.tiles.IPreTickOneShotListener;

import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

public class TickHandler {

	private static List<IPreTickOneShotListener> listenerList = new ArrayList<IPreTickOneShotListener>();
	
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
	
	/*@SubscribeEvent
	public void onPlayerPreTick(PlayerTickEvent event) {
		ProxyInfo proxy = ProxyInfo.getPlayerProxy(event.player);
		
		//Stop container from closing due to player being far away
		if(event.phase == Phase.START)
		{
			proxy.container = event.player.openContainer;
			event.player.openContainer = null;
		} else if(event.phase == Phase.END) {
			proxy.player.openContainer = proxy.container;
			proxy.container = null;
		}
	}*/
	
	public static void registerListener(IPreTickOneShotListener listener)
	{
		listenerList.add(listener);
	}
}
