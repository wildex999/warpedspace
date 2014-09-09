package com.wildex999.warpedspace.inventory;

import net.minecraft.entity.player.InventoryPlayer;

public class ControllerContainer extends NetworkContainer {
	
	public ControllerContainer(InventoryPlayer playerInventory, BaseNetworkInventoryTile tileInventory) {
		super(playerInventory, tileInventory);
		
		initPlayerInventory(playerInventory, 8, 140);
		initNetworkCardInventory(tileInventory, 7, 16);
	}

}
