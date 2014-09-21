package com.wildex999.warpedspace.warpednetwork.iface;

import net.minecraft.block.BlockRedstoneWire;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.tiles.TileNetworkInterface;
import com.wildex999.warpedspace.warpednetwork.AgentEntry;

/*
 * TODO: Store energy given from block at first get*Power of a tick, and then -- for
 * each proxied request.
 * TODO: Cache so we don't have to go through entry?(Flag, and update on Agent update)
 */

public class InterfaceRedstoneManager {
	public TileNetworkInterface tile;
	
	private static int sideCount = 6;
	
	public byte redStonePowerCost = 0; //How much to reduce the restone power when proxying it
	private byte[] weakRedstone = new byte[sideCount];
	private byte[] strongRedstone = new byte[sideCount];
    public boolean gotPower;
	//private boolean indirectPowered = false;
	
	public InterfaceRedstoneManager(TileNetworkInterface tile) {
		this.tile = tile;
	}
	
	//Check our hosted block for it's weak and strong redstone
	public void update() {
        gotPower = true;
		AgentEntry entry = tile.currentEntry;
		boolean zero = false;
		if(entry == null || !entry.active || !entry.isValid())
			zero = true;
		else if(!entry.agent.isNetworkReachable() || !entry.block.canProvidePower())
			zero = true;
		
		for(int i = 0; i < sideCount; i++)
		{
			if(zero)
			{
				weakRedstone[i] = 0;
				strongRedstone[i] = 0;
			}
			else
			{
				weakRedstone[i] = (byte)entry.block.isProvidingWeakPower(entry.world, entry.x, entry.y, entry.z, i);
				strongRedstone[i] = (byte)entry.block.isProvidingStrongPower(entry.world, entry.x, entry.y, entry.z, i);
			}
		}
        if(zero)
            gotPower = false;
		/*if(zero)
			indirectPowered = false;
		else
			indirectPowered = entry.world.isBlockIndirectlyGettingPowered(entry.x, entry.y, entry.z);*/
	}

	public int getHostedWeakPower(int dir) {
		if(weakRedstone[dir] > 0)
			return weakRedstone[dir]-redStonePowerCost;
		/*else if(indirectPowered)
			return 1;*/ //Problem with this is that it would always give 1 power even if redstone wire did not point in that direction
		return 0;
	}
	
	public int getHostedStrongPower(int dir) {
		return strongRedstone[dir]-redStonePowerCost;
	}
}
