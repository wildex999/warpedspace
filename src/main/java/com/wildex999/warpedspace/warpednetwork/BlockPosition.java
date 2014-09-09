package com.wildex999.warpedspace.warpednetwork;

import net.minecraft.block.Block;

public class BlockPosition {
	public Block block;
	public int x, y, z;
	
	public BlockPosition(Block block, int x, int y, int z) {
		this.block = block;
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
