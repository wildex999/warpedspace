package com.wildex999.warpedspace.networking;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.gui.NetworkAgentGui;
import com.wildex999.warpedspace.gui.WarpedControllerGui;
import com.wildex999.warpedspace.warpednetwork.AgentNode;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/*
 * Update Network Agent Node GUI with the name and state of the different managed tiles.
 */


public class MessageNetworkAgentUpdate extends MessageBase {
	
	protected List<NetworkAgentGui.TileState> tiles = new ArrayList<NetworkAgentGui.TileState>();
	protected int networkState;
	
	//Receive constructor
	public MessageNetworkAgentUpdate() {}
	
	public MessageNetworkAgentUpdate(int networkState, NetworkAgentGui.TileState[] tiles) {
		this.networkState = networkState;
		for(NetworkAgentGui.TileState tile : tiles)
			this.tiles.add(tile);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		networkState = buf.readInt();
		int count = buf.readInt();
		for(;count > 0; count--)
		{
			String name = ByteBufUtils.readUTF8String(buf);
			String tileName = ByteBufUtils.readUTF8String(buf);
			boolean active = buf.readBoolean();
			tiles.add(new NetworkAgentGui.TileState(name, tileName, active));
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(networkState);
		buf.writeInt(tiles.size());
		for(NetworkAgentGui.TileState tile : tiles)
		{
			ByteBufUtils.writeUTF8String(buf, tile.name);
			ByteBufUtils.writeUTF8String(buf, tile.tileName);
			buf.writeBoolean(tile.active);
		}
	}
	
public static class Handler implements IMessageHandler<MessageNetworkAgentUpdate, IMessage> {
        
        @Override
        public IMessage onMessage(MessageNetworkAgentUpdate message, MessageContext ctx) {
        	GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        	if(!(screen instanceof NetworkAgentGui.GUI))
        		return null;
        	NetworkAgentGui.GUI agentGui = (NetworkAgentGui.GUI)screen;
        	
        	ModLog.logger.info("CLIENT GOT AGENT INFO");
        	
        	agentGui.updateEntries(message.networkState, message.tiles);
        	
        	return null;
        }
    }
}
