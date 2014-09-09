package com.wildex999.warpedspace.gui.interfaces;

/*
 * If a GUI screen implements this, and is the current screen, it will be notified
 * if any changes in the Network list.
 */

public interface INetworkListGui {
	
	//A new network was created
	public void networkCreated(int id, String name, String owner);
	
	//An existing network is being renamed(Called before rename)
	public void networkRename(int id, String newName);
	
	//Removing a network(Called before actually removed)
	public void networkRemoved(int id);
	
	//Reload the whole list from source
	public void reloadNetworkList();
}
