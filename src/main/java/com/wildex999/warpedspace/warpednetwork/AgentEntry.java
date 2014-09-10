package com.wildex999.warpedspace.warpednetwork;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

//Agent Entry is used in the Network Agent to describe a Block shared on the network and any information used with it.
//Before using:
//If active == false, then this Tile is added to an Agent, but not added to the network, and should not be used.
//If isValid returns false, then this TileEntry is no longer valid, and you should do a new lookup on the Network.

public class AgentEntry {
	public Block block;
	public int x, y, z;
	public World world;
	public String name;
	public INetworkAgent agent;
	public long gid; //Global tile id. Saved on world save to indicate the agent slot. Stays the same as long as the agent or tile isn't removed.
	public boolean active; //False if the entry is not added on the network(For example due to name collision)
	
	public AgentEntry() {
		this.gid = -1;
		this.active = false;
	}
	
	public AgentEntry(Block block, World world, int x, int y, int z, String name, INetworkAgent agent) {
		this.block = block;
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;
		this.name = name;
		this.active = false;
		this.agent = agent;
		gid = -1;
	}
	
	public boolean isValid() {
		if(gid > 0)
			return true;
		return false;
	}
	
	//Makes the entry invalid.
	//Note: Only called AFTER it has been removed from network and agent
	public void invalidate() {
		gid = -1;
		active = false;
		block = null;
	}
	
	
}
