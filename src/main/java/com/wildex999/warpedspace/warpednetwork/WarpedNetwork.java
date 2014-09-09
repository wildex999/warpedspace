package com.wildex999.warpedspace.warpednetwork;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.ReturnMessage;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;

public class WarpedNetwork {
	
	private INetworkController controller;
	private Set<INode> nodeMap;
	
	//TODO: Turn relayList into a quadTree
	private Set<INetworkRelay> relayMap;
	
	//Map with each name pointing to a TileEntry on an Agent.
	//Note: Before using a Tile you should check the Agent for permission.
	private Map<String, AgentEntry> tileMap;
	
	private Map<String, List<INameListener>> nameListeners;
	private Map<String, List<IEntryListener>> entryListeners;
	private List<ITileListener> tileListeners;
	
	public int id;
	public String name;
	public String owner;
	public boolean isOnline = true; //TODO: Implement
	
	public WarpedNetwork(int id, String name, String owner) {
		nodeMap = new HashSet<INode>();
		relayMap = new HashSet<INetworkRelay>();
		tileMap = new HashMap<String, AgentEntry>();
		nameListeners = new HashMap<String, List<INameListener>>();
		entryListeners = new HashMap<String, List<IEntryListener>>();
		tileListeners = new ArrayList<ITileListener>();
		
		this.id = id;
		this.name = name;
		this.owner = owner;
	}
	
	//Register an agent for a tile with the given name
	public boolean addTile(AgentEntry tile) {
		if(tile == null || tileMap.containsKey(tile.name) || !tile.isValid())
			return false;
		
		tileMap.put(tile.name, tile);
		for(ITileListener listener : tileListeners)
			listener.tileAdded(tile);
		callEntryListeners(tile.name, true);
		
		return true;
	}
	
	//Add a new node to the Network
	public boolean addNode(INode node) {
		return nodeMap.add(node);
	}
	
	//Register a relay for the network
	public boolean addRelay(INetworkRelay relay) {
		return relayMap.add(relay);
	}
	
	//Remove node from network.
	//The node has the reponsebility to clean up
	//any other registered services.
	public void removeNode(INode node) {
		nodeMap.remove(node);
	}
	
	public void removeRelay(INetworkRelay relay) {
		relayMap.remove(relay);
	}
	
	//Rename a tile entry on the network
	//Returns:
	//Ok - The tile was renamed
	//TileNotExist - No tile with the given name exists on the network
	//TileNameTaken - The name is already taken
	public ReturnMessage renameTile(AgentEntry entry, String newName) {
		if(tileMap.containsKey(newName))
			return ReturnMessage.TileNameTaken;
		
		AgentEntry tile = tileMap.remove(entry.name);
		if(tile == null)
			return ReturnMessage.TileNotExist;
		
		tileMap.put(newName, tile);
		
		//Old name is now free, inform listeners
		callTileNameListeners(entry.name);
		
		//Tell anyone listening for entries with new name that it's available.
		//TODO: Move over to ID system, so this is removed.
		callEntryListeners(entry.name, true);
		
		return ReturnMessage.Ok;
	}
	
	public void removeTile(String name, AgentEntry tile) {
		if(tileMap.remove(name) != null)
		{
			callTileNameListeners(name);
			for(ITileListener listener: tileListeners)
				listener.tileRemoved(tile);
			callEntryListeners(name, false);
		}
	}
	
	//Mark a tile as not reachable on the network(But still reserving the name)
	//This is called when the Agent looses contact with the network
	public void setTileUnreachable(AgentEntry tile) {
		for(ITileListener listener : tileListeners)
			listener.tileUnavailable(tile);
	}
	
	//Mark tile as reachable on the network
	//Only called if it has earlier been marked as Unreachable.
	public void setTileReachable(AgentEntry tile) {
		for(ITileListener listener : tileListeners)
			listener.tileAvailable(tile);
	}
	
	private void callTileNameListeners(String name) {
		//Call name listeners
		
		//TODO: Keep a count of listeners in the tile entry itself to make removal
		//of entries with no listeners cheaper?(INodeAgent.addListener, .removeListener, .getListenerCount)
		//Or maybe Map<String, Pair<INodeAgent, Integer>>. (Avoid second name lookup)
		List<INameListener> listenerList = nameListeners.get(name);
		if(listenerList == null)
			return;
		
		Iterator<INameListener> iter = listenerList.iterator();
		while(iter.hasNext())
		{
			INameListener listener = iter.next();
			if(listener.onNameFree(name))
			{
				if(listenerList.size() == 1)
					nameListeners.remove(name);
				else
					iter.remove();
				
				return;
			}
		}
		
	}
	
