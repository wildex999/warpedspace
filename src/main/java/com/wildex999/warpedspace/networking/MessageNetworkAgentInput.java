package com.wildex999.warpedspace.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.gui.NetworkAgentGui;
import com.wildex999.warpedspace.tiles.TileNetworkAgent;
import com.wildex999.warpedspace.tiles.TileNetworkManager;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/*
 * Message from client with input field update
 */

public class MessageNetworkAgentInput extends MessageBase {
	
	protected TileEntityInfo tileEntityInfo;
	protected int inputIndex;
	protected String newName;
	
	
	//Receive Constructor
	public MessageNetworkAgentInput() {}
	
	//inputIndex is the input that was changed(0 - 5 = north etc. 6 = Node Name)
	public MessageNetworkAgentInput(TileEntity tile, int inputIndex, String newName) {
		tileEntityInfo = new TileEntityInfo(tile);
		this.inputIndex = inputIndex;
		this.newName = newName;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		tileEntityInfo = readTileEntity(buf);
		inputIndex = buf.readInt();
		newName = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		writeTileEntity(buf, tileEntityInfo);
		buf.writeInt(inputIndex);
		ByteBufUtils.writeUTF8String(buf, newName);
	}
	
public static class Handler implements IMessageHandler<MessageNetworkAgentInput, IMessage> {
        
        @Override
        public IMessage onMessage(MessageNetworkAgentInput message, MessageContext ctx) {
        	World world = getWorld(ctx);
        	TileEntity baseTile = message.tileEntityInfo.getTileEntity(world);
        	
        	if(baseTile == null)
        	{
        		ModLog.logger.error("Network Agent Input failed due to missing NetworkAgent at X|Y|Z: "
        				+ message.tileEntityInfo.posX + " | " + message.tileEntityInfo.posY + " | " + message.tileEntityInfo.posZ);
        		return null;
        	}
        	
        	if(!(baseTile instanceof TileNetworkAgent))
        	{
        		ModLog.logger.error("Network Agent Input failed due to wrong TileEntity(Expected NetworkAgent) at X|Y|Z: "
        				+ message.tileEntityInfo.posX + " | " + message.tileEntityInfo.posY + " | " + message.tileEntityInfo.posZ);
        		return null;
        	}
        	
        	TileNetworkAgent agent = (TileNetworkAgent)baseTile;
        	if(!agent.onClientInput(message.inputIndex, message.newName)) {
        		ModLog.logger.error("Problem while changing Network Agent tile name by player: " + ctx.getServerHandler().playerEntity.getGameProfile().getName());
        	}
        	
        	//Inform all watchers of name change(Even if it failed)
        	agent.sendNetworkUpdate(null, false);
        	
        	return null;
        }
    }

}
