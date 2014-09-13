package com.wildex999.warpedspace.inventory;

import com.wildex999.warpedspace.TickHandler;

import net.minecraft.entity.player.InventoryPlayer;


public class InterfaceContainer extends ControllerContainer {

	public InterfaceContainer(InventoryPlayer playerInventory,
			BaseNetworkInventoryTile tileInventory) {
		super(playerInventory, tileInventory);
	}
	
	//When sending container update from the interface we make sure it
	//actually sends the network card instead of the hosted inventory
	@Override
	public void detectAndSendChanges() {
		boolean inWorldTick = TickHandler.inWorldTick;
		TickHandler.inWorldTick = false;
		super.detectAndSendChanges();
		TickHandler.inWorldTick = inWorldTick;
	}

}
