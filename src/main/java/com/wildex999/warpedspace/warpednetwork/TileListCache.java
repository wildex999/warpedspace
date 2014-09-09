package com.wildex999.warpedspace.warpednetwork;

import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;

/*
 * Client side cache of all Available TileEntities on a network.
 * This cache is activated and maintained when the player opens an Interface to
 * select a Tile Entity from the network.
 * 
 * The cache will continue to be updated even after the client has closed the GUI, to enable
 * faster initialization of the list on the next GUI open.
 * This is done with the assumtion that once a client has opened the GUI, they most likely
 * will do so again in the near future.
 * 
 * After x minutes the cache will no longer be maintained.
 */

@SideOnly(Side.CLIENT)
public class TileListCache {
	public static Map<WarpedNetwork, TileListCache> cacheMap = new HashMap<WarpedNetwork, TileListCache>();
	
	public boolean active;
}
