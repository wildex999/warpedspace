package com.wildex999.warpedspace.warpednetwork;

import org.apache.commons.lang3.tuple.Pair;

import com.wildex999.warpedspace.ReturnMessage;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/*
 * A Node Agent works on behalf of Tile Entities, and gives
 * access to them over the network.
 * 
 * Getting a named Tile entity will return a TileEntry. This can be re-used assuming
 * you verify it's validity:
 * If isValid returns false, the cache Entry is invalid, and you must do a network lookup.
 * If active is false, then the Entry is still valid, but the Tile is currently not accessible by the network.
 * 
 */

public interface INetworkAgent extends INode {
	
	//Get number of Tile Entities that this agent gives access to.
	public int getTileCount();
	
	//Check if the given node and player is allowed to use the Tile Entity.
	//This is usually called after cache checking.
	public boolean canUseTile(INode requestingNode, String requestingPlayer, AgentEntry tile);
	
	//Add the block with the given name to the network.
	//newEntry should be a unused and new AgentEntry, and should be checked for validity before use
	//after this function call.
	public ReturnMessage setTile(String name, Block block, World world, int x, int y, int z, AgentEntry newEntry);
	
	//Rename a Tile already added to the network.
	public ReturnMessage renameTile(AgentEntry entry, String newName);
	
	//Remove an existing tile
	//Returns false if tile did not exist in this agent to start with
	public boolean removeTile(AgentEntry entry);
	
}
