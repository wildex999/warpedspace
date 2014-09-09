package com.wildex999.warpedspace.networking;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.WarpedSpace;
import com.wildex999.warpedspace.warpednetwork.AgentEntry;
import com.wildex999.warpedspace.warpednetwork.BaseNodeTile;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import io.netty.buffer.ByteBuf;

//Tell Server to Open GUI.
//Used to open previous GUI.

public class MessageOpenTileGui extends MessageBase {

	protected int guiId;
	protected TileEntityInfo tile;
	
	//Receive
	public MessageOpenTileGui() {}
	
	
	public MessageOpenTileGui(TileEntity tile, int guiId) {
		this.tile = new TileEntityInfo(tile);
		this.guiId = guiId;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		tile = readTileEntity(buf);
		guiId = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		writeTileEntity(buf, tile);
		buf.writeInt(guiId);
	}

	
	public static class Handler implements IMessageHandler<MessageOpenTileGui, IMessage> {
        
        @Override
        public IMessage onMessage(MessageOpenTileGui message, MessageContext ctx) {
        	World world = getWorld(ctx);
        	TileEntity tile = message.tile.getTileEntity(world);
        	
        	//This one can fail often due to lag and world changes, so don't print error
        	if(tile == null)
        		return null;
        	
        	EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        	player.openGui(WarpedSpace.instance, message.guiId, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
        	
            return null;
        }
    }
}
