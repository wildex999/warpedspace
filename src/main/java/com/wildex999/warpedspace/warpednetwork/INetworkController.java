package com.wildex999.warpedspace.warpednetwork;

/*
 * The Network Controller is the single entity which must be
 * reachable for the network to be usable.
 * 
 * The Network Controller also defines the limits of the network.
 * For example it can limit how many nodes can be connected, how many items may flow through,
 * how much energy can be stored and flow, fluids, redstone etc.
 * 
 * 
 * TODO: When are networks removed? We don't want the network
 * to be removed when the Controller is unloaded or destroyed, as it might
 * come back soon.
 * However, we can't have networks laying around after they are no longer used.
 * 
 * Destroy network if no controller exists on shutdown?
 * Thus everything connected to network will have to try to reconnect until 
 * the network is created again.
 * Only remove when player physically removes it?(Put all connected nodes into 'no network')
 */

public interface INetworkController {
	

	//Join the network defined by the id(Usually gotten from a network Card)
	//If the network already has a Controller, this will fail and return null.
	public WarpedNetwork joinNetwork(int id);
	
	//Get the current network the controller is in charge of
	public WarpedNetwork getNetwork();
	
	//Leave the network, putting it in an unusable state
	//Usually called when controller is destroyed or unloaded.
	public void leaveNetwork();
}
