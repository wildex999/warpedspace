package com.wildex999.warpedspace.warpednetwork;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.Messages;
import com.wildex999.warpedspace.TickHandler;
import com.wildex999.warpedspace.inventory.BaseNetworkInventoryTile;
import com.wildex999.warpedspace.items.ItemNetworkCard;
import com.wildex999.warpedspace.tiles.IPreTickOneShotListener;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class BaseNodeTile extends BaseNetworkInventoryTile implements IPreTickOneShotListener, INode {

	public static int ticksRelaySearch = 20;
	public int relaySearchTicks = 0;
	
	protected boolean isReachable = false;
	protected WarpedNetwork currentNetwork;
	protected INetworkRelay currentRelay;
	protected String owner;
	
	//Synced on TileEntity to show activity while not in gui
	protected int activity;
	

	//Make sure we have a connection to the Network
	public void updateEntity() {
		//TODO: Add Relay watcher list, so when relay join or upgrade range, let everyone in list test for Relay visibility
		if(currentRelay == null && currentNetwork != null)
		{
			//Search for new Relay for our connected network
			if(relaySearchTicks++ >= ticksRelaySearch)
			{
				relaySearchTicks = 0;
				tryJoinRelay();
			}
		}
	}
	
	//Try to join a relay if currently got none
	public void tryJoinRelay() {
		INetworkRelay relay = currentNetwork.getRelayForNode(this, true);
		if(relay == null)
			return;
		
		if(this.joinRelay(relay))
			return;
		//TODO: Continue search if relay doesn't allow us to join
		//Let getRelayForNode do the joining?(it call node.joinRelay)
	}
	
	@Override
	public boolean joinNetwork(WarpedNetwork network) {
		if(currentNetwork != null)
			return false;
		
		if(network.addNode(this))
		{
			currentNetwork = network;
			
			//Try to get Relay at once
			tryJoinRelay();
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public void validate() {
		super.validate();
		TickHandler.registerListener(this);
	}
	
	//Called when a network card is placed/removed
	public void onNetworkCardUpdate() {
		if(worldObj.isRemote)
			return;
		
		ModLog.logger.info("Network Card Update");
		
		if(stackNetworkCard == null)
		{
			if(currentNetwork != null)
				leaveNetwork();
			return;
		}
		
		int networkId = ItemNetworkCard.getNetworkId(stackNetworkCard);
		
		//Check if card was replaced
		if(currentNetwork != null && currentNetwork.id != networkId)
			leaveNetwork();
		
		CoreNetworkManager manager = CoreNetworkManager.serverNetworkManager;
		WarpedNetwork network = manager.networks.get(networkId);
		
		if(network == null)
			return;
		
		joinNetwork(network);
	}
	
	public int getNetworkStateMessage() {
		int networkState = Messages.online;
		if(currentNetwork == null)
			networkState = Messages.noNetwork;
		else if(!currentNetwork.isOnline)
			networkState = Messages.networkOffline;
		else if(currentRelay == null)
			networkState = Messages.noRelay;
		else if(!isReachable)
			networkState = Messages.notReachable;
		
		return networkState;
	}
	
	@Override
	public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
		super.setInventorySlotContents(slotIndex, itemStack);
		
		onNetworkCardUpdate();
	}
	
	@Override
	public ItemStack decrStackSize(int slotIndex, int removeCount) {
		ItemStack stack = super.decrStackSize(slotIndex, removeCount);
		
		onNetworkCardUpdate();
		
		return stack;
	}
	
	@Override
	public World getWorld() {
		return getWorldObj();
	}
	
	@Override
	public int getPosX() {
		return xCoord;
	}

	@Override
	public int getPosY() {
		return yCoord;
	}

	@Override
	public int getPosZ() {
		return zCoord;
	}

	@Override
	public void eventLostRelay() {
		isReachable = false;
		currentRelay = null;
	}

	@Override
	public void eventLostNetwork() {
		leaveNetwork();
	}

	@Override
	public void eventNetworkDisconnect() {
		isReachable = false;
	}

	@Override
	public void eventNetworkReconnect() {
		if(currentRelay != null && currentRelay.isNetworkReachable())
			isReachable = true;
	}

	@Override
	public void eventNetworkNoPower() {
		isReachable = false;
	}

	@Override
	public void eventNetworkGotPower() {
		if(currentRelay != null && currentRelay.isNetworkReachable())
			isReachable = true;
	}

	@Override
	public boolean joinRelay(INetworkRelay relay) {
		if(relay.addNode(currentNetwork, this))
		{
			currentRelay = relay;
			ModLog.logger.info("Joined relay: " + relay);
			if(currentRelay.isNetworkReachable())
				isReachable = true;
			return true;
		}
		return false;
	}

	@Override
	public WarpedNetwork getNetwork() {
		return currentNetwork;
	}

	@Override
	public boolean isNetworkReachable() {
		if(currentNetwork == null || currentRelay == null)
			return false;
		return isReachable;
	}

	@Override
	public void leaveNetwork() {
		if(currentRelay != null)
			leaveRelay();
		
		if(currentNetwork == null)
			return;
		
		isReachable = false;
		currentNetwork.removeNode(this);
		currentNetwork = null;
	}

	@Override
	public void leaveRelay() {
		if(currentRelay != null)
		{
			isReachable = false;
			currentRelay.removeNode(currentNetwork, this);
			currentRelay = null;
		}
	}
	
	@Override
	public INetworkRelay getRelay() {
		return currentRelay;
	}

	@Override
	public String getOwner() {
		return owner;
	}

	@Override
	public void setOwner(String username) {
		owner = username;
	}
	
	@Override
	public void onLoadComplete() {
		onNetworkCardUpdate();
	}

}
