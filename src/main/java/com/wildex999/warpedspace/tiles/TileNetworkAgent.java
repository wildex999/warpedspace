package com.wildex999.warpedspace.tiles;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import scala.Int;

import com.wildex999.utils.BlockItemName;
import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.Messages;
import com.wildex999.warpedspace.ReturnMessage;
import com.wildex999.warpedspace.TickHandler;
import com.wildex999.warpedspace.blocks.BlockNetworkAgent;
import com.wildex999.warpedspace.gui.NetworkAgentGui;
import com.wildex999.warpedspace.gui.interfaces.IGuiWatchers;
import com.wildex999.warpedspace.items.ItemNetworkCard;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.MessageNetworkAgentUpdate;
import com.wildex999.warpedspace.networking.MessageNetworkManagerUpdate;
import com.wildex999.warpedspace.warpednetwork.AgentNode;
import com.wildex999.warpedspace.warpednetwork.BlockPosition;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.INetworkRelay;
import com.wildex999.warpedspace.warpednetwork.INode;
import com.wildex999.warpedspace.warpednetwork.INodeAgent;
import com.wildex999.warpedspace.warpednetwork.AgentEntry;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

public class TileNetworkAgent extends AgentNode implements IPreTickOneShotListener, IGuiWatchers {
	private static final Random rand = new Random();
	private BlockNetworkAgent block;
	private AgentEntry[] entries = new AgentEntry[sideCount]; //Internal ID for each direction(-1 if not joined)
	
	private List<EntityPlayer> watchers;
	
	//Stored/Synced
	private String nameList[] = new String[sideCount]; //If name is stored, but idList has it at -1 that means the name is set, but not added to network.
	
	//Tile Direction Index
	public static final byte NORTH = 0;
	public static final byte SOUTH = 1;
	public static final byte WEST = 2;
	public static final byte EAST = 3;
	public static final byte TOP = 4;
	public static final byte BOTTOM = 5;
	public static final byte NODENAME = 6;
	public static final byte sideCount = 7;
	
	public TileNetworkAgent() {
		inventoryName = "Network Agent";
		for(int i = 0; i<sideCount; i++)
		{
			entries[i] = null;
			nameList[i] = null;
		}
		watchers = new ArrayList<EntityPlayer>();
	}
	
