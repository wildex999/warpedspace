package com.wildex999.warpedspace.tiles;

import java.util.ArrayList;
import java.util.HashSet;
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
import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.blocks.BlockNetworkAgent;
import com.wildex999.warpedspace.gui.NetworkAgentGui;
import com.wildex999.warpedspace.gui.interfaces.IGuiWatchers;
import com.wildex999.warpedspace.items.ItemNetworkCard;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.MessageNetworkAgentUpdate;
import com.wildex999.warpedspace.networking.MessageNetworkManagerUpdate;
import com.wildex999.warpedspace.warpednetwork.AgentNodeTile;
import com.wildex999.warpedspace.warpednetwork.BlockPosition;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.INetworkRelay;
import com.wildex999.warpedspace.warpednetwork.INode;
import com.wildex999.warpedspace.warpednetwork.INetworkAgent;
import com.wildex999.warpedspace.warpednetwork.AgentEntry;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

public class TileNetworkAgent extends AgentNodeTile implements IPreTickOneShotListener, IGuiWatchers {
	private static final Random rand = new Random();
	private BlockNetworkAgent block;
	private AgentEntry[] entries = new AgentEntry[sideCount]; //Internal ID for each direction(-1 if not joined)
	
	@SideOnly(Side.CLIENT)
	private boolean[] sideState;
	
	private HashSet<EntityPlayer> watchers;
	
	//Stored/Synced
	private String nameList[] = new String[sideCount]; //If name is stored, but idList has it at -1 that means the name is set, but not added to network.
	private long gidList[] = new long[sideCount]; //Gid's for initial load
	
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
		watchers = new HashSet<EntityPlayer>();
		
		if(WarpedSpace.isClient)
			clientInit();
	}
	
	@SideOnly(Side.CLIENT)
	public void clientInit() {
		sideState = new boolean[sideCount];
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
			byte tileMeta = 0;
			if(entry != null && i != sideCount-1) //No need to send the name of the agent
			{
				tileName = BlockItemName.get(entry.block, entry.world, entry.x, entry.y, entry.z);
				tileMeta = (byte)entry.world.getBlockMetadata(entry.x, entry.y, entry.z);
			}
			
			boolean active = (name.length() != 0 && entry != null && entry.active);
			tiles[i] = new NetworkAgentGui.TileState(name, tileName, tileMeta, active);
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
			{
				//onNeighborBlockUpdate
				if(currentNetwork != null)
					currentNetwork.entryUpdate(oldEntry);
				return;
			}
		
		if(newTile == null) //Remove tile
		{
			//TODO: Make it optional whether to reuse gid when destroying block(Toggle)
			gidList[side] = oldEntry.gid;
			ModLog.logger.info("RemoveTile: " + oldTile);
			removeTile(side);
			
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return;
		}
		
		if(oldTile == null) //Setting new tile
		{
			ModLog.logger.info("New Tile: " + newTile.block);
			long prevGid = gidList[side];
			gidList[side] = (long)-1; //Only use stored gid once
			ReturnMessage ret = addTile(nameList[side], newTile, side, prevGid);
			if(ret != ReturnMessage.TileAdded)
				ModLog.logger.info("Failed: " + ret + " with name: " + nameList[side]);
			
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return;
		}
		else //Replacing existing tile
		{
			ModLog.logger.info("Old tile: " + oldTile + " | new tile: " + newTile.block);
			long oldGid = oldEntry.gid; //Reuse gid when blocks are replaced(State change for Furnace etc.)
			removeTile(side);
			ReturnMessage ret = addTile(nameList[side], newTile, side, oldGid);
			if(ret != ReturnMessage.TileAdded)
				ModLog.logger.info("Failed: " + ret + " with name: " + nameList[side]);
			return;
		}
	}
	
	//Set the tile with the given name for direction
	//If the name is empty or null, it will generate a default name.
	//If gid is set > 0, it will be re-used on the new AgentEntry.
	//Returns:
	//TileAdded - The tile was added to the Agent and network
	//TileAlreadyAdded - A tile has already been added for this side
	private ReturnMessage addTile(String name, BlockPosition block, int dirIndex, long gid) {
		ReturnMessage ret;
		AgentEntry entry = entries[dirIndex];
		
		if(entry != null)
			return ReturnMessage.TileAlreadyAdded;
		
		if(name == null || name.length() == 0)
			name = getDefaultName(block);
		
		nameList[dirIndex] = name; //Always update the name
		
		entry = new AgentEntry();
		if(gid > 0)
			entry.gid = gid;
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
			if(tile instanceof INetworkAgent)
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
	
	public AgentEntry getEntry(int side) {
		if(side < 0 || side >= sideCount)
			return null;
		return entries[side];
	}
	
	//Returns true if there is an entry for the given side
	public boolean sideUsed(int side) {
		if(side < 0 || side >= sideCount)
			return false;
		
		if(worldObj.isRemote)
			return sideState[side];
		else
			return entries[side] != null;
	}
	
	@Override
	protected void onCacheInvalidate() {
		//TODO: Clear idList and then get the id of each side.
	}
	
	@Override
	public void validate() {
		if(worldObj.isRemote)
			return;
		super.validate();
		TickHandler.registerListener(this);
	}
	
	@Override
	public void invalidate() {
		if(worldObj.isRemote)
			return;
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
		
		for(int i=0; i < sideCount; i++)
		{
			//Name
			nameList[i] = data.getString("nameSide" + i);
			
			//GID
			String key = "gidSide" + i;
			if(data.hasKey(key))
				gidList[i] = data.getLong(key);
			else
				gidList[i] = (long)-1;
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		
		for(int i=0; i < sideCount; i++)
		{
			//Name
			String name = nameList[i];
			if(name != null && name.length() != 0)
				data.setString("nameSide" + i, name);
			
			//GID
			AgentEntry entry = entries[i];
			String key = "gidSide" + i;
			if(entry != null)
				data.setLong(key, entry.gid);
			else
				data.setLong(key, gidList[i]);
		}

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
		//Check for pending renames
		for(int side = 0; side < sideCount; side++)
		{
			AgentEntry entry = entries[side];
			if(entry == null)
				continue;
			if(!entry.name.equals(nameList[side]))
				onClientInput(side, nameList[side]);
		}
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
	
	@Override
	public Packet getDescriptionPacket() {
        NBTTagCompound data = new NBTTagCompound();

        for(int i=0; i<sideCount; i++)
        	data.setBoolean("s"+i, sideUsed(i));
        
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, data);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		NBTTagCompound data = pkt.func_148857_g();
		
		for(int i=0; i<sideCount; i++)
			sideState[i] = data.getBoolean("s"+i);
	}
	
	
}
