package com.wildex999.warpedspace.warpednetwork;

//Tile listeners will be informed every time a change is made on tiles.
//A change is defined as: Tile added, Tile removed, Tile unavailable, Tile available
//This is usually used when Players are viewing the Tile list for a network, to keep
//it up to date on the client.
//Note: Renaming/Changing an entry involves a remove and re-add with changed info

public interface ITileListener {

	//Tile added to the network
	public void tileAdded(AgentEntry tile);
	
	//Tile removed from the network
	public void tileRemoved(AgentEntry tile);
	
	//Called when a tile is made available on the network.
	//Note: Newly added tiles are by default available, so this is not called after tileAdded()
	public void tileAvailable(AgentEntry tile);
	
	//Tile is unreachable on the network, but still exists.
	public void tileUnavailable(AgentEntry tile);
	
}
