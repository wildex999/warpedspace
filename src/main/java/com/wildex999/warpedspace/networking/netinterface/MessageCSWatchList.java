package com.wildex999.warpedspace.networking.netinterface;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import io.netty.buffer.ByteBuf;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.gui.interfaces.INetworkListGui;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.networking.warpednetwork.MessageSCNetworkList;
import com.wildex999.warpedspace.networking.warpednetwork.MessageSCNetworkList.NetworkInfo;
import com.wildex999.warpedspace.tiles.TileNetworkInterface;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

//Sent to inform server whether or not we wan't to get the Tile list and updates
//for a network. Usually sent when the client opens a GUI listing tiles, and
//when they close it.

public class MessageCSWatchList extends MessageBase {

	protected boolean doWatch;
	protected TileEntityInfo tile;
	
	//Receive constructor
	public MessageCSWatchList() {}
	
	public MessageCSWatchList(TileEntity tile, boolean watch) {
		doWatch = watch;
		this.tile = new TileEntityInfo(tile);
		if(this.tile == null)
			ModLog.logger.error("Trying to watch Tile list from null Tile!");
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		doWatch = buf.readBoolean();
		tile = readTileEntity(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(doWatch);
		writeTileEntity(buf, tile);
	}
	
	public static class Handler implements IMessageHandler<MessageCSWatchList, IMessage> {
        
        @Override
        public IMessage onMessage(MessageCSWatchList message, MessageContext ctx) {
        	World world = getWorld(ctx);
        	TileEntity tile = message.tile.getTileEntity(world);
      
        	if(tile == null || !(tile instanceof TileNetworkInterface))
        		return null;
        	
        	TileNetworkInterface tileInterface = (TileNetworkInterface)tile;
        	
        	WarpedNetwork network = tileInterface.getNetwork();
        	if(network == null)
        	{
        		ModLog.logger.info("Server Watch no network");
        		return null;
        	}
        	
        	ModLog.logger.info("Server got Tile Watch: " + message.doWatch);
        	
        	if(message.doWatch)
        		tileInterface.addTileWatcher(ctx.getServerHandler().playerEntity);
        	else
        		tileInterface.removeTileWatcher(ctx.getServerHandler().playerEntity);
        	
            return null;
        }
    }
	
}
