package com.wildex999.warpedspace.tiles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.wildex999.utils.BlockItemName;
import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.Messages;
import com.wildex999.warpedspace.TickHandler;
import com.wildex999.warpedspace.gui.interfaces.IGuiWatchers;
import com.wildex999.warpedspace.items.ItemNetworkCard;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.netinterface.MessageSCInterfaceUpdate;
import com.wildex999.warpedspace.networking.netinterface.MessageTilesList;
import com.wildex999.warpedspace.warpednetwork.BaseNodeTile;
import com.wildex999.warpedspace.warpednetwork.IEntryListener;
import com.wildex999.warpedspace.warpednetwork.INetworkRelay;
import com.wildex999.warpedspace.warpednetwork.ITileListener;
import com.wildex999.warpedspace.warpednetwork.AgentEntry;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

public class TileNetworkInterface extends BaseNodeTile implements IGuiWatchers, ITileListener, IEntryListener {
	private List<EntityPlayer> watchers;
	private List<EntityPlayerMP> tileWatchers;
	
	public String storedEntry;
	public AgentEntry currentEntry;
	
	public TileNetworkInterface() {
		inventoryName = "Network Interface";

		watchers = new ArrayList<EntityPlayer>();
		tileWatchers = new ArrayList<EntityPlayerMP>();
		storedEntry = "";
		currentEntry = null;
	}
	
	//Send GUI update to clients.
	//player: Player to send update to. If null it will send to every watcher
	//queue: Whether or not to queue the message until next tick
	public void sendGuiUpdate(EntityPlayer player, boolean queue) {
		if(currentEntry != null && !currentEntry.isValid())
		{
			currentEntry = null;
			storedEntry = "";
		}
		
		String itemName = "";
		int entryState;
		if(storedEntry.length() == 0)
			entryState = Messages.offline;
		else
			entryState = Messages.notSet;
		
		if(currentEntry != null)
		{
			storedEntry = currentEntry.name; //Update stored name in case of rename
			if(currentEntry.active)
				entryState = Messages.online;
			else
				entryState = Messages.offline;
			
			itemName = BlockItemName.get(currentEntry.block, currentEntry.world, currentEntry.x, currentEntry.y, currentEntry.z);
		}
		
		MessageBase messageUpdate = new MessageSCInterfaceUpdate(getNetworkStateMessage(), entryState, storedEntry, itemName);
		
		if(player != null)
		{
			if(queue)
				messageUpdate.queueToPlayer((EntityPlayerMP)player);
			else
				messageUpdate.sendToPlayer((EntityPlayerMP)player);
		}
		else
		{
			for(EntityPlayer currentPlayer : watchers)
			{
				if(queue)
					messageUpdate.queueToPlayer((EntityPlayerMP)currentPlayer);
				else
					messageUpdate.sendToPlayer((EntityPlayerMP)currentPlayer);
			}
		}
	}
	
	//Update from Client
	public void clientUpdate(EntityPlayerMP player, String selected) {
		setEntry(owner, selected);
	}
	
	public void setEntry(String player, String entryName) {
		//Try to set current entry to the selected
		storedEntry = entryName;
		if(entryName.length() == 0)
		{
			currentEntry = null;
			ModLog.logger.info("Current Entry SET TO 0");
		}
		else
		{
			if(currentNetwork != null)
			{
				currentEntry = currentNetwork.getBlock(this, player, entryName);
				if(currentEntry == null) //TODO: Decide if we failed due to the entry not existing or permissions denied us.
				{
					//Wait for entry
					currentNetwork.registerEntryListener(entryName, this);
				}
			}
		}
		
		this.markDirty();
		sendGuiUpdate(null, false);
	}
	
	public void addTileWatcher(EntityPlayerMP player) {
		tileWatchers.add(player);
		
		if(currentNetwork == null)
		{
			ModLog.logger.info("Null network on add tile watcher");
			return;
		}
		
		currentNetwork.registerTileListener(this);
		
		//Send current tile list
		ModLog.logger.info("Sending tile list to player: " + player.getGameProfile().getName());
		MessageBase listMessage = new MessageTilesList(currentNetwork);
		listMessage.sendToPlayer(player);

	}
	
	public void removeTileWatcher(EntityPlayerMP player) {
		tileWatchers.remove(player);
		
		if(tileWatchers.size() == 0 && currentNetwork != null)
			currentNetwork.unregisterTileListener(this);
	}
	
	@Override
	public boolean joinNetwork(WarpedNetwork network) {
		if(!super.joinNetwork(network))
			return false;
		
		setEntry(owner, storedEntry);
		
		if(tileWatchers.size() > 0)
			currentNetwork.registerTileListener(this);
		
		sendGuiUpdate(null, false);
		
		return true;
	}
	
	@Override
	public void leaveNetwork() {
		currentNetwork.unregisterTileListener(this);
		super.leaveNetwork();
		currentEntry = null;
		sendGuiUpdate(null, false);
	}
	
	@Override
	public boolean joinRelay(INetworkRelay relay) {
		if(super.joinRelay(relay))
		{
			sendGuiUpdate(null, false);
			return true;
		}
		else
			return false;
	}
	
	@Override
	public void eventLostRelay() {
		super.eventLostRelay();
		sendGuiUpdate(null, false);
	}
	
	@Override
	public void eventNetworkDisconnect() {
		super.eventNetworkDisconnect();
		sendGuiUpdate(null, false);
	}
	
	@Override
	public void eventNetworkReconnect() {
		super.eventNetworkReconnect();
		sendGuiUpdate(null, false);
	}
	
	@Override
	public void eventLostNetwork() {
		super.eventLostNetwork();
		sendGuiUpdate(null, false);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		if(worldObj.isRemote)
			return;
		
		data.setString("selection", storedEntry);
		
		//TODO: Write settings
	}
	
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		
		storedEntry = data.getString("selection");
		
		//TODO: Read settings
	}
	
	@Override
	public void addWatcher(EntityPlayer player) {
		if(worldObj.isRemote)
			return;
		watchers.add(player);
		sendGuiUpdate(player, true);
	}

	@Override
	public void removeWatcher(EntityPlayer player) {
		if(worldObj.isRemote)
			return;
		watchers.remove(player);
	}

	@Override
	public void tileAdded(AgentEntry tile) {
		//TODO
	}

	@Override
	public void tileRemoved(AgentEntry tile) {
		//TODO
	}

	@Override
	public void tileAvailable(AgentEntry tile) {
		//TODO
	}

	@Override
	public void tileUnavailable(AgentEntry tile) {
		//TODO
	}

	@Override
	public boolean onEntryAvailable(String name) {
		//Our entry now exists, try to get it
		this.setEntry(owner, name);
		return true;
	}

	@Override
	public boolean onEntryUnavailable(String name) {
		return false;
	}

}
