package com.wildex999.utils;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BlockItemName {
	//Try to get item/block name for the given block
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
	
	//Try to get an itemstack given an item/block name, and metadata
	public static ItemStack getItem(String name, byte meta) {
		ItemStack item = null;
		if(name.length() != 0)
		{
			item = new ItemStack((Item)Item.itemRegistry.getObject(name), 1, meta);
			if(item.getItem() == null)
			{
				Block drawBlock = Block.getBlockFromName(name);
				item = new ItemStack(drawBlock, 1, meta);
				if(item.getItem() == null)
					item = null;
			}
		}
		
		return item;
	}
}
