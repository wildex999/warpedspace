package com.wildex999.warpedspace.networking.netinterface;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import io.netty.buffer.ByteBuf;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.gui.NetworkInterfaceGui;
import com.wildex999.warpedspace.items.ItemPortableNetworkInterface;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.tiles.TileNetworkInterface;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

//Message sent from client when updating the interface, either by selecting new Block,
//or change settings on the existing one.

public class MessageCSInterfaceUpdate extends MessageBase {
	protected TileEntityInfo tile;
	protected long selection;
	protected boolean portableTile;
	
	//Receiver
	public MessageCSInterfaceUpdate() {}
	
	public MessageCSInterfaceUpdate(TileEntity tile, boolean portableTile, long selection) {
		this.tile = new TileEntityInfo(tile);
		this.selection = selection;
		this.portableTile = portableTile;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		tile = readTileEntity(buf);
		selection = buf.readLong();
		portableTile = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		writeTileEntity(buf, tile);
		buf.writeLong(selection);
		buf.writeBoolean(portableTile);
	}
	
	public static class Handler implements IMessageHandler<MessageCSInterfaceUpdate, IMessage> {
        
        @Override
        public IMessage onMessage(MessageCSInterfaceUpdate message, MessageContext ctx) {
        	World world = getWorld(ctx);
        	TileEntity tile;
        	EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        	if(message.portableTile)
        		tile = ItemPortableNetworkInterface.getProxyInterface(player);
        	else
        		tile = message.tile.getTileEntity(world);
        	
        	if(tile == null || !(tile instanceof TileNetworkInterface))
        	{
        		ModLog.logger.error("Got null TileEntity while trying to update Network Interface at x: "+ message.tile.posZ + " y: "+ message.tile.posY + " z: " + message.tile.posZ);
        		return null;
        	}
        	
        	TileNetworkInterface tileInterface = (TileNetworkInterface)tile;
        	tileInterface.clientUpdate(player, message.selection);

            return null;
        }
    }

}
