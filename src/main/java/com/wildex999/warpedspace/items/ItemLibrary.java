package com.wildex999.warpedspace.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class ItemLibrary {
	public static Item itemNetworkCard;
	
	public static void init() {
		itemNetworkCard = new ItemNetworkCard();
	}
	
	public static void register(ItemBase item) {
		GameRegistry.registerItem(item, item.getProperName());
	}
}
