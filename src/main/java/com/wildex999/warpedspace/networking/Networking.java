package com.wildex999.warpedspace.networking;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.networking.netinterface.MessageCSInterfaceUpdate;
import com.wildex999.warpedspace.networking.netinterface.MessageCSWatchList;
import com.wildex999.warpedspace.networking.netinterface.MessageSCInterfaceUpdate;
import com.wildex999.warpedspace.networking.netinterface.MessageTilesList;
import com.wildex999.warpedspace.networking.netinterface.MessageTilesUpdate;
import com.wildex999.warpedspace.networking.networkdetector.MessageDetectorRelayList;
import com.wildex999.warpedspace.networking.warpednetwork.MessageCSNetworkCreate;
import com.wildex999.warpedspace.networking.warpednetwork.MessageCSNetworkUpdate;
import com.wildex999.warpedspace.networking.warpednetwork.MessageSCNetworkList;
import com.wildex999.warpedspace.networking.warpednetwork.MessageSCNetworkUpdate;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class Networking {
	private static int nextId;
	private static SimpleNetworkWrapper channel;
	
	public static void init() {
		channel = NetworkRegistry.INSTANCE.newSimpleChannel("WarpedSpace1");
		
		//Messages
		//TODO: Load config first, and then print out which ID is registered per message if debugging enabled.
		
		//Release 1(Keep id ordering of previous message by always adding new ones at the end)
		//When removing an old message, replace it with a call to getFreeId().
		channel.registerMessage(MessageCSNetworkCreate.Handler.class, MessageCSNetworkCreate.class, getFreeId(), Side.SERVER);
		channel.registerMessage(MessageCSNetworkUpdate.Handler.class, MessageCSNetworkUpdate.class, getFreeId(), Side.SERVER);
		channel.registerMessage(MessageNetworkManagerUpdate.Handler.class, MessageNetworkManagerUpdate.class, getFreeId(), Side.SERVER);
		channel.registerMessage(MessageNetworkAgentInput.Handler.class, MessageNetworkAgentInput.class, getFreeId(), Side.SERVER);
		channel.registerMessage(MessageCSWatchList.Handler.class, MessageCSWatchList.class, getFreeId(), Side.SERVER);
		channel.registerMessage(MessageCSInterfaceUpdate.Handler.class, MessageCSInterfaceUpdate.class, getFreeId(), Side.SERVER);
		channel.registerMessage(MessageActivate.Handler.class, MessageActivate.class, getFreeId(), Side.SERVER);
		channel.registerMessage(MessageWatchGui.Handler.class, MessageWatchGui.class, getFreeId(), Side.SERVER);
		channel.registerMessage(MessageOpenTileGui.Handler.class, MessageOpenTileGui.class, getFreeId(), Side.SERVER);
		
		channel.registerMessage(MessageControllerUpdate.Handler.class, MessageControllerUpdate.class, getFreeId(), Side.CLIENT);
		channel.registerMessage(MessageSCNetworkUpdate.Handler.class, MessageSCNetworkUpdate.class, getFreeId(), Side.CLIENT);
		channel.registerMessage(MessageSCNetworkList.Handler.class, MessageSCNetworkList.class, getFreeId(), Side.CLIENT);
		channel.registerMessage(MessageNetworkManagerUpdate.Handler.class, MessageNetworkManagerUpdate.class, getFreeId(), Side.CLIENT);
		channel.registerMessage(MessageNetworkAgentUpdate.Handler.class, MessageNetworkAgentUpdate.class, getFreeId(), Side.CLIENT);
		channel.registerMessage(MessageTilesList.Handler.class, MessageTilesList.class, getFreeId(), Side.CLIENT);
		channel.registerMessage(MessageSCInterfaceUpdate.Handler.class, MessageSCInterfaceUpdate.class, getFreeId(), Side.CLIENT);
		channel.registerMessage(MessageProxyChunk.Handler.class, MessageProxyChunk.class, getFreeId(), Side.CLIENT);
		channel.registerMessage(MessageRelayUpdate.Handler.class, MessageRelayUpdate.class, getFreeId(), Side.CLIENT);
		channel.registerMessage(MessageTilesUpdate.Handler.class, MessageTilesUpdate.class, getFreeId(), Side.CLIENT);
        channel.registerMessage(MessageDetectorRelayList.Handler.class, MessageDetectorRelayList.class, getFreeId(), Side.CLIENT);
		
		//Release 2
		//Release 3
		//etc.
	}
	
	public static int getFreeId() {
		if(nextId >= 255)
			ModLog.logger.warn("Registered more than 255 messages. This might cause problems!");
		return nextId++;
	}
	
	public static SimpleNetworkWrapper getChannel() {
		return channel;
	}
}