	//Send GUI update to player.
	//If player is null, send to all watcher.
	public void sendNetworkUpdate(EntityPlayerMP player, boolean queue) {
		NetworkAgentGui.TileState[] tiles = new NetworkAgentGui.TileState[sideCount];
		
		if(player == null && watchers.size() == 0)
			return;
		
		//Fill Tile list
		for(int i = 0; i < sideCount; i++)
		{
			String name = nameList[i];
			if(name == null)
				name = "";
			
			//Get Item or Block name
			AgentEntry entry = entries[i];
			String tileName = "";
			if(entry != null && i != sideCount-1) //No need to send the name of the agent
				tileName = BlockItemName.get(entry.block, entry.world, entry.x, entry.y, entry.z);
			
			boolean active = (name.length() != 0 && entries[i] != null && entries[i].active);
			tiles[i] = new NetworkAgentGui.TileState(name, tileName, active);
		}
		
		
		MessageBase messageUpdate = new MessageNetworkAgentUpdate(getNetworkStateMessage(), tiles);
		if(player != null)
		{
			if(queue)
				messageUpdate.queueToPlayer(player);
			else
				messageUpdate.sendToPlayer(player);
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
	
	//Check for neighbor tile entities
	//TODO: Create function for single side update
	public void updateNeighborTiles()
	{
		if(worldObj.isRemote)
			return;
		
		ModLog.logger.info("UPDATE");
		
		//Test if changed compared to what is currently
		for(int i = 0; i < sideCount; i++)
			updateNeighborSide(i);
		
		sendNetworkUpdate(null, false);
	}
	
	protected BlockPosition checkSide(int side) {
		int x = xCoord;
		int y = yCoord;
		int z = zCoord;
		Block block;
		
		switch(side) {
		case WEST:
			x -= 1;
			break;
		case EAST:
			x += 1;
			break;
		case NORTH:
			z -= 1;
			break;
		case SOUTH:
			z += 1;
			break;
		case TOP:
			y += 1;
			break;
		case BOTTOM:
			y -= 1;
			break;
		case NODENAME:
			return new BlockPosition(this.getBlockType(), x, y, z);
			default:
				return null;
		}
		block = worldObj.getBlock(x, y, z);
		return new BlockPosition(block, x, y, z);
	}
	
	//Update neightbor on side
	protected void updateNeighborSide(int side) {
		BlockPosition newTile = checkSide(side);
		Block oldTile;
		
		if(newTile.block == Blocks.air)
			newTile = null;
		
		AgentEntry oldEntry = entries[side];
		if(oldEntry == null || !oldEntry.isValid())
			oldTile = null;
		else
			oldTile = oldEntry.block;
		
		if(newTile == null && oldTile == null)
			return; //No change
		
		if(newTile != null)
			if(newTile.block == oldTile)
				return; //No change
		
		if(newTile == null) //Remove tile
		{
			ModLog.logger.info("RemoveTile: " + oldTile);
			removeTile(side);
			return;
		}
		
		if(oldTile == null) //Setting new tile
		{
			ModLog.logger.info("New Tile: " + newTile.block);
			ReturnMessage ret = addTile(nameList[side], newTile, side);
			if(ret != ReturnMessage.TileAdded)
				ModLog.logger.info("Failed: " + ret + " with name: " + nameList[side]);
			return;
		}
		else //Replacing existing tile
		{
			ModLog.logger.info("Old tile: " + oldTile + " | new tile: " + newTile.block);
			removeTile(side);
			ReturnMessage ret = addTile(nameList[side], newTile, side);
			if(ret != ReturnMessage.TileAdded)
				ModLog.logger.info("Failed: " + ret + " with name: " + nameList[side]);
			return;
		}
	}
	
	//Set the tile with the given name for direction
	//If the name is empty or null, it will generate a default name.
	//Returns:
	//TileAdded - The tile was added to the Agent and network
	//TileAlreadyAdded - A tile has already been added for this side
	private ReturnMessage addTile(String name, BlockPosition block, int dirIndex) {
		ReturnMessage ret;
		AgentEntry entry = entries[dirIndex];
		
		if(entry != null)
			return ReturnMessage.TileAlreadyAdded;
		
		if(name == null || name.length() == 0)
			name = getDefaultName(block);
		
		nameList[dirIndex] = name; //Always update the name
		
		entry = new AgentEntry();
		ret = setTile(name, block.block, worldObj, block.x, block.y, block.z, entry);
		
		entries[dirIndex] = entry;
		
		return ret;	
	}
	
	//Remove the tile for the given direction
	private void removeTile(int dirIndex) {
		String name = nameList[dirIndex];
		
		if(entries[dirIndex] == null)
			return;
		
		removeTile(entries[dirIndex]);
		entries[dirIndex] = null;
	}
	
	//Get a default name for a given block, checking tile entites
	private String getDefaultName(BlockPosition block) {
		String type;
		
		TileEntity tile = worldObj.getTileEntity(block.x, block.y, block.z);
		
		if(tile != null)
		{
			if(tile instanceof INodeAgent)
				type = "Agent_";
			else if(tile instanceof INetworkRelay)
				type = "Relay_";
			else
				type = "Tile_";
		} else
			type = "Block_";
		
		return type + block.x + "_" + block.y + "_" + block.z;
	}
	
	//Update sent from Client to server
	public boolean onClientInput(int index, String name) {
		if(index < 0 || index >= sideCount)
			return false;
		
		AgentEntry entry = entries[index];
		
		/*//Check for update from side(Could have had name conflict when not in network, causing the tile to have not been added)
		if(entry == null)
		{
			nameList[index] = name;
			updateNeighborSide(index);
			id = idList[index];
		}*/
		
		if(name.length() == 0 && entry != null)
			name = getDefaultName(new BlockPosition(entry.block, entry.x, entry.y, entry.z));
		
		String oldName = nameList[index];
		nameList[index] = name;
		
		if(entry == null)
			return true; //Not added, but renamed for when added
		
		//Try to rename
		if(this.renameTile(entry, name) == ReturnMessage.InternalError)
			nameList[index] = oldName;
		
		return true;
	}
	
	@Override
	protected void onCacheInvalidate() {
		//TODO: Clear idList and then get the id of each side.
	}
	
	@Override
	public void validate() {
		super.validate();
		TickHandler.registerListener(this);
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		leaveNetwork();
	}
	
	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		leaveNetwork();
	}	
	
	@Override
	public void onLoadComplete() {
		onNetworkCardUpdate();
		updateNeighborTiles();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		nameList[NORTH] = data.getString("northName");
		nameList[SOUTH] = data.getString("southName");
		nameList[WEST] = data.getString("westName");
		nameList[EAST] = data.getString("eastName");
		nameList[TOP] = data.getString("topName");
		nameList[BOTTOM] = data.getString("bottomName");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		
		String name = nameList[NORTH];
		if(name != null && name.length() != 0)
			data.setString("northName", name);
		
		name = nameList[SOUTH];
		if(name != null && name.length() != 0)
			data.setString("southName", name);
		
		name = nameList[WEST];
		if(name!= null && name.length() != 0)
			data.setString("westName", name);
		
		name = nameList[EAST];
		if(name != null && name.length() != 0)
			data.setString("eastName", nameList[EAST]);
		
		name = nameList[TOP];
		if(name != null && name.length() != 0)
			data.setString("topName", name);
		
		name = nameList[BOTTOM];
		if(name != null && name.length() != 0)
			data.setString("bottomName", name);
	}

	@Override
	public void addWatcher(EntityPlayer player) {
		if(!worldObj.isRemote)
		{
			watchers.add(player);
			
			//Send update packet to player
			sendNetworkUpdate((EntityPlayerMP)player, true);
		}
	}

	@Override
	public void removeWatcher(EntityPlayer player) {
		if(!worldObj.isRemote)
			watchers.remove(player);
	}

	@Override
	public boolean joinRelay(INetworkRelay relay) {
		boolean ret = super.joinRelay(relay);
		if(ret)
			sendNetworkUpdate(null, false);
		return ret;
	}
	
	@Override
	protected void onJoinedNetwork() {
		sendNetworkUpdate(null, false);
	}

	@Override
	protected void onLeavingNetwork() {
	}

	@Override
	protected void onTileSet(AgentEntry entry) {
		sendNetworkUpdate(null, false);
	}

	@Override
	protected void onTileAdded(AgentEntry entry) {
		sendNetworkUpdate(null, false);
	}

	@Override
	protected void onTileRemovedNetwork(AgentEntry entry) {
		sendNetworkUpdate(null, false);
	}

	@Override
	protected void onTileRemove(AgentEntry entry) {
	}

	@Override
	protected void onTileRenamed(AgentEntry entry, String newName) {
		sendNetworkUpdate(null, false);
	}

	@Override
	protected void onTileRemoved(AgentEntry entry) {
		sendNetworkUpdate(null, false);
	}

	@Override
	protected void onLeftNetwork() {
		sendNetworkUpdate(null, false);
	}
	
	

}
