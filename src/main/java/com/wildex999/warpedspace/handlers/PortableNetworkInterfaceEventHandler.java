package com.wildex999.warpedspace.handlers;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.items.ItemLibrary;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class PortableNetworkInterfaceEventHandler {
    //Called when a player logs out to remove the proxy TileEntity
    @SubscribeEvent
    public void handlePortableNetworkInterface(PlayerEvent.PlayerLoggedOutEvent event) {
        ItemLibrary.itemPortableNetworkInterface.tileMap.remove(event.player);
    }
}
