package com.wildex999.warpedspace.inventory;

import java.util.List;

import com.wildex999.warpedspace.gui.interfaces.IGuiWatchers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class NetworkContainer extends Container {
	
	private BaseNetworkInventoryTile networkNodeInventory;
	private InventoryPlayer playerInventory;
	private int playerInventoryStart; //Index used for first player inventory slot.
	private int networkCardInventoryStart;
	private int cardOldX, cardOldY;
	
	protected int slotNetworkCard = 0;
	
	public NetworkContainer(InventoryPlayer playerInventory, BaseNetworkInventoryTile tileInventory) {
		networkNodeInventory = tileInventory;
		this.playerInventory = playerInventory;
		
		if(tileInventory instanceof IGuiWatchers)
			((IGuiWatchers)tileInventory).addWatcher(playerInventory.player);
	}
	
	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		
		if(networkNodeInventory instanceof IGuiWatchers)
			((IGuiWatchers)networkNodeInventory).removeWatcher(playerInventory.player);
	}
	
	//Initialize the player inventory
	protected void initPlayerInventory(InventoryPlayer inventory, int playerInventoryX, int playerInventoryY) {
		int i;

		playerInventoryStart = this.inventorySlots.size();
		
        for (i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(inventory, j + i * 9 + 9, playerInventoryX + j * 18, playerInventoryY + i * 18));
            }
        }

        for (i = 0; i < 9; ++i)
        {
            this.addSlotToContainer(new Slot(inventory, i, playerInventoryX + i * 18, playerInventoryY + 58));
        }
	}
	
	protected void initNetworkCardInventory(IInventory inventory, int invX, int invY) {
		networkCardInventoryStart = this.inventorySlots.size();
		cardOldX = invX;
		cardOldY = invY;
		this.addSlotToContainer(new SlotNetworkCard(inventory, slotNetworkCard, invX, invY));
	}
	
	//Move the position of the player inventory slots on the screen
	public void setPlayerInventoryPosition(int x, int y) {
		Slot slot;
		int offset = playerInventoryStart;
		int i;
		
        for (i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
            	slot = (Slot)this.inventorySlots.get(offset++);
            	slot.xDisplayPosition = x + j * 18;
            	slot.yDisplayPosition = y + i * 18;
            }
        }

        for (i = 0; i < 9; ++i)
        {
        	slot = (Slot)this.inventorySlots.get(offset++);
        	slot.xDisplayPosition = x + i * 18;
        	slot.yDisplayPosition = y + 58;
        }
	}
	
	public void showNetworkCard(boolean show) {
		Slot slot = (Slot)this.inventorySlots.get(networkCardInventoryStart);
		
		if(show == false)
		{
			slot.xDisplayPosition = -1000;
			slot.yDisplayPosition = -1000;
		}
		else
		{
			slot.xDisplayPosition = cardOldX;
			slot.yDisplayPosition = cardOldY;
		}
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return networkNodeInventory.isUseableByPlayer(player);
	}
	
	@Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
    {
		ItemStack itemStack = null;
		ItemStack slotStack;
		Slot selectedSlot = (Slot)inventorySlots.get(slotIndex);
		
		if(selectedSlot != null && selectedSlot.getHasStack())
		{
			slotStack = selectedSlot.getStack();
			itemStack = slotStack.copy();
			if(slotIndex < 36)
			{
				if(networkNodeInventory.getInventoryStackLimit() == 1)
				{
					Slot inputSlot = (Slot)inventorySlots.get(36);
					if(inputSlot == null || inputSlot.getHasStack() || !inputSlot.isItemValid(slotStack))
						return null;
					
					inputSlot.putStack(slotStack.splitStack(1));
				}
				else
				{
					if(!this.mergeItemStack(slotStack, 36, 37, false))
						return null;
				}
			}
			else if(slotIndex >= 36)
			{
				if(!this.mergeItemStack(slotStack, 0, 36, false))
					return null;
			}
			else
				return null;
			
			
			if(slotStack.stackSize == 0)
				selectedSlot.putStack(null);
			else
				selectedSlot.onSlotChanged();
			
			if(slotStack.stackSize == itemStack.stackSize)
				return null;
		}
		
		return itemStack;
    }

}
