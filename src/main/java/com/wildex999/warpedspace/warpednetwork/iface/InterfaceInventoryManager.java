package com.wildex999.warpedspace.warpednetwork.iface;

import com.wildex999.warpedspace.tiles.TileNetworkInterface;
import com.wildex999.warpedspace.warpednetwork.AgentEntry;

import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class InterfaceInventoryManager {
	//If the hosted block is not a Tile Entity, or does not implement IInventory, 
	//we just return 0 in getSizeInventory.
	private TileNetworkInterface tile;
	private IInventory hostedInventory;
	private ISidedInventory hostedSidedInventory;
	
	private int[] emptyList = new int[0];
	private int[] slotList; //Used when checking slot in side when ISidedInventory is not implemented
	
	public InterfaceInventoryManager(TileNetworkInterface tile) {
		this.tile = tile;
	}
	
	public void update() {
		//TODO: Does this need to be checked for each update?
		//Maybe only check in a onNewTile() method.
		boolean zero = false;
		AgentEntry entry = tile.currentEntry;
		if(entry == null || !tile.isNetworkReachable())
			zero = true;
		else if(!entry.active || !entry.isValid() || !entry.agent.isNetworkReachable())
			zero = true;
		
		if(zero)
		{
			hostedInventory = null;
			hostedSidedInventory = null;
			return;
		}
		TileEntity hostedTile = entry.world.getTileEntity(entry.x, entry.y, entry.z);
		
		if(hostedTile instanceof IInventory)
		{
			//Handle double chest
			if(entry.block instanceof BlockChest)
			{
				hostedInventory = ((BlockChest)entry.block).func_149951_m(entry.world, entry.x, entry.y, entry.z);
				if(hostedInventory == null)
					hostedInventory = (IInventory)hostedTile;
			}
			else
				hostedInventory = (IInventory)hostedTile;
		}
		else
		{
			//ISidedInventory extends IInventory
			hostedInventory = null;
			hostedSidedInventory = null;
			return;
		}
		
		if(hostedTile instanceof ISidedInventory)
			hostedSidedInventory = (ISidedInventory)hostedTile;
		else
		{
			hostedSidedInventory = null;
			slotList = new int[hostedInventory.getSizeInventory()];
			for(int slot = 0; slot < hostedInventory.getSizeInventory(); slot++)
				slotList[slot] = slot;
		}
	}

	public int getSizeInventory() {
		if(hostedInventory == null)
			return 0;
		return hostedInventory.getSizeInventory();
	}

	public ItemStack getStackInSlot(int slot) {
		if(hostedInventory == null)
			return null;
		return hostedInventory.getStackInSlot(slot);
	}

	public ItemStack decrStackSize(int slot, int count) {
		if(hostedInventory == null)
			return null;
		return hostedInventory.decrStackSize(slot, count);
	}

	public ItemStack getStackInSlotOnClosing(int slot) {
		if(hostedInventory == null)
			return null;
		return hostedInventory.getStackInSlotOnClosing(slot);
	}

	public void setInventorySlotContents(int slot, ItemStack stack) {
		if(hostedInventory == null)
			return;
		hostedInventory.setInventorySlotContents(slot, stack);
	}

	public String getInventoryName() {
		if(hostedInventory == null)
			return "";
		return hostedInventory.getInventoryName();
	}

	public boolean hasCustomInventoryName() {
		if(hostedInventory == null)
			return false;
		return hostedInventory.hasCustomInventoryName();
	}

	public int getInventoryStackLimit() {
		if(hostedInventory == null)
			return 0;
		return hostedInventory.getInventoryStackLimit();
	}

	public void markDirty() {
		if(hostedInventory != null)
			hostedInventory.markDirty();
	}

	public boolean isUseableByPlayer(EntityPlayer player) {
		if(hostedInventory == null)
			return false;
		return hostedInventory.isUseableByPlayer(player);
	}

	public void openInventory() {
		if(hostedInventory != null)
			hostedInventory.openInventory();
	}

	public void closeInventory() {
		if(hostedInventory != null)
			hostedInventory.closeInventory();
	}

	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(hostedInventory == null)
			return false;
		return hostedInventory.isItemValidForSlot(slot, stack);
	}

	//Sided specific
	//If your hosted tile entity does not have ISidedInventory, we simply return all sides all true, 
	//since some blocks(Like hoppers) wille actually check the sides if it implements ISidedInventory, and not if it doesn't.
	public int[] getAccessibleSlotsFromSide(int side) {
		if(hostedSidedInventory == null)
		{
			if(hostedInventory != null)
				return slotList;
			else
				return emptyList;
		}
		return hostedSidedInventory.getAccessibleSlotsFromSide(side);
	}

	public boolean canInsertItem(int slot, ItemStack p_102007_2_, int p_102007_3_) {
		if(hostedSidedInventory == null)
		{
			if(hostedInventory != null && slot >= 0 && slot < getSizeInventory())
				return true;
			else
				return false;
		}
		return hostedSidedInventory.canInsertItem(slot, p_102007_2_, p_102007_3_);
	}

	public boolean canExtractItem(int slot, ItemStack p_102008_2_, int p_102008_3_) {
		if(hostedSidedInventory == null)
		{
			if(hostedInventory != null && slot >= 0 && slot < getSizeInventory())
				return true;
			else
				return false;
		}
		return hostedSidedInventory.canExtractItem(slot, p_102008_2_, p_102008_3_);
	}
}
