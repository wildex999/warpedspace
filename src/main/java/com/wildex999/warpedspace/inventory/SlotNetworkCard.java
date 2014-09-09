package com.wildex999.warpedspace.inventory;

import com.wildex999.warpedspace.items.ItemNetworkCard;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotNetworkCard extends Slot {

	public boolean isOutput;
	
	public SlotNetworkCard(IInventory inventory, int slot, int x, int y, boolean output) {
		super(inventory, slot, x, y);
		isOutput = output;
	}
	
	public SlotNetworkCard(IInventory inventory, int slot, int x, int y) {
		super(inventory, slot, x, y);
		isOutput = false;
	}
	
	@Override
	public boolean isItemValid(ItemStack itemStack) {
		if(isOutput)
			return false;
		if(itemStack.getItem() instanceof ItemNetworkCard)
			return true;
		return false;
	}

}
