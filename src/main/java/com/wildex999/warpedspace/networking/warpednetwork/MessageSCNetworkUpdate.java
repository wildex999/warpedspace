package com.wildex999.warpedspace.networking.warpednetwork;

import net.minecraft.client.Minecraft;
import io.netty.buffer.ByteBuf;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.gui.interfaces.INetworkListGui;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageSCNetworkUpdate extends MessageBase {
	public static final byte TASK_CREATE = 0;
	public static final byte TASK_REMOVE = 1;
	public static final byte TASK_RENAME = 2;
	public static final byte TASK_OWNER = 3;
	public static final byte TASK_MULTI = 4; //Multiple tasks
	
	protected byte task;
	protected int id;
	protected String name;
	protected String owner;

	//Task Multi
	public MessageSCNetworkUpdate() {
		this.task = TASK_MULTI;
		//TODO: Method to add tasks
	}
	
	//Task Create
	public MessageSCNetworkUpdate(int id, String name, String owner) {
		this.id = id;
		this.name = name;
		this.owner = owner;
		this.task = TASK_CREATE;
	}
	
	//Task Remove
	public MessageSCNetworkUpdate(int id) {
		this.id = id;
		this.task = TASK_REMOVE;
	}
	
	//Task Rename/Change owner
	public MessageSCNetworkUpdate(int id, String name, boolean changeOwner) {
		this.id = id;
		
		if(changeOwner)
		{
			this.task = TASK_OWNER;
			this.owner = name;
		}
		else
		{
			this.task = TASK_RENAME;
			this.name = name;
		}
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		task = buf.readByte();
		id = buf.readInt();
		
		switch(this.task)
		{
		case TASK_CREATE:
			name = ByteBufUtils.readUTF8String(buf);
			owner = ByteBufUtils.readUTF8String(buf);
			break;
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
				ModLog.logger.warn("Server Received NetworkUpdate message with invalid task: " + this.task);
			break;
		}
		
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(task);
		buf.writeInt(id);
		
		switch(this.task)
		{
		case TASK_CREATE:
			ByteBufUtils.writeUTF8String(buf, name);
			ByteBufUtils.writeUTF8String(buf, owner);
			break;
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
	
	public static class Handler implements IMessageHandler<MessageSCNetworkUpdate, IMessage> {
        
        @Override
        public IMessage onMessage(MessageSCNetworkUpdate message, MessageContext ctx) {
        	
        	//If any GUI is open, notify it of list update
        	INetworkListGui currentGui = null;
        	if(Minecraft.getMinecraft().currentScreen instanceof INetworkListGui)
        		currentGui = (INetworkListGui)Minecraft.getMinecraft().currentScreen;
        	
        	CoreNetworkManager networkManager = CoreNetworkManager.clientNetworkManager;
        	switch(message.task)
        	{
        	case TASK_CREATE:
        		networkManager.clientAddNetwork(message.id, message.name, message.owner);
        		if(currentGui != null)
        			currentGui.networkCreated(message.id, message.name, message.owner);
        		break;
        	case TASK_REMOVE:
        		//Allow GUI to get the network info before we remove it
        		if(currentGui != null)
        			currentGui.networkRemoved(message.id);
        		networkManager.clientRemoveNetwork(message.id);
        		break;
        	case TASK_RENAME:
        		//Call GUI update before, as they usually use the old name for listings
        		if(currentGui != null)
        			currentGui.networkRename(message.id, message.name);
        		networkManager.clientRenameNetwork(message.id, message.name);
        		break;
        	case TASK_OWNER:
        		//TODO:
        		break;
        	}
        	
        	return null;
        }
    }
}
