package com.wildex999.warpedspace.inventory;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.items.ItemNetworkCard;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class BaseNetworkInventoryTile extends TileEntity implements ISidedInventory {
	
	// Stack is not given any slot index, allowing any class overiding
	// to set the slot which contains the network card.
	protected ItemStack stackNetworkCard;
	protected int[] defaultNetworkCardSlot = new int[] { 0 };
	protected String inventoryName = "inventory.node.networkcard";

	@Override
	public int getSizeInventory() {
		// Contain a single space for the Network Card
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slotIndex) {
		return stackNetworkCard;
	}

	@Override
	public ItemStack decrStackSize(int slotIndex, int removeCount) {
		// Network Card does not stack, so simply return it.
		ItemStack out = stackNetworkCard;
		stackNetworkCard = null;

		return out;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotIndex) {
		if (stackNetworkCard != null) {
			ItemStack out = stackNetworkCard;
			stackNetworkCard = null;
			return out;
		}
		return null;
	}

	@Override
	public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
		stackNetworkCard = itemStack;
		if (stackNetworkCard != null && stackNetworkCard.stackSize > 1)
			stackNetworkCard.stackSize = 1;
	}

	@Override
	public String getInventoryName() {
		return inventoryName;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return true;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		// TODO: Limit to owner/Permitted player
		return true;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack) {
		//This is called by machines(Like the Hopper) who pushes the items into
		//the inventory.
		//Players in a GUI check directly on the Slot, thus we need this code two places(SlotNetworkCard).
		if(itemStack.getItem() instanceof ItemNetworkCard)
			return true;
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		// Override to use other slots, and limit sides
		return defaultNetworkCardSlot;
	}

	@Override
	public boolean canInsertItem(int slotIndex, ItemStack itemStack, int side) {
		// Override to do extra checks, for example side.
		return this.isItemValidForSlot(slotIndex, itemStack);
	}

	@Override
	public boolean canExtractItem(int slotIndex, ItemStack itemStack, int side) {
		// Anything can extract the network card from any side.
		// Override to add additional behavior.
		return true;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		
		if(stackNetworkCard != null)
		{
			NBTTagCompound itemData = new NBTTagCompound();
			stackNetworkCard.writeToNBT(itemData);
			data.setTag("networkCard", itemData);
		}
		
	}
	
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		
		NBTTagCompound itemData = data.getCompoundTag("networkCard");
		stackNetworkCard = ItemStack.loadItemStackFromNBT(itemData);
	}
}
