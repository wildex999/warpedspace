package com.wildex999.warpedspace.tiles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.blocks.BlockNetworkController;
import com.wildex999.warpedspace.gui.WarpedControllerGui;
import com.wildex999.warpedspace.gui.interfaces.IGuiWatchers;
import com.wildex999.warpedspace.inventory.BaseNetworkInventoryTile;
import com.wildex999.warpedspace.items.ItemNetworkCard;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.MessageControllerUpdate;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.INetworkController;
import com.wildex999.warpedspace.warpednetwork.INetworkRelay;
import com.wildex999.warpedspace.warpednetwork.INode;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileNetworkController extends BaseNetworkInventoryTile implements INetworkController, INetworkRelay, IGuiWatchers {
	
	private WarpedNetwork currentNetwork;
	private Set<INode> nodes; //Nodes connected to our Relay
	
	private byte tickRetry; //Retry joining network
	private byte tickWatcherUpdate; //Update Watchers
	private List<EntityPlayer> watchers;
	
	//Stored/Synced vars
	private String owner = "";
	
	
	
	public TileNetworkController() {
		nodes = new HashSet<INode>();
		inventoryName = "Network Controller";
		tickRetry = 0;
		watchers = new ArrayList<EntityPlayer>();
	}

	@Override
	public WarpedNetwork joinNetwork(int id) {
		CoreNetworkManager networkManager = CoreNetworkManager.getInstance(worldObj);

		WarpedNetwork network = networkManager.networks.get(id);
		if(network == null)
			return null;
		
		if(network.getController() != null)
			return null;
		
		if(!network.setController(this))
			return null;
		currentNetwork = network;
		
		network.addRelay(this);
		
		return currentNetwork;
	}

	@Override
	public void leaveNetwork() {
		
		if(currentNetwork != null && currentNetwork.getController() == this)
		{
			onNetworkLeave();
			
			WarpedNetwork temp = currentNetwork; //Avoid loop, as setController will call leaveNetwork.
			currentNetwork = null;
			temp.setController(null);
		}
		currentNetwork = null;
	}
	
	protected void onNetworkLeave() {
		//Unregister as Relay
		currentNetwork.removeRelay(this);
		
		for(INode node : nodes)
			node.eventLostRelay();
		nodes.clear();

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	//Called when Network Card is placed/removed
	protected void onNetworkCardUpdate() {
		if(worldObj.isRemote)
			return;
		
		ModLog.logger.info("Controller: Network Card CHange");
		
		if(stackNetworkCard == null)
		{
			if(currentNetwork != null)
			{
				leaveNetwork();
				updateWatchers();
			}
			return;
		}
		
		int networkId = ItemNetworkCard.getNetworkId(stackNetworkCard);
		
		//Check if card was replaced
		if(currentNetwork != null && currentNetwork.id != networkId)
			leaveNetwork();
		
		joinNetwork(networkId);
		
		updateWatchers();
	}
	
	
	//Get an update packet to send to players
	public MessageControllerUpdate getUpdatePacket() {
		if(currentNetwork != null)
			return new MessageControllerUpdate(currentNetwork.name, currentNetwork.getNodeCount(), 
							currentNetwork.getTileCount(), currentNetwork.getRelayCount());
		else
			return new MessageControllerUpdate("", 0, 0, 0);		
	}
	
	//Send update packet to all watchers
	public void updateWatchers() {
		MessageBase messageUpdate = getUpdatePacket();
		for(EntityPlayer player : watchers)
			messageUpdate.sendToPlayer((EntityPlayerMP)player);
	}
	
	@Override
	public void updateEntity() {
		if(worldObj.isRemote)
			return;
		
		if(currentNetwork == null && stackNetworkCard != null)
		{
			//Retry joining at a regular interval
			if(tickRetry-- <= 0)
			{
				int networkId = ItemNetworkCard.getNetworkId(stackNetworkCard);
				currentNetwork = joinNetwork(networkId);
				tickRetry = 20;
			}
		}
		
		if(tickWatcherUpdate-- <= 0)
		{
			MessageBase messageUpdate = getUpdatePacket();
			
			for(EntityPlayer player : watchers)
				messageUpdate.sendToPlayer((EntityPlayerMP)player);
			
			tickWatcherUpdate = 20;
		}
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
	public WarpedNetwork getNetwork() {
		return currentNetwork;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		writeCustomNBT(data);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		readCustomNBT(data);
	}
	
	//Write our custom data to the NBT
	public void writeCustomNBT(NBTTagCompound data) {
		if(owner.length() != 0)
			data.setString("owner", owner);
	}
	
	//Read our custom data from the NBT
	public void readCustomNBT(NBTTagCompound data) {
		owner = data.getString("owner");
	}
	
	/*@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound data = new NBTTagCompound();
		writeCustomNBT(data);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, data);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		NBTTagCompound data = pkt.func_148857_g();
		
		readCustomNBT(data);
	}*/
	
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
	public void addWatcher(EntityPlayer player) {
		if(worldObj.isRemote)
			return;
		
		watchers.add(player);
		
		//Send current GUI state next tick
		tickWatcherUpdate = 0;
	}

	@Override
	public void removeWatcher(EntityPlayer player) {
		if(worldObj.isRemote)
			return;
		
		watchers.remove(player);
	}
	
	//==Relay Methods==//

	@Override
	public boolean joinNetwork(WarpedNetwork network) {
		//Controller does not join the network
		return false;
	}

	@Override
	public boolean joinRelay(INetworkRelay relay) {
		//Controller does not join a relay
		return false;
	}

	@Override
	public INetworkRelay getRelay() {
		//The controller does not connect to any relay, so returns null.
		//Network check should recognize that this is a Controller, and don't try to
		//get it's relay.
		return null;
	}

	@Override
	public void leaveRelay() {}

	@Override
	public boolean isNetworkReachable() {
		return true;
	}

	@Override
	public void setOwner(String username) {
		owner = username;
	}

	@Override
	public String getOwner() {
		return owner;
	}

	@Override
	public void eventLostRelay() {}

	@Override
	public void eventLostNetwork() {}

	@Override
	public void eventNetworkDisconnect() {}

	@Override
	public void eventNetworkReconnect() {}

	@Override
	public void eventNetworkNoPower() {}

	@Override
	public void eventNetworkGotPower() {}

	@Override
	public boolean addNode(WarpedNetwork net, INode node) {
		if(net != currentNetwork)
			return false;
		return nodes.add(node);
	}

	@Override
	public boolean removeNode(WarpedNetwork net, INode node) {
		if(net != currentNetwork)
			return false;
		return nodes.remove(node);
	}

	@Override
	public boolean isNetworkAvailable(WarpedNetwork net) {
		if(net == currentNetwork)
			return true;
		return false;
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
	public int getRadius() {
		//TODO: Allow upgrading radius.
		//Should not store radius directly on tileentity, but instead
		//store the upgrade. So if upgrade value changes it will load the correct value.
		//return 8;
		return 10000;
	}

}
