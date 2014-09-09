package com.wildex999.warpedspace.networking.warpednetwork;

import java.util.Map;

import net.minecraft.client.Minecraft;
import io.netty.buffer.ByteBuf;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.gui.interfaces.INetworkListGui;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageSCNetworkList extends MessageBase {
	protected CoreNetworkManager networkManager;
	protected NetworkInfo[] networks;
	
	public MessageSCNetworkList() {}
	
	public MessageSCNetworkList(CoreNetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		//It seems a bit uneccesary to put it into an Array to just
		//move it over to a map right after.
		//Maybe just add it to the map directly? Don't know if it would be bad
		//to do so in fromBytes?
		int networkCount = buf.readInt();
		networks = new NetworkInfo[networkCount];
		for(int index = 0; index < networkCount; index++)
		{
			int id = buf.readInt();
			String name = ByteBufUtils.readUTF8String(buf);
			String owner = ByteBufUtils.readUTF8String(buf);
			networks[index] = new NetworkInfo(id, name, owner);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		
		buf.writeInt(networkManager.networks.size());
		for(Map.Entry<Integer, WarpedNetwork> entry : networkManager.networks.entrySet())
		{
			WarpedNetwork network = entry.getValue();
			buf.writeInt(entry.getKey());
			ByteBufUtils.writeUTF8String(buf, network.name);
			ByteBufUtils.writeUTF8String(buf, network.owner);
		}
	}

	public static class Handler implements IMessageHandler<MessageSCNetworkList, IMessage> {
        
        @Override
        public IMessage onMessage(MessageSCNetworkList message, MessageContext ctx) {
        	CoreNetworkManager networkManager = CoreNetworkManager.clientNetworkManager;
        	
        	ModLog.logger.info("Got Network list from server: " + message.networks.length + " entries.");
        	
        	//If any GUI is open, notify it of list update
        	INetworkListGui currentGui = null;
        	if(Minecraft.getMinecraft().currentScreen instanceof INetworkListGui)
        		currentGui = (INetworkListGui)Minecraft.getMinecraft().currentScreen;
        	
        	//Clear the current network list
        	networkManager.clearNetworks();
        	
        	//Load from array
        	for(NetworkInfo info : message.networks)
        		networkManager.clientAddNetwork(info.id, info.name, info.owner);

        	
        	if(currentGui != null)
        		currentGui.reloadNetworkList();
        	
            return null;
        }
    }
	
	public static class NetworkInfo {
		public int id;
		public String name;
		public String owner;
		
		public NetworkInfo(int id, String name, String owner) {
			this.id = id;
			this.name = name;
			this.owner = owner;
		}
	}
	
}
