package com.wildex999.warpedspace.warpednetwork;

//Name listeners are listeneres registered to be notified when a name on the agent map becomes free.
//This is used when tiles are trying to use a name already taken, to avoid polling for it.

public interface INameListener {
	
	//Return true if you took the name
	//(Will remove listener from list and not continue on to next listener for name)
	public boolean onNameFree(String name);
}
