package com.wildex999.warpedspace.networking;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.EmptyChunk;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.warpednetwork.AgentEntry;
import com.wildex999.warpedspace.warpednetwork.BaseNodeTile;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

//Empty chunk for client when proxying distant block

public class MessageProxyChunk extends MessageBase {

	protected World world;
	protected int chunkX;
	protected int chunkZ;
	
	//Receiver
	public MessageProxyChunk() {}
	
	public MessageProxyChunk(World world, int chunkX, int chunkY) {
		this.world = world;
		this.chunkX = chunkX;
		this.chunkZ = chunkY;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		chunkX = buf.readInt();
		chunkZ = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(chunkX);
		buf.writeInt(chunkZ);
	}
	
public static class Handler implements IMessageHandler<MessageProxyChunk, IMessage> {
        
        @Override
        public IMessage onMessage(MessageProxyChunk message, MessageContext ctx) {
        	World world = getWorld(ctx);
        	
        	ModLog.logger.info("GOT CHUNK MESSAGE!");
        	if(world.getChunkFromChunkCoords(message.chunkX, message.chunkZ) instanceof EmptyChunk)
        	{
        		ModLog.logger.info("CREATE CHUNK: " + message.chunkX + " | " + message.chunkZ);
        		world.getChunkProvider().loadChunk(message.chunkX, message.chunkZ);
        	}
        	
            return null;
        }
    }

}
