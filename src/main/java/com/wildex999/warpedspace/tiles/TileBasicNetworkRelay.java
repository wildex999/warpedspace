package com.wildex999.warpedspace.tiles;

import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.wildex999.warpedspace.blocks.relay.BlockRelayBase;
import com.wildex999.warpedspace.gui.interfaces.IGuiWatchers;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.MessageRelayUpdate;
import com.wildex999.warpedspace.warpednetwork.INode;
import com.wildex999.warpedspace.warpednetwork.RelayNodeTile;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

public class TileBasicNetworkRelay extends RelayNodeTile implements IGuiWatchers {

	private HashSet<EntityPlayer> watchers;
	
	public TileBasicNetworkRelay() {
		inventoryName = "Network Relay";
		
		watchers = new HashSet<EntityPlayer>();
	}
	
	public void setRadius(int radius) {
		relayRadius = radius;
	}
	
	@Override
	public int getRadius() {
		if(relayRadius == 0)
		{
			Block block = getBlockType();
			if(block == null)
				return 0;
			if(!(block instanceof BlockRelayBase))
				return 0;
			relayRadius = ((BlockRelayBase)block).getRadius();
		}
		return relayRadius;
	}
	
	//If player is null, send to all watchers
	public void sendNetworkUpdate(EntityPlayerMP player, boolean queue) {
		if(player == null && watchers.size() == 0)
			return;
		
		MessageBase messageUpdate = new MessageRelayUpdate(getNetworkStateMessage(), nodeList.size());
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
	
	@Override
	public void addWatcher(EntityPlayer player) {
		if(worldObj.isRemote)
			return;
		watchers.add(player);
		
		sendNetworkUpdate((EntityPlayerMP)player, true);
	}

	@Override
	public void removeWatcher(EntityPlayer player) {
		if(worldObj.isRemote)
			return;
		watchers.remove(player);
	}
	
	//--Events--
	
	@Override
	public boolean addNode(WarpedNetwork net, INode node) {
		if(!super.addNode(net, node))
			return false;
		sendNetworkUpdate(null, false);
		return true;
	}
	
	@Override
	public boolean removeNode(WarpedNetwork net, INode node) {
		if(!super.removeNode(net, node))
			return false;
		sendNetworkUpdate(null, false);
		return true;
	}
	
	@Override
	public boolean joinNetwork(WarpedNetwork network) {
		if(!super.joinNetwork(network))
			return false;
		
		sendNetworkUpdate(null, false);
		return true;
	}
	
	@Override
	public void leaveNetwork() {
		super.leaveNetwork();
		
		sendNetworkUpdate(null, false);
	}
	
	@Override
	public void eventLostRelay() {
		super.eventLostRelay();
		
		sendNetworkUpdate(null, false);
	}
	
	@Override
	public void eventNetworkDisconnect() {
		super.eventNetworkDisconnect();
		
		sendNetworkUpdate(null, false);
	}
	
	@Override
	public void eventNetworkReconnect() {
		super.eventNetworkReconnect();
		
		sendNetworkUpdate(null, false);
	}

}
