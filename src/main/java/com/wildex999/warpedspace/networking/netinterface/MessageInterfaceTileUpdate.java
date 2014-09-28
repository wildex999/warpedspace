package com.wildex999.warpedspace.networking.netinterface;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.gui.NetworkInterfaceGui;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.tiles.TileNetworkInterface;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Created by Wildex999 on 27.09.2014.
 */
public class MessageInterfaceTileUpdate extends MessageBase {
    protected boolean updateTile;
    protected TileEntityInfo tile;

    //Receiver
    public MessageInterfaceTileUpdate() {}

    //Sender
    //Send true before forcing tile to send it's update, and false after
    public MessageInterfaceTileUpdate(TileEntity tile, boolean updateTile) {
        this.updateTile = updateTile;
        this.tile = new TileEntityInfo(tile);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        tile = readTileEntity(buf);
        updateTile = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        writeTileEntity(buf, tile);
        buf.writeBoolean(updateTile);
    }

    public static class Handler implements IMessageHandler<MessageInterfaceTileUpdate, IMessage> {

        @Override
        public IMessage onMessage(MessageInterfaceTileUpdate message, MessageContext ctx) {
            if(TileNetworkInterface.hostingInterface != null) {
                TileNetworkInterface.hostingInterface.clientHostTile(message.updateTile);
                return null;
            }

            World world = getWorld(ctx);
            TileEntity tile = message.tile.getTileEntity(world);
            if(tile == null || !(tile instanceof TileNetworkInterface))
                return null; //Can often be null, so ignore

            TileNetworkInterface tileInterface = (TileNetworkInterface)tile;
            tileInterface.clientHostTile(message.updateTile);

            return null;
        }
    }
}
