package com.wildex999.warpedspace;

public enum ReturnMessage {
	Ok(""),
	TileAdded("Tile Entity was added to the network"),
	TileAddedNoNetwork("Tile was added to agent, but there was no network to add it to"),
	TileAlreadyAdded("A Tile Entity has already been added for this side"),
	TileAddedNameTaken("The tile was added to the Agent, but name was already taken on the network. Will add to network when the name is free"),
	TileNotExist("A tile with the given name does not exist"),
	TileNameTaken("The given name is already taken"),
	NoNetwork("No network has been set"),
	RenamedLocal("Tile renamed on the Agent, but didn't exist on a network"),
	RenamedLocalNameTaken("Tile renamed on the Agent, but name was already taken on the network"),
	InternalError("Unknown internal error");
	
	public final String message;
	private ReturnMessage(String message) {
		this.message = message;
	}
}
