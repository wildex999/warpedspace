package com.wildex999.warpedspace.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.gui.WarpedControllerGui;
import com.wildex999.warpedspace.tiles.TileNetworkManager;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

public class MessageControllerUpdate extends MessageBase {
	public String networkName;
	private int nodes;
	private int tiles;
	private int relays;
	
	
	public MessageControllerUpdate() {}
	
	public MessageControllerUpdate(String networkName, int nodes, int tiles, int relays) {
		this.networkName = networkName;
		this.nodes = nodes;
		this.tiles = tiles;
		this.relays = relays;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		networkName = ByteBufUtils.readUTF8String(buf);
		nodes = buf.readInt();
		tiles = buf.readInt();
		relays = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, networkName);
		buf.writeInt(nodes);
		buf.writeInt(tiles);
		buf.writeInt(relays);
	}
	
public static class Handler implements IMessageHandler<MessageControllerUpdate, IMessage> {
        
        @Override
        public IMessage onMessage(MessageControllerUpdate message, MessageContext ctx) {
        	GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        	
        	if(!(screen instanceof WarpedControllerGui.GUI)) 
        		return null;
        	
        	WarpedControllerGui.GUI gui = (WarpedControllerGui.GUI)screen;
        	gui.updateNetworkInfo(message.networkName, message.nodes, message.tiles, message.relays);
        	
        	return null;
        }
    }

}
