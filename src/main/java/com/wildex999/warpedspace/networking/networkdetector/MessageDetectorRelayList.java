package com.wildex999.warpedspace.networking.networkdetector;

import com.wildex999.warpedspace.items.ItemLibrary;
import com.wildex999.warpedspace.items.ItemNetworkDetector;
import com.wildex999.warpedspace.networking.MessageBase;
import com.wildex999.warpedspace.warpednetwork.INetworkRelay;
import com.wildex999.warpedspace.warpednetwork.WarpedNetwork;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class MessageDetectorRelayList extends MessageBase {

    protected List<INetworkRelay> relayList;
    protected List<ItemNetworkDetector.RelayInfo> clientRelayList;
    protected WarpedNetwork network;

    //Receive
    public MessageDetectorRelayList() {}

    public MessageDetectorRelayList(List<INetworkRelay> relayList, WarpedNetwork network) {
        this.relayList = relayList;
        this.network = network;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int relayCount = buf.readInt();
        clientRelayList = new ArrayList<ItemNetworkDetector.RelayInfo>(relayCount);
        for(int i = 0; i < relayCount; i++)
        {
            int x = buf.readInt();
            int z = buf.readInt();
            int radius = buf.readInt();
            boolean useable = buf.readBoolean(); //TODO: Pack this to byte(Java already does?)
            clientRelayList.add(new ItemNetworkDetector.RelayInfo(x, z, radius, useable));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(relayList.size());
        for(INetworkRelay relay : relayList)
        {
            buf.writeInt(relay.getPosX());
            //We ignore y, since relays currently ignore height
            buf.writeInt(relay.getPosZ());
            buf.writeInt(relay.getRadius());
            buf.writeBoolean(relay.isNetworkAvailable(network));
        }
    }

    public static class Handler implements IMessageHandler<MessageDetectorRelayList, IMessage> {

        @Override
        public IMessage onMessage(MessageDetectorRelayList message, MessageContext ctx) {
            ItemLibrary.itemNetworkDetector.setRelayList(message.clientRelayList);
            return null;
        }
    }
}
