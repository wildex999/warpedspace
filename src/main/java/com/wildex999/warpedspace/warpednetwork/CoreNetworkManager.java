package com.wildex999.warpedspace.warpednetwork;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.warpednetwork.MessageSCNetworkList;
import com.wildex999.warpedspace.networking.warpednetwork.MessageSCNetworkUpdate;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/*
 * Manages the internal list of networks, and allows adding, removing and renaming.
 */

public class CoreNetworkManager {
	// Named map of all networks on server
	// TODO: Private networks(With invite and permisions for other people)
	public Map<Integer, WarpedNetwork> networks = new HashMap<Integer, WarpedNetwork>();
	public TreeMap<String, Integer> networkNames = new TreeMap<String, Integer>(); // Name to Int map
	public int nextFreeId; // Should be stored in global network list, to avoid id reuse.

	//Work around in single-player(And future allow per World networks)
	public static CoreNetworkManager serverNetworkManager = new CoreNetworkManager();
	public static CoreNetworkManager clientNetworkManager = new CoreNetworkManager();
	
	public static CoreNetworkManager getInstance(World world) {
		if(world.isRemote)
			return clientNetworkManager;
		else
			return serverNetworkManager;
	}
	
	// Add a new network, returning it's id on success, or a negative number on failure
	// -1 = Name exists
	public int addNetwork(String name, String owner) {
		if (networkNames.containsKey(name))
			return -1;
		int id = nextFreeId++;
		WarpedNetwork newNetwork = new WarpedNetwork(id, name, owner);
		newNetwork.owner = owner;

		networks.put(id, newNetwork);
		networkNames.put(name, id);

		NetworkSaveHandler.isDirty = true;

		// Send update message to all clients
		MessageBase createMessage = new MessageSCNetworkUpdate(id, name, owner);
		createMessage.sendToAll();

		return id;
	}

	// Called client side to add network
	@SideOnly(Side.CLIENT)
	public void clientAddNetwork(int id, String name, String owner) {
		WarpedNetwork newNetwork = new WarpedNetwork(id, name, owner);
		networks.put(id, newNetwork);
		networkNames.put(name, id);
	}

	//Set the network for the given id. Used when loading networks.
	//Does not send update to players.
	public boolean setNetwork(int id, String name, String owner) {
		if(networks.containsKey(id))
		{
			ModLog.logger.warn("Trying to set network would overwrite existing: " + id + " name: " + name);
			return false;
		}
		
		if(networkNames.containsKey(name))
		{
			ModLog.logger.warn("Trying to set network name mapping conflicts with existing: " + name);
			return false;
		}
		
		WarpedNetwork newNetwork = new WarpedNetwork(id, name, owner);
		newNetwork.owner = owner;
		
		networks.put(id, newNetwork);
		networkNames.put(name, id);
		
		return true;
	}
	
	// Remove a network, returns true if removed, or false if it didn't exist
	public boolean removeNetwork(int networkId) {
		WarpedNetwork network = networks.get(networkId);

		if (network == null)
			return false;

		networks.remove(networkId);
		networkNames.remove(network.name);

		NetworkSaveHandler.isDirty = true;

		// Send update message to all clients
		MessageBase removeMessage = new MessageSCNetworkUpdate(networkId);
		removeMessage.sendToAll();

		return true;
	}
	
	@SideOnly(Side.CLIENT)
	public void clientRemoveNetwork(int networkId) {
		WarpedNetwork network = networks.get(networkId);
		
		if(network == null)
		{
			ModLog.logger.warn("Client Remove network failed, network did not exist: " + networkId);
			return;
		}
		
		networks.remove(networkId);
		networkNames.remove(network.name);
	}

	// Rename a network, returning true on success, or false if it failed
	public boolean renameNetwork(int networkId, String newName) {

		WarpedNetwork network = networks.get(networkId);
		if (network == null)
			return false;

		if (networkNames.containsKey(newName))
			return false;

		if (networkNames.remove(network.name) == null) {
			// Assert: This should not happen, it would mean id and names are
			// out of sync
			ModLog.logger.warn("Network with id " + networkId + " missing name map during rename from "
					+ network.name + " to " + newName + ". Names and id's may be out of sync!");
			return false;
		}

		networkNames.put(newName, networkId);
		network.name = newName;

		NetworkSaveHandler.isDirty = true;

		// Send update message to all clients
		MessageBase renameMessage = new MessageSCNetworkUpdate(networkId, newName, false);
		renameMessage.sendToAll();
		
		ModLog.logger.info("Renamed to: " + newName);

		return true;
	}
	
	@SideOnly(Side.CLIENT)
	public void clientRenameNetwork(int networkId, String newName) {
		WarpedNetwork network = networks.get(networkId);
		
		if(network == null)
		{
			ModLog.logger.warn("Client rename network failed, network did not exist: " + networkId);
			return;
		}
		
		if(networkNames.remove(network.name) == null) {
			ModLog.logger.warn("Network with id " + networkId + " missing name map during rename from "
					+ network.name + " to " + newName + ". Names and id's may be out of sync!");
			return;
		}
		
		network.name = newName;
		networkNames.put(newName, networkId);
	}
	
	//Remove all listed networks
	public void clearNetworks() {
		networks.clear();
		networkNames.clear();
	}
	
	//On Player joined, send network list
	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent loginEvent) {
		ModLog.logger.info("Sending list to player logging in: " + loginEvent.player.getGameProfile().getName());
		MessageBase listMessage = new MessageSCNetworkList(this);
		listMessage.sendToPlayer((EntityPlayerMP)loginEvent.player);
	}
}
