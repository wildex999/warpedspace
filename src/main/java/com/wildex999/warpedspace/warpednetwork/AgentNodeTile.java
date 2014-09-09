package com.wildex999.warpedspace.warpednetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.ReturnMessage;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class AgentNodeTile extends BaseNodeTile implements INodeAgent, INameListener {
	
	protected HashSet<AgentEntry> tileList; //Lookup from name
	protected Map<String, AgentEntry> unregisteredTileList; //List of tiles currently not added to network
	
	
	public AgentNodeTile() {
		currentNetwork = null;
		currentRelay = null;
		
		//We use LinkedHashMap so that id's stay consistent
		tileList = new HashSet<AgentEntry>();
		unregisteredTileList = new HashMap<String, AgentEntry>();
	}
	
	@Override
	public boolean joinNetwork(WarpedNetwork network) {
		if(!super.joinNetwork(network))
			return false;
		
		//Register all known tile entities
		for(AgentEntry entry : tileList)
			addToNetwork(entry);
		
		onJoinedNetwork();
		ModLog.logger.info("Join network");
		
		return true;
	}

	@Override
	public void leaveNetwork() {
		onLeavingNetwork();
		
		ModLog.logger.info("Leave network");
		
		//Remove added tiles
		if(currentNetwork != null)
		{
			for(AgentEntry entry : tileList)
			{
				if(entry == null)
					continue;
				currentNetwork.removeTile(entry.name, entry);
				entry.active = false;
			}
			for(Map.Entry<String, AgentEntry> entry : unregisteredTileList.entrySet())
				currentNetwork.unregisterNameListener(entry.getValue().name, this);
			unregisteredTileList.clear();
		}
		
		super.leaveNetwork();
		
		onLeftNetwork();
	}
	
	@Override
	public int getTileCount() {
		//Return list instead of map, even if it can contain null's,
		//this allows iterating of all Tiles.
		return tileList.size();
	}

	
	//Add a block to the Agent and network with the given name. If it fails to add
	//the block to the network, it will automatically try again later.
	//Override this to do checks before adding block, and then call super.
	//Returns:
	//TileAddedNoNetwork - Added to Agent. Will attempt to add to network when connected.
	//TileAddedNameTaken - Added to Agent. Will attempt to add to network when name is free.
	//TileAdded - Tile was added to both Agent and Network.
	@Override
	public ReturnMessage setTile(String name, Block block, World world, int x, int y, int z, AgentEntry newEntry) {
		newEntry.block = block;
		newEntry.world = world;
		newEntry.x = x;
		newEntry.y = y;
		newEntry.z = z;
		newEntry.name = name;
		newEntry.agent = this;
		newEntry.gid = 1; //TODO: Generate unique gid
		tileList.add(newEntry);
		
		ReturnMessage addReturn = addToNetwork(newEntry);
		if(addReturn == ReturnMessage.Ok)
			return ReturnMessage.TileAdded;
		else if(addReturn == ReturnMessage.NoNetwork)
			return ReturnMessage.TileAddedNoNetwork;
		else if(addReturn == ReturnMessage.TileAddedNameTaken)
			return ReturnMessage.TileAddedNameTaken;
		
		return ReturnMessage.TileAdded;
	}
	
	//Add entry to network
	//Returns:
	//Ok
	//NoNetwork
	//TileAddedNameTaken - Name already taken on network, added to Name listener
	private ReturnMessage addToNetwork(AgentEntry entry) {
		if(currentNetwork == null)
			return ReturnMessage.NoNetwork;
		
		//Try to add tile to network(Does name check)
		if(!currentNetwork.addTile(entry))
		{
			//Register listener for name
			unregisteredTileList.put(entry.name, entry);
			currentNetwork.registerNameListener(entry.name, this);
			return ReturnMessage.TileAddedNameTaken;
		}
		else
		{
			entry.active = true;
			if(!isReachable)
				currentNetwork.setTileUnreachable(entry);
		}
		
		return ReturnMessage.Ok;
	}
	
	//Rename an existing tile entry on both the agent and (if added) the network
	//Returns:
	//Ok - It was renamed successfully
	//RenamedLocal - It was renamed on the Agent, but was not currently added to the network
	//RenamedLocalNameTaken - It was renamed on the Agent, but not on the network due to the new name was taken, Listening for the name to become free.
	//InternalError - Hit an undefined state during the rename. The tile has been removed from the agent.
	@Override
	public ReturnMessage renameTile(AgentEntry entry, String newName) {
		ReturnMessage ret = ReturnMessage.Ok;
		
		if(entry.name.equals(newName))
			return ret;
		
		String oldName = entry.name;
		if(currentNetwork != null && entry.active)
		{
			ReturnMessage netRet = currentNetwork.renameTile(entry, newName);
			if(netRet == ReturnMessage.TileNotExist)
			{
				ModLog.logger.error("Tried to rename tile entry: " + oldName + " on network: " + currentNetwork.name + " but failed: " + netRet.message);
				ModLog.logger.error("This should not happen, and is an internal error. This should be reported to the mod author!");
				this.removeTile(entry);
				return ReturnMessage.InternalError;
			} else if(netRet == ReturnMessage.TileNameTaken) 
			{
				currentNetwork.removeTile(oldName, entry);
				entry.active = false;
				//Add as listener for the new name
				unregisteredTileList.put(newName, entry);
				currentNetwork.registerNameListener(newName, this);
				
				ret = ReturnMessage.RenamedLocalNameTaken;
			}
		}
		else
			ret = ReturnMessage.RenamedLocal;
		
		entry.name = newName;
		
		onTileRenamed(entry, oldName);
		
		return ret;
	}

	@Override
	public boolean removeTile(AgentEntry entry) {
				
		onTileRemove(entry);
		
		if(!tileList.remove(entry))
			return false;

		if(currentNetwork != null)
		{	
			if(entry.active)
				currentNetwork.removeTile(entry.name, entry);
			else
			{
				currentNetwork.unregisterNameListener(entry.name, this);
				unregisteredTileList.remove(entry.name);
			}
		}
		
		onTileRemoved(entry);
		entry.invalidate();
		
		return true;
	}

	@Override
	public boolean canUseTile(INode requestingNode, String requestingPlayer, AgentEntry entry) {
		if(entry == null)
			return false;
		return true;
	}
	
	@Override
	public boolean onNameFree(String name) {
		//Check unregistered list for entry with this name
		AgentEntry entry = unregisteredTileList.get(name);
		if(entry == null)
			return false;
		
		if(currentNetwork.addTile(entry))
		{
			entry.active = true;
			unregisteredTileList.remove(name);
			return true;
		}
		
		return false;
	}
	
	public void markTilesUnreachable() {
		if(currentNetwork == null)
			return;
		for(AgentEntry entry : tileList)
			currentNetwork.setTileUnreachable(entry);
	}
	
	public void markTilesReachable() {
		if(currentNetwork == null)
			return;
		for(AgentEntry entry : tileList)
			currentNetwork.setTileReachable(entry);
	}
	
	@Override
	public void eventLostRelay() {
		super.eventLostRelay();
		markTilesUnreachable();
	}
	
	@Override
	public boolean joinRelay(INetworkRelay relay) {
		boolean joined = super.joinRelay(relay);
		markTilesReachable();
		return joined;
	}
	
	@Override
	public void eventNetworkDisconnect() {
		super.eventNetworkDisconnect();
		markTilesUnreachable();
	}
	
	@Override
	public void eventNetworkReconnect() {
		super.eventNetworkReconnect();
		markTilesReachable();
	}
	
	
	//--EVENTS--
	
	//Override this to do Agent specific things on network join.
	//For Example: Register tile entities.
	protected abstract void onJoinedNetwork();
	
	//Called before leaving the network.
	//Note: AgentNode already removes registered Tiles from network when leaving.
	protected abstract void onLeavingNetwork();
	
	//Called after left network, before joining a new one
	protected abstract void onLeftNetwork();

	//Called when the internal ID's are invalidated
	//(I.e, must use name instead of cache to get tile)
	protected abstract void onCacheInvalidate();
	
	//Called when a tile entity is added to the agent
	protected abstract void onTileSet(AgentEntry entry);
	
	//Called when a tile entity is made accessible(added) on the network
	protected abstract void onTileAdded(AgentEntry entry);
	
	//Called when a tile entity is removed from the network(But not the agent, who will try to re-add it)
	protected abstract void onTileRemovedNetwork(AgentEntry entry);
	
	//Called BEFORE a tile is removed
	protected abstract void onTileRemove(AgentEntry entry);
	
	//Called after a tile has been removed(Note: Entry is now invalid for use, and Tile Entitly might have been invalidated)
	protected abstract void onTileRemoved(AgentEntry entry);
	
	//Called after the tile is renamed
	protected abstract void onTileRenamed(AgentEntry entry, String oldName);
	
}
