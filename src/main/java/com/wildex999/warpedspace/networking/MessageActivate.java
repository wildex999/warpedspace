package com.wildex999.warpedspace.networking;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.tiles.TileNetworkManager;
import com.wildex999.warpedspace.warpednetwork.AgentEntry;
import com.wildex999.warpedspace.warpednetwork.BaseNodeTile;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;
import com.wildex999.warpedspace.warpednetwork.INode;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

//Message used to simulate the player right clicking(Activating) a block from a distance.
//Send when player clicks "Activate" inside GUI's.

//TODO: Add some security to stop player from cheating by spoofing this message.
//(Then again, do we care?)

public class MessageActivate extends MessageBase {

	protected String activateEntry;
	protected TileEntityInfo tile;
	
	//Receiver
	public MessageActivate() {}
	
	
	public MessageActivate(BaseNodeTile node, String entry) {
		this.activateEntry = entry;
		this.tile = new TileEntityInfo(node);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		tile = readTileEntity(buf);
		activateEntry = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		writeTileEntity(buf, tile);
		ByteBufUtils.writeUTF8String(buf, activateEntry);
	}
	
	public static class Handler implements IMessageHandler<MessageActivate, IMessage> {
        
        @Override
        public IMessage onMessage(MessageActivate message, MessageContext ctx) {
        	World world = getWorld(ctx);
        	TileEntity tile = message.tile.getTileEntity(world);
        	if(tile == null || !(tile instanceof BaseNodeTile))
        	{
        		ModLog.logger.error("Tried to activate entry: " + message.activateEntry + ", but got null/wrong tile at x: " + message.tile.posX + " y: " + message.tile.posY + " z: " + message.tile.posX);
        	}
        	
        	BaseNodeTile node = (BaseNodeTile)tile;
        	WarpedNetwork network = node.getNetwork();
        	
        	if(network == null)
        	{
        		ModLog.logger.error("Tried to activate entry: " + message.activateEntry + ", but got null network at x: " + message.tile.posX + " y: " + message.tile.posY + " z: " + message.tile.posX);
        		return null;
        	}
        	
        	EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        	AgentEntry entry = network.getBlock(null, player.getGameProfile().getName(), message.activateEntry);
        	//TODO: Send message to player if missing permission to activate tile
        	if(entry == null)
        		return null;
        	
        	//Invalid and inactive entries can not be used
        	if(!entry.isValid() || !entry.active)
        		return null;
        	
        	//Set-up Fake Player to proxy the request, as some containers will check player proximity
        	//ProxyPlayer proxyPlayer = new ProxyPlayer(player, (WorldServer)tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
        	
        	//Send block and any tile entity data to client in case it's used
        	//TODO: Check if player realy needs this sent(Is the chunk already loaded?)
        	Chunk chunk = entry.world.getChunkFromBlockCoords(entry.x, entry.z);
        	MessageProxyChunk messageChunk = new MessageProxyChunk(entry.world, chunk.xPosition, chunk.zPosition);
        	messageChunk.sendToPlayer(player);
        	
        	S23PacketBlockChange blockPacket = new S23PacketBlockChange(entry.x, entry.y, entry.z, entry.world);
        	player.playerNetServerHandler.sendPacket(blockPacket);
        	Block block = entry.world.getBlock(entry.x, entry.y, entry.z);
        	TileEntity blockTile = entry.world.getTileEntity(entry.x, entry.y, entry.z);
        	if(blockTile != null)
        	{
        		Packet tilePacket = blockTile.getDescriptionPacket();
        		if(tilePacket != null)
        			player.playerNetServerHandler.sendPacket(tilePacket);
        	}
        	
        	//TODO: Continue sending tile entity updates
        	
        	//TODO: Get click orientation/Position from either in-game menu or from where the player clicked the Interface
        	entry.block.onBlockActivated(entry.world, entry.x, entry.y, entry.z, player, 0, 0f, 0f, 0f);
        	
            return null;
        }
    }

}
