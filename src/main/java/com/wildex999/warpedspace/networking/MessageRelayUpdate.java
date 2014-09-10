package com.wildex999.warpedspace.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.wildex999.warpedspace.gui.BasicNetworkRelayGui;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageRelayUpdate extends MessageBase {

	int networkState;
	int nodes;
	
	//Receive
	public MessageRelayUpdate() {}
	
	public MessageRelayUpdate(int networkState, int nodes) {
		this.networkState = networkState;
		this.nodes = nodes;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		networkState = buf.readInt();
		nodes = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(networkState);
		buf.writeInt(nodes);
	}
	
	public static class Handler implements IMessageHandler<MessageRelayUpdate, IMessage> {
        
        @Override
        public IMessage onMessage(MessageRelayUpdate message, MessageContext ctx) {
        	GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        	
        	if(!(screen instanceof BasicNetworkRelayGui.GUI)) 
        		return null;
        	
        	BasicNetworkRelayGui.GUI gui = (BasicNetworkRelayGui.GUI)screen;
        	gui.clientUpdate(message.networkState, message.nodes);
        	
        	return null;
        }
    }

}
