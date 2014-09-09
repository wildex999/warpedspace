package com.wildex999.warpedspace.inventory;

import net.minecraft.entity.player.InventoryPlayer;

public class NetworkManagerContainer extends NetworkContainer {
	
	protected int slotNetworkCardOut = 1;
	
	public NetworkManagerContainer(InventoryPlayer playerInventory, BaseNetworkInventoryTile tileInventory) {
		super(playerInventory, tileInventory);
		
		initPlayerInventory(playerInventory, 43, 157);
		initNetworkCardInventory(tileInventory, 8, 119);
		
		//Network Card output
		this.addSlotToContainer(new SlotNetworkCard(tileInventory, slotNetworkCardOut, 64, 119, true));
	}

}
