package com.wildex999.utils;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class BlockItemName {
	public static String get(Block block, World world, int x, int y, int z) {
		//Item blockItem = block.getItem(world, x, y, z);
		Item blockItem = Item.getItemFromBlock(block);
		String blockName = "";
		if(blockItem != null)
			blockName = Item.itemRegistry.getNameForObject(blockItem);
		if(blockName == null || blockName.length() == 0)
			blockName = Block.blockRegistry.getNameForObject(block);
		if(blockName == null)
			blockName = "";
		
		return blockName;
	}
}
