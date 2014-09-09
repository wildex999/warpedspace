package com.wildex999.warpedspace.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.tiles.TileNetworkManager;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class MessageNetworkManagerUpdate extends MessageBase {

	protected boolean writeEnabled;
	protected int networkId;
	protected TileEntityInfo tile;
	
	public MessageNetworkManagerUpdate() {}
	
	public MessageNetworkManagerUpdate(boolean writeEnabled, WarpedNetwork currentNetwork, TileEntity tile) {
		this.writeEnabled = writeEnabled;
		if(currentNetwork != null)
			this.networkId = currentNetwork.id;
		else
			this.networkId = -1;
		this.tile = new TileEntityInfo(tile);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		tile = this.readTileEntity(buf);
		writeEnabled = buf.readBoolean();
		networkId = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		this.writeTileEntity(buf, tile);
		buf.writeBoolean(writeEnabled);
		buf.writeInt(networkId);
	}
	
	public static class Handler implements IMessageHandler<MessageNetworkManagerUpdate, IMessage> {
        
        @Override
        public IMessage onMessage(MessageNetworkManagerUpdate message, MessageContext ctx) {
        	ModLog.logger.info("Got Network Manager Update on side: " + ctx.side);
        	
        	World world = getWorld(ctx);
        	CoreNetworkManager networkManager = CoreNetworkManager.getInstance(world);
        	TileEntity baseTile = message.tile.getTileEntity(world);
        	
        	if(networkManager == null || baseTile == null)
        	{
        		ModLog.logger.error("Network Manager Update failed due to null pointer. NetworkManager: " + networkManager + " | baseTile: " + baseTile);
        		return null;
        	}
        	
        	if(!(baseTile instanceof TileNetworkManager))
        	{
        		ModLog.logger.error("Network Manager Update Tile Entity not of type TileNetworkManager! "
        				+ message.tile.posX + " | " + message.tile.posY + " | " + message.tile.posZ);
        		return null;
        	}
        	TileNetworkManager manager = (TileNetworkManager)baseTile;
        	WarpedNetwork network = networkManager.networks.get(message.networkId);
        	
        	manager.updateData(message.writeEnabled, network);
        	if(ctx.side == Side.SERVER)
        		manager.updatedWatchers();
        	
            return null;
        }
    }
}
