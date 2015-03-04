package com.wildex999.warpedspace.warpednetwork.iface;

import com.wildex999.utils.ModLog;
import com.wildex999.warpedspace.tiles.TileNetworkInterface;
import com.wildex999.warpedspace.warpednetwork.AgentEntry;

//Provide the light level of the hosted block, minus any cost

public class InterfaceLightManager {
	public TileNetworkInterface tile;
	public int oldLightValue;
	
	public InterfaceLightManager(TileNetworkInterface tile) {
		this.tile = tile;
		oldLightValue = 0;
	}
	
	public void update() {
		//Update lighting
		//ModLog.logger.info("Update Interface LIGHT: " + this);
		int lightLevel = getLightLevel();
		if(lightLevel == oldLightValue)
			return;
		oldLightValue = lightLevel;
		
		tile.getWorldObj().func_147451_t(tile.xCoord, tile.yCoord, tile.zCoord);
	}
	
	public int getLightLevel() {
		
		if(!tile.getWorldObj().isRemote)
		{
			AgentEntry entry = tile.currentEntry;
			//ModLog.logger.info("Server light. Entry: " + entry + " Tile: " + tile);
			if(entry == null || !entry.active || !entry.isValid())
				return 0;
			else if(!entry.agent.isNetworkReachable())
				return 0;
			
			return entry.block.getLightValue();
		}
		else
		{
			//ModLog.logger.info("Client light.");
			return oldLightValue;
		}
	}
}
