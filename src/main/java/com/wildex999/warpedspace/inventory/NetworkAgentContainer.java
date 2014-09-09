package com.wildex999.warpedspace.inventory;

import net.minecraft.entity.player.InventoryPlayer;

public class NetworkAgentContainer extends NetworkContainer {

	public NetworkAgentContainer(InventoryPlayer playerInventory, BaseNetworkInventoryTile tileInventory) {
		super(playerInventory, tileInventory);
		
		initPlayerInventory(playerInventory, 47, 173);
		initNetworkCardInventory(tileInventory, 7, 16);
	}

}
