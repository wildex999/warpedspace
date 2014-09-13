package com.wildex999.warpedspace.warpednetwork;

/*
 * An entry Listener will be informed when an entry with the given id is available on the network.
 * This is used by the network Interface to allow it to use an Entry it has stored.
 */

public interface IEntryListener {
	//Return true to be removed from listener list
	public boolean onEntryAvailable(long gid);
	public boolean onEntryUpdate(AgentEntry entry);
	public boolean onEntryUnavailable(long gid);
}
