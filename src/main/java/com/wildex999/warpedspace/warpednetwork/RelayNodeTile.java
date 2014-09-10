package com.wildex999.warpedspace.warpednetwork;

import java.util.HashSet;

/*Base Relay implementation
 * Keeps a list of connected nodes, and will keep them informed to changes
 * in network and node connectivity.
 */

public class RelayNodeTile extends BaseNodeTile implements INetworkRelay {

	public HashSet<INode> nodeList;
	protected int relayRadius;
	
	public RelayNodeTile() {
		nodeList = new HashSet<INode>();
		relayRadius = 0;
	}
	
	@Override
	public boolean addNode(WarpedNetwork net, INode node) {
		if(currentNetwork != net)
			return false;
		return nodeList.add(node);
	}

	@Override
	public boolean removeNode(WarpedNetwork net, INode node) {
		if(currentNetwork != net)
			return false;
		return nodeList.remove(node);
	}

	@Override
	public boolean isNetworkAvailable(WarpedNetwork net) {
		if(currentNetwork == net)
			return isReachable;
		return false;
	}

	@Override
	public int getRadius() {
		return relayRadius;
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		leaveNetwork();
	}
	
	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		leaveNetwork();
	}
	
	//--Events--
	
	@Override
	public boolean joinNetwork(WarpedNetwork network) {
		if(!super.joinNetwork(network))
			return false;
		currentNetwork.addRelay(this);
		
		return true;
	}
	
	@Override
	public void leaveNetwork() {
		if(currentNetwork == null)
			return;
		currentNetwork.removeRelay(this);
		
		super.leaveNetwork();
		
		//Inform nodes that this relay no longer exist
		for(INode node : nodeList)
			node.eventLostRelay();
		nodeList.clear();
	}
	
	@Override
	public void leaveRelay() {
		super.leaveRelay();

		this.eventNetworkDisconnect();
	}
	
	@Override
	public boolean joinRelay(INetworkRelay relay) {
		if(!super.joinRelay(relay))
			return false;
		
		this.eventNetworkReconnect();
		return true;
	}
	
	@Override
	public void eventLostRelay() {
		super.eventLostRelay();
		
		//Inform nodes we no longer are reachable
		for(INode node : nodeList)
			node.eventNetworkDisconnect();
	}
	
	@Override
	public void eventNetworkDisconnect() {
		super.eventNetworkDisconnect();
		
		for(INode node : nodeList)
			node.eventNetworkDisconnect();
	}
	
	@Override
	public void eventNetworkReconnect() {
		super.eventNetworkReconnect();
		
		//We now have a full path to the controller, and are reachable
		for(INode node : nodeList)
			node.eventNetworkReconnect();
	}

}
