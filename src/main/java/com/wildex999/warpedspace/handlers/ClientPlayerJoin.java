package com.wildex999.warpedspace.handlers;

import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import com.wildex999.utils.ModLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import org.json.JSONObject;

/*
Gather some data during Alpha.
TODO: Remove this before public release
 */
public class ClientPlayerJoin {

    boolean isLocal = false;
    String server = "unknown";

    @SubscribeEvent
    public void onClientJoinServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        try {
            isLocal = event.isLocal;
            server = event.manager.getSocketAddress().toString();
        } catch(Exception e) {}
    }

    @SubscribeEvent
    public void onClientJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        try {
            EntityPlayer player = event.player;
            MessageBuilder messageBuilder = new MessageBuilder("6f81f49c820daa79ca2522a7398881d8");
            JSONObject props = new JSONObject();
            String uuid = "unknown";
            if (player != null) {
                uuid = player.getGameProfile().getId().toString();
                props.put("name", player.getGameProfile().getName());
            }

            props.put("isLocal", isLocal);
            props.put("server", server);

            JSONObject update = messageBuilder.event(uuid, "joinGame", props);
            MixpanelAPI mixpanel = new MixpanelAPI();
            mixpanel.sendMessage(update);
        }
        catch(Exception e) {  }
    }
}
