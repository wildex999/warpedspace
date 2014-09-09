package com.wildex999.warpedspace.proxyplayer;

import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;

//Bypass default vanilla container behavior(Close when outside range)
//TODO: Don't bypass plugins/mods denying access depending on permissions 

public class ProxyContainer {
	
	@SubscribeEvent
	public void onContainerOpenEvent(PlayerOpenContainerEvent event) {
		//TODO: Check if player has 'proxy' set to true(Set in MessageActivate etc.), so 
		//as to allow default behavior when not proxying
		event.setResult(Result.ALLOW);
	}
}
