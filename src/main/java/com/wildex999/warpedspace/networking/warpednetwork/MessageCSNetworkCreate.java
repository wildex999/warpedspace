package com.wildex999.warpedspace.networking.warpednetwork;

import io.netty.buffer.ByteBuf;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/*
 * Create a new network
 */

public class MessageCSNetworkCreate extends MessageBase {

	protected String name;
	
	public MessageCSNetworkCreate() {}
	
	public MessageCSNetworkCreate(String name) {
		this.name = name;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		name = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, name);
	}
	
	public static class Handler implements IMessageHandler<MessageCSNetworkCreate, IMessage> {
        
        @Override
        public IMessage onMessage(MessageCSNetworkCreate message, MessageContext ctx) {
        	
        	CoreNetworkManager networkManager = CoreNetworkManager.serverNetworkManager;
        	int error = networkManager.addNetwork(message.name, ctx.getServerHandler().playerEntity.getGameProfile().getName());
        	if(error < 0)
        	{
        		//Send error message to player
        		//TODO
        		ModLog.logger.info("Unable to add network: " + message.name + " due to error: " + error);
        	}
        	
            return null;
        }
    }

}
