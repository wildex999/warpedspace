package com.wildex999.warpedspace.networking.netinterface;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import io.netty.buffer.ByteBuf;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.gui.NetworkInterfaceGui;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.MessageNetworkManagerUpdate;
import com.wildex999.warpedspace.tiles.TileNetworkManager;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageSCInterfaceUpdate extends MessageBase {

	protected int networkState;
	protected int entryState;
	protected long gid;
	protected String selectedName;
	protected String selectedBlockName;
	protected byte selectedBlockMeta;
	
	//Receiver constructor
	public MessageSCInterfaceUpdate() {}
	
	public MessageSCInterfaceUpdate(int networkState, int entryState, String selectedName, long gid, String selectedBlockName, byte selectedBlockMeta) {
		this.networkState = networkState;
		this.entryState = entryState;
		this.selectedName = selectedName;
		this.gid = gid;
		this.selectedBlockName = selectedBlockName;
		this.selectedBlockMeta = selectedBlockMeta;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		networkState = buf.readInt();
		entryState = buf.readInt();
		selectedName = ByteBufUtils.readUTF8String(buf);
		gid = buf.readLong();
		selectedBlockName = ByteBufUtils.readUTF8String(buf);
		selectedBlockMeta = buf.readByte();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(networkState);
		buf.writeInt(entryState);
		ByteBufUtils.writeUTF8String(buf, selectedName);
		buf.writeLong(gid);
		ByteBufUtils.writeUTF8String(buf, selectedBlockName);
		buf.writeByte(selectedBlockMeta);
	}
	
	public static class Handler implements IMessageHandler<MessageSCInterfaceUpdate, IMessage> {
        
        @Override
        public IMessage onMessage(MessageSCInterfaceUpdate message, MessageContext ctx) {
        	GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        	ModLog.logger.info("Got gid: " + message.gid + " screen: " + screen);
        	if(screen == null || !(screen instanceof NetworkInterfaceGui.GUI))
        		return null;
        	
        	NetworkInterfaceGui.GUI interfaceGui = (NetworkInterfaceGui.GUI)screen;
        	interfaceGui.networkUpdate(message.networkState, message.entryState, message.selectedName, message.gid, message.selectedBlockName, message.selectedBlockMeta);
        	
            return null;
        }
    }

}
