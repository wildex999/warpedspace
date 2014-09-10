package com.wildex999.warpedspace.networking.netinterface;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import io.netty.buffer.ByteBuf;

import com.wildex999.warpedspace.gui.NetworkInterfaceGui;
import com.wildex999.warpedspace.gui.interfaces.INetworkListGui;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.warpednetwork.MessageSCNetworkUpdate;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageTilesUpdate extends MessageBase {
	public static final byte TASK_ADD = 0;
	public static final byte TASK_REMOVE = 1; 
	public static final byte TASK_ACTIVE = 2; //Set active true/false

	
	protected byte task;
	protected long gid;
	protected String entryName;
	protected String tileName;
	protected byte tileMeta;
	protected boolean active;
	
	//Receive
	public MessageTilesUpdate() {}

	//Task: Remove
	public MessageTilesUpdate(String entryName) {
		this.task = TASK_REMOVE;
		this.entryName = entryName; //List is stored by string, so we send string
	}
	
	//Task: Add
	public MessageTilesUpdate(String entryName, String tileName, byte tileMeta, long gid, boolean active) {
		this.task = TASK_ADD;
		this.entryName = entryName;
		this.tileName = tileName;
		this.tileMeta = tileMeta;
		this.gid = gid;
		this.active = active;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		task = buf.readByte();
		switch(task)
		{
		case TASK_ADD:
			entryName = ByteBufUtils.readUTF8String(buf);
			tileName = ByteBufUtils.readUTF8String(buf);
			tileMeta = buf.readByte();
			gid = buf.readLong();
			active = buf.readBoolean();
			break;
		case TASK_REMOVE:
			entryName = ByteBufUtils.readUTF8String(buf);
			break;
		}
		
	}


	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(task);
		switch(task)
		{
		case TASK_ADD:
			ByteBufUtils.writeUTF8String(buf, entryName);
			ByteBufUtils.writeUTF8String(buf, tileName);
			buf.writeByte(tileMeta);
			buf.writeLong(gid);
			buf.writeBoolean(active);
			break;
		case TASK_REMOVE:
			ByteBufUtils.writeUTF8String(buf, entryName);
			break;
		}
		
	}
	
	public static class Handler implements IMessageHandler<MessageTilesUpdate, IMessage> {
        
        @Override
        public IMessage onMessage(MessageTilesUpdate message, MessageContext ctx) {
        	GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        	
        	if(screen == null || !(screen instanceof NetworkInterfaceGui.GUI))
        		return null;
        	
        	NetworkInterfaceGui.GUI guiInterface = (NetworkInterfaceGui.GUI)screen;
        	
        	switch(message.task)
        	{
        	case TASK_ADD:
        		guiInterface.addTile(message.entryName, message.tileName, message.tileMeta, message.gid, message.active);
        		break;
        	case TASK_REMOVE:
        		guiInterface.removeTile(message.entryName);
        		break;
        	case TASK_ACTIVE:
        		//TODO
        		break;
        	}
        	
        	return null;
        }
    }
}
