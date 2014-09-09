package com.wildex999.warpedspace.networking;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.networking.warpednetwork.MessageCSNetworkCreate;
import com.wildex999.warpedspace.warpednetwork.CoreNetworkManager;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/*
 * Sent to client to show a info/warning/error message on their screen.
 */

public class MessageShowMessage extends MessageBase {

	@Override
	public void fromBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		
	}

}
