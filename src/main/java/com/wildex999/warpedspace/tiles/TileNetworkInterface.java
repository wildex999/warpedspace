package com.wildex999.warpedspace.tiles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import akka.actor.FSM.State;

import com.wildex999.utils.BlockItemName;
import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.Messages;
import com.wildex999.warpedspace.TickHandler;
import com.wildex999.warpedspace.gui.interfaces.IGuiWatchers;
import com.wildex999.warpedspace.items.ItemNetworkCard;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.netinterface.MessageSCInterfaceUpdate;
import com.wildex999.warpedspace.networking.netinterface.MessageTilesList;
import com.wildex999.warpedspace.networking.netinterface.MessageTilesUpdate;
import com.wildex999.warpedspace.warpednetwork.BaseNodeTile;
import com.wildex999.warpedspace.warpednetwork.IEntryListener;
import com.wildex999.warpedspace.warpednetwork.INetworkRelay;
import com.wildex999.warpedspace.warpednetwork.ITileListener;
import com.wildex999.warpedspace.warpednetwork.AgentEntry;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileNetworkInterface extends BaseNodeTile implements IGuiWatchers, ITileListener, IEntryListener {
	private HashSet<EntityPlayer> watchers;
	private HashSet<EntityPlayerMP> tileWatchers;
	
	public String storedEntry;
	public long storedGid;
	public AgentEntry currentEntry;
	
	//Client data
	@SideOnly(Side.CLIENT)
	public Block hostBlock = null;
	@SideOnly(Side.CLIENT)
	public int x,y,z;
	@SideOnly(Side.CLIENT)
	public NBTTagCompound tileTag;
	
	public TileNetworkInterface() {
		inventoryName = "Network Interface";

		watchers = new HashSet<EntityPlayer>();
		tileWatchers = new HashSet<EntityPlayerMP>();
		storedEntry = "";
		storedGid = -1;
		currentEntry = null;
	}
	
	//Send GUI update to clients.
	//player: Player to send update to. If null it will send to every watcher
	//queue: Whether or not to queue the message until next tick
	public boolean sendGuiUpdate(EntityPlayer player, boolean queue) {
		if(currentEntry != null && !currentEntry.isValid())
		{
			//entry cache invalid, try to get new with gid
			currentEntry = null;
			setEntry(owner, storedGid, storedEntry);
			return false;
		}
		
		if(player == null && watchers.size() == 0)
			return false;
		
		String itemName = "";
		byte itemMeta = 0;
		int entryState;
		if(storedEntry.length() != 0)
			entryState = Messages.offline;
		else
			entryState = Messages.notSet;
		
		if(currentEntry != null)
		{
			storedEntry = currentEntry.name; //Update stored name in case of rename
			storedGid = currentEntry.gid; //GID should not change, but just in case(For future)
			
			if(currentEntry.active)
				entryState = Messages.online;
			else
				entryState = Messages.offline;
			
			itemName = BlockItemName.get(currentEntry.block, currentEntry.world, currentEntry.x, currentEntry.y, currentEntry.z);
			itemMeta = (byte)currentEntry.world.getBlockMetadata(currentEntry.x, currentEntry.y, currentEntry.z);
		}
		ModLog.logger.info("Interface GUI Update: " + storedEntry + " gid: " + storedGid);
		MessageBase messageUpdate = new MessageSCInterfaceUpdate(getNetworkStateMessage(), entryState, storedEntry, storedGid, itemName, itemMeta);
		
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
		
		//Right clicking will glitch the render, so we just send the data packet again when people are in GUI
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		
		return true;
	}
	
	//Update from Client
	public void clientUpdate(EntityPlayerMP player, long gid) {
		setEntry(owner, gid, "");
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	public void setEntry(String player, long gid, String entryName) {
		//Try to set current entry to the selected
		if(gid < 0)
		{
			if(currentNetwork != null && storedGid >= 0)
				currentNetwork.unregisterEntryListener(storedGid, this);
			
			currentEntry = null;
			storedEntry = "";
			storedGid = -1;
		}
		else
		{
			storedEntry = entryName;
			if(currentNetwork != null)
			{
				AgentEntry oldEntry = currentEntry;
				currentEntry = currentNetwork.getBlock(this, player, gid);
				if(storedGid != gid)
				{
					currentNetwork.unregisterEntryListener(storedGid, this);
					currentNetwork.registerEntryListener(gid, this);
				}
				
				if(currentEntry != null) //TODO: Decide if we failed due to the entry not existing or permissions denied us.
				{

					storedEntry = currentEntry.name;
					storedGid = currentEntry.gid;
				}
				
				if(currentEntry != oldEntry)
				{
					ModLog.logger.info("ENTRY CHANGED!");
					this.markDirty();
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
					sendGuiUpdate(null, false);
				}
			}
		}

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
		
		if(storedGid >= 0)
			currentNetwork.registerEntryListener(storedGid, this);
		
		setEntry(owner, storedGid, storedEntry);
		
		if(tileWatchers.size() > 0)
			currentNetwork.registerTileListener(this);
		
		sendGuiUpdate(null, false);
		
		return true;
	}
	
	@Override
	public void leaveNetwork() {
		currentNetwork.unregisterTileListener(this);
		if(storedGid >= 0)
			currentNetwork.unregisterEntryListener(storedGid, this);
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
		
		data.setString("selectionName", storedEntry);
		data.setLong("selectionGid", storedGid);
		
		//TODO: Write settings
	}
	
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		
		storedEntry = data.getString("selectionName");
		storedGid = data.getLong("selectionGid");
		
		//TODO: Read settings
	}
	
	@Override
	public void addWatcher(EntityPlayer player) {
		if(worldObj.isRemote)
			return;
		watchers.add(player);
		if(!sendGuiUpdate(player, true))
			sendGuiUpdate(player, true); //Send might fail due to Entity update, and will not queue, so retry
	}

	@Override
	public void removeWatcher(EntityPlayer player) {
		if(worldObj.isRemote)
			return;
		watchers.remove(player);
	}

	@Override
	public void tileAdded(AgentEntry tile) {
		byte tileMeta = (byte)tile.world.getBlockMetadata(tile.x, tile.y, tile.z);
		MessageBase messageUpdate = new MessageTilesUpdate(tile.name, BlockItemName.get(tile.block, tile.world, tile.x, tile.y, tile.z), tileMeta, tile.gid, tile.active);
		for(EntityPlayerMP watcher : tileWatchers)
			messageUpdate.sendToPlayer(watcher);
	}

	@Override
	public void tileRemoved(AgentEntry tile) {
		MessageBase messageUpdate = new MessageTilesUpdate(tile.name);
		for(EntityPlayerMP watcher : tileWatchers)
			messageUpdate.sendToPlayer(watcher);
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
	public boolean onEntryAvailable(long gid) {
		//Our entry now exists, try to get it
		this.setEntry(owner, gid, storedEntry);
		return false;
	}

	@Override
	public boolean onEntryUnavailable(long gid) {
		this.setEntry(owner, gid, storedEntry);
		return false;
	}
	
	@Override
	public Packet getDescriptionPacket() {
        NBTTagCompound data = new NBTTagCompound();
        if(currentEntry == null)
        	return null;
        
        //TODO: Listen for changes in selected Entry, and update client when changed(Lever switch, changed block etc.)
        
        data.setString("item", BlockItemName.get(currentEntry.block, currentEntry.world, currentEntry.x, currentEntry.y, currentEntry.z));
        data.setByte("meta", (byte)currentEntry.world.getBlockMetadata(currentEntry.x, currentEntry.y, currentEntry.z));
        
        //Send TileEntity and it's getDescriptionPacket and re-create it on the client while rendering(If the tile is not in client loaded chunk)
        data.setInteger("x", currentEntry.x);
        data.setInteger("y", currentEntry.y);
        data.setInteger("z", currentEntry.z);
        
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, data);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		NBTTagCompound data = pkt.func_148857_g();

		String itemName = data.getString("item");
		byte itemMeta = data.getByte("meta");
		x = data.getInteger("x");
		y = data.getInteger("y");
		z = data.getInteger("z");
		
		this.hostBlock = Block.getBlockFromName(itemName);
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, itemMeta, 1);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		ModLog.logger.info("DATA PACKET: " + hostBlock);
		
	}
	
}
