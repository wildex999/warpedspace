package com.wildex999.warpedspace.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.gui.NetworkInterfaceGui;
import com.wildex999.warpedspace.gui.interfaces.IGuiWatchers;
import com.wildex999.warpedspace.networking.netinterface.MessageSCInterfaceUpdate;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

//Manually add as watcher to gui

public class MessageWatchGui extends MessageBase {
	
	protected TileEntityInfo tile;
	protected boolean watch;
	
	//Receive
	public MessageWatchGui() {}
	
	public MessageWatchGui(TileEntity tile, boolean watch) {
		this.tile = new TileEntityInfo(tile);
		this.watch = watch;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		tile = readTileEntity(buf);
		watch = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		writeTileEntity(buf, tile);
		buf.writeBoolean(watch);
	}
	
	public static class Handler implements IMessageHandler<MessageWatchGui, IMessage> {
        
        @Override
        public IMessage onMessage(MessageWatchGui message, MessageContext ctx) {
        	World world = getWorld(ctx);
        	TileEntity tile = world.getTileEntity(message.tile.posX, message.tile.posY, message.tile.posZ);
        	
        	if(tile == null || !(tile instanceof IGuiWatchers))
        	{
        		ModLog.logger.info("Adding GUI Watcher failed due to null or incorrect tile at x: "+ message.tile.posX +" y: "+ message.tile.posY +" z: "+ message.tile.posZ);
        		return null;
        	}
        	
        	IGuiWatchers watcherTile = (IGuiWatchers)tile;
        	if(message.watch)
        		watcherTile.addWatcher(ctx.getServerHandler().playerEntity);
        	else
        		watcherTile.removeWatcher(ctx.getServerHandler().playerEntity);
        	
            return null;
        }
    }

}
