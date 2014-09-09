package com.wildex999.warpedspace.networking;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.lang3.tuple.Pair;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class MessageBase implements IMessage {
	
	//Queued messages are mostly used by GUI to send initial status messages,
	//Queuing the message to send after the GUI is done initializing on the client.
	private class QueuedMessage {
		public EntityPlayerMP player;
		public MessageBase message;
		
		public QueuedMessage(EntityPlayerMP player, MessageBase message)  { 
			this.player = player; this.message = message;
		}
	}
	private static List<QueuedMessage> queueList = new LinkedList<QueuedMessage>(); 
	
	public class TileEntityInfo {
		public int posX, posY, posZ;
		
		public TileEntityInfo(TileEntity tile) {
			posX = tile.xCoord; posY = tile.yCoord; posZ = tile.zCoord;
		}
		
		public TileEntityInfo(int x, int y, int z) {
			posX = x; posY = y; posZ = z; 
		}
		
		public TileEntity getTileEntity(World world) {
			return world.getTileEntity(posX, posY, posZ);
		}
	}
	
	//Constructor used when receiving
	public MessageBase() {}
	
	//Write the tile entity position
	public void writeTileEntity(ByteBuf buf, TileEntityInfo tile) {
		buf.writeInt(tile.posX);
		buf.writeInt(tile.posY);
		buf.writeInt(tile.posZ);
	}
	
	//Get the TileEntity written to the packet
	public TileEntityInfo readTileEntity(ByteBuf buf) {
		return new TileEntityInfo(buf.readInt(), buf.readInt(), buf.readInt());
	}
	
	public static World getWorld(MessageContext ctx) {
		if(ctx.side == Side.CLIENT)
			return getWorldClient();
		else
			return getWorldServer(ctx);
	}
	
	@SideOnly(Side.CLIENT)
	public static World getWorldClient() {
		return Minecraft.getMinecraft().theWorld;
	}
	
	public static World getWorldServer(MessageContext ctx) {
		return ctx.getServerHandler().playerEntity.worldObj;
	}
	
	public static void sendQueuedMessages() {
		for(QueuedMessage message : queueList) {
			message.message.sendToPlayer(message.player);
		}
		queueList.clear();
	}
	
	
	//--Send Packet--//
	
	//--From Server
	public void sendToPlayer(EntityPlayerMP player) {
		Networking.getChannel().sendTo(this, player);
	}
	
	//Queue packet for beginning of next tick
	public void queueToPlayer(EntityPlayerMP player) {
		queueList.add(new QueuedMessage(player, this));
	}
	
	public void sendToDimension(int dimensionId) {
		Networking.getChannel().sendToDimension(this, dimensionId);
	}
	
	public void sendToAllAround(TargetPoint point) {
		Networking.getChannel().sendToAllAround(this, point);
	}
	
	public void sendToAll() {
		Networking.getChannel().sendToAll(this);
	}
	
	
	//--From Client
	public void sendToServer() {
		Networking.getChannel().sendToServer(this);
	}
}
