package com.wildex999.warpedspace.tiles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.minecraft.client.renderer.texture.Stitcher.Slot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.gui.interfaces.IGuiWatchers;
import com.wildex999.warpedspace.inventory.BaseNetworkInventoryTile;
import com.wildex999.warpedspace.items.ItemNetworkCard;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.MessageNetworkManagerUpdate;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

public class TileNetworkManager extends BaseNetworkInventoryTile implements IGuiWatchers {
	public static final int inputSlot = 0;
	public static final int outputSlot = 1;
	
    private static final int[] slotsTop = new int[] {inputSlot};
    private static final int[] slotsBottom = new int[] {outputSlot};
    private static final int[] slotsSides = new int[] {inputSlot};
	
	private ItemStack stackNetworkCardOut;
	
	public WarpedNetwork currentNetwork;
	public boolean writeNetworkCard;
	private HashSet<EntityPlayer> watchers;
	
	public TileNetworkManager() {
		inventoryName = "Network Manager";
		writeNetworkCard = false;
		watchers = new HashSet<EntityPlayer>();
	}
	
	public void setCurrentNetwork(WarpedNetwork network) {
		currentNetwork = network;
		stackNetworkCardOut = null;
	}
	
	@Override
	public void updateEntity() {
		if(worldObj.isRemote)
			return;
		
		if(writeNetworkCard && currentNetwork != null)
		{
			//Write one Card per tick
			if(stackNetworkCard == null)
				return;
			
			//Output empty, create new with output
			if(stackNetworkCardOut == null)
			{
				stackNetworkCardOut = stackNetworkCard.splitStack(1);
				ItemNetworkCard.setNetworkId(stackNetworkCardOut, currentNetwork.id);
				
				if(stackNetworkCard.stackSize <= 0)
					stackNetworkCard = null;
				
				return;
			}
			if(stackNetworkCardOut.stackSize == this.getInventoryStackLimit())
				return;
			
			//If output is not empty, check if we can add to stack
			if(ItemNetworkCard.getNetworkId(stackNetworkCardOut) != currentNetwork.id)
				return;
			
			stackNetworkCard.stackSize--;
			stackNetworkCardOut.stackSize++;
			
			if(stackNetworkCard.stackSize <= 0)
				stackNetworkCard = null;
			
		}
	}
	
	@Override
	public int getSizeInventory() {
		return 2;
	}
	
	@Override
	public ItemStack getStackInSlot(int slotIndex) {
		switch(slotIndex)
		{
		case inputSlot:
			return stackNetworkCard;
		case outputSlot:
			return stackNetworkCardOut;
		}

		return null;
	}
	
	//Data from client/server
	public void updateData(boolean writeEnabled, WarpedNetwork network) {
		currentNetwork = network;
		writeNetworkCard = writeEnabled;
	}
	
	//Send data to all watching clients
	public void updatedWatchers() {
		//Send update to watching players
		if(watchers.size() != 0)
		{
			MessageBase updateMessage = new MessageNetworkManagerUpdate(writeNetworkCard, currentNetwork, this);
			for(EntityPlayer player : watchers)
				updateMessage.sendToPlayer((EntityPlayerMP)player);
		}
	}
	
	public void setStackInSlot(int slotIndex, ItemStack stack) {
		switch(slotIndex)
		{
		case inputSlot:
			stackNetworkCard = stack;
			break;
		case outputSlot:
			stackNetworkCardOut = stack;
			break;
		}
	}
	
	@Override
	public ItemStack decrStackSize(int slotIndex, int removeCount) {
		ItemStack stack = getStackInSlot(slotIndex);
		ItemStack stackOut;
		
		if(stack == null)
			return null;
		
		if(stack.stackSize < removeCount)
		{
			//Just return what we have
			stackOut = stack;
			setStackInSlot(slotIndex, null);
			return stackOut;
		} else {
			//Remove part of stack
			stackOut = stack.splitStack(removeCount);
			if(stack.stackSize <= 0)
				setStackInSlot(slotIndex, null);
			return stackOut;
		}
	}
	
	@Override
	public ItemStack getStackInSlotOnClosing(int slotIndex) {
		ItemStack stack = getStackInSlot(slotIndex);
		
		if(stack != null)
		{
			setStackInSlot(slotIndex, null);
			return stack;
		}
		return null;
	}
	
	@Override
	public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
		setStackInSlot(slotIndex, itemStack);
		if(itemStack != null && itemStack.stackSize > getInventoryStackLimit())
			itemStack.stackSize = getInventoryStackLimit();
	}
	
	@Override
	public int getInventoryStackLimit() {
		return 64;
	}
	
	@Override
	public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack) {
		if(slotIndex == inputSlot && itemStack.getItem() instanceof ItemNetworkCard)
			return true;
		
		return false;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		//TODO: Figure how sides works with Block Facing etc.
		//For now, input at top and sides, output below.
		if(side == 0)
			return slotsBottom;
		if(side == 1)
			return slotsTop;
		
		return slotsSides;
	}
	
	@Override
	public boolean canInsertItem(int slotIndex, ItemStack itemStack, int side) {
		if(side == 0 || slotIndex == outputSlot)
			return false;
		return isItemValidForSlot(slotIndex, itemStack);
	}
	
	@Override
	public boolean canExtractItem(int slotIndex, ItemStack itemStack, int side) {
		if(side != 0 && slotIndex == outputSlot)
			return false;
		if(side == 0 && slotIndex == inputSlot)
			return false;
		return true;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		if(worldObj.isRemote)
			return;
		
		if(stackNetworkCardOut != null)
		{
			NBTTagCompound itemData = new NBTTagCompound();
			stackNetworkCardOut.writeToNBT(itemData);
			data.setTag("networkCardOut", itemData);
		}
		
		if(currentNetwork != null)
		{
			data.setInteger("networkId", currentNetwork.id);
			ModLog.logger.info("WRITE network id");
		}
		data.setBoolean("writeNetworkCard", writeNetworkCard);
		
	}
	
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		
		NBTTagCompound itemData = data.getCompoundTag("networkCardOut");
		stackNetworkCardOut = ItemStack.loadItemStackFromNBT(itemData);
		
		if(data.hasKey("networkId"))
		{
			CoreNetworkManager networkManager;
			if(worldObj == null)
				networkManager = CoreNetworkManager.serverNetworkManager;
			else
				networkManager = CoreNetworkManager.getInstance(worldObj);
			
			Integer networkId = data.getInteger("networkId");
			currentNetwork = networkManager.networks.get(networkId);
		}
		writeNetworkCard = data.getBoolean("writeNetworkCard");
		
	}

	@Override
	public void addWatcher(EntityPlayer player) {
		if(!worldObj.isRemote)
		{
			watchers.add(player);
			
			//Send update packet to player
			MessageBase messageUpdate = new MessageNetworkManagerUpdate(writeNetworkCard, currentNetwork, this);
			messageUpdate.sendToPlayer((EntityPlayerMP)player);
		}
	}

	@Override
	public void removeWatcher(EntityPlayer player) {
		if(!worldObj.isRemote)
			watchers.remove(player);
	}
}
