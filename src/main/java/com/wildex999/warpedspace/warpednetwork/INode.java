package com.wildex999.warpedspace.warpednetwork;

/*
 * A Node for the Warped Space Network will, by itself, only allow for registering to and accessing the network.
 * It does not provide any service to the network.
 */

public interface INode {
	
	//Attempt to join the given network
	//Returns false on failure.
	public boolean joinNetwork(WarpedNetwork network);
	
	//Attempt to join and use the given relay
	//Returns false on failure.
	public boolean joinRelay(INetworkRelay relay);
	
	//Get the current network this Node is registered to.
	public WarpedNetwork getNetwork();
	
	//Get the current relay used by this Node to access the network.
	public INetworkRelay getRelay();
	
	//Leave the current network
	//Should also leave the current relay.
	public void leaveNetwork();
	
	//Leave the current relay
	public void leaveRelay();
	
	//Get whether or not the node is reachable on the network
	//(Connected to relay and Relay has connection to Network Controller/Mannager)
	public boolean isNetworkReachable();
	
	//Set the owner of the Node.
	//Used when first placing the node, or to transfer ownership after placed.
	public void setOwner(String username);
	
	//The username of the player who owns(placed) the node.
	public String getOwner();
	
	//Return Relay position
	public int getPosX();
	public int getPosY();
	public int getPosZ();
	
	//--Events--
	
	//Called when the connection to the current Relay is lost.
	//This can be due to being out of range, the Relay being destroyed,
	//or some other reason.
	public void eventLostRelay();
	
	//Called when the network this node is connected to no longer exists.
	//This can be because the network blacklisted the node, the network no longer exists,
	//or any other reason.
	public void eventLostNetwork();
	
	//Called when the node no longer has a connection to the network controller
	//through the current Relay.
	public void eventNetworkDisconnect();
	
	//Called when the Relay has recovered it's connection to the network controller.
	public void eventNetworkReconnect();
	
	//Called when the network no longer has any power.
	//No actions should be allowed.
	public void eventNetworkNoPower();
	
	//Called when power returns after being gone.
	public void eventNetworkGotPower();
	
}