	//Tell Entry listeners the entry is available/unavailable
	private void callEntryListeners(String entry, boolean available) {
		List<IEntryListener> listenerList = entryListeners.get(entry);
		if(listenerList == null)
			return;
		
		Iterator<IEntryListener> iter = listenerList.iterator();
		while(iter.hasNext())
		{
			IEntryListener listener = iter.next();
			boolean remove;
			if(available)
				remove = listener.onEntryAvailable(entry);
			else
				remove = listener.onEntryUnavailable(entry);
			
			if(remove)
			{
				if(listenerList.size() == 1)
					entryListeners.remove(entry);
				else
					iter.remove();
			}
		}
		
	}
	
	//Helper function.
	//Will do the lookup and permission check for the given Block
	//Returns null if any of these fails.
	public AgentEntry getBlock(INode requestingNode, String requestingPlayer, String tileName) {
		AgentEntry entry = tileMap.get(tileName);
		if(entry == null)
			return null;
		if(!entry.agent.canUseTile(requestingNode, requestingPlayer, entry))
			return null;
		
		return entry;
	}
	
	
	public int getTileCount() {
		return tileMap.size();
	}
	
	public int getNodeCount() {
		return nodeMap.size();
	}
	
	public int getRelayCount() {
		return relayMap.size();
	}
	
	public Map<String, AgentEntry> getTileMap() {
		return tileMap;
	}
	
	//Return a relay covering the given node
	//If gotConnection is true, it will return the first Relay within range who also has
	//a line to the Network Controller(I.e, a Relay that can be used now)
	//Returns null if no Relay is found.
	public INetworkRelay getRelayForNode(INode node, boolean gotConnection) {
		//TODO: Use Spatial Partitioning(QuadTree) for faster lookup
		
		//For now we only do two dimensional radius
		int nX = node.getPosX();
		int nZ = node.getPosZ();
		
		for(INetworkRelay relay : relayMap) {
			if(gotConnection)
			{
				if(!relay.isNetworkReachable())
					continue;
			}
			
			//Do Radius check
			int diffX = nX - relay.getPosX();
			int diffZ = nZ - relay.getPosZ();
			if((diffX*diffX) + (diffZ*diffZ) <= relay.getRadius()*relay.getRadius())
				return relay;
		}
		
		return null;
	}
	
	public boolean setController(INetworkController controller) {
		if(this.controller != null)
			this.controller.leaveNetwork();
		
		//TODO: Inform network of new controller(Event?)
		this.controller = controller;
		
		return true;
	}
	
	public INetworkController getController() {
		return controller;
	}
	
	//Add a listener for the given name
	public boolean registerNameListener(String name, INameListener listener) {
		List<INameListener> listenerList = nameListeners.get(name);
		if(listenerList == null)
		{
			listenerList = new LinkedList<INameListener>();
			nameListeners.put(name, listenerList);
		}
		
		return listenerList.add(listener);
	}
	
	//Remove the given listener for the given name
	public void unregisterNameListener(String name, INameListener listener) {
		List<INameListener> listenerList = nameListeners.get(name);
		if(listenerList == null)
			return;
		
		if(listenerList.size() == 1)
			nameListeners.remove(name);
		else
			listenerList.remove(listener);
	}
	
	public boolean registerEntryListener(String entry, IEntryListener listener) {
		List<IEntryListener> listenerList = entryListeners.get(entry);
		if(listenerList == null)
		{
			listenerList = new LinkedList<IEntryListener>();
			entryListeners.put(entry, listenerList);
		}
		
		return listenerList.add(listener);
	}
	
	public void unregisterEntryListener(String entry, IEntryListener listener) {
		List<IEntryListener> listenerList = entryListeners.get(entry);
		if(listenerList == null)
			return;
		
		if(listenerList.size() == 1)
			entryListeners.remove(entry);
		else
			listenerList.remove(listener);
	}
	
	public void registerTileListener(ITileListener listener) {
		tileListeners.add(listener);
	}
	
	public void unregisterTileListener(ITileListener listener) {
		tileListeners.remove(listener);
	}
	
}