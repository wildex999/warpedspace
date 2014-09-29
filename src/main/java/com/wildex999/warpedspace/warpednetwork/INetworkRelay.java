package com.wildex999.warpedspace.warpednetwork;

/*
 * A network relay has the job of making a network accessible to 
 * nodes inside it's radius.
 * 
 * A relay can relay a number of networks at once.
 * 
 * So for example if a Relay is in range of a Network Controller, it has the ability
 * to be a primary relay for that network.
 * 
 * If it's within range of another relay, it can connect to the network and itself become
 * a relay for that network.
 * It does so by connecting to the Relay as a node, and then registering itself on the
 * network as a Relay.
 * 
 * Any Nodes within range of the relay will have access to all networks it relay.
 * 
 * Note: The Relays does NOT have any responsibility in registering/removing Nodes
 * from the network. It just has to update the nodes if the network becomes unavailable
 * due to loosing either contact with the node or if the relay itself looses contact with
 * the network.
 */

public interface INetworkRelay extends INode {
	
	//Add a node to the given network. Called by any node who want to join the
	//network, using this as their relay.
	//Can deny Node by not adding it and returning false.
	//Return true once node has been added.
	public boolean addNode(WarpedNetwork net, INode node);

	//Remove a node from the network
	public boolean removeNode(WarpedNetwork net, INode node);
	
	//Return true if this relay has contact with the network(It has a line to the Network controller)
	//Note: Even if the network is out of power, the relay still has a link to it.
	public boolean isNetworkAvailable(WarpedNetwork net);
	
	//Radius in which Nodes can connect to the Relay.
	public int getRadius();
	
	
}
