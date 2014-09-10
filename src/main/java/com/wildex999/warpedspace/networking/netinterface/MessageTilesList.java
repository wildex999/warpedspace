package com.wildex999.warpedspace.networking.netinterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import com.wildex999.utils.BlockItemName;
import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.gui.NetworkInterfaceGui;
import com.wildex999.warpedspace.gui.interfaces.INetworkListGui;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.warpednetwork.MessageSCNetworkList;
import com.wildex999.warpedspace.networking.warpednetwork.MessageSCNetworkList.NetworkInfo;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.INetworkAgent;
import com.wildex999.warpedspace.warpednetwork.AgentEntry;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

//Initial list of all Tile Entities in a Network

public class MessageTilesList extends MessageBase {
	
	protected WarpedNetwork network;
	protected List<MessageEntry> entryList;
	protected String networkName;
	
	//Receiver
	public MessageTilesList() {}
	
	public MessageTilesList(WarpedNetwork network) {
		this.network = network;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		networkName = ByteBufUtils.readUTF8String(buf);
		int entryCount = buf.readInt();
		entryList = new ArrayList<MessageEntry>(entryCount);
		
		for(int i=0; i < entryCount; i++)
		{
			long gid = buf.readLong();
			String entryName = ByteBufUtils.readUTF8String(buf);
			String tileName = ByteBufUtils.readUTF8String(buf);
			byte tileMeta = buf.readByte();
			boolean active = buf.readBoolean();
			
			entryList.add(new MessageEntry(entryName, tileName, tileMeta, gid, active));
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		int tileCount = network.getTileCount();
		Map<String, AgentEntry> tileMap = network.getTileMap();
		
		ByteBufUtils.writeUTF8String(buf, network.name);
		buf.writeInt(tileMap.size());
		
		for(Map.Entry<String, AgentEntry> entry : tileMap.entrySet())
		{
			AgentEntry tile = entry.getValue();
			String name = entry.getKey();
			boolean enabled = tile.agent.isNetworkReachable();
			
			buf.writeLong(tile.gid);
			ByteBufUtils.writeUTF8String(buf, name);
			
			String blockName = BlockItemName.get(tile.block, tile.world, tile.x, tile.y, tile.z);
			ByteBufUtils.writeUTF8String(buf, blockName);
			buf.writeByte(tile.world.getBlockMetadata(tile.x, tile.y, tile.z));
			buf.writeBoolean(enabled);
		}
		
	}
	
	public static class Handler implements IMessageHandler<MessageTilesList, IMessage> {
        
        @Override
        public IMessage onMessage(MessageTilesList message, MessageContext ctx) {
        	CoreNetworkManager networkManager = CoreNetworkManager.getInstance(getWorld(ctx));
        	
        	WarpedNetwork network = networkManager.networks.get(networkManager.networkNames.get(message.networkName));
        	if(network == null)
        	{
        		ModLog.logger.error("Got Tile List for network: " + message.networkName + ", but could not find network on client!");
        		return null;
        	}
        	
        	GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        	if(screen == null || !(screen instanceof NetworkInterfaceGui.GUI))
        		return null;
        	
        	NetworkInterfaceGui.GUI interfaceGui = (NetworkInterfaceGui.GUI)screen;
        	interfaceGui.autoUpdateTileList(false);
        	for(MessageEntry entry : message.entryList)
        		interfaceGui.addTile(entry.entryName, entry.tileName, entry.tileMeta, entry.gid, entry.enabled);
        	
        	interfaceGui.updateTileList();
        	interfaceGui.autoUpdateTileList(true);
        	
            return null;
        }
    }
	
	protected class MessageEntry {
		public String entryName;
		public String tileName;
		public byte tileMeta;
		public long gid;
		public boolean enabled;
		
		public MessageEntry(String entryName, String tileName, byte tileMeta, long gid, boolean enabled) {
			this.entryName = entryName;
			this.tileName = tileName;
			this.tileMeta = tileMeta;
			this.enabled = enabled;
			this.gid = gid;
		}
	}
}
