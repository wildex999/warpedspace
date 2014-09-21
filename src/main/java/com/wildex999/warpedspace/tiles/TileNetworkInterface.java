package com.wildex999.warpedspace.tiles;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import akka.actor.FSM.State;

import com.wildex999.utils.BlockItemName;
import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.Messages;
import com.wildex999.warpedspace.TickHandler;
import com.wildex999.warpedspace.gui.interfaces.IGuiWatchers;
import com.wildex999.warpedspace.items.ItemNetworkCard;
import com.wildex999.warpedspace.items.ItemPortableNetworkInterface;
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
import com.wildex999.warpedspace.warpednetwork.iface.InterfaceInventoryManager;
import com.wildex999.warpedspace.warpednetwork.iface.InterfaceRedstoneManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileNetworkInterface extends BaseNodeTile implements IGuiWatchers, ITileListener, IEntryListener, ISidedInventory {
	private HashSet<EntityPlayer> watchers;
	private HashSet<EntityPlayerMP> tileWatchers;
	
	public EntityPlayer itemPlayer; //Set to the player using this as a Proxy for ItemPortableInterface
	
	public String storedEntry;
	public long storedGid;
	public AgentEntry currentEntry;
	
	private InterfaceRedstoneManager redstoneManager;
	private InterfaceInventoryManager inventoryManager;
	
	//Client data
	@SideOnly(Side.CLIENT)
	public Block hostBlock;
	@SideOnly(Side.CLIENT)
	public int x,y,z;
	@SideOnly(Side.CLIENT)
	public TileEntity proxyTile;
	
	public TileNetworkInterface() {
		inventoryName = "Network Interface";

		watchers = new HashSet<EntityPlayer>();
		tileWatchers = new HashSet<EntityPlayerMP>();
		storedEntry = "";
		storedGid = -1;
		currentEntry = null;
		
		redstoneManager = new InterfaceRedstoneManager(this);
		inventoryManager = new InterfaceInventoryManager(this);
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
			
			if(currentEntry.active && currentEntry.agent.isNetworkReachable())
				entryState = Messages.online;
			else
				entryState = Messages.offline;
			
			itemName = BlockItemName.get(currentEntry.block, currentEntry.world, currentEntry.x, currentEntry.y, currentEntry.z);
			itemMeta = (byte)currentEntry.world.getBlockMetadata(currentEntry.x, currentEntry.y, currentEntry.z);
		}

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
		
		//Right clicking will glitch the render(Server resends block), so we just send the data packet again when people are in GUI
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
					//Make sure we're not hosting ourself(Causes a LOT of problems due to infinite loops)
					if(currentEntry.world == this.worldObj && currentEntry.x == xCoord && currentEntry.y == yCoord && currentEntry.z == zCoord)
					{
						currentEntry = null;
					}
					else
					{
						storedEntry = currentEntry.name;
						storedGid = currentEntry.gid;
					}
				}
				
				if(currentEntry != oldEntry)
				{
					this.markDirty();
					entryUpdated();
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
	
	//Entry updated(Added, removed, updated)
	public void entryUpdated() {
		redstoneManager.update();
		inventoryManager.update();
		//TODO: Call onNeighborUpdate(tile)
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
        //If we are a redstone provider, we need to inform neighbors one step further out
        //about our update(For weak redstone through a block)
        if(redstoneManager.gotPower)
        {
            worldObj.notifyBlocksOfNeighborChange(xCoord+1, yCoord, zCoord, getBlockType());
            worldObj.notifyBlocksOfNeighborChange(xCoord-1, yCoord, zCoord, getBlockType());
            worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord+1, zCoord, getBlockType());
            worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord-1, zCoord, getBlockType());
            worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord+1, getBlockType());
            worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord-1, getBlockType());
        }

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
		
		//If portable proxy, update the network stored
		if(itemPlayer != null)
		{
			ItemStack currentStack = itemPlayer.inventory.getCurrentItem();
			if(currentStack != null && currentStack.getItem() instanceof ItemPortableNetworkInterface)
				ItemPortableNetworkInterface.setNetwork(currentStack, currentNetwork);
		}
		
		return true;
	}
	
	@Override
	public void leaveNetwork() {
		if(currentNetwork == null)
			return;
		
		currentNetwork.unregisterTileListener(this);
		if(storedGid >= 0)
			currentNetwork.unregisterEntryListener(storedGid, this);
		super.leaveNetwork();
		currentEntry = null;
		
		entryUpdated();
		sendGuiUpdate(null, false);
		
		//If portable proxy, update the network stored
		if(itemPlayer != null)
		{
			ItemStack currentStack = itemPlayer.inventory.getCurrentItem();
			if(currentStack != null && currentStack.getItem() instanceof ItemPortableNetworkInterface)
				ItemPortableNetworkInterface.setNetwork(currentStack, null);
		}
	}
	
	@Override
	public boolean joinRelay(INetworkRelay relay) {
		if(super.joinRelay(relay))
		{
			sendGuiUpdate(null, false);
			entryUpdated();
			return true;
		}
		else
			return false;
	}
	
	@Override
	public void eventLostRelay() {
		super.eventLostRelay();
		
		entryUpdated();
		sendGuiUpdate(null, false);
	}
	
	@Override
	public void eventNetworkDisconnect() {
		super.eventNetworkDisconnect();
		
		entryUpdated();
		sendGuiUpdate(null, false);
	}
	
	@Override
	public void eventNetworkReconnect() {
		super.eventNetworkReconnect();
		
		entryUpdated();
		sendGuiUpdate(null, false);
	}
	
	@Override
	public void eventLostNetwork() {
		super.eventLostNetwork();
		
		entryUpdated();
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
	public boolean onEntryUpdate(AgentEntry entry) {
		//Entry Block has updated(reachable, onNeighborBlockChange etc.), send update to clients
		//TODO: When tile is not reachable, who it on the rendering(Red outline/cover? Maybe render text: OFFLINE)
		entryUpdated();
		sendGuiUpdate(null, false);
		
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
        if(currentEntry == null || !currentEntry.isValid() || !currentEntry.active)
        	return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, data);
        
        //TODO: Listen for changes in selected Entry, and update client when changed(Lever switch, changed block etc.)
        
        data.setString("item", BlockItemName.get(currentEntry.block, currentEntry.world, currentEntry.x, currentEntry.y, currentEntry.z));
        data.setByte("meta", (byte)currentEntry.world.getBlockMetadata(currentEntry.x, currentEntry.y, currentEntry.z));
        
        //Send TileEntity and it's getDescriptionPacket and re-create it on the client while rendering(If the tile is not in client loaded chunk)
        data.setInteger("x", currentEntry.x);
        data.setInteger("y", currentEntry.y);
        data.setInteger("z", currentEntry.z);
        
        //TODO: Only send tile data if client does not have chunk loaded(What about if client unloads chunk afterwards?)
        TileEntity tile = currentEntry.world.getTileEntity(currentEntry.x, currentEntry.y, currentEntry.z);
        if(tile != null && tile != this)
        {
        	Packet packet = tile.getDescriptionPacket();
        	//Only send TileData if it's already in the form of NBT
        	if(packet instanceof S35PacketUpdateTileEntity)
        	{
        		S35PacketUpdateTileEntity dataPacket = (S35PacketUpdateTileEntity)packet;
        		//AT not working(As usual, no damn info about it) resorting to reflection(slow?)
        		try
        		{
	        		Field f = dataPacket.getClass().getDeclaredField("field_148860_e");
	        		f.setAccessible(true);
	        		NBTTagCompound tileData = (NBTTagCompound)f.get(dataPacket);
	        		data.setTag("tileData", tileData);
        		}
        		catch(Exception e) {} //Ignore Ignore Ignore
        	}
        }
        
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
		
		Block oldBlock = hostBlock;
		hostBlock = Block.getBlockFromName(itemName);
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, itemMeta, 1);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		
		if(hostBlock == null || hostBlock != oldBlock)
			proxyTile = null;
		
		//Proxy TileEntity
		if(data.hasKey("tileData") && hostBlock != null)
		{
			if(proxyTile == null)
			{
				if(hostBlock.hasTileEntity(itemMeta))
				{
					proxyTile = hostBlock.createTileEntity(getWorldObj(), itemMeta);
					if(proxyTile != null)
						proxyTile.setWorldObj(getWorldObj()); //Share the same world as the Interface
				}
			}
			if(proxyTile != null)
			{
				NBTTagCompound tileData = data.getCompoundTag("tileData");
				//TODO: This is a crash waiting to happen, add a filter in config file for Block/Tiles this does not work with
				proxyTile.onDataPacket(net, new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tileData));
			}
		}
		
		ModLog.logger.info("DATA PACKET: " + hostBlock);
		
	}
	
	
	//---REDSTONE---//
	
	public InterfaceRedstoneManager getRedstoneManager() {
		return redstoneManager;
	}
	
	//---INVENTORY---//
	//If the hosted block is not a Tile Entity, or does not implement IInventory, 
	//we just return 0 in getSizeInventory.
	//TODO: Pass this on to InventoryManager
	
	public InterfaceInventoryManager getInventoryManager() {
		return inventoryManager;
	}
	
    @Override
    public int getSizeInventory() {
        if(worldObj.isRemote || !TickHandler.inWorldTick)
            return super.getSizeInventory();
    	return inventoryManager.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
    	if(worldObj.isRemote || !TickHandler.inWorldTick) //When in client/opening GUI(GUI Network handler runs outside world tick)
    		return super.getStackInSlot(slot);
    	return inventoryManager.getStackInSlot(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
    	if(worldObj.isRemote || !TickHandler.inWorldTick)
    		return super.decrStackSize(slot, count);
    	return inventoryManager.decrStackSize(slot, count);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
    	if(worldObj.isRemote || !TickHandler.inWorldTick)
    		return super.getStackInSlotOnClosing(slot);
    	return inventoryManager.getStackInSlotOnClosing(slot);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
    	if(worldObj.isRemote || !TickHandler.inWorldTick)
    		super.setInventorySlotContents(slot, stack);
    	else
    		inventoryManager.setInventorySlotContents(slot, stack);
    }

    @Override
    public String getInventoryName() {
    	if(worldObj.isRemote || !TickHandler.inWorldTick)
    		return super.getInventoryName();
    	return inventoryManager.getInventoryName();
    }

    @Override
    public boolean hasCustomInventoryName() {
    	if(worldObj.isRemote || !TickHandler.inWorldTick)
    		return super.hasCustomInventoryName();
    	return inventoryManager.hasCustomInventoryName();
    }

    @Override
    public int getInventoryStackLimit() {
    	if(worldObj.isRemote || !TickHandler.inWorldTick)
    		return super.getInventoryStackLimit();
    	return inventoryManager.getInventoryStackLimit();
    }

    @Override
    public void markDirty() {
    	if(worldObj.isRemote || !TickHandler.inWorldTick)
    		super.markDirty();
    	else
    		inventoryManager.markDirty();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
    	if(worldObj.isRemote || !TickHandler.inWorldTick)
    		return super.isUseableByPlayer(player);
    	return inventoryManager.isUseableByPlayer(player);
    }

    @Override
    public void openInventory() {
    	if(worldObj.isRemote || !TickHandler.inWorldTick)
    		super.openInventory();
    	else
    		inventoryManager.openInventory();
    }

    @Override
    public void closeInventory() {
    	if(worldObj.isRemote || !TickHandler.inWorldTick)
    		super.closeInventory();
    	else
    		inventoryManager.closeInventory();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
    	if(worldObj.isRemote || !TickHandler.inWorldTick)
    		return super.isItemValidForSlot(slot, stack);
    	return inventoryManager.isItemValidForSlot(slot, stack);
    }
    
    //Sided specific
    //If your hosted tile entity does not have ISidedInventory, we simply return all sides all true
    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
    	if(worldObj.isRemote || !TickHandler.inWorldTick)
    		return super.getAccessibleSlotsFromSide(side);
    	return inventoryManager.getAccessibleSlotsFromSide(side);
    }

    @Override
    public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {
    	if(worldObj.isRemote || !TickHandler.inWorldTick)
    		return super.canInsertItem(p_102007_1_, p_102007_2_, p_102007_3_);
    	return inventoryManager.canInsertItem(p_102007_1_, p_102007_2_, p_102007_3_);
    }

    @Override
    public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
    	if(worldObj.isRemote || !TickHandler.inWorldTick)
    		return super.canExtractItem(p_102008_1_, p_102008_2_, p_102008_3_);
    	return inventoryManager.canExtractItem(p_102008_1_, p_102008_2_, p_102008_3_);
    }
	
	
}
