package com.wildex999.warpedspace.networking.warpednetwork;

import io.netty.buffer.ByteBuf;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/*
 * Update an Network(Rename, Remove, Change owner)
 */

public class MessageCSNetworkUpdate extends MessageBase {
	public static final byte TASK_REMOVE = 1;
	public static final byte TASK_RENAME = 2;
	public static final byte TASK_OWNER = 3;
	
	byte task;
	int id;
	String name;
	String owner;
	
	public MessageCSNetworkUpdate() {}
	
	//Task Remove
	public MessageCSNetworkUpdate(int id) {
		this.id = id;
		this.task = TASK_REMOVE;
	}
	
	//Task Rename/Change owner
	public MessageCSNetworkUpdate(int id, String name, boolean changeOwner) {
		this.id = id;
		
		if(changeOwner)
		{
			task = TASK_OWNER;
			this.owner = name;
		}
		else
		{
			task = TASK_RENAME;
			this.name = name;
		}
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		task = buf.readByte();
		id = buf.readInt();
		
		switch(this.task)
		{
		case TASK_REMOVE:
			//Nothing more to read
			break;
		case TASK_RENAME:
			name = ByteBufUtils.readUTF8String(buf);
			break;
		case TASK_OWNER:
			owner = ByteBufUtils.readUTF8String(buf);
			break;
		default:
				ModLog.logger.warn("Client Received NetworkUpdate message with invalid task: " + this.task);
			break;
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(task);
		buf.writeInt(id);
		
		switch(this.task)
		{
		case TASK_REMOVE:
			//Only require task and id.
			break;
		case TASK_RENAME:
			ByteBufUtils.writeUTF8String(buf, name);
			break;
		case TASK_OWNER:
			ByteBufUtils.writeUTF8String(buf, owner);
			break;
		}
	}
	
	public static class Handler implements IMessageHandler<MessageCSNetworkUpdate, IMessage> {
        
        @Override
        public IMessage onMessage(MessageCSNetworkUpdate message, MessageContext ctx) {
        	
        	CoreNetworkManager networkManager = CoreNetworkManager.serverNetworkManager;
        	int error = 0;
        	switch(message.task)
        	{
        	case TASK_REMOVE:
        		if(!networkManager.removeNetwork(message.id))
        			error = -1; //Network doesn't exist
        		break;
        	case TASK_RENAME:
        		if(!networkManager.renameNetwork(message.id, message.name))
        			error = -2; //Network doesn't exist, or name out of sync
        		break;
        	case TASK_OWNER:
        		//TODO:
        		break;
        	}
        	
        	if(error < 0)
        	{
        		//Send error message to player
        		//TODO
        		ModLog.logger.info("Unable to update network: " + message.id + " due to error: " + error);
        	}
        	
            return null;
        }
    }
}
